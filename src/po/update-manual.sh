#!/bin/bash

echo "Updating pot file"
po4a-updatepo -f latex -m ../doc/manual.tex -p spotmachine-manual.pot

while read line; do
  f=${line:0:1}
  if [ "$f" != "#" ] && [ "$line" != "en" ] && [ "$line" != "" ]; then
    echo "Updating language $line"
    po4a-updatepo -f latex -m ../doc/manual.tex -p manual-$line.po
  fi
done < languages-manual

