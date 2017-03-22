#################################################
# Copyright (C) 2017 Per Ivar Gjerløw
# All rights reserved.
#
# This software may be modified and distributed under the terms
# of the MIT license.  See the LICENSE file for details.
#
# Description:
#   This installation file updates the git config
#   and copies the pre-commit file to its correct location
##################################################
#!/bin/sh

# the root of the common path is the root of the project 
GITHOOK_COMMON_PATH=example_path

# Make sure all scripts have unix end line
find . -name "*.sh" -exec dos2unix {} \;
find . -name "*pre-commit" -exec dos2unix {} \;

############################## GIT CONFIGURATION SETTINGS ##############################
# ------------- GLOBAL GIT CONFIG SETTINGS -------------------------------
# JIRA username
if [ ! -n "$(git config --global githook.jira.username)" ];
then
   read -p "Enter username used by JIRA: " jira_username

   if [ ! -z ${jira_username} ];
   then
     git config --global githook.jira.username ${jira_username}
   fi
fi

# JIRA password (base64 encoded)
if [ ! -n "$(git config --global githook.jira.password)" ];
then
  read -sp "Enter JIRA password: " jira_password; echo
  read -sp "Re-enter JIRA password: " retyped_jira_password; echo

  if [ "${jira_password}" != "${retyped_jira_password}" ];
  then
    echo "The entered passwords are not identical. Exiting..."
	exit 1
  else
    # Encodes the password
	encoded_password=$( openssl enc -base64 <<< ${jira_password} )
	git config --global githook.jira.password ${encoded_password}
  fi
fi

# JIRA address
if [ ! -n "$(git config --global githook.jira.address)" ];
then
   read -p "Enter JIRA address (e.g https://jira.domain.com): " jira_address

   if [ ! -z ${jira_address} ];
   then
     git config --global githook.jira.address ${jira_address}
   fi
fi

# ------------- LOCAL GIT CONFIG SETTINGS -------------------------------
# Language settings
if [ ! -n "$(git config --local githook.language)" ];
then
   read -p "Enter language (e.g EN or NO): " language

   if [ ! -z ${language} ];
   then
     git config --local githook.language ${language}
   fi
fi

# JIRA project keys
  if [ ! -n "$(git config --local githook.jira.projectkey)" ];
  then
     add_projectkey_response=Y

     while [ $add_projectkey_response == Y -o $add_projectkey_response == y ]; do
       echo ""
       read -p "Enter JIRA project key (e.g EXAMPLE): " jira_projectkey

       if [ -n "${jira_projectkey}" ];
       then
         git config --add --local githook.jira.projectkey "${jira_projectkey}"
       fi
       read -p "Add more JIRA projects [y/n]? " -n 1 -r add_projectkey_response
     done
  fi
############################## GIT CONFIGURATION SETTINGS ##############################

# Copy the necessary pre-commit file to its correct location
if [ ! -e .git/hooks/pre-commit ]; then
    cp -p ${GITHOOK_COMMON_PATH}/hooks/pre-commit .git/hooks/
fi