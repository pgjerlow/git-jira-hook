/**
 * Copyright (C) 2015 Per Ivar Gjerløw
 * All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */
package org.karivar.utils;

import org.karivar.utils.domain.IssueKeyNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class GitHook {
    private final Logger logger = LoggerFactory.getLogger(GitHook.class);
    private static GitHook githook;
    private ResourceBundle messages;
    private static CommitMessageManipulator manipulator;
    private JiraConnector jiraConnector;

    public static void main(String[] args) {
        githook = new GitHook();
        manipulator = new CommitMessageManipulator();
        githook.init(args);
    }

    public void init(String[] args) {

        loadI18nMessages(GitConfig.getLanguageSettings());
        printInitalText();

        if (args != null && args.length > 0) {
            manipulator.loadCommitMessage(args[0]);

            // Load JIRA project list
           Optional<String> jiraProjectKeys =  GitConfig.getJiraProjects();

            // Load JIRA issue types and their accepted statuses.

            // Get options for
            //   1: override communication with JIRA altogether
            //   2: override (e.g force) commits
            boolean isJiraCommunicationOverridden = manipulator.isCommunicationOverridden();
            boolean isCommitOverridden = manipulator.isCommitOverridden();

            if (!isJiraCommunicationOverridden && !isCommitOverridden) {
                logger.debug("Preparing to communicate with Jira");

//                String jiraIssuekey = null;

//                if (jiraProjectKeys.isPresent()) {
//                    try {
//                        jiraIssuekey = manipulator.getJiraIssueKeyFromCommitMessage(jiraProjectKeys.get());
//                        logger.debug("found key");
//                    } catch (IssueKeyNotFoundException e) {
//                        e.printStackTrace();
//                    }
//                } else {
//                    logger.debug("There are no project keys registered in git config");
//                }
//
//                if (jiraIssuekey != null) {
//                    logger.debug("There is a jira issue key. Start contacting Jira to fetch the issue itself");
//                }
                JiraConnector jiraConnector = new JiraConnector();
                jiraConnector.connectToJira(GitConfig.getJiraUsername(),
                        GitConfig.getJiraEncodedPassword(), GitConfig.getJiraAddress());

            } else {
                logger.debug("Communication with Jira is overridden or commit is overridden");
            }

            // Contact JIRA, fetch JIRA issue and check state and return populated issue

            // If state is OK, manipulate commit message.

            // Update the commit message file and allow commit


        } else {
            logger.error(messages.getString("error.githook.nocommitfile"));
        }
    }

    private void loadI18nMessages(Optional<String> languageSettings) {
        if (languageSettings.isPresent()) {
            messages = ResourceBundle.getBundle("messages", Locale.forLanguageTag(languageSettings.get()));
        } else messages = ResourceBundle.getBundle("messages");
    }

    private void printInitalText() {
        logger.info(messages.getString("startup.information") +  " 0.0.1");
    }

}
