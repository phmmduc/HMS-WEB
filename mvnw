#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Wrapper script for UNIX
# ----------------------------------------------------------------------------

[ -z "$JAVA_HOME" ] && [ -d "/Library/Java/JavaVirtualMachines/jdk-26.jdk/Contents/Home" ] && export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-26.jdk/Contents/Home"

if [ -n "$JAVA_HOME" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

if which mvn >/dev/null 2>&1; then
    exec mvn "$@"
fi

BASE_DIR=$(dirname "$0")
WRAPPER_JAR="$BASE_DIR/.mvn/wrapper/maven-wrapper.jar"
PROPERTIES_FILE="$BASE_DIR/.mvn/wrapper/maven-wrapper.properties"

if [ ! -f "$WRAPPER_JAR" ]; then
    echo "Downloading Maven Wrapper..."
    WRAPPER_URL=$(grep 'wrapperUrl=' "$PROPERTIES_FILE" | cut -d'=' -f2-)
    if [ -z "$WRAPPER_URL" ]; then
        WRAPPER_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"
    fi
    curl -fsSL -o "$WRAPPER_JAR" "$WRAPPER_URL" 2>/dev/null || wget -q -O "$WRAPPER_JAR" "$WRAPPER_URL" 2>/dev/null
fi

if [ -f "$WRAPPER_JAR" ]; then
    exec "$JAVACMD" -jar "$WRAPPER_JAR" "$@"
else
    echo "Lỗi: Không tìm thấy Maven hoặc maven-wrapper.jar. Vui lòng cài đặt Maven (brew install maven) hoặc kết nối mạng để tải wrapper." >&2
    exit 1
fi

