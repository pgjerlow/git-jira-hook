/**
 * Copyright (C) 2015 Per Ivar Gjerløw
 * All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */
package org.karivar.utils;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import com.google.common.io.Files;

import java.util.*;


/**
 * This class is responsible for handling of the commit message. This includes reading, saving and extracting
 * information (e.g the given Jira issue key and options)
 */
public class CommitMessageManipulator {
    private final Logger logger = LoggerFactory.getLogger(CommitMessageManipulator.class);

    private static final String JIRA_COMMUNICATION_OVERRIDDEN = "-O";
    private static final String JIRA_ASSIGNEE_OVERRIDDEN = "-A";
    private static final String JIRA_COMMIT_OVERRIDDEN = "NONE";

    private ResourceBundle messages;
    private String commitMessageFilename;
    private List<String> commitFileContents = null;
    private boolean jiraIssueKeyFound;

    public CommitMessageManipulator(ResourceBundle bundle) {
        messages = bundle;
    }


    public void loadCommitMessage(String filename) {

        // Load the commit message file
        try {
            File file = new File(filename);
            commitFileContents = Files.readLines(file, Charsets.UTF_8);
            logger.debug("The file contents are: \n\t {}", commitFileContents);
            commitMessageFilename = filename;

        } catch (FileNotFoundException e) {
            logger.error(messages.getString("error.loadfile.filenotfound") + filename);
        } catch (IOException e) {
            logger.error(messages.getString("loadfile.commit.io"));
        }
    }

    public void writeCommitMessage(List<String> commitFileContents) {
        File file = new File(commitMessageFilename);
        try {
            Files.asCharSink(file, Charsets.UTF_8).writeLines(commitFileContents);
        } catch (IOException e) {
            logger.error(messages.getString("writefile.commit.io"), e);
        }
    }

    public boolean isCommunicationOverridden() {
        boolean isOverridden = false;
        if (commitFileContents != null && commitFileContents.size() > 0) {
            String[] wordList = commitFileContents.get(0).split("\\s+");
            String last = wordList[wordList.length - 1];

            if (last.equalsIgnoreCase(JIRA_COMMUNICATION_OVERRIDDEN)) {
                isOverridden = true;
            }
        }
        return isOverridden;
    }

    public boolean isCommitOverridden() {

        boolean isCommitOverridden = false;
        if (commitFileContents != null && commitFileContents.size() > 0) {
            String[] wordList = commitFileContents.get(0).split("\\s+");

            if (wordList.length > 0) {
                String first = wordList[0];

                if (first.equalsIgnoreCase(JIRA_COMMIT_OVERRIDDEN)) {
                    isCommitOverridden = true;
                }
            }
        }
        return isCommitOverridden;
    }

    public boolean isAssigneeOverridden() {
        boolean isAssigneeOverridden = false;
        if (commitFileContents != null && commitFileContents.size() > 0) {
            String[] wordList = commitFileContents.get(0).split("\\s+");
            String last = wordList[wordList.length - 1];

            if (last.equalsIgnoreCase(JIRA_ASSIGNEE_OVERRIDDEN)) {
                isAssigneeOverridden = true;
            }
        }

        return isAssigneeOverridden;
    }


    public Optional<String> getJiraIssueKeyFromCommitMessage(String jiraIssuePattern) {
        Optional<String> jiraIssueKey = Optional.empty();
        if (commitFileContents != null && commitFileContents.size() > 0) {
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
                                    jiraIssueKey = Optional.of(word.toUpperCase());
                                    jiraIssueKeyFound = true;
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    logger.error(messages.getString("githook.jiraissue.pattern.notfound"));
                }
            } else {
                logger.error(messages.getString("githook.jiraissue.empty"));
            }
        }

        return jiraIssueKey;
    }

    /**
     * Removes any options from the original commit message (first line only)
     * @return
     */
    public List<String> getStrippedCommitMessage() {
        ArrayList<String> strippedCommitMessage = Lists.newArrayList();
        if (commitFileContents != null && commitFileContents.size() > 0) {
            strippedCommitMessage = (ArrayList<String>) commitFileContents;
            strippedCommitMessage.set(0, getStripped(strippedCommitMessage.get(0)));
        }

        return strippedCommitMessage;

    }

    private String getStripped(final String firstLine) {


        //String[] wordList = firstLine.split("\\s+");
        List<String> arrayList = Arrays.asList(firstLine);
        String[] wordList = arrayList.get(0).split("\\s+");

        if (isCommitOverridden() || isCommunicationOverridden() || jiraIssueKeyFound) {
            wordList[0] = wordList[0].toUpperCase();
        }

        // Have to iterate through all words
        for (int i = 1; i < wordList.length; i++) {
            String word = wordList[i];
            // Assignee
            if (word.equalsIgnoreCase(JIRA_ASSIGNEE_OVERRIDDEN)) {
                wordList = Arrays.copyOf(wordList, wordList.length-1);
            }

            if (word.equalsIgnoreCase(JIRA_COMMUNICATION_OVERRIDDEN)) {
                wordList = Arrays.copyOf(wordList, wordList.length-1);
            }
        }

        return String.join(" ", wordList);
    }
}



