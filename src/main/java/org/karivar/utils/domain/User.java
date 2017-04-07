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

public class User {
    private final String name;
    private final String displayName;

    public User(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).
                add("Username", name).
                add("Display name", displayName)
                .toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof User) {
            User that = (User) obj;
            return Objects.equal(this.name, that.name)
                    && Objects.equal(this.displayName, that.displayName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode( name, displayName);
    }
}
