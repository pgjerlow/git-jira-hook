# Background
For a project in a management regime it's particularly important to have control over what changes are being checked-in and that these are linked to a JIRA-issues which will be a part of a delivery.<br>
At the moment there's no communication between git and JIRA, so there is a risk of code changes erroneously being delivered into production.<br><br>
This git-hook is implemented based on the experiences of similar component designed for several earlier engagements by me and other colleagues.

#Purpose
This component will overcome this challenge and create greater confidence that erroneous code changes are not supplied.

##Conceptually
Conceptually this component works as follows:

* Is there a JIRA issue number present in the git commit message and does it belongs to the right project?
* Is there an actual JIRA issue which complies with the given JIRA issue number?
* Is the developer set as 'Assignee' on the JIRA issue?
* Has the JIRA issue a status that accepts code to be checked in?


If these rules are satisfied, the code is committed to git repository.<br>
In addition, this component manipulates the commit message to increase traceability based on convention below.<br>

```
<ORIGINAL_JIRAISSUE_KEY> <COMMIT MESSAGE>
<empty line>
summary: <JIRAISSUE_SUMMARY>
sub-task of: <PARENT_JIRAISSUE_KEY> <PARENT__JIRAISSUE_SUMMARY> (optional)
related to: <RELATED_JIRAISSUE_KEY> <RELATED_JIRAISSUE_SUMMARY> (optional)
<empty line> (optional)
<additional information> (optional)
<empty line>
<hook version information>
```

Example<br>

Original commit message:<br>
```
EXAMPLE-123 Fixed problems with sending emails to customer
```

After execution:<br>
```
EXAMPLE-123 Fixed problems with sending emails to customer

summary: Sending email does not work
related to: ERROR-3923 Cannot send email to customer

Committed using hook v. 1.0.2
```

##Logical
As stated in the Conceptual description, the git-jira hooks checks if the state of the given issue key is fulfilled.
In case those rules are too restrictive, there are options for loosen some of the rules.

```
Option: -A
Description: Override check of assignee. Must be capital letter and the last part of message
Example: EXAMPLE-123 Fixed something -A

Option: -O
Description: Override communication with JIRA
Example: EXAMPLE-123 Fixed something -O

Option: NONE
Description: Used for checking-in code into git without having any JIRA issue key. Note: Use with caution due to loss of traceability
Example: NONE pruned git tree
```


##Physical
This component is based on REST calls to JIRA-instance.
It is based on the jira-rest-java-client (JRJC) library version 3.0 which is an open source library under Apache Licence 2.0 (http://www.apache.org/licences/LICENCE-2.0) licensing.
Project homepage is https://ecosystem.atlassian.net/wiki/display/JRJC and the source code can be downloaded via https://bitbucket.org/atlassian/jira-rest-java-client.<br><br>
In addition, information from git configuration is read in order to log on to JIRA and which JIRA projects the hook will be tied up with. The relevant issue types and statuses that accept check-in of code are contained in a property file which is packaged into the jar file.


#How to install
add information here
#How to use
add information here
