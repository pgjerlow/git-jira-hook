package org.karivar.utils;


import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.karivar.utils.domain.IssueKeyNotFoundException;
import org.karivar.utils.domain.JiraIssue;
import org.karivar.utils.domain.ParentJiraIssue;
import org.karivar.utils.domain.User;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Optional;

public class JiraConnector {

    private final Logger logger = LoggerFactory.getLogger(JiraConnector.class);
    private IssueRestClient issueRestClient;


    /**
     * Connects to the JIRA instance.
     * @param jiraUsername
     * @param jiraEncodedPassword
     * @param jiraAddress
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
     *
     * @param jiraIssueKey
     * @return
     * @throws IssueKeyNotFoundException
     */
    public JiraIssue getJiraPopulatedIssue(Optional<String> jiraIssueKey) throws IssueKeyNotFoundException {

        JiraIssue populatedJiraIssue = null;

        if (issueRestClient != null && jiraIssueKey.isPresent()) {
            try {
                Promise<Issue> issuePromise = issueRestClient.getIssue(jiraIssueKey.get());
                Issue issue = issuePromise.claim();

                populatedJiraIssue = mapJiraIssue(issue);

            } catch (RestClientException e) {
                if (e.getStatusCode().isPresent() && e.getStatusCode().get() == 403) {
                    // Forbidden access
                    throw new IssueKeyNotFoundException("Unable to authorize. Check your JIRA username and password");
                } else e.printStackTrace();
            }
        }


        return populatedJiraIssue;
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
            jiraAddressUri = new URI(jiraAddress.get());
        } catch (URISyntaxException e) {
            logger.error("The JIRA address is misspelled.");
        }
        return jiraAddressUri;
    }

    private JiraIssue mapJiraIssue(Issue issue) {
        JiraIssue jiraIssue = null;

        if (issue != null) {
            jiraIssue = new JiraIssue(issue.getKey(), issue.getSummary());

            if (issue.getAssignee() != null) {
                User assignee = new User(issue.getAssignee().getName(), issue.getAssignee().getDisplayName());
                jiraIssue.setAssignee(assignee);
            }

            if (issue.getStatus() != null) {
                jiraIssue.setStatus(issue.getStatus().getName());
            }

            if (issue.getIssueType() != null) {
                jiraIssue.setSubtask(issue.getIssueType().isSubtask());
            }

            if (issue.getResolution() != null) {
                jiraIssue.setResoluition(issue.getResolution().getDescription());
            }

            if (issue.getField("parent") != null) {
                IssueField parentIssueField = issue.getField("parent");
                ParentJiraIssue parentJiraIssue = null;

                JSONObject jsonObject = (JSONObject) parentIssueField.getValue();
                try {
                    String parentKey = jsonObject.getString("key");
                    JSONObject parentFields = (JSONObject) jsonObject.get("fields");
                    String parentSummary = parentFields.getString("summary");
                    parentJiraIssue = new ParentJiraIssue(parentKey, parentSummary);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                jiraIssue.setParentIssue(Optional.of(parentJiraIssue));
            }

        }

        return  jiraIssue;
    }

}
