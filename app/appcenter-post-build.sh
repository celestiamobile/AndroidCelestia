SYMBOL_DIRECTORY=${APPCENTER_SOURCE_DIRECTORY}/app/build/intermediates/cmake/release/obj
cd $SYMBOL_DIRECTORY

SYMBOL_ZIP_PATH="symbols.zip"
zip -vr $SYMBOL_ZIP_PATH * -x "*.DS_Store"

appcenter crashes upload-symbols --breakpad $SYMBOL_ZIP_PATH --app $APP_IDENTIFIER --token $APP_CENTER_TOKEN --disable-telemetry --debug
