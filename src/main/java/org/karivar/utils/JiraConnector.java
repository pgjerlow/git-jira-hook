/*
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
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.karivar.utils.domain.*;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

class JiraConnector {

    private final Logger logger = LoggerFactory.getLogger(JiraConnector.class);
    private IssueRestClient issueRestClient;
    private final ResourceBundle messages;

    JiraConnector(ResourceBundle bundle) {
       messages = bundle;
    }

    /**
     * Connects to the JIRA instance.
     * @param jiraUsername the JIRA username
     * @param jiraEncodedPassword the base46 encoded password
     * @param jiraAddress the JIRA address
     */
    void connectToJira( final String jiraUsername,
                                 final String jiraEncodedPassword,
                                 final String jiraAddress) {
        if (jiraUsername != null && jiraEncodedPassword != null && jiraAddress != null) {
            final AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();

            URI jiraAddressUri = getJiraAddressUri(jiraAddress);
            String decodedPassword = getDecodedPassword(jiraEncodedPassword);

            JiraRestClient restClient = factory.createWithBasicHttpAuthentication(
                    jiraAddressUri, jiraUsername, decodedPassword);

            issueRestClient = restClient.getIssueClient();

        } else {
            logger.error(messages.getString("error.jira.connection"));
        }
    }

    /**
     * Fetches the populated JIRA issue for the given issue key.
     * @param jiraIssueKey the given jira issue id
     * @param issueLinks a list containing all names of JIRA issue link names
     * @return the fully populated JIRA issue
     * @throws IssueKeyNotFoundException in case of problems (connectivity, malformed messages, invalid argument, etc.)
     */
    JiraIssue getJiraPopulatedIssue(Optional<String> jiraIssueKey, List<String> issueLinks)
            throws IssueKeyNotFoundException {

        String issueKey = null;

        if (jiraIssueKey.isPresent()) {
            issueKey = jiraIssueKey.get();
        }
        return mapJiraIssue(fetchBasicJiraIssue(issueKey), issueLinks);
    }

    private String getDecodedPassword(String jiraEncodedPassword) {
        if (!Strings.isNullOrEmpty(jiraEncodedPassword)) {
            byte[] passwordBytes = Base64.getDecoder().decode(jiraEncodedPassword);
            return new String(passwordBytes, Charsets.UTF_8);
        }
        return null;
    }

    private URI getJiraAddressUri(String jiraAddress) {

        URI jiraAddressUri = null;
        try {
            if (!Strings.isNullOrEmpty(jiraAddress)) {
                jiraAddressUri = new URI(jiraAddress);
            }
        } catch (URISyntaxException e) {
            logger.error(messages.getString("error.jira.connection.url"));
        }
        return jiraAddressUri;
    }

    private JiraIssue mapJiraIssue(JiraIssueHolder issueHolder, List<String> issueLinks)
            throws IssueKeyNotFoundException {
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
                jiraIssue.setIssueTypeName(issue.getIssueType().getName());
            }

            if (issue.getResolution() != null) {
                jiraIssue.setResolution(Optional.of(issue.getResolution().getDescription()));
            }

            // parent issue
            IssueField parentIssueField = issue.getField("parent");
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

    private List<BasicJiraIssue> getRelatedIssues(Issue issue, List<String> issuesLinkList) {
        Iterator<IssueLink> issueLinkIterator;
        if (issue.getIssueLinks() != null ) {
            issueLinkIterator = issue.getIssueLinks().iterator();
            List<BasicJiraIssue> relatedJiraIssues = Lists.newArrayList();

            JiraIssueHolder relatedIssueHolder;

            while (issueLinkIterator.hasNext()) {
                IssueLink issueLink = issueLinkIterator.next();
                for (String issueLinkTypeName : issuesLinkList) {
                    if (issueLink.getIssueLinkType().getName().equalsIgnoreCase(issueLinkTypeName)) {
                        String relatedIssueKey = issueLink.getTargetIssueKey();
                        relatedIssueHolder = fetchBasicJiraIssue(relatedIssueKey);
                        relatedJiraIssues.add(relatedIssueHolder.getJiraIssue());
                    }
                }
            }
            return relatedJiraIssues;
        }
        return Lists.newArrayList();
    }

    private JiraIssueHolder fetchBasicJiraIssue(String jiraIssueKey) throws IssueKeyNotFoundException {
        JiraIssueHolder holder = null;

        if (issueRestClient != null && !Strings.isNullOrEmpty(jiraIssueKey)) {
            try {
                Promise<Issue> issuePromise = issueRestClient.getIssue(jiraIssueKey);
                Issue issue = issuePromise.claim();

                BasicJiraIssue basicJiraIssue = new BasicJiraIssue(issue.getKey(), issue.getSummary());
                holder = new JiraIssueHolder(basicJiraIssue, issue);

            } catch (RestClientException e) {
                if (e.getStatusCode().isPresent() && e.getStatusCode().get() == 401) {
                    // Forbidden access
                    throw new IssueKeyNotFoundException(messages.getString("error.jira.statuscode.401"));
                }
                if (e.getStatusCode().isPresent() && e.getStatusCode().get() == 403) {
                    // Forbidden access
                    throw new IssueKeyNotFoundException(messages.getString("error.jira.statuscode.403"));
                }
                if (e.getStatusCode().isPresent() && e.getStatusCode().get() == 404) {
                    // The issue doesn't exist
                    throw new IssueKeyNotFoundException(messages.getString("error.jira.statuscode.404")
                            + jiraIssueKey);
                }
                else {
                    logger.error("Got unhandled RestClientException\n\t", e);
                }
            } catch (Exception e) {
                if (e.getCause() instanceof ConnectException) {
                    throw new IssueKeyNotFoundException(messages.getString("error.jira.connection.refused"));
                } else {
                    logger.error("Got unhandled Exception\n\t", e);
                }
            }
        }
        return holder;
    }

    private BasicJiraIssue getParentIssueInfo(IssueField parentIssueField) throws IssueKeyNotFoundException {
        BasicJiraIssue basicJiraIssue;
        JSONObject jsonObject = (JSONObject) parentIssueField.getValue();
        try {
            String parentKey = jsonObject.getString("key");
            JSONObject parentFields = (JSONObject) jsonObject.get("fields");
            String parentSummary = parentFields.getString("summary");
            basicJiraIssue = new BasicJiraIssue(parentKey, parentSummary);

        } catch (JSONException e) {
            throw new IssueKeyNotFoundException("JSONException", e);
        }
        return basicJiraIssue;
    }

}
