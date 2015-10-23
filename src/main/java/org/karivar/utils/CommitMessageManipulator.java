/**
 * Copyright (C) 2015 Per Ivar Gjerløw
 * All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */
package org.karivar.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

/**
 * This class is responsible for handling of the commit message. This includes reading, saving and extracting
 * information (e.g the given Jira issue key and options)
 */
public class CommitMessageManipulator {
    private final Logger logger = LoggerFactory.getLogger(CommitMessageManipulator.class);

    private static final String JIRA_COMMUNICATION_OVERRIDDEN = "-O";
    private static final String JIRA_ASSIGNEE_OVERRIDDEN = "-A";
    private static final String JIRA_COMMIT_OVERRIDDEN = "NONE";

    private String commitFileContents = null;

    public void loadCommitMessage(String filename) {

        // Load the commit message file
        Path path = Paths.get(filename);
        try {
            commitFileContents = Files.lines(path, StandardCharsets.UTF_8)
                    .collect(Collectors.joining());
            logger.debug("The file contents are {}", commitFileContents);
        } catch (IOException e) {
            logger.error("Unable to read commit message file. \nGot exception {}", e);
        }
    }

    public boolean isCommunicationOverridden() {
        return false;
    }

    public boolean isCommitOverridden() {
        return false;
    }

    public boolean isAssigneeOverridden() {
        return false;
    }


}
