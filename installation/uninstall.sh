#################################################
# Copyright (C) 2017 Per Ivar Gjerløw
# All rights reserved.
#
# This software may be modified and distributed under the terms
# of the MIT license.  See the LICENSE file for details.
#
# Description:
#   This script removes the git config for the githook
#   and removes the pre-commit, commit-msg and the githook file.
##################################################
#!/bin/sh

# removes any git config settings for git hook in the global config
if [ ! -z "$(git config --global githook.jira)" ]; then
  git config --global --remove-section githook.jira
fi

# removes any git config settings for git hook in the local config
if [ ! -z "$(git config githook.jira)" ]; then
  git config --remove-section githook.jira
fi
git config --unset githook.language

# Remove files used by the git-jira hook
if [ -e .git/hooks/pre-commit ]; then
  rm -f .git/hooks/pre-commit
fi

if [ -e .git/hooks/commit-msg ]; then
  rm -f .git/hooks/commit-msg
fi

if [ -d .git/hooks/util ]; then
    rm -rf .git/hooks/util
fi