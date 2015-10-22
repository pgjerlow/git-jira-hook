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

public class GitHook {
    private final Logger logger = LoggerFactory.getLogger(GitHook.class);
    private static GitHook githook;

    public static void main(String[] args) {
        githook = new GitHook();
        githook.init(args);
    }

    public void init(String[] args) {

        if (args != null) {
            // Loads the COMMIT_MSG file
            Path path = Paths.get(args[0]);
            try {
                String commitMessageContents = Files.lines(path, StandardCharsets.UTF_8).collect(Collectors.joining());
                logger.debug("The file contents are {}", commitMessageContents);
            } catch (IOException e) {
                logger.error("Unable to read commit message file. \nGot exception {}", e);
            }

        } else {
            logger.error("No commit message found");
        }
    }

}
