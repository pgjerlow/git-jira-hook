/*
 * Copyright (C) 2017 Per Ivar Gjerløw
 * All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */
package org.karivar.utils.utils;

import org.karivar.utils.domain.BasicJiraIssue;
import org.karivar.utils.domain.JiraIssue;
import org.karivar.utils.domain.User;

import java.util.List;
import java.util.Optional;

public class JiraIssueBuilder {

    private final String newKey;
    private final String newDescription;
    private String newStatus;
    private List<BasicJiraIssue> newRelatedIssues;
    private Optional<BasicJiraIssue> newParentIssue;
    private boolean newSubtask;
    private String newIssueTypeName;
    private Optional<User> newAssignee;

    public JiraIssueBuilder(String key, String description) {
        this.newKey = key;
        this.newDescription = description;
    }

    public JiraIssueBuilder setStatus(String status) {
        this.newStatus = status;
        return this;
    }

    public JiraIssueBuilder setRelatedIssues(List<BasicJiraIssue> relatedIssues) {
        this.newRelatedIssues = relatedIssues;
        return this;
    }

    public JiraIssueBuilder setParentIssue(Optional<BasicJiraIssue> parentIssue) {
        this.newParentIssue = parentIssue;
        return this;
    }

    public JiraIssueBuilder setSubtask(boolean isSubtask) {
        this.newSubtask = isSubtask;
        return this;
    }

    public JiraIssueBuilder setIssueTypeName(String issueTypeName) {
        this.newIssueTypeName = issueTypeName;
        return this;
    }

    public JiraIssueBuilder setAssignee(Optional<User> assignee) {
        this.newAssignee = assignee;
        return this;
    }

    public JiraIssue build() {
        JiraIssue issue = new JiraIssue(newKey, newDescription);
        issue.setStatus(newStatus);
        issue.setRelatedIssues(newRelatedIssues);
        issue.setParentIssue(newParentIssue);
        issue.setSubtask(newSubtask);
        issue.setIssueTypeName(newIssueTypeName);
        issue.setAssignee(newAssignee);
        return issue;
    }

}
