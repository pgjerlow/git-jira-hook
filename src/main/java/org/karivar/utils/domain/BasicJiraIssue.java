package org.karivar.utils.domain;


public class BasicJiraIssue {
    private String key;
    private String summary;

    public BasicJiraIssue(String key, String summary) {
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
