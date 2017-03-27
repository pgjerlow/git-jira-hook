/**
 * Copyright (C) 2015 Per Ivar Gjerløw
 * All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */
package org.karivar.utils;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.karivar.utils.domain.IssueKeyNotFoundException;
import org.karivar.utils.domain.JiraIssue;
import org.karivar.utils.other.UTF8Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class GitHook {
    private static final String HOOK_VERSION = "0.5.0";
    private final Logger logger = LoggerFactory.getLogger(GitHook.class);
    private static GitHook githook;
    private ResourceBundle messages;
    private static CommitMessageManipulator manipulator;
    private JiraConnector jiraConnector;
    private JiraIssue populatedIssue;
    private Map<String, List<String>> issueTypesAndStatuses;
    private List<String> issueLinks;
    private String jiraProjectKeys;
    private boolean commitOverridden = false;
    private boolean jiraCommunicationOverridden = false;
    private boolean assigneeOverridden = false;

    public static void main(String[] args) {
        githook = new GitHook();
        githook.init(args);
    }

    private void init(String[] args) {

        loadI18nMessages(GitConfig.getLanguageSettings());
        manipulator = new CommitMessageManipulator(messages);
        printInitalText();

        if (args != null && args.length > 0) {
            manipulator.loadCommitMessage(args[0]);
            loadApplicationProperties();
            fetchPopulatedJiraIssue();
            checkStateAndManipulateCommitMessage();

        } else {
            logger.error(messages.getString("error.githook.nocommitfile"));
        }
    }

    private void loadApplicationProperties() {
        // Load JIRA project list
        jiraProjectKeys =  GitConfig.getJiraProjects();

        // Load JIRA issue types and their accepted statuses.
        issueTypesAndStatuses = loadIssueTypesAndStatuses();

        // Load JIRA issue links
        issueLinks = loadIssueLinks();

    }

    private void fetchPopulatedJiraIssue() {
        // Get options for
        //   1: override communication with JIRA altogether
        //   2: override (e.g force) commits
        jiraCommunicationOverridden = manipulator.isCommunicationOverridden();
        commitOverridden = manipulator.isCommitOverridden();

        if (!jiraCommunicationOverridden && !commitOverridden) {
            // Contact JIRA, fetch JIRA issue and check state and return populated issue
            logger.debug("Preparing to communicate with Jira");

            jiraConnector = new JiraConnector(messages);
            jiraConnector.connectToJira(GitConfig.getJiraUsername(),
                    GitConfig.getJiraEncodedPassword(), GitConfig.getJiraAddress());

            try {
                Optional<String> issueKey = manipulator.getJiraIssueKeyFromCommitMessage(
                        getJiraIssueKey(jiraProjectKeys));
                populatedIssue = jiraConnector.getJiraPopulatedIssue(issueKey, issueLinks);
            } catch (IssueKeyNotFoundException e) {
                logger.error(e.getLocalizedMessage());
                System.exit(1);
            }
        } else {
            logger.debug("Communication with Jira is overridden or commit is overridden");
        }
    }

    private void checkStateAndManipulateCommitMessage() {
        // check status against allowed statues
        boolean statusOK = checkAllowedStatus();

        boolean assigneeOK;
        assigneeOverridden = manipulator.isAssigneeOverridden();
        if (!assigneeOverridden) {
            // check assignee
            assigneeOK = checkAssignee();
        } else if (manipulator.isCommitOverridden()) {
            // the commit is overridden. Thus assignee is not found, but OK anyway
            assigneeOK = true;
        } else {
            assigneeOK = assigneeOverridden;
        }

        if (statusOK && assigneeOK) {
            // Status is OK. Start manipulating commit message and accept commits to repo
            manipulator.manipulateCommitMessage(populatedIssue, getHookInformation(),
                    jiraCommunicationOverridden, assigneeOverridden);

        } else {
            // Status is not OK.
            if (!statusOK) {
                if (populatedIssue != null) {
                    logger.info(messages.getString("commitnotallowedstatus")
                            + populatedIssue.getStatus());
                }

                if (!assigneeOK) {
                    if (populatedIssue.getAssignee() != null && populatedIssue.getAssignee().isPresent()) {
                        logger.info(messages.getString("commitnotallowedassignee")
                                + populatedIssue.getAssignee().get().getDisplayName());
                    } else {
                        logger.info(messages.getString("commitnotallowedassigneeunknown"));
                    }
                }
            }
            System.exit(1);
        }
    }

    private String getHookInformation() {
        return messages.getString("commit.convention.hookinformation") + HOOK_VERSION;
    }

    private boolean checkAssignee() {
        if (populatedIssue != null && populatedIssue.getAssignee().isPresent()) {
            String assignedUsername = populatedIssue.getAssignee().
                    get().getName();

            if (GitConfig.getJiraUsername() != null && assignedUsername.equals(GitConfig.getJiraUsername())) {
                return true;
            }
        } else {
            return true;
        }

        return false;
    }

    private boolean checkAllowedStatus() {

        if (manipulator.isCommunicationOverridden()) {
            return true;
        }

        if (manipulator.isCommitOverridden()){
            // Have to handle if NONE as first parameter is used
            return true;
        }

        if (populatedIssue != null) {
            List<String> issueTypeStatuses = issueTypesAndStatuses.get(populatedIssue.getIssueTypeName());

            if (issueTypeStatuses != null && !issueTypeStatuses.isEmpty()) {

                for (String status : issueTypeStatuses) {
                    if (status.equals(populatedIssue.getStatus())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private void loadI18nMessages(String languageSettings) {
        if (languageSettings != null) {
            messages = ResourceBundle.getBundle("messages", Locale.forLanguageTag(languageSettings),
                    new UTF8Control());
        } else messages = ResourceBundle.getBundle("messages");
    }

    private Map<String, List<String>> loadIssueTypesAndStatuses() {
        Map<String, List<String>> issueTypesAndStatuses = Maps.newHashMap();

        Properties properties = loadPropertiesFile("issuetypes.properties");

        if (!properties.isEmpty()) {
            for (Object property : properties.keySet()) {
                String key = (String) property;

                String values = properties.getProperty(key);

                if (key.contains("_")) {
                    key = key.replaceAll("_", " ");
                }

                List<String> items = Lists.newArrayList(Splitter.on(", ").split(values));
                issueTypesAndStatuses.put(key, items);
            }
        }

        return issueTypesAndStatuses;
    }

    private List<String> loadIssueLinks() {
        List<String> links = Lists.newArrayList();

        Properties properties = loadPropertiesFile("issuelinks.properties");

        if (!properties.isEmpty()) {
            String values = properties.getProperty("issuelinks");
            links = Lists.newArrayList(Splitter.on(", ").split(values));
        }

        return links;
    }

    private Properties loadPropertiesFile(String filename) {
        Properties properties = new Properties();

        try {
            InputStream reader = getClass().getClassLoader().getResourceAsStream(filename);
            properties.load(reader);

        } catch (IOException e) {
            logger.error(messages.getString("error.loadfile.io"), filename);
        } catch (IllegalArgumentException e) {
            logger.error(messages.getString("error.loadfile.malformed"), filename);
        }
        return properties;
    }

    private void printInitalText() {
        logger.info(messages.getString("startup.information"), HOOK_VERSION);
    }

    private String getJiraIssueKey(String jiraProjectPattern) {
        String issueKey = null;

        if (jiraProjectPattern != null) {

            Optional<String> possibleIssueKey = manipulator.getJiraIssueKeyFromCommitMessage(
                    jiraProjectPattern);

            if (possibleIssueKey.isPresent()) {
                issueKey = possibleIssueKey.get();
            }

        } else {
            logger.debug("There are no project keys registered in git config");
        }

        return issueKey;
    }

}
