#!/bin/bash

#java 11
mvn clean package -Dmaven.test.skip=true -f pom.xml
if [ $? -ne 0 ]; then
    echo "Maven build failed. Stopping script."
    exit 1
fi

ssh -i $GILJABI_PEM $SERVER_USER@$SERVER_IP << EOF
  echo "Attempting to gracefully stop Spring Boot..."
  # Assuming usage of Spring Boot Actuator to shutdown
  curl -X POST localhost:9090/actuator/shutdown

  echo "Executing kill script..."
  cd $SERVER_PATH
  ./kill.sh
  if [ \$? -ne 0 ]; then
    echo "Failed to execute kill script. Exiting..."
    exit 1
  fi

  mv giljabi-2.jar backup

  echo "Kill script executed successfully."
EOF

if [ $? -eq 0 ]; then
  echo "Deployment script completed successfully."
else
  echo "Deployment failed. Please check the logs."
fi

echo "Deploying to $SERVER_IP"
#scp -i $GILJABI_PEM $LOCAL_PATH $SERVER_USER@$SERVER_IP:$SERVER_PATH
rsync -avh --progress -e "ssh -i $GILJABI_PEM" $LOCAL_PATH $SERVER_USER@$SERVER_IP:$SERVER_PATH/

ssh -i $GILJABI_PEM $SERVER_USER@$SERVER_IP << EOF
  echo "Changing directory to $SERVER_PATH..."
  cd $SERVER_PATH
  ./run.sh
EOF

# mvn clean


