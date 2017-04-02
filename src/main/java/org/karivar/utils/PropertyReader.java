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

public class PropertyReader {

    private ResourceBundle messages;
    private Map<String, List<String>> issueTypesAndStatuses;
    private final Logger logger = LoggerFactory.getLogger(PropertyReader.class);

    public PropertyReader(ResourceBundle bundle) {
        messages = bundle;
    }

    /**
     *
     * @return
     */
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

    public Map<String, List<String>> getIssueTypesAndStatuses() {
        loadIssueTypesAndStatuses();
        return issueTypesAndStatuses;
    }

    /**
     *
     * @return
     */
    public List<String> loadIssueLinks() {
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
}
