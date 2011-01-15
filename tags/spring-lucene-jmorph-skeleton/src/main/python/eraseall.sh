#!/bin/bash

# Sets the server to a consistent clean, empty state.

pushd /big3/Work/HunglishMondattar/hunglish-webapp/ > "/dev/null"
mvn tomcat:stop
popd > "/dev/null"

echo Erasing all data from deployment site. Are you fucking sure?
sudo -u tomcat6 bash eraseall_from_tomcat.sh

pushd /big3/Work/HunglishMondattar/hunglish-webapp/ > "/dev/null"
mvn tomcat:start
popd > "/dev/null"
