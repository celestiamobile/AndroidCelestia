#!/bin/sh

export PATH="/opt/homebrew/bin:/usr/local/opt/gettext/bin:$PATH"

cd `dirname $0`

CELESTIA_ROOT=`pwd`/src/main/assets/CelestiaResources
CELESTIA_REPO_ROOT=`pwd`/../../Celestia
CELESTIA_CONTENT_REPO_ROOT=`pwd`/../../CelestiaContent
CELESTIA_LOCALIZATION_REPO_ROOT=`pwd`/../../CelestiaLocalization

LOCALE_ROOT=$CELESTIA_ROOT/locale
PROJECT_TEMP_DIR=`pwd`/temp

mkdir -p $PROJECT_TEMP_DIR
mkdir -p $LOCALE_ROOT

convert_po()
{
    POT=$1/$2.pot
    for po in $1/*.po; do
        f=${po##*/};f=${f%.*}
        LANG_ROOT=$LOCALE_ROOT/$f/LC_MESSAGES
        mkdir -p $LANG_ROOT
        if [ $po -nt $LANG_ROOT/$2.mo ];then
            echo "Create $LANG_ROOT/$2.mo"
            msgmerge --quiet --output-file=$PROJECT_TEMP_DIR/$f.po --lang=$f --sort-output $po $POT
            msgfmt -o $LANG_ROOT/$2.mo $PROJECT_TEMP_DIR/$f.po
        fi
    done
}

convert_po "$CELESTIA_REPO_ROOT/po" "celestia"
convert_po "$CELESTIA_CONTENT_REPO_ROOT/po" "celestia-data"
convert_po "$CELESTIA_LOCALIZATION_REPO_ROOT/common" "celestia_ui"

rm -rf $PROJECT_TEMP_DIR

CELESTIA_APP_RES_ROOT=`pwd`/src/main/res

create_values_folder()
{
    for po in $1/*.po; do
        f=${po##*/};f=${f%.*}
        ANDROID_LOCALE=$(echo "$f" | sed 's/_/-r/')
        VALUES_FOLDER=$CELESTIA_APP_RES_ROOT/values-$ANDROID_LOCALE
        mkdir -p $VALUES_FOLDER
        STRINGS_XML=$VALUES_FOLDER/strings.xml
        if [ ! -f $STRINGS_XML ];then
            echo "Create $STRINGS_XML"
            touch $STRINGS_XML
            cat <<EOF >$STRINGS_XML
<resources>
    <string name="app_name">Celestia</string>
    <string name="celestia_language">$f</string>
</resources>
EOF
        fi
    done
}

create_values_folder "$CELESTIA_LOCALIZATION_REPO_ROOT/common"
