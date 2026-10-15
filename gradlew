#!/usr/bin/env sh
if [ -z "$JAVA_HOME" ] && [ -d "C:/Users/nn/Desktop/code/jdk-21.0.6+7" ]; then
    export JAVA_HOME="C:/Users/nn/Desktop/code/jdk-21.0.6+7"
fi
"C:/Users/nn/Desktop/code/gradle-8.7/bin/gradle" "$@"
