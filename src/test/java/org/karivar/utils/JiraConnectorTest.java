package org.karivar.utils;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import mockit.Deencapsulation;
import mockit.Injectable;
import mockit.Tested;
import org.junit.*;
import org.karivar.utils.other.UTF8Control;

import java.net.URI;
import java.util.*;

import static org.junit.Assert.*;


public class JiraConnectorTest {

    @Tested
    private JiraConnector jiraConnector;

    @Injectable
    private static ResourceBundle resourceBundle;

    @Injectable
    private IssueRestClient issueRestClient;

    @BeforeClass
    public static void setUpClass() {
        resourceBundle = ResourceBundle.getBundle("messages", Locale.forLanguageTag("en"),
                new UTF8Control());
    }

    @AfterClass
    public static void tearDown() throws Exception {
        resourceBundle = null;
    }

    @Test
    public void getDecodedPasswordHello() {
        jiraConnector = new JiraConnector(resourceBundle);
        String decodedPassword = Deencapsulation.invoke(jiraConnector, "getDecodedPassword", "aGVsbG8=");
        assertNotNull(decodedPassword);
        assertEquals("hello", decodedPassword);
    }

    @Test
    public void getDecodedPasswordGermanUber() {
        jiraConnector = new JiraConnector(resourceBundle);
        String decodedPassword = Deencapsulation.invoke(jiraConnector, "getDecodedPassword", "w5xiZXI=");
        assertNotNull(decodedPassword);
        assertEquals("Über", decodedPassword);
    }

    @Test
    public void getJiraAddressUriEmpty() {
        jiraConnector = new JiraConnector(resourceBundle);
        URI uri = Deencapsulation.invoke(jiraConnector, "getJiraAddressUri", "");
        assertNull(uri);
    }

    @Test
    public void getJiraAddressUriKnownDns() {
        jiraConnector = new JiraConnector(resourceBundle);
        URI uri = Deencapsulation.invoke(jiraConnector, "getJiraAddressUri", "https:///google.com");
        assertNotNull(uri);
        assertEquals("https:///google.com", uri.toString());
    }

}
