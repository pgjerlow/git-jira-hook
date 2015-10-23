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

import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class GitHook {
    private final Logger logger = LoggerFactory.getLogger(GitHook.class);
    private static GitHook githook;
    private ResourceBundle messages;
    private CommitMessageManipulator manipulator;

    public static void main(String[] args) {
        githook = new GitHook();
        githook.init(args);
    }

    public void init(String[] args) {

        loadI18nMessages(GitConfig.getLanguageSettings());
        printInitalText();

        if (args != null) {
            manipulator.loadCommitMessage(args[0]);

            // Load JIRA project list

            // Load JIRA issue types and their accepted statuses.

            // Get options for
            //   1: override communication with JIRA altogether
            //   2: override (e.g force) commits
            boolean isJiraCommunicationOverridden = manipulator.isCommunicationOverridden();
            boolean isCommitOverridden = manipulator.isCommitOverridden();

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
