#!/bin/bash

# if you put -1 for npx cap run then it's live run where you need the react running
npm run build

if [ $? -eq 0 ]; then
  echo "Build Successful. Syncing Capacitor..."
  npx cap sync
  
  echo "Deploying to Android..."
  npx cap run android
else
  echo "Build Failed. Aborting."
  exit 1
fi
