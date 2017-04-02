/*
 * Copyright (C) 2015 Per Ivar Gjerløw
 * All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */
package org.karivar.utils;

import org.karivar.utils.domain.IssueKeyNotFoundException;
import org.karivar.utils.domain.JiraIssue;
import org.karivar.utils.other.UTF8Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

public class GitHook {
    private static final String HOOK_VERSION = "0.5.2";
    private final Logger logger = LoggerFactory.getLogger(GitHook.class);
    private ResourceBundle messages;
    private static CommitMessageManipulator manipulator;
    private boolean commitOverridden = false;
    private boolean jiraCommunicationOverridden = false;

    public static void main(String[] args) {
        GitHook githook = new GitHook();
        githook.init(args);
    }

    private void init(String[] args) {

        loadI18nMessages(GitConfig.getLanguageSettings());
        manipulator = new CommitMessageManipulator(messages);
        printInitalText();

        if (args != null && args.length > 0) {
            manipulator.loadCommitMessage(args[0]);
            //fetchPopulatedJiraIssue();

            JiraIssue populatedIssue = null;
            try {
                populatedIssue = getPopulatedJiraIssue();
            } catch (IssueKeyNotFoundException e) {
                logger.error(e.getLocalizedMessage());
                System.exit(1);
            }

            if (!manipulator.checkStateAndManipulateCommitMessage(populatedIssue,
                    jiraCommunicationOverridden, HOOK_VERSION)) {
                System.exit(1);
            }

        } else {
            logger.error(messages.getString("error.githook.nocommitfile"));
        }
    }

    private JiraIssue getPopulatedJiraIssue() throws IssueKeyNotFoundException {
        // Get options for
        //   1: override communication with JIRA altogether
        //   2: override (e.g force) commits
        jiraCommunicationOverridden = manipulator.isCommunicationOverridden();
        commitOverridden = manipulator.isCommitOverridden();

        if (!jiraCommunicationOverridden && !commitOverridden) {
            // Contact JIRA, fetch JIRA issue and check state and return populated issue
            logger.debug("Preparing to communicate with JIRA");

            JiraConnector jiraConnector = new JiraConnector(messages);
            jiraConnector.connectToJira(GitConfig.getJiraUsername(),
                    GitConfig.getJiraEncodedPassword(), GitConfig.getJiraAddress());


            Optional<String> issueKey = manipulator.getJiraIssueKeyFromCommitMessage(
                    manipulator.getJiraIssueKey(GitConfig.getJiraProjects()));

            PropertyReader propertyReader = new PropertyReader(messages);
            return jiraConnector.getJiraPopulatedIssue(issueKey,  propertyReader.loadIssueLinks());
        } else {
            logger.debug("Communication with JIRA is overridden or commit is overridden");
        }
        return null;
    }

    private void loadI18nMessages(String languageSettings) {
        if (languageSettings != null) {
            messages = ResourceBundle.getBundle("messages", Locale.forLanguageTag(languageSettings),
                    new UTF8Control());
        } else messages = ResourceBundle.getBundle("messages");
    }

    private void printInitalText() {
        logger.info(messages.getString("startup.information"), HOOK_VERSION);
    }



}
