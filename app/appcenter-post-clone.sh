#!/usr/bin/env bash

cd $APPCENTER_SOURCE_DIRECTORY
ln -s app/src/main/cpp/dependency/android app/src/main/cpp/libs

cd ..

# Clone the Celestia repo (modified)
git clone https://github.com/eyvallah/Celestia

# Install gettext, needed for translation
brew install gettext
