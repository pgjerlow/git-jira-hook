/**
 * Copyright (C) 2017 Per Ivar Gjerløw
 * All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */
package org.karivar.utils.domain;

import java.util.List;
import java.util.Optional;

public class JiraIssue extends BasicJiraIssue {

    private String status;
    private List<BasicJiraIssue> relatedIssues;
    private Optional<BasicJiraIssue> parentIssue;
    private boolean subtask;
    private Optional<User> assignee;
    private Optional<String> resolution;

    public JiraIssue(String key, String summary) {
        super(key, summary);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<BasicJiraIssue> getRelatedIssues() {
        return relatedIssues;
    }

    public void setRelatedIssues(List<BasicJiraIssue> relatedIssues) {
        this.relatedIssues = relatedIssues;
    }

    public Optional<BasicJiraIssue> getParentIssue() {
        return parentIssue;
    }

    public void setParentIssue(Optional<BasicJiraIssue> parentIssue) {
        this.parentIssue = parentIssue;
    }

    public boolean isSubtask() {
        return subtask;
    }

    public void setSubtask(boolean subtask) {
        this.subtask = subtask;
    }

    public Optional<User> getAssignee() {
        return assignee;
    }

    public void setAssignee(Optional<User> assignee) {
        this.assignee = assignee;
    }

    public Optional<String> getResolution() {
        return resolution;
    }

    public void setResolution(Optional<String> resolution) {
        this.resolution = resolution;
    }
}
