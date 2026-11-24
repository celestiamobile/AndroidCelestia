#!/bin/sh

set -e

cd "$(dirname "$0")"

CELESTIA_ANDROID_ROOT=${1:?Missing Android resources destination}
CELESTIA_LOCALIZATION_REPO_ROOT="$(pwd)/../../CelestiaLocalization"
CELESTIA_LOCALIZATION_ANDROID_ROOT="$CELESTIA_LOCALIZATION_REPO_ROOT/android/${2:?Missing Android module name}"

rsync -rv --quiet "$CELESTIA_LOCALIZATION_ANDROID_ROOT"/values* "$CELESTIA_ANDROID_ROOT"
