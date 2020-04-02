#!/bin/sh

export PATH="/usr/local/opt/gettext/bin:$PATH"

cd `dirname $0`;

DIDBUILD=0

CELESTIA_ROOT=`pwd`/src/main/assets/CelestiaResources
CELESTIA_REPO_ROOT=`pwd`/../../Celestia

LOCALE_ROOT=$CELESTIA_ROOT/locale
PROJECT_TEMP_DIR=`pwd`/temp

mkdir -p $PROJECT_TEMP_DIR
mkdir -p $LOCALE_ROOT

POT=$CELESTIA_REPO_ROOT/po/celestia.pot

for po in $CELESTIA_REPO_ROOT/po/*.po; do
    f=${po##*/};f=${f%.*}
    LANG_ROOT=$LOCALE_ROOT/$f/LC_MESSAGES
    mkdir -p $LANG_ROOT
    if [ $po -nt $LANG_ROOT/celestia.mo ];then
        echo "Create $LANG_ROOT/celestia.mo"
        msgmerge --quiet --output-file=$PROJECT_TEMP_DIR/$f.po --lang=$f --sort-output $po $POT
        msgfmt -o $LANG_ROOT/celestia.mo $PROJECT_TEMP_DIR/$f.po
        DIDBUILD=1
    fi
done

POT=$CELESTIA_REPO_ROOT/po2/celestia_constellations.pot

for po in $CELESTIA_REPO_ROOT/po2/*.po; do
    f=${po##*/};f=${f%.*}
    LANG_ROOT=$LOCALE_ROOT/$f/LC_MESSAGES
    mkdir -p $LANG_ROOT
    if [ $po -nt $LANG_ROOT/celestia_constellations.mo ];then
        echo "Create $LANG_ROOT/celestia_constellations.mo"
        msgmerge --quiet --output-file=$PROJECT_TEMP_DIR/$f.po --lang=$f --sort-output $po $POT
        msgfmt -o $LANG_ROOT/celestia_constellations.mo $PROJECT_TEMP_DIR/$f.po
        DIDBUILD=1
    fi
done

rm -rf $PROJECT_TEMP_DIR
