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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * This class is responsible for retrieving information from the local and global
 * git configuration
 */
public class GitConfig {
    private static final Logger logger = LoggerFactory.getLogger(GitConfig.class);

    private static final String JIRA_USERNAME = "githook.jira.username";
    private static final String JIRA_PASSWORD = "githook.jira.password";
    private static final String JIRA_ADDRESS = "githook.jira.address";
    private static final String GIT_HOOK_LANGUAGE_SETTINGS = "githook.language";
    private static final String JIRA_PROJECTS = "githook.jira.projectkey";

    /**
     * Gets the Jira username from the global git configuration
     * @return the Jira username
     */
    public static String getJiraUsername() {

        try {
            return getValueFromGitConfig(JIRA_USERNAME, true, false);
        } catch (InterruptedException e) {
            logger.error("InterruptedException", e);
        } catch (IOException e) {
            logger.error("IOException", e);
        }
        return null;
    }

    /**
     * Gets the base64 encoded Jira password from the global git configuration
     * @return the Jira password
     */
    public static String getJiraEncodedPassword() {

        try {
            return getValueFromGitConfig(JIRA_PASSWORD, true, false);
        } catch (InterruptedException e) {
            logger.error("InterruptedException", e);
        } catch (IOException e) {
            logger.error("IOException", e);
        }
        return null;
    }

    /**
     * Gets the Jira address from the global git configuration
     * @return the Jira address
     */
    public static String getJiraAddress() {

        try {
            return getValueFromGitConfig(JIRA_ADDRESS, true, false);
        } catch (InterruptedException e) {
            logger.error("InterruptedException", e);
        } catch (IOException e) {
            logger.error("IOException", e);
        }
        return null;
    }

    /**
     * Gets the language settings from the global git configuration
     * @return the language settings
     */
    public static String getLanguageSettings() {
        try {
            return getValueFromGitConfig(GIT_HOOK_LANGUAGE_SETTINGS, false, false);
        } catch (InterruptedException e) {
            logger.error("InterruptedException", e);
        } catch (IOException e) {
            logger.error("IOException", e);
        }
        return null;
    }

    /**
     * Gets a list of potential Jira projects from the local git configuration
     * @return the Jira projects
     */
    public static String getJiraProjects() {

        try {
            return getValueFromGitConfig(JIRA_PROJECTS, false, true);
        } catch (InterruptedException e) {
            logger.error("InterruptedException", e);
        } catch (IOException e) {
            logger.error("IOException", e);
        }
        return null;
    }


    private static String getValueFromGitConfig(String key, boolean isGlobalElement, boolean multipleFetches)
            throws InterruptedException, IOException {
        String command = "git config ";

        if (isGlobalElement) {
            command += "--global ";
        }

        if (multipleFetches) {
            command += "--get-all ";
        }

        command += key;
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(command);
        int errorCode = process.waitFor();

        if (errorCode == 0) {
            return output(process.getInputStream(), multipleFetches);
        }

        return null;
    }

    private static String output(InputStream is, boolean multipleLines) {
          if (multipleLines) {
            return getMultipleResults(is);
        } else {
            return getSingleResult(is);
        }
    }

    private static String getSingleResult(InputStream is) {
        Scanner scanner = new Scanner(new InputStreamReader(is)).useDelimiter("\\A");
        if (scanner.hasNextLine()) {
            return scanner.nextLine();
        }
        return null;
    }

    private static String getMultipleResults(InputStream is) {
        Scanner scanner;
        String results = new BufferedReader(new InputStreamReader(is)).lines().
                collect(Collectors.joining(System.lineSeparator()));
        scanner = new Scanner(results).useDelimiter(System.lineSeparator());

        String joinedStrings = "";
        while (scanner.hasNextLine()) {
            joinedStrings += scanner.nextLine();

            if (scanner.hasNextLine()) {
                joinedStrings += " ";
            }
        }

        if (!joinedStrings.isEmpty()) {
            return joinedStrings;
        }

        return null;
    }
}
