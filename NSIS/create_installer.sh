#!/bin/bash
set -e

VERSIONMAJOR=$(grep '<version>' ../pom.xml | head -1 | sed -e 's/^.*<version>//g' -e 's/\..*$//g')
VERSIONMINOR=$(grep '<version>' ../pom.xml | head -1 | sed -E 's/^.*<version>[0-9]+\.//g' | sed -E -e 's/(\..*|-SNAPSHOT|)<\/version>.*$//g')

echo "Building installer for version $VERSIONMAJOR.$VERSIONMINOR"

makensis -V4 -DVERSIONMAJOR="$VERSIONMAJOR" -DVERSIONMINOR="$VERSIONMINOR" ./installer.nsi
