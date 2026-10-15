@echo off
if not defined JAVA_HOME (
    set "JAVA_HOME=C:\Users\nn\Desktop\code\jdk-21.0.6+7"
)
set "PATH=%JAVA_HOME%\bin;%PATH%"
"C:\Users\nn\Desktop\code\gradle-8.7\bin\gradle.bat" %*
