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
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * This class is responsible for retrieving information from the local and global
 * git configuration
 */
public class GitConfig {
    private final Logger logger = LoggerFactory.getLogger(GitConfig.class);

    private static final String JIRA_USERNAME = "githook.jira.username";
    private static final String JIRA_PASSWORD = "githookjira.password";
    private static final String JIRA_ADDRESS = "githook.jira.address";
    private static final String GIT_HOOK_LANGUAGE_SETTINGS = "githook.language";
    private static final String JIRA_PROJECTS = "githook.jira.projects";

    /**
     * Gets the Jira username from the global git configuration
     * @return the Jira username
     */
    public static Optional<String> getJiraUsername() {
        return Optional.empty();
    }

    /**
     * Gets the base64 encoded Jira password from the global git configuration
     * @return the Jira password
     */
    public static Optional<String> getJiraEncodedPassword() {
        return Optional.empty();
    }

    /**
     * Gets the Jira address from the global git configuration
     * @return the Jira address
     */
    public static Optional<String> getJiraAddress() {
        return Optional.empty();
    }

    /**
     * Gets the language settings from the global git configuration
     * @return the language settings
     */
    public static Optional<String> getLanguageSettings() {
        return Optional.empty();
    }

    /**
     * Gets a list of potential Jira projects from the local git configuration
     * @return the Jira projects
     */
    public static Optional<List<String>> getJiraProjects() {
        return Optional.empty();
    }


    private Optional<String> getValueFromGitConfig(String key, boolean isGlobalElement)
            throws InterruptedException, IOException {
        String command = "git config ";

        if (isGlobalElement) {
            command += "--global ";
        }

        command += "key";

        ProcessBuilder builder = new ProcessBuilder(command);
        Process process = builder.start();

        int errorCode = process.waitFor();
        logger.debug("Execution of {} got the return code {}", command, errorCode);

        if (errorCode == 0) {
            return output(process.getInputStream());
        }
        return Optional.empty();
    }

    private Optional<String> output(InputStream is) {
        Scanner scanner = new Scanner(new InputStreamReader(is));

        if (scanner.hasNextLine()) {
            return Optional.of(scanner.nextLine());
        }

        return Optional.empty();
    }
}
