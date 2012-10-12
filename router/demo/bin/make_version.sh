#!/bin/bash
#
# This script creates a new version.  It also updates the release notes to 
# include a list of all the checked in changes that have occurred since
# the previous release.

source ./bin/utils.sh

function usage() {
  echo
  echo "Usage:  $0 <new version number>";
  echo
  exit 1;
}

NEWVERSION=$1
[ -z $NEWVERSION ] && {
 usage; 
}

export RELEASE_NOTES="src/net/medcommons/ReleaseNotes.txt"
export VERSION_FILE="src/net/medcommons/public_version.txt"

# Check if the version file is already modified
svn status $VERSION_FILE | grep -q -e "^M" -e "^C" && {
  err "The version file ($VERSION_FILE) is already modified.\n\nPlease revert or commit this change manually before versioning."  
}

# Check if the release notes are already modified
svn status $RELEASE_NOTES | grep -q -e "^M" -e "^C" && {
  err "The release notes file ($RELEASE_NOTES) is already modified.\n\nPlease revert or commit this change manually before versioning."  
}

# Check that the version given is actually new
OLDVERSION=`cat src/net/medcommons/public_version.txt`;
if [ $OLDVERSION == $NEWVERSION ];
then
  err "Error: requested version $NEWVERSION is equal to current version."
fi

LASTREV=`svn info src/net/medcommons/public_version.txt | grep "Last Changed Rev" | grep -E -o "[0-9]+"`

let LASTREV='LASTREV+1'

# Write the header for the new section of the release notes
printf "  ========= Release $NEWVERSION ===========\n" > /tmp/NewReleaseNotes.txt

# Add the comments from subversion
svn log . -r $LASTREV:HEAD | 
  grep -E -v -e "--" -e 'r[0-9]{3,} \|'  \
  | sed -r '/[A-z0-9]{1,}/ s/^/  * /'   \
  | fmt -t -s -w 60 \
  | unix2dos        \
  >> /tmp/NewReleaseNotes.txt ||
  err "Unable to obtain subversion change log.  Please check."
  
# Add a blank line  
echo >> /tmp/NewReleaseNotes.txt

msg "Adding release notes as follows: "
cat /tmp/NewReleaseNotes.txt
echo
COMMIT=false
PS3="Select an Option ==>"
select option in "Continue and Commit" "Continue without Committing" "Edit" "Quit";
do
  case "$REPLY" in 
    1) COMMIT=true; break;;
    2) COMMIT=false; break;;
    3) vi /tmp/NewReleaseNotes.txt;;
    4) err "Aborting:  no files have been changed.";;
  esac
  msg "Release notes as follows: "
  cat /tmp/NewReleaseNotes.txt
done

msg "Updating version files in your repository ..."

# Join the new release notes to the old release notes
cat /tmp/NewReleaseNotes.txt src/net/medcommons/ReleaseNotes.txt > /tmp/ReleaseNotes.txt 

# Copy the whole new release notes file to the official file
cp /tmp/ReleaseNotes.txt src/net/medcommons/ReleaseNotes.txt ||
  err "Unable to update release notes file:  please check."
  
# Copy the requested version to the version file
echo $NEWVERSION > src/net/medcommons/public_version.txt ||
  err "Unable to update public_version file - please check."

if $COMMIT;
then
  msg "Committing version to repository..."
  svn commit --message "Version $NEWVERSION created." \
    src/net/medcommons/public_version.txt src/net/medcommons/ReleaseNotes.txt
fi

