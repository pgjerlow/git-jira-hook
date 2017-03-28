/**
 * Copyright (C) 2017 Per Ivar Gjerløw
 * All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */
package org.karivar.utils;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static org.junit.Assert.*;

public class CommitMessageManipulatorTest {

    private CommitMessageManipulator manipulator;
    private static  ResourceBundle resourceBundle;
    private static String JIRA_ISSUE_PATTERNS = "EXAMPLE PR OTHER";

    @BeforeClass
    public static void setUpClass() {
        resourceBundle = ResourceBundle.getBundle("messages");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        resourceBundle = null;
    }

//    @Test(expected = FileNotFoundException.class)
    @Test
    public void loadCommitMessageNoFilename() throws Exception {
        manipulator = new CommitMessageManipulator(resourceBundle);
        manipulator.loadCommitMessage(null);
    }

    @Test
    public void loadCommitMessageEmptyFilename() {
        manipulator = new CommitMessageManipulator(resourceBundle);
        manipulator.loadCommitMessage("");
    }

    @Test
    public void loadCommitMessageUnknownFilename() {
        manipulator = new CommitMessageManipulator(resourceBundle);
        manipulator.loadCommitMessage("src/test/resources/something");
    }

    @Test
    public void loadCommitMessageKnownFilename() {
        manipulator = new CommitMessageManipulator(resourceBundle);
        manipulator.loadCommitMessage("src/test/resources/emptycommit.txt");
    }

    @Test
    public void isCommunicationOverriddenNotOverridden() throws Exception {
        manipulator = new CommitMessageManipulator(resourceBundle);
        manipulator.loadCommitMessage("src/test/resources/onlinenormalcommit.txt");
        assertFalse("Communication with JIRA isn't overridden",  manipulator.isCommunicationOverridden());
    }

    @Test
    public void isCommunicationOverridden() throws Exception {
        manipulator = new CommitMessageManipulator(resourceBundle);
        manipulator.loadCommitMessage("src/test/resources/onelinejiraconnectionoverridden.txt");
        assertTrue("Communication with JIRA isn't overridden",  manipulator.isCommunicationOverridden());
    }

    @Test
    public void isCommitOverriddenNotOverridden() throws Exception {
        manipulator = new CommitMessageManipulator(resourceBundle);
        manipulator.loadCommitMessage("src/test/resources/onlinenormalcommit.txt");
        assertFalse("Commit isn't overridden",  manipulator.isCommitOverridden());
    }

    @Test
    public void isCommitOverridden() throws Exception {
        manipulator = new CommitMessageManipulator(resourceBundle);
        manipulator.loadCommitMessage("src/test/resources/onelinenone.txt");
        assertTrue("Commit isn't overridden",  manipulator.isCommitOverridden());
    }

    @Test
    public void isAssigneeOverriddenNotOverridden() throws Exception {
        manipulator = new CommitMessageManipulator(resourceBundle);
        manipulator.loadCommitMessage("src/test/resources/onlinenormalcommit.txt");
        assertFalse("Assignee isn't overridden",  manipulator.isAssigneeOverridden());
    }

    @Test
    public void isAssigneeOverridden() throws Exception {
        manipulator = new CommitMessageManipulator(resourceBundle);
        manipulator.loadCommitMessage("src/test/resources/onelineassigneeoverridden.txt");
        assertTrue("Assignee isn't overridden",  manipulator.isAssigneeOverridden());
    }

    @Test
    public void getJiraIssueKeyFromCommitMessage() throws Exception {
        manipulator = new CommitMessageManipulator(resourceBundle);
        manipulator.loadCommitMessage("src/test/resources/onlinenormalcommit.txt");
        Optional<String> jiraIssue = manipulator.getJiraIssueKeyFromCommitMessage(JIRA_ISSUE_PATTERNS);
        assertTrue("There is a JIRA issue", jiraIssue.isPresent());
        assertEquals("EXAMPLE-1", jiraIssue.get());
    }

    @Test
    public void getJiraIssueKeyFromCommitMessageNone() throws Exception {
        manipulator = new CommitMessageManipulator(resourceBundle);
        manipulator.loadCommitMessage("src/test/resources/onelinenone.txt");
        Optional<String> jiraIssue = manipulator.getJiraIssueKeyFromCommitMessage(JIRA_ISSUE_PATTERNS);
        assertFalse("There is no JIRA issue", jiraIssue.isPresent());
    }

    @Test
    public void getJiraIssueKeyFromCommitMessageAssigneeOverridden() throws Exception {
        manipulator = new CommitMessageManipulator(resourceBundle);
        manipulator.loadCommitMessage("src/test/resources/onelineassigneeoverridden.txt");
        Optional<String> jiraIssue = manipulator.getJiraIssueKeyFromCommitMessage(JIRA_ISSUE_PATTERNS);
        assertTrue("There is a JIRA issue", jiraIssue.isPresent());
        assertEquals("EXAMPLE-1", jiraIssue.get());
    }

    @Test
    public void getJiraIssueKeyFromCommitMessageCommunicationOverridden() throws Exception {
        manipulator = new CommitMessageManipulator(resourceBundle);
        manipulator.loadCommitMessage("src/test/resources/onelineassigneeoverridden.txt");
        Optional<String> jiraIssue = manipulator.getJiraIssueKeyFromCommitMessage(JIRA_ISSUE_PATTERNS);
        assertTrue("There is a JIRA issue", jiraIssue.isPresent());
        assertEquals("EXAMPLE-1", jiraIssue.get());
    }

    @Test
    public void getJiraIssueKeyFromCommitMessageNoJiraIssue() throws Exception {
        manipulator = new CommitMessageManipulator(resourceBundle);
        manipulator.loadCommitMessage("src/test/resources/onelinenoissue.txt");
        Optional<String> jiraIssue = manipulator.getJiraIssueKeyFromCommitMessage(JIRA_ISSUE_PATTERNS);
        assertFalse("There is no JIRA issue", jiraIssue.isPresent());
    }

    @Test
    public void getJiraIssueKeyFromCommitMessageUnregisteredPattern() throws Exception {
        manipulator = new CommitMessageManipulator(resourceBundle);
        manipulator.loadCommitMessage("src/test/resources/onelineunregisteredjirapattern.txt");
        Optional<String> jiraIssue = manipulator.getJiraIssueKeyFromCommitMessage(JIRA_ISSUE_PATTERNS);
        assertFalse("There is no JIRA issue", jiraIssue.isPresent());
    }

    @Test
    public void getJiraIssueKeyFromCommitMessageEmptyCommit() throws Exception {
        manipulator = new CommitMessageManipulator(resourceBundle);
        manipulator.loadCommitMessage("src/test/resources/emptycommit.txt");
        Optional<String> jiraIssue = manipulator.getJiraIssueKeyFromCommitMessage(JIRA_ISSUE_PATTERNS);
        assertFalse("There is no JIRA issue", jiraIssue.isPresent());
    }

    @Test
    public void getStrippedCommitMessageEmptyCommit() throws Exception {
        manipulator = new CommitMessageManipulator(resourceBundle);
        manipulator.loadCommitMessage("src/test/resources/emptycommit.txt");
        List<String> message = manipulator.getStrippedCommitMessage();
        assertNotNull(message);
        assertEquals(0, message.size());
    }

//    @Test
//    public void manipulateCommitMessage() throws Exception {
//    }

}