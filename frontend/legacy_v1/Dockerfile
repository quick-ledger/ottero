# Use an official Node.js runtime as the base image
FROM node:14-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy package.json and package-lock.json to the working directory
COPY package*.json ./

# Install app dependencies
RUN npm install --production

# Copy the rest of the app source code to the working directory
COPY . .

# Expose the port on which your app listens
EXPOSE 3000

# Define the command to run your app
CMD [ "npm", "start" ]