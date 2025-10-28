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

        # Skip normal maps (case-insensitive)
        echo "$FILENAME" | grep -iq "normal"
        if [ $? -eq 0 ]; then
            echo "Skipping resize for normal map: $FILE"
            continue
        fi

        ALPHA=$(identify -format "%A" "$FILE" 2>/dev/null)
        ALPHA_LOWER=$(echo "$ALPHA" | tr '[:upper:]' '[:lower:]')

        if [ "$ALPHA_LOWER" = "false" ] || [ "$ALPHA_LOWER" = "undefined" ]; then
            echo "Converting texture with no alpha channel"
            convert "$FILE" -resize "${2}x${2}" -define png:exclude-chunks=date,time "${1}/$FILENAME"
        else
            echo "Converting texture with alpha channel"
            convert "$FILE" \( +clone -alpha extract \) -alpha off -resize "${2}x${2}" -compose CopyOpacity -composite -define png:exclude-chunks=date,time "${1}/$FILENAME"
        fi
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
