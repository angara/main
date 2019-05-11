#!/bin/bash

export APP='angara.net/main'
export GIT=`git rev-parse HEAD`
export BLD=`date -Isec`

export CLASSES="tmp/classes"

rm -r ${CLASSES}
mkdir -p ${CLASSES}
BUILD_EDN="${CLASSES}/build.edn"

echo "{:app \"${APP}\" :git \"${GIT}\" :bld \"${BLD}\"}">${BUILD_EDN}

clj -e "(set! *compile-path* \"${CLASSES}\") (compile 'web.main)" \
  && clj -A:uberjar \
  || exit 1

echo "start command:"
echo "  CONFIG_EDN=pat/to/runtime.edn java -cp notify.jar notify.main"

#.
