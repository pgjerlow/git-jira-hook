/*
 * Copyright (C) 2017 Per Ivar Gjerløw
 * All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */
package org.karivar.utils;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

class PropertyReader {

    private final ResourceBundle messages;
    private Map<String, List<String>> issueTypesAndStatuses;
    private List<String> issueLinks;
    private final Logger logger = LoggerFactory.getLogger(PropertyReader.class);

     PropertyReader(ResourceBundle bundle) {
        messages = bundle;
    }

    /**
     * Loads a property file containing all JIRA issue types and their corresponding statuses
     * which allows code check-in.
     * @return a map containing issue types and their statuses.
     */
    Map<String, List<String>> getIssueTypesAndStatuses() {
        loadIssueTypesAndStatuses();
        return issueTypesAndStatuses;
    }

    /**
     * Loads a property file containing names of the JIRA link names which will be used to connect relevant issues
     * together to increase the level of traceability.
     * @return a list containing all JIRA link names.
     */
    List<String> getIssueLinks() {
        loadIssueLinks();
        return issueLinks;
    }


     private void loadIssueLinks() {
         issueLinks = Lists.newArrayList();

        Properties properties = loadPropertiesFile("issuelinks.properties");

        if (!properties.isEmpty()) {
            String values = properties.getProperty("issuelinks");
            issueLinks = Lists.newArrayList(Splitter.on(", ").split(values));
        }
    }

    private void loadIssueTypesAndStatuses() {
        issueTypesAndStatuses = Maps.newHashMap();

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
}
