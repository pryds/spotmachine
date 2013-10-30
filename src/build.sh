#!/bin/bash

OUTPUTDIR="../bin"

echo "Building Java class files to $OUTPUTDIR"
javac -deprecation -classpath .:./resources/gettext-commons-0.9.6.jar -d $OUTPUTDIR main/SpotMachine.java

echo "Building localized ResourceBundles for language en"
msgfmt --java2 -d $OUTPUTDIR -r i18n.Messages -l en po/spotmachine.pot

while read line; do
  f=${line:0:1}
  if [ "$f" != "#" ] && [ "$line" != "en" ] && [ "$line" != "" ]; then
    echo "Building localized ResourceBundles for language $line"
    msgfmt --java2 -d $OUTPUTDIR -r i18n.Messages -l $line po/$line.po
  fi
done < po/languages

echo "Getting git describe info"
git describe > $OUTPUTDIR/version

