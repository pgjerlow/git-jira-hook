/**
 * Copyright (C) 2017 Per Ivar Gjerløw
 * All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */
package org.karivar.utils.domain;

public class IssueKeyNotFoundException extends RuntimeException {
    public IssueKeyNotFoundException(String message) {
        super(message);
    }
}
