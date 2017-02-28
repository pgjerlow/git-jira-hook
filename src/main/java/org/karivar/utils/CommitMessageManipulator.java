/**
 * Copyright (C) 2015 Per Ivar Gjerløw
 * All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */
package org.karivar.utils;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.karivar.utils.domain.IssueKeyNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


/**
 * This class is responsible for handling of the commit message. This includes reading, saving and extracting
 * information (e.g the given Jira issue key and options)
 */
public class CommitMessageManipulator {
    private final Logger logger = LoggerFactory.getLogger(CommitMessageManipulator.class);

    private static final String JIRA_COMMUNICATION_OVERRIDDEN = "-O";
    private static final String JIRA_ASSIGNEE_OVERRIDDEN = "-A";
    private static final String JIRA_COMMIT_OVERRIDDEN = "NONE";

    private List<String> commitFileContents = null;

    public void loadCommitMessage(String filename) {

        // Load the commit message file
        Path path = Paths.get(filename);
        try {
            commitFileContents = Files.readAllLines(path, StandardCharsets.UTF_8);
            logger.debug("The file contents are: \n\t {}", commitFileContents);
        } catch (IOException e) {
            logger.error("Unable to read commit message file. \nGot exception {}", e);
        }
    }

    public boolean isCommunicationOverridden() {
        boolean isOverridden = false;

        String[] wordList = commitFileContents.get(0).split("\\s+");
        String last = wordList[wordList.length - 1];

        if (last.equalsIgnoreCase(JIRA_COMMUNICATION_OVERRIDDEN)) {
            isOverridden = true;
        }
        return isOverridden;
    }

    public boolean isCommitOverridden() {

        boolean isCommitOverridden = false;

        String[] wordList = commitFileContents.get(0).split("\\s+");

        if (wordList.length > 0) {
            String first = wordList[0];

            if (first.equals(JIRA_COMMIT_OVERRIDDEN)) {
                isCommitOverridden = true;
            }
        }

        return isCommitOverridden;
    }

    public boolean isAssigneeOverridden() {
        boolean isAssigneeOverridden = false;

        String[] wordList = commitFileContents.get(0).split("\\s+");
        String last = wordList[wordList.length - 1];

        if (last.equalsIgnoreCase(JIRA_ASSIGNEE_OVERRIDDEN)) {
            isAssigneeOverridden = true;
        }

        return isAssigneeOverridden;
    }

    public String getJiraIssueKeyFromCommitMessage(String jiraIssuePattern) throws IssueKeyNotFoundException {
        String jiraIssueKey = null;
        String firstLineOfCommitMessage = commitFileContents.get(0);
        logger.debug("Starting getJiraIssueKeyFromCommitMessage({}, {})", firstLineOfCommitMessage, jiraIssuePattern);

        if (!Strings.isNullOrEmpty(firstLineOfCommitMessage)) {

            if (!Strings.isNullOrEmpty(jiraIssuePattern)) {
                Splitter jiraPatternSplitter = Splitter.on(" ");
                List<String> commitLineWords = jiraPatternSplitter.splitToList(firstLineOfCommitMessage);
                List<String> jiraIssuePatterns = jiraPatternSplitter.splitToList(jiraIssuePattern);

                if (!commitLineWords.isEmpty()) {
                    for (String pattern : jiraIssuePatterns) {
                        for (String word : commitLineWords) {
                            if (word.toUpperCase().startsWith(pattern.toUpperCase())) {
                                logger.debug("Found issue key {}", word);
                                jiraIssueKey = word.toUpperCase();
                                break;
                            }
                        }
                    }
                }  else{
                    logger.error("The jira issue pattern is not found");
                    throw new IssueKeyNotFoundException("The jira issue pattern is not found");
                }
            } else {
                logger.error("The jira issue pattern is not found");
                throw new IssueKeyNotFoundException("The jira issue pattern is not found");
            }
        }  else {
                logger.error("The commit line is empty");
                throw new IssueKeyNotFoundException("The commit line is empty");
        }

        return jiraIssueKey;
    }
}



