#!/usr/bin/env sh
set -eu

rm -rf build
mkdir -p build/classes

find src/main/java -name "*.java" > build/sources.txt

javac --release 17 -encoding UTF-8 -d build/classes @build/sources.txt

jar --create \
    --file build/NetAssist.jar \
    --main-class com.yousef.netassist.Main \
    -C build/classes .

echo "Built build/NetAssist.jar"
