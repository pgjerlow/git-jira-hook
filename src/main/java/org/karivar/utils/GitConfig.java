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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.Scanner;

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
    private static final String JIRA_PROJECTS = "githook.jira.projects";

    /**
     * Gets the Jira username from the global git configuration
     * @return the Jira username
     */
    public static Optional<String> getJiraUsername() {

        try {
            return getValueFromGitConfig(JIRA_USERNAME, true);
        } catch (InterruptedException e) {
            logger.error("InterruptedException", e);
        } catch (IOException e) {
            logger.error("IOException", e);
        }
        return Optional.empty();
    }

    /**
     * Gets the base64 encoded Jira password from the global git configuration
     * @return the Jira password
     */
    public static Optional<String> getJiraEncodedPassword() {

        try {
            return getValueFromGitConfig(JIRA_PASSWORD, true);
        } catch (InterruptedException e) {
            logger.error("InterruptedException", e);
        } catch (IOException e) {
            logger.error("IOException", e);
        }
        return Optional.empty();
    }

    /**
     * Gets the Jira address from the global git configuration
     * @return the Jira address
     */
    public static Optional<String> getJiraAddress() {

        try {
            return getValueFromGitConfig(JIRA_ADDRESS, true);
        } catch (InterruptedException e) {
            logger.error("InterruptedException", e);
        } catch (IOException e) {
            logger.error("IOException", e);
        }
        return Optional.empty();
    }

    /**
     * Gets the language settings from the global git configuration
     * @return the language settings
     */
    public static Optional<String> getLanguageSettings() {
        try {
            return getValueFromGitConfig(GIT_HOOK_LANGUAGE_SETTINGS, false);
        } catch (InterruptedException e) {
            logger.error("InterruptedException", e);
        } catch (IOException e) {
            logger.error("IOException", e);
        }
        return Optional.empty();
    }

    /**
     * Gets a list of potential Jira projects from the local git configuration
     * @return the Jira projects
     */
    public static Optional<String> getJiraProjects() {

        try {
            return getValueFromGitConfig(JIRA_PROJECTS, false);
        } catch (InterruptedException e) {
            logger.error("InterruptedException", e);
        } catch (IOException e) {
            logger.error("IOException", e);
        }
        return Optional.empty();
    }


    private static Optional<String> getValueFromGitConfig(String key, boolean isGlobalElement)
            throws InterruptedException, IOException {
        String command = "git config ";

        if (isGlobalElement) {
            command += "--global ";
        }

        command += key;
        Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(command);
        int errorCode = process.waitFor();

        if (errorCode == 0) {
            return output(process.getInputStream());
        }

        return Optional.empty();
    }

    private static Optional<String> output(InputStream is) {
        Scanner scanner = new Scanner(new InputStreamReader(is)).useDelimiter("\\A");

        if (scanner.hasNextLine()) {
            return Optional.of(scanner.nextLine());
        }

        return Optional.empty();
    }
}
