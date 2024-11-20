#!/bin/sh

APP_NAME=giljabi-2.jar

PID=$(ps -ef | grep $APP_NAME | grep -v grep | awk '{print $2}')
if [ -z "$PID" ]; then
    echo "Application is not running."
else
    echo "Killing application with PID: $PID"
    kill -9 $PID
    echo "Application terminated."
fi

echo "Start giljabi application"
nohup /usr/lib/jvm/java-11-openjdk-amd64/bin/java -jar -Dspring.profiles.active=prod -Xms256m -Xmx256m -Djava.net.preferIPv4Stack=true -Duser.timezone=Asia/Seoul -Djasypt.encryptor.password=JasyptEncryption_key $APP_NAME > /dev/null 2>&1 &
