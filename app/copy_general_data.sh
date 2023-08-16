#!/bin/sh

cd `dirname $0`

CELESTIA_ROOT=`pwd`/src/sideload/assets/CelestiaResources
CELESTIA_REPO_ROOT=`pwd`/../../Celestia

mkdir -p $CELESTIA_ROOT

CELESTIA_CONTENT_REPO_ROOT=`pwd`/../../CelestiaContent

for directory in 'images' 'locale' 'scripts' 'shaders';do
    f=$CELESTIA_REPO_ROOT/$directory
    if [ ! -f $CELESTIA_ROOT/$directory ] || [ $f -nt $CELESTIA_ROOT/$directory ];then
        echo "rsync -rv --quiet --exclude='CMakeLists.txt' $f $CELESTIA_ROOT"
        rsync -rv --quiet --exclude='CMakeLists.txt' $f $CELESTIA_ROOT
    fi
done

for directory in 'data' 'extras' 'extras-standard' 'models' 'textures' 'warp';do
    f=$CELESTIA_CONTENT_REPO_ROOT/$directory
    if [ ! -f $CELESTIA_ROOT/$directory ] || [ $f -nt $CELESTIA_ROOT/$directory ];then
        echo "rsync -rv --quiet --exclude='CMakeLists.txt' --exclude='well-known-dsonames.txt' --exclude='well-known-starnames.txt' $f $CELESTIA_ROOT"
        rsync -rv --quiet --exclude='CMakeLists.txt' --exclude='well-known-dsonames.txt' --exclude='well-known-starnames.txt' $f $CELESTIA_ROOT
    fi
done

for file in "celestia.cfg" "controls.txt" "demo.cel" "guide.cel" "start.cel" "COPYING" "AUTHORS" "TRANSLATORS";do
    f=$CELESTIA_REPO_ROOT/$file
    if [ ! -f $CELESTIA_ROOT/$file ] || [ $f -nt $CELESTIA_ROOT/$file ];then
        echo "cp $f $CELESTIA_ROOT/$file"
        cp $f $CELESTIA_ROOT/$file
    fi
done
