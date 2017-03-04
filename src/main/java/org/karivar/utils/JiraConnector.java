package org.karivar.utils;


import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;
import org.karivar.utils.domain.JiraIssue;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Optional;

public class JiraConnector {

    private final Logger logger = LoggerFactory.getLogger(JiraConnector.class);
    private IssueRestClient issueRestClient;


    /**
     *
     * @param jiraUsername
     * @param jiraEncodedPassword
     * @param jiraAddress
     * @return
     */
    public boolean connectToJira(Optional<String> jiraUsername,
                                 Optional<String> jiraEncodedPassword,
                                 Optional<String> jiraAddress) {
        if (jiraUsername.isPresent() && jiraEncodedPassword.isPresent() && jiraAddress.isPresent()) {
            final AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();

            URI jiraAddressUri = null;
            try {
                jiraAddressUri = new URI(jiraAddress.get());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            byte[] passwdBytes = Base64.getDecoder().decode(jiraEncodedPassword.get());
            String decodedPassword = null;
            try {
                decodedPassword = new String(passwdBytes, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            JiraRestClient restClient = factory.createWithBasicHttpAuthentication(jiraAddressUri, jiraUsername.get(), decodedPassword);

            if (restClient != null) {
                issueRestClient = restClient.getIssueClient();
                Promise<Issue> issuePromise = issueRestClient.getIssue("FP-3");

                Issue issue = issuePromise.claim();
                logger.debug("found issie");
            }

        } else {
            logger.error("Connection information to JIRA is missing. Cannot continue");
        }

        return false;
    }

    /**
     *
     * @param jiraIssueKey
     * @return
     */
    public JiraIssue getJiraIssue(Optional<String> jiraIssueKey) {
        return null;
    }
}
