/**
 * Copyright (C) 2017 Per Ivar Gjerløw
 * All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */
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
