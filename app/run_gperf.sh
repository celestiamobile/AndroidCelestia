#!/bin/sh

if [ "$(uname)" = "Darwin" ]; then
    HOMEBREW_PATH=$(brew --prefix)
    export PATH="$HOMEBREW_PATH/bin:$PATH"
fi

CELESTIA_REPO_PATH=$1

cd "$CELESTIA_REPO_PATH/src/celscript/legacy"
gperf commands.gperf -m4 --output-file=commands.inc

cd "$CELESTIA_REPO_PATH/src/celephem"
gperf customrotation.gperf -m4 --output-file=customrotation.inc
gperf customorbit.gperf -m4 --output-file=customorbit.inc

cd "$CELESTIA_REPO_PATH/src/celengine"
gperf location.gperf -m4 --output-file=location.inc
