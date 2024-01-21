#! /usr/bin/env bash
#
# ------------------------------------------------------------------
#  Babelfish Compass
#  Compatibility assessment tool for Babelfish for T-SQL
# ------------------------------------------------------------------
# Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
# SPDX-License-Identifier: Apache-2.0
# ------------------------------------------------------------------
#

DIRECTORY="$(dirname "$0")"
cd "${DIRECTORY}"
COMPASS="$(pwd)"
JAVA="$(which java)"

# Check for Java 8 or later
JAVA_OUTPUT=$(${JAVA} -d64 -fullversion 2>&1)
if [ "$?" -ne 0 ]; then
    echo "64-bit Java/JRE not found. Please install 64-bit JRE 8 or later"
    exit 1
fi
JAVA_VERSION=$(echo "${JAVA_OUTPUT}" | cut -d ' ' -f 4 | tr -d '"')
JAVA_VERSION_MAJOR=$(echo "${JAVA_VERSION}" | cut -d '.' -f 1)
if [ "${JAVA_VERSION_MAJOR}" -eq 1 ]; then
    JAVA_VERSION_MAJOR=$(echo "${JAVA_VERSION}" | cut -d '.' -f 2)
fi
if [ "${JAVA_VERSION_MAJOR}" -lt 8 ]; then
    echo "Babelfish Compass requires 64-bit Java/JRE 8 or later. Java version found: ${JAVA_VERSION_MAJOR}"
    echo "Run \"java -version\" and verify the version ID starts with \"1.8\" or later"
    exit 1
fi

# assume Java is in the PATH, this was tested above
# assuming 12GB is enough
${JAVA} -Duser.language=en-US -server -Xmx12g -jar compass.jar "$@"

#
# end
#
