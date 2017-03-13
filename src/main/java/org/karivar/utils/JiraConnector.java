/**
 * Copyright (C) 2017 Per Ivar Gjerløw
 * All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */
package org.karivar.utils;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.IssueLink;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;
import com.google.common.collect.Lists;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.karivar.utils.domain.*;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class JiraConnector {

    private final Logger logger = LoggerFactory.getLogger(JiraConnector.class);
    private IssueRestClient issueRestClient;

    private static String PARENT_JSON_NAME = "parent";


    /**
     * Connects to the JIRA instance.
     * @param jiraUsername the JIRA username
     * @param jiraEncodedPassword the base46 encoded password
     * @param jiraAddress the JIRA address
     */
    public void connectToJira(Optional<String> jiraUsername,
                                 Optional<String> jiraEncodedPassword,
                                 Optional<String> jiraAddress) {
        if (jiraUsername.isPresent() && jiraEncodedPassword.isPresent() && jiraAddress.isPresent()) {
            final AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();

            URI jiraAddressUri = getJiraAddressUri(jiraAddress);
            String decodedPassword = getDecodedPassword(jiraEncodedPassword);

            JiraRestClient restClient = factory.createWithBasicHttpAuthentication(
                    jiraAddressUri, jiraUsername.get(), decodedPassword);

            issueRestClient = restClient.getIssueClient();

        } else {
            logger.error("Connection information to JIRA is missing. Cannot continue");
        }
    }

    /**
     * Fetches the populated JIRA issue for the given issue key.
     * @param jiraIssueKey the given jira issue id
     * @param issueLinks
     * @return the fully populated JIRA issue
     * @throws IssueKeyNotFoundException in case of problems (connectivity, malformed messages, invalid argument, etc.)
     */
    public JiraIssue getJiraPopulatedIssue(Optional<String> jiraIssueKey, List<String> issueLinks) throws IssueKeyNotFoundException {
        return mapJiraIssue(fetchBasicJiraIssue(jiraIssueKey), issueLinks);
    }

    private String getDecodedPassword(Optional<String> jiraEncodedPassword) {
        byte[] passwdBytes = Base64.getDecoder().decode(jiraEncodedPassword.get());
        String decodedPassword = null;
        try {
            decodedPassword = new String(passwdBytes, "utf-8");
        } catch (UnsupportedEncodingException e) {
            logger.error("Unable to decode the password");
        }
        return decodedPassword;
    }

    private URI getJiraAddressUri(Optional<String> jiraAddress) {

        URI jiraAddressUri = null;
        try {
            if (jiraAddress.isPresent()) {
                jiraAddressUri = new URI(jiraAddress.get());
            }
        } catch (URISyntaxException e) {
            logger.error("The JIRA address is misspelled.");
        }
        return jiraAddressUri;
    }

    private JiraIssue mapJiraIssue(JiraIssueHolder issueHolder, List<String> issueLinks) {

        Issue issue = issueHolder.getIssue();
        JiraIssue jiraIssue = new JiraIssue(issueHolder.getJiraIssue().getKey(),
                issueHolder.getJiraIssue().getSummary());

        if (issue != null) {

            if (issue.getAssignee() != null) {
                User assignee = new User(issue.getAssignee().getName(), issue.getAssignee().getDisplayName());
                jiraIssue.setAssignee(Optional.of(assignee));
            }

            if (issue.getStatus() != null) {
                jiraIssue.setStatus(issue.getStatus().getName());
            }

            if (issue.getIssueType() != null) {
                jiraIssue.setSubtask(issue.getIssueType().isSubtask());
            }

            if (issue.getResolution() != null) {
                jiraIssue.setResolution(Optional.of(issue.getResolution().getDescription()));
            }

            // parent issue
            IssueField parentIssueField = issue.getField(PARENT_JSON_NAME);
            if (parentIssueField != null) {
                BasicJiraIssue basicJiraIssue = getParentIssueInfo(parentIssueField);
                jiraIssue.setParentIssue(Optional.of(basicJiraIssue));
            }

            // related issues
            if (issue.getIssueLinks() != null) {
                List<BasicJiraIssue> relatedJiraIssues = getRelatedIssues(issue, issueLinks);
                jiraIssue.setRelatedIssues(relatedJiraIssues);
            }

        }

        return  jiraIssue;
    }

    private List<BasicJiraIssue> getRelatedIssues(Issue issue, List<String> issuesLinks) {
        Iterator<IssueLink> issueLinkIterator;
        issueLinkIterator = issue.getIssueLinks().iterator();
        List<BasicJiraIssue> relatedJiraIssues = Lists.newArrayList();

        JiraIssueHolder relatedIssueHolder;
        Iterator<String> issueLinksIterator = issuesLinks.iterator();
        while (issueLinkIterator.hasNext()) {
            IssueLink issueLink = issueLinkIterator.next();

            while (issueLinksIterator.hasNext()) {
                String issueLinkType = issueLinksIterator.next();
                if (issueLink.getIssueLinkType().getName().equalsIgnoreCase(issueLinkType)) {
                    Optional<String> relatedIssueKey = Optional.of(issueLink.getTargetIssueKey());
                    relatedIssueHolder = fetchBasicJiraIssue(relatedIssueKey);
                    relatedJiraIssues.add(relatedIssueHolder.getJiraIssue());
                }
            }


        }
        return relatedJiraIssues;
    }

    private JiraIssueHolder fetchBasicJiraIssue(Optional<String> jiraIssueKey) throws IssueKeyNotFoundException {
        JiraIssueHolder holder = null;

        if (issueRestClient != null && jiraIssueKey.isPresent()) {
            try {
                Promise<Issue> issuePromise = issueRestClient.getIssue(jiraIssueKey.get());
                Issue issue = issuePromise.claim();

                BasicJiraIssue basicJiraIssue = new BasicJiraIssue(issue.getKey(), issue.getSummary());
                holder = new JiraIssueHolder(basicJiraIssue, issue);

            } catch (RestClientException e) {
                if (e.getStatusCode().isPresent() && e.getStatusCode().get() == 403) {
                    // Forbidden access
                    throw new IssueKeyNotFoundException("Unable to authorize access. " +
                            "Check your JIRA username and password");
                } else e.printStackTrace();
            }
        }
        return holder;
    }

    private BasicJiraIssue getParentIssueInfo(IssueField parentIssueField) {
        BasicJiraIssue basicJiraIssue = null;
        JSONObject jsonObject = (JSONObject) parentIssueField.getValue();
        try {
            String parentKey = jsonObject.getString("key");
            JSONObject parentFields = (JSONObject) jsonObject.get("fields");
            String parentSummary = parentFields.getString("summary");
            basicJiraIssue = new BasicJiraIssue(parentKey, parentSummary);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return basicJiraIssue;
    }

}
