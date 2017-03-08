/**
 * Copyright (C) 2017 Per Ivar Gjerløw
 * All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */
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
        return jiraIssue;
    }

    public Issue getIssue() {
        return issue;
    }
}
