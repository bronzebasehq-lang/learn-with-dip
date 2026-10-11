#!/bin/bash
# Manual APK build (no Gradle — Gradle daemon is broken in this VM).
# Usage: ./build.sh   -> outputs build/learn-with-dip.apk
set -e
cd "$(dirname "$0")"
ANDROID_HOME="${ANDROID_HOME:-$HOME/android-sdk}"
BT="$ANDROID_HOME/build-tools/34.0.0"
PLATFORM="$ANDROID_HOME/platforms/android-34/android.jar"

mkdir -p build/gen build/classes build/dex

echo "== aapt2 compile =="
$BT/aapt2 compile --dir app/src/main/res -o build/compiled_res.zip

echo "== aapt2 link =="
$BT/aapt2 link -o build/base.apk \
  -I "$PLATFORM" \
  --manifest app/src/main/AndroidManifest.xml \
  --java build/gen \
  --min-sdk-version 26 --target-sdk-version 34 \
  build/compiled_res.zip

echo "== javac =="
find build/gen app/src/main/java -name "*.java" > build/sources.txt
javac -encoding UTF-8 -source 17 -target 17 -nowarn \
  -cp "$PLATFORM" -d build/classes @build/sources.txt

echo "== d8 =="
$BT/d8 --lib "$PLATFORM" --min-api 26 --output build/dex \
  $(find build/classes -name "*.class")

echo "== package + align + sign =="
cp build/base.apk build/unaligned.apk
zip -uj -q build/unaligned.apk build/dex/classes.dex
$BT/zipalign -f 4 build/unaligned.apk build/aligned.apk
if [ ! -f build/debug.keystore ]; then
  keytool -genkeypair -keystore build/debug.keystore -alias androiddebugkey \
    -keyalg RSA -keysize 2048 -validity 10950 \
    -storepass android -keypass android \
    -dname "CN=Android Debug,O=Android,C=US"
fi
$BT/apksigner sign --ks build/debug.keystore \
  --ks-pass pass:android --key-pass pass:android \
  --out build/learn-with-dip.apk build/aligned.apk

echo "== verify =="
$BT/apksigner verify build/learn-with-dip.apk && echo "SIGNED OK"
ls -la build/learn-with-dip.apk
