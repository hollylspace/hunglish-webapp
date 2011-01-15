#!/bin/bash

# Sets the server to a consistent clean, empty state.

set -e

mavendir=/big3/Work/HunglishMondattar/hunglish-webapp/

pushd $mavendir > "/dev/null"
mvn tomcat:stop
popd > "/dev/null"

echo Erasing all data from deployment site. Are you fucking sure?
sudo -u tomcat6 bash eraseall_from_tomcat.sh

pushd $mavendir > "/dev/null"
mvn tomcat:start
popd > "/dev/null"
