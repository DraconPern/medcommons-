#!/bin/sh
#
BASENAME=/usr/bin/basename
#
if [ "$CONFIGURATION" != "Distribution" ]; then
    echo "XcodeMakeAdHocIPA.sh: '$CONFIGURATION' is an invalid configuration" 1>&2
    exit 1
fi
#
if [ "$SDK_NAME" != "iphoneos4.0" ]; then
    echo "XcodeMakeAdHocIPA.sh: '$SDK_NAME' is an invalid SDK name" 1>&2
    exit 1
fi
#
IPA_NAME=`$BASENAME "$PRODUCT_NAME" .ipa`
#
APP_BUNDLE_PATH="$BUILT_PRODUCTS_DIR"/"$IPA_NAME".app
IPA_DIR_PATH="$TARGET_BUILD_DIR"
ITUNES_ARTWORK_PATH="$SRCROOT"/Resources/"$IPA_NAME"/"$IPA_NAME"-512x512.png
ITUNES_METADATA_PLIST_PATH="$SRCROOT"/Resources/"$IPA_NAME"/"$IPA_NAME"-iTunesMetadata.plist
#
export APP_BUNDLE_PATH            \
       IPA_DIR_PATH               \
       ITUNES_ARTWORK_PATH        \
       ITUNES_METADATA_PLIST_PATH
#
"$SRCROOT"/BuildScripts/MakeAdHocIPA.sh
