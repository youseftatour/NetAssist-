#!/usr/bin/env sh
set -eu

if [ ! -f target/NetAssist.jar ]; then
    ./build.sh
fi

java -jar target/NetAssist.jar
