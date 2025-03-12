#!/bin/bash
# Use the JAVA_HOME as defined by the base image
export JAVA_HOME=/usr/java/openjdk-21
export PATH=$JAVA_HOME/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin

echo "JAVA_HOME is set to: $JAVA_HOME"

if [ -f "$JAVA_HOME/bin/java" ]; then
    echo "Found Java at: $JAVA_HOME/bin/java"
else
    echo "Java not found at: $JAVA_HOME/bin/java"
fi

# Execute your application
exec "$JAVA_HOME/bin/java" -jar /app/app.jar
