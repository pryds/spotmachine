#!/bin/bash

if [ "$1" == "en" ] || [ -z "$1" ]; then
  echo "Building manual for language en"
  pdflatex manual.tex && pdflatex manual.tex
elif [ "$1" == "clean" ]; then
  echo "Cleaning up after building manual"
  rm *.aux
  rm *.log
  rm *.toc
  rm manual-*.tex
else
  echo "Building manual for language $1"
  po4a-translate -f latex -m manual.tex -p ../po/manual-$1.po -l manual-$1.tex -k 10
  pdflatex manual-$1.tex && pdflatex manual-$1.tex
fi

