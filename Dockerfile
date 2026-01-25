# Use Node.js 20
FROM node:20-slim

# Install libatomic (required for Node on some architectures)
RUN apt-get update && apt-get install -y libatomic1 && rm -rf /var/lib/apt/lists/*

# Create app directory
WORKDIR /usr/src/app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy the rest of the code (including the 'backend' folder)
COPY . .

# Expose the port your server.js uses (likely 5000 or 3000)
EXPOSE 5000

# Start the server
CMD ["node", "backend/server.js"]