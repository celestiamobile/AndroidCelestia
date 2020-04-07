#!/usr/bin/env bash

cd $APPCENTER_SOURCE_DIRECTORY
ln -s app/src/main/cpp/dependency/android app/src/main/cpp/libs

cd ..

# Clone the Celestia repo (modified)
git clone https://github.com/eyvallah/Celestia

# Install gettext, needed for translation
brew install gettext

# Install appcenter cli to upload symbols
brew install node
brew unlink node@6
brew link --overwrite node
npm install -g appcenter-cli
