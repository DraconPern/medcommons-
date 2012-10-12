#!/bin/sh
#
# Required environment variables:
#
#   APP_BUNDLE_PATH            - path to input .app bundle
#   IPA_DIR_PATH               - path to directory for output IPA file
#
# Optional environment variables:
#
#   IPA_NAME                   - name of output IPA file (no extension)
#                                (default: extracted from APP_BUNDLE_PATH)
#   ITUNES_ARTWORK_PATH        - path to input iTunesArtwork file
#                                (default: none)
#   ITUNES_METADATA_PLIST_PATH - path to input iTunesMetadata.plist file
#                                (default: none)
#
BASENAME=/usr/bin/basename
COPY="/bin/cp -p"
DITTO=/usr/bin/ditto
EXPR=/bin/expr
MKDIR="/bin/mkdir -p"
REMOVE="/bin/rm -f"
ZIP=/usr/bin/zip
#
if [ $# -ne 0 ]; then
    echo "Usage: MakeAdHocIPA.sh" 1>&2
    exit 1
fi
#
if [ -z "$APP_BUNDLE_PATH" -a -n "${APP_BUNDLE_PATH-XXX}" ]; then
    echo "MakeAdHocIPA.sh: environment variable APP_BUNDLE_PATH is not set" 1>&2
    exit 1
elif [ ! -e "$APP_BUNDLE_PATH" ]; then
    echo "MakeAdHocIPA.sh: '$APP_BUNDLE_PATH' does not exist" 1>&2
    exit 1
elif [ ! -d "$APP_BUNDLE_PATH" ]; then
    echo "MakeAdHocIPA.sh: '$APP_BUNDLE_PATH' is not an application bundle" 1>&2
    exit 1
elif [ ! -r "$APP_BUNDLE_PATH" ]; then
    echo "MakeAdHocIPA.sh: '$APP_BUNDLE_PATH' is not readable" 1>&2
    exit 1
else
    APP_NAME=`$BASENAME "$APP_BUNDLE_PATH"`
    DEFAULT_IPA_NAME=`$EXPR "$APP_NAME" : '^\([^.]\{1,\}\)\.app$'`

    if [ -z "$DEFAULT_IPA_NAME" ]; then
        echo "MakeAdHocIPA.sh: '$APP_BUNDLE_PATH' is not an application bundle" 1>&2
        exit 1
    fi
fi
#
if [ -z "$IPA_DIR_PATH" -a -n "${IPA_DIR_PATH-XXX}" ]; then
    echo "MakeAdHocIPA.sh: environment variable IPA_DIR_PATH is not set" 1>&2
    exit 1
elif [ ! -e "$IPA_DIR_PATH" ]; then
    echo "MakeAdHocIPA.sh: '$IPA_DIR_PATH' does not exist" 1>&2
    exit 1
elif [ ! -d "$IPA_DIR_PATH" ]; then
    echo "MakeAdHocIPA.sh: '$IPA_DIR_PATH' is not a directory" 1>&2
    exit 1
elif [ ! -w "$IPA_DIR_PATH" ]; then
    echo "MakeAdHocIPA.sh: '$IPA_DIR_PATH' is not writable" 1>&2
    exit 1
fi
#
if [ -z "$IPA_NAME" -a -n "${IPA_NAME-XXX}" ]; then
    IPA_NAME="$DEFAULT_IPA_NAME"
else
    TMP_IPA_NAME=`$EXPR "$IPA_NAME" : '^\([^.]\{1,\}\)$'`

    if [ -z "$TMP_IPA_NAME" ]; then
        echo "MakeAdHocIPA.sh: '$IPA_NAME' is an invalid IPA name" 1>&2
        exit 1
    fi
fi
#
if [ -z "$ITUNES_ARTWORK_PATH" -a -n "${ITUNES_ARTWORK_PATH-XXX}" ]; then
    ITUNES_ARTWORK_PATH=""
elif [ ! -e "$ITUNES_ARTWORK_PATH" ]; then
    echo "MakeAdHocIPA.sh: '$ITUNES_ARTWORK_PATH' does not exist" 1>&2
    exit 1
elif [ ! -f "$ITUNES_ARTWORK_PATH" ]; then
    echo "MakeAdHocIPA.sh: '$ITUNES_ARTWORK_PATH' is not a regular file" 1>&2
    exit 1
elif [ ! -r "$ITUNES_ARTWORK_PATH" ]; then
    echo "MakeAdHocIPA.sh: '$ITUNES_ARTWORK_PATH' is not readable" 1>&2
    exit 1
fi
#
if [ -z "$ITUNES_METADATA_PLIST_PATH" -a -n "${ITUNES_METADATA_PLIST_PATH-XXX}" ]; then
    ITUNES_METADATA_PLIST_PATH=""
elif [ ! -e "$ITUNES_METADATA_PLIST_PATH" ]; then
    echo "MakeAdHocIPA.sh: '$ITUNES_METADATA_PLIST_PATH' does not exist" 1>&2
    exit 1
elif [ ! -f "$ITUNES_METADATA_PLIST_PATH" ]; then
    echo "MakeAdHocIPA.sh: '$ITUNES_METADATA_PLIST_PATH' is not a regular file" 1>&2
    exit 1
elif [ ! -r "$ITUNES_METADATA_PLIST_PATH" ]; then
    echo "MakeAdHocIPA.sh: '$ITUNES_METADATA_PLIST_PATH' is not readable" 1>&2
    exit 1
fi
#
TMP_DIR="$TMPDIR/$IPA_NAME-$$"
#
$MKDIR "$TMP_DIR"
#
cd $TMP_DIR
#
$DITTO "$APP_BUNDLE_PATH" Payload/"$APP_NAME"
#
if [ -n "$ITUNES_ARTWORK_PATH" ]; then
    $COPY "$ITUNES_ARTWORK_PATH" iTunesArtwork
fi
#
if [ -n "$ITUNES_METADATA_PLIST_PATH" ]; then
    $COPY "$ITUNES_METADATA_PLIST_PATH" iTunesMetadata.plist
fi
#
$ZIP -ry "$IPA_DIR_PATH/$IPA_NAME".ipa *
#
$REMOVE -r "$TMP_DIR"
#
exit 0
