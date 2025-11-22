#!/bin/bash

VERSIONMAJOR=`grep '<version>' pom.xml | head -1 | sed -e 's/^.*<version>//g' -e 's/\..*$//g'`
VERSIONMINOR=`grep '<version>' pom.xml | head -1 | sed -E 's/^.*<version>[0-9]+?\.//g' | sed -E -e 's/(\..*|-SNAPSHOT|)<\/version>.*$//g'`
sed -z -i -e "s/!define\ VERSIONMAJOR\ [0-9]/!define\ VERSIONMAJOR $VERSIONMAJOR\ /" NSIS/installer.nsi
sed -z -i -e "s/!define\ VERSIONMINOR\ [0-9]/!define\ VERSIONMINOR $VERSIONMINOR\ /" NSIS/installer.nsi
sed -z -i -e "s/OutFile\ \"Installer.exe\"/OutFile\ \"Installer-$VERSIONMAJOR.$VERSIONMINOR.exe\"\ /" NSIS/installer.nsi
sed -z -i -e "s/BrandingText\ \"Tihwin\"/BrandingText\ \"Tihwin v$VERSIONMAJOR.$VERSIONMINOR\"\ /" NSIS/installer.nsi
