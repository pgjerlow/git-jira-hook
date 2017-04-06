/*
 * Copyright (C) 2017 Per Ivar Gjerløw
 * All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */
package org.karivar.utils;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import mockit.Expectations;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.karivar.utils.domain.BasicJiraIssue;
import org.karivar.utils.domain.JiraIssue;
import org.karivar.utils.domain.User;
import org.karivar.utils.other.UTF8Control;

import java.io.File;
import java.util.*;

import static org.junit.Assert.*;

public class CommitMessageManipulatorTest {

    private CommitMessageManipulator manipulator;
    private static  ResourceBundle resourceBundle;
    private static String JIRA_ISSUE_PATTERNS = "EXAMPLE PR ERROR";
    private static String PROCESSED_COMMIT_PATH = "src/test/resources/output.txt";
    private JiraIssue issue;

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
    public void loadCommitMessageEmptyCommit() {
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

    @Test
    public void getStrippedCommitMessage() throws Exception {
        manipulator = new CommitMessageManipulator(resourceBundle);
        manipulator.loadCommitMessage("src/test/resources/onlinenormalcommit.txt");
        List<String> message = manipulator.getStrippedCommitMessage();
        assertNotNull(message);
        assertEquals(1, message.size());
        assertEquals("example-1 Added som files for this issue", message.get(0));
    }

    @Test
    public void getStrippedCommitMessageNone() throws Exception {
        manipulator = new CommitMessageManipulator(resourceBundle);
        manipulator.loadCommitMessage("src/test/resources/onelinenone.txt");
        List<String> message = manipulator.getStrippedCommitMessage();
        assertNotNull(message);
        assertEquals(1, message.size());
        assertEquals("NONE did some configuration manager work", message.get(0));
    }

    @Test
    public void getStrippedCommitMessageNoJiraIssue() throws Exception {
        manipulator = new CommitMessageManipulator(resourceBundle);
        manipulator.loadCommitMessage("src/test/resources/onelinenoijiraissue.txt");
        List<String> message = manipulator.getStrippedCommitMessage();
        assertNotNull(message);
        assertEquals(1, message.size());
        assertEquals("this is a commit with no jira issue", message.get(0));
    }

    @Test
    public void getStrippedCommitMessageAssigneeOverridden() throws Exception {
        manipulator = new CommitMessageManipulator(resourceBundle);
        manipulator.loadCommitMessage("src/test/resources/onelineassigneeoverridden.txt");
        List<String> message = manipulator.getStrippedCommitMessage();
        assertNotNull(message);
        assertEquals(1, message.size());
        assertEquals("EXAMPLE-1 even more functionality added for wrong assignee", message.get(0));
    }

    @Test
    public void getStrippedCommitMessageConnectionOverridden() throws Exception {
        manipulator = new CommitMessageManipulator(resourceBundle);
        manipulator.loadCommitMessage("src/test/resources/onelinejiraconnectionoverridden.txt");
        List<String> message = manipulator.getStrippedCommitMessage();
        assertNotNull(message);
        assertEquals(1, message.size());
        assertEquals("EXAMPLE-1 added some more functionality", message.get(0));
    }

    @Test
    public void manipulateCommitMessageEmptyCommit() throws Exception {
        manipulator = new CommitMessageManipulator(resourceBundle);
        manipulator.loadCommitMessage("src/test/resources/emptyCommit.txt");
        manipulator.manipulateCommitMessage(null, "Hook v 1.0", null, false, false);
    }

    @Test
    public void manipulateCommitMessageEmptyCommitOnelineNormalCommit() throws Exception {
        manipulator = new CommitMessageManipulator(resourceBundle);
        manipulator.loadCommitMessage("src/test/resources/onlinenormalcommit.txt");

        issue = new JiraIssueBuilder("EXAMPLE-1", "Add functionality for accounting")
                .setStatus("In Progress")
                .setAssignee(Optional.of(new User("alice", "Alice Developer")))
                .setIssueTypeName("Improvement")
                .setSubtask(false)
                .build();

        manipulator.manipulateCommitMessage(issue, "Hook v 1.0", PROCESSED_COMMIT_PATH, false, false);

        File output = new File(PROCESSED_COMMIT_PATH);
        List<String> commitFileContents = Files.readLines(output, Charsets.UTF_8);
        assertNotNull(output);
        assertEquals(4, commitFileContents.size());
        assertEquals("example-1 Added som files for this issue", commitFileContents.get(0));
        assertEquals("", commitFileContents.get(1));
        assertEquals("Summary: Add functionality for accounting", commitFileContents.get(2));
        assertEquals("Hook v 1.0", commitFileContents.get(3));
    }

    @Test
    public void manipulateCommitMessageEmptyCommitOnelineNone() throws Exception {
        manipulator = new CommitMessageManipulator(resourceBundle);
        manipulator.loadCommitMessage("src/test/resources/onelinenone.txt");

        manipulator.manipulateCommitMessage(null, "Hook v 1.0", PROCESSED_COMMIT_PATH, false, false);

        File output = new File(PROCESSED_COMMIT_PATH);
        List<String> commitFileContents = Files.readLines(output, Charsets.UTF_8);
        assertNotNull(output);
        assertEquals(3, commitFileContents.size());
        assertEquals("NONE did some configuration manager work", commitFileContents.get(0));
        assertEquals("", commitFileContents.get(1));
        assertEquals("Hook v 1.0", commitFileContents.get(2));
    }

    @Test
    public void manipulateCommitMessageEmptyCommitOnelineAssigneeOverridden() throws Exception {
        manipulator = new CommitMessageManipulator(resourceBundle);
        manipulator.loadCommitMessage("src/test/resources/onelineassigneeoverridden.txt");

        issue = new JiraIssueBuilder("EXAMPLE-1", "Add functionality for accounting")
                .setStatus("In Progress")
                .setAssignee(Optional.of(new User("alice", "Alice Developer")))
                .setIssueTypeName("Improvement")
                .setSubtask(false)
                .build();

        manipulator.manipulateCommitMessage(issue, "Hook v 1.0", PROCESSED_COMMIT_PATH, false, true);

        File output = new File(PROCESSED_COMMIT_PATH);
        List<String> commitFileContents = Files.readLines(output, Charsets.UTF_8);
        assertNotNull(output);
        assertEquals(5, commitFileContents.size());
        assertEquals("EXAMPLE-1 even more functionality added for wrong assignee", commitFileContents.get(0));
        assertEquals("", commitFileContents.get(1));
        assertEquals("Summary: Add functionality for accounting", commitFileContents.get(2));
        assertEquals("Assigned user is overridden", commitFileContents.get(3));
        assertEquals("Hook v 1.0", commitFileContents.get(4));
    }

    @Test
    public void manipulateCommitMessageEmptyCommitOnelineConnectionOverridden() throws Exception {
        manipulator = new CommitMessageManipulator(resourceBundle);
        manipulator.loadCommitMessage("src/test/resources/onelinejiraconnectionoverridden.txt");

        issue = new JiraIssueBuilder("EXAMPLE-1", "Add functionality for accounting")
                .setStatus("In Progress")
                .setAssignee(Optional.of(new User("alice", "Alice Developer")))
                .setIssueTypeName("Improvement")
                .setSubtask(false)
                .build();

        manipulator.manipulateCommitMessage(issue, "Hook v 1.0", PROCESSED_COMMIT_PATH, true, false);

        File output = new File(PROCESSED_COMMIT_PATH);
        List<String> commitFileContents = Files.readLines(output, Charsets.UTF_8);
        assertNotNull(output);
        assertEquals(5, commitFileContents.size());
        assertEquals("EXAMPLE-1 added some more functionality", commitFileContents.get(0));
        assertEquals("", commitFileContents.get(1));
        assertEquals("Summary: Add functionality for accounting", commitFileContents.get(2));
        assertEquals("Communication with JIRA is overridden", commitFileContents.get(3));
        assertEquals("Hook v 1.0", commitFileContents.get(4));
    }

    @Test
    public void manipulateCommitMessageEmptyCommitMultilineNormalCommit() throws Exception {
        manipulator = new CommitMessageManipulator(resourceBundle);
        manipulator.loadCommitMessage("src/test/resources/multilinenormalcommit.txt");

        issue = new JiraIssueBuilder("EXAMPLE-1", "Add functionality for accounting")
                .setStatus("In Progress")
                .setAssignee(Optional.of(new User("alice", "Alice Developer")))
                .setIssueTypeName("Improvement")
                .setSubtask(false)
                .build();

        manipulator.manipulateCommitMessage(issue, "Hook v 1.0", PROCESSED_COMMIT_PATH, false, false);

        File output = new File(PROCESSED_COMMIT_PATH);
        List<String> commitFileContents = Files.readLines(output, Charsets.UTF_8);
        assertNotNull(output);
        assertEquals(10, commitFileContents.size());
        assertEquals("example-1 Added som files for this issue", commitFileContents.get(0));
        assertEquals("", commitFileContents.get(1));
        assertEquals("This commit consists of the following changed files", commitFileContents.get(2));
        assertEquals("", commitFileContents.get(3));
        assertEquals("- foo.java This have been merged from example-2", commitFileContents.get(4));
        assertEquals("- bar.java", commitFileContents.get(5));
        assertEquals("- baz.java This have been merged from example-3", commitFileContents.get(6));
        assertEquals("", commitFileContents.get(7));
        assertEquals("Summary: Add functionality for accounting", commitFileContents.get(8));
        assertEquals("Hook v 1.0", commitFileContents.get(9));
    }

    @Test
    public void manipulateCommitMessageEmptyCommitMultilineNormalCommitParentIssue() throws Exception {
        manipulator = new CommitMessageManipulator(resourceBundle);
        manipulator.loadCommitMessage("src/test/resources/multilinenormalcommit.txt");

        issue = new JiraIssueBuilder("EXAMPLE-1", "Add functionality for accounting")
                .setStatus("In Progress")
                .setAssignee(Optional.of(new User("alice", "Alice Developer")))
                .setIssueTypeName("Improvement")
                .setParentIssue(Optional.of(new BasicJiraIssue("EXAMPLE-4", "Accounting doesn't work properly")))
                .setSubtask(true)
                .build();

        manipulator.manipulateCommitMessage(issue, "Hook v 1.0", PROCESSED_COMMIT_PATH, false, false);

        File output = new File(PROCESSED_COMMIT_PATH);
        List<String> commitFileContents = Files.readLines(output, Charsets.UTF_8);
        assertNotNull(output);
        assertEquals(11, commitFileContents.size());
        assertEquals("example-1 Added som files for this issue", commitFileContents.get(0));
        assertEquals("", commitFileContents.get(1));
        assertEquals("This commit consists of the following changed files", commitFileContents.get(2));
        assertEquals("", commitFileContents.get(3));
        assertEquals("- foo.java This have been merged from example-2", commitFileContents.get(4));
        assertEquals("- bar.java", commitFileContents.get(5));
        assertEquals("- baz.java This have been merged from example-3", commitFileContents.get(6));
        assertEquals("", commitFileContents.get(7));
        assertEquals("Summary: Add functionality for accounting", commitFileContents.get(8));
        assertEquals("Sub-task of: EXAMPLE-4 Accounting doesn't work properly", commitFileContents.get(9));
        assertEquals("Hook v 1.0", commitFileContents.get(10));
    }

    @Test
    public void manipulateCommitMessageEmptyCommitMultilineNormalCommitParentIssueRelatedIssues() throws Exception {
        manipulator = new CommitMessageManipulator(resourceBundle);
        manipulator.loadCommitMessage("src/test/resources/multilinenormalcommit.txt");

        List<BasicJiraIssue> relatedIssues = new ArrayList<>();
        relatedIssues.add(new BasicJiraIssue("ERROR-123", "Found an error in listing of accounts"));
        relatedIssues.add(new BasicJiraIssue("EXAMPLE-5", "Listing of accounts are missing"));

        issue = new JiraIssueBuilder("EXAMPLE-1", "Add functionality for accounting")
                .setStatus("In Progress")
                .setAssignee(Optional.of(new User("alice", "Alice Developer")))
                .setIssueTypeName("Improvement")
                .setParentIssue(Optional.of(new BasicJiraIssue("EXAMPLE-4", "Accounting doesn't work properly")))
                .setRelatedIssues(relatedIssues)
                .setSubtask(true)
                .build();

        manipulator.manipulateCommitMessage(issue, "Hook v 1.0", PROCESSED_COMMIT_PATH, false, false);

        File output = new File(PROCESSED_COMMIT_PATH);
        List<String> commitFileContents = Files.readLines(output, Charsets.UTF_8);
        assertNotNull(output);
        assertEquals(13, commitFileContents.size());
        assertEquals("example-1 Added som files for this issue", commitFileContents.get(0));
        assertEquals("", commitFileContents.get(1));
        assertEquals("This commit consists of the following changed files", commitFileContents.get(2));
        assertEquals("", commitFileContents.get(3));
        assertEquals("- foo.java This have been merged from example-2", commitFileContents.get(4));
        assertEquals("- bar.java", commitFileContents.get(5));
        assertEquals("- baz.java This have been merged from example-3", commitFileContents.get(6));
        assertEquals("", commitFileContents.get(7));
        assertEquals("Summary: Add functionality for accounting", commitFileContents.get(8));
        assertEquals("Sub-task of: EXAMPLE-4 Accounting doesn't work properly", commitFileContents.get(9));
        assertEquals("Related to: ERROR-123 Found an error in listing of accounts", commitFileContents.get(10));
        assertEquals("Related to: EXAMPLE-5 Listing of accounts are missing", commitFileContents.get(11));
        assertEquals("Hook v 1.0", commitFileContents.get(12));
    }

    @Test
    public void checkStateAndManipulateCommitMessageNormalCommitCorrectUsername() {

        new Expectations(GitConfig.class) {{
            GitConfig.getJiraUsername(); result = "alice";
        }};

        manipulator = new CommitMessageManipulator(resourceBundle);
        manipulator.loadCommitMessage("src/test/resources/multilinenormalcommit.txt");

        issue = new JiraIssueBuilder("EXAMPLE-1", "Add functionality for accounting")
                .setStatus("In Progress")
                .setAssignee(Optional.of(new User("alice", "Alice Developer")))
                .setIssueTypeName("Improvement")
                .setSubtask(false)
                .build();

        boolean status = manipulator.checkStateAndManipulateCommitMessage(issue, false,
                PROCESSED_COMMIT_PATH,"Hook v 1.0");
        assertTrue("The issue is OK", status);

    }

    @Test
    public void checkStateAndManipulateCommitMessageNormalCommitCorrectUsernameWrongStatus() {

        new Expectations(GitConfig.class) {{
            GitConfig.getJiraUsername(); result = "alice";
        }};

        manipulator = new CommitMessageManipulator(resourceBundle);
        manipulator.loadCommitMessage("src/test/resources/multilinenormalcommit.txt");

        issue = new JiraIssueBuilder("EXAMPLE-1", "Add functionality for accounting")
                .setStatus("To-Do")
                .setAssignee(Optional.of(new User("alice", "Alice Developer")))
                .setIssueTypeName("Improvement")
                .setSubtask(false)
                .build();

        boolean status = manipulator.checkStateAndManipulateCommitMessage(issue, false,
                PROCESSED_COMMIT_PATH,"Hook v 1.0");
        assertFalse("The issue is OK", status);

    }

    @Test
    public void checkStateAndManipulateCommitMessageNormalCommitWrongUsername() {

        new Expectations(GitConfig.class) {{
            GitConfig.getJiraUsername(); result = "bob";
        }};

        manipulator = new CommitMessageManipulator(resourceBundle);
        manipulator.loadCommitMessage("src/test/resources/multilinenormalcommit.txt");

        issue = new JiraIssueBuilder("EXAMPLE-1", "Add functionality for accounting")
                .setStatus("In Progress")
                .setAssignee(Optional.of(new User("alice", "Alice Developer")))
                .setIssueTypeName("Improvement")
                .setSubtask(false)
                .build();

        boolean status = manipulator.checkStateAndManipulateCommitMessage(issue, false,
                PROCESSED_COMMIT_PATH,"Hook v 1.0");
        assertFalse("The issue is OK", status);

    }

    @Test

    public void checkStateAndManipulateCommitMessageNormalCommitWrongUsernameAndWrongStatus() {

        new Expectations(GitConfig.class) {{
            GitConfig.getJiraUsername(); result = "bob";
        }};

        manipulator = new CommitMessageManipulator(resourceBundle);
        manipulator.loadCommitMessage("src/test/resources/multilinenormalcommit.txt");

        issue = new JiraIssueBuilder("EXAMPLE-1", "Add functionality for accounting")
                .setStatus("Done")
                .setAssignee(Optional.of(new User("alice", "Alice Developer")))
                .setIssueTypeName("Improvement")
                .setSubtask(false)
                .build();

        boolean status = manipulator.checkStateAndManipulateCommitMessage(issue, false,
                PROCESSED_COMMIT_PATH,"Hook v 1.0");
        assertFalse("The issue is OK", status);
    }

    @Test
    public void checkStateAndManipulateCommitMessageAssigneeOverridden() {


        manipulator = new CommitMessageManipulator(resourceBundle);
        manipulator.loadCommitMessage("src/test/resources/onelineassigneeoverridden.txt");

        issue = new JiraIssueBuilder("EXAMPLE-1", "Add functionality for accounting")
                .setStatus("In Progress")
                .setAssignee(Optional.of(new User("alice", "Alice Developer")))
                .setIssueTypeName("Improvement")
                .setSubtask(false)
                .build();

        boolean status = manipulator.checkStateAndManipulateCommitMessage(issue, false,
                PROCESSED_COMMIT_PATH,"Hook v 1.0");
        assertTrue("The issue is OK", status);
    }

    @Test
    public void checkStateAndManipulateCommitMessageAssigneeNotSet() {

        manipulator = new CommitMessageManipulator(resourceBundle);
        manipulator.loadCommitMessage("src/test/resources/onelineassigneeoverridden.txt");

        issue = new JiraIssueBuilder("EXAMPLE-1", "Add functionality for accounting")
                .setStatus("In Progress")
                .setIssueTypeName("Improvement")
                .setSubtask(false)
                .build();

        boolean status = manipulator.checkStateAndManipulateCommitMessage(issue, false,
                PROCESSED_COMMIT_PATH,"Hook v 1.0");
        assertTrue("The issue is OK", status);
    }

    @Test

    public void checkStateAndManipulateCommitMessageOnelineNone() {

        manipulator = new CommitMessageManipulator(resourceBundle);
        manipulator.loadCommitMessage("src/test/resources/onelinenone.txt");

        issue = new JiraIssueBuilder("EXAMPLE-1", "Add functionality for accounting")
                .setStatus("In Progress")
                .setIssueTypeName("Improvement")
                .setSubtask(false)
                .build();

        boolean status = manipulator.checkStateAndManipulateCommitMessage(issue, false,
                PROCESSED_COMMIT_PATH,"Hook v 1.0");
        assertTrue("The issue is OK", status);
    }

    @Test
    public void checkStateAndManipulateCommitMessageConnectionOverridden() {

        manipulator = new CommitMessageManipulator(resourceBundle);
        manipulator.loadCommitMessage("src/test/resources/onelinejiraconnectionoverridden.txt");

        issue = new JiraIssueBuilder("EXAMPLE-1", "Add functionality for accounting")
                .setStatus("In Progress")
                .setIssueTypeName("Improvement")
                .setSubtask(false)
                .build();

        boolean status = manipulator.checkStateAndManipulateCommitMessage(issue, false,
                PROCESSED_COMMIT_PATH,"Hook v 1.0");
        assertTrue("The issue is OK", status);
    }

    @Test
    public void getJiraIssueKeyFromPattern() {
        manipulator = new CommitMessageManipulator(resourceBundle);
        manipulator.loadCommitMessage("src/test/resources/onlinenormalcommit.txt");

        String key = manipulator.getJiraIssueKeyFromPattern(JIRA_ISSUE_PATTERNS);
        assertNotNull(key);
        assertEquals("EXAMPLE-1" , key);
    }

    @Test
    public void getJiraIssueKeyFromPatternNotFoundInPattern() {
        manipulator = new CommitMessageManipulator(resourceBundle);
        manipulator.loadCommitMessage("src/test/resources/onlinenormalcommit.txt");

        String key = manipulator.getJiraIssueKeyFromPattern("BUG");
        assertNull(key);
    }
}