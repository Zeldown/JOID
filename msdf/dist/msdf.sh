#!/bin/sh
exec java -jar "$(dirname "$0")/joid-msdf-@JOID_VERSION@.jar" "$@"
