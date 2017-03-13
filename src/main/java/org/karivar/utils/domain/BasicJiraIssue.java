/**
 * Copyright (C) 2017 Per Ivar Gjerløw
 * All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */
package org.karivar.utils.domain;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

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

    @Override
    public String toString() {
        return getToStringHelper().toString();
    }

    protected MoreObjects.ToStringHelper getToStringHelper() {
        return MoreObjects.toStringHelper(this).
                add("JIRA issue key", key).
                add("Summary", summary);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BasicJiraIssue) {
            BasicJiraIssue that = (BasicJiraIssue) obj;
            return Objects.equal(this.key, that.key)
                    && Objects.equal(this.summary, that.summary);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode( key, summary);
    }
}
