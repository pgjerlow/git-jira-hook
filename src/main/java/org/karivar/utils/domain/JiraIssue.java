package org.karivar.utils.domain;


import java.util.List;
import java.util.Optional;

public class JiraIssue extends ParentJiraIssue {

    private String status;
    private List<Optional<RelatedJiraIssue>> relatedIssues;
    private Optional<ParentJiraIssue> parentIssue;
    private boolean subtask;
    private User assignee;
    private String resoluition;

    public JiraIssue(String key, String summary) {
        super(key, summary);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Optional<RelatedJiraIssue>> getRelatedIssues() {
        return relatedIssues;
    }

    public void setRelatedIssues(List<Optional<RelatedJiraIssue>> relatedIssues) {
        this.relatedIssues = relatedIssues;
    }

    public Optional<ParentJiraIssue> getParentIssue() {
        return parentIssue;
    }

    public void setParentIssue(Optional<ParentJiraIssue> parentIssue) {
        this.parentIssue = parentIssue;
    }

    public boolean isSubtask() {
        return subtask;
    }

    public void setSubtask(boolean subtask) {
        this.subtask = subtask;
    }

    public User getAssignee() {
        return assignee;
    }

    public void setAssignee(User assignee) {
        this.assignee = assignee;
    }

    public String getResoluition() {
        return resoluition;
    }

    public void setResoluition(String resoluition) {
        this.resoluition = resoluition;
    }
}
