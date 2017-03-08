package org.karivar.utils.domain;

import com.atlassian.jira.rest.client.api.domain.Issue;

public class JiraIssueHolder {
    private BasicJiraIssue jiraIssue;
    private Issue issue;

    public JiraIssueHolder(BasicJiraIssue jiraIssue, Issue issue) {
        this.jiraIssue = jiraIssue;
        this.issue = issue;
    }

    public BasicJiraIssue getJiraIssue() {
        return this.jiraIssue;
    }

    public Issue getIssue() {
        return this.issue;
    }
}
