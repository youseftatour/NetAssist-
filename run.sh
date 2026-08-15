#!/usr/bin/env sh
set -eu

if [ ! -f build/NetAssist.jar ]; then
    ./build.sh
fi

java -jar build/NetAssist.jar
