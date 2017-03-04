package org.karivar.utils.domain;


public class ParentJiraIssue {
    private String key;
    private String summary;

    public ParentJiraIssue(String key, String summary) {
        this.key = key;
        this.summary = summary;
    }

    public String getKey() {
        return key;
    }

    public String getSummary() {
        return summary;
    }
}
