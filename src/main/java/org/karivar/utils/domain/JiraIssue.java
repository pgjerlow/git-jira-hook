package org.karivar.utils.domain;


import java.util.List;
import java.util.Optional;

public class JiraIssue extends ParentJiraIssue {

    private String status;
    private List<Optional<RelatedJiraIssue>> relatedIssues;
    private Optional<ParentJiraIssue> parentIssue;
    private boolean subtask;


    public  JiraIssue(String key, String summary) {
        super(key, summary);
    }




}
