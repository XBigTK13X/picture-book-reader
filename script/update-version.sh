#! /bin/bash

if [ -z $1 ]; then
  echo "First argument of version string is required, current version is below"
  cat /media/kretst/LINDATA/dev/github/picture-book-reader/android-client/src/com/simplepathstudios/pbr/PBRSettings.java | grep ClientVersion
  exit 1
fi

BUILD_VERSION="$1"

BUILD_DATE=$(date +'%B %d, %Y')

echo "Version $BUILD_VERSION - Built $BUILD_DATE"

sed -i -E "s/versionName \\\"{1}(.*?)\\\"{1}/versionName \\\"${BUILD_VERSION}\\\"/" ./android-client/build.gradle
sed -i -E "s/ClientVersion = \\\"{1}(.*?)\\\"{1}/ClientVersion = \\\"${BUILD_VERSION}\\\"/" ./android-client/src/com/simplepathstudios/pbr/PBRSettings.java
sed -i -E "s/BuildDate = \\\"{1}(.*?)\\\"{1}/BuildDate = \\\"${BUILD_DATE}\\\"/" ./android-client/src/com/simplepathstudios/pbr/PBRSettings.java
