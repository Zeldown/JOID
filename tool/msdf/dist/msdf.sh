#!/bin/sh
exec java -jar "$(dirname "$0")/joid-tool-msdf-@JOID_VERSION@.jar" "$@"
