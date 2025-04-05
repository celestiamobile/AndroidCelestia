#!/bin/sh

if [ "$(uname)" = "Darwin" ]; then
    HOMEBREW_PATH=$(brew --prefix)
    export PATH="$HOMEBREW_PATH/bin:$PATH"
fi

cd `dirname $0`

CELESTIA_CONTENT_REPO_ROOT=`pwd`/../../CelestiaContent

cd $CELESTIA_CONTENT_REPO_ROOT/textures

downsize_texture()
{
    find hires/ -type f \( -iname "*.jpg" -o -iname "*.png" -o -iname "*.dds" \) | while read -r FILE; do
        FILENAME=$(basename "$FILE")
        convert "$FILE" -resize "${2}x${2}" -define png:exclude-chunks=date,time "${1}/$FILENAME"
        if [ $? -ne 0 ]; then
            echo "Failed to convert: $FILE"
            exit 1
        else
            echo "Successfully converted: $FILE"
        fi
    done
}

echo "Converting to medres"
downsize_texture "medres" 2048
echo "Converting to lores"
downsize_texture "lores" 1024
