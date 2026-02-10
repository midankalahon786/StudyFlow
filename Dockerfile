# 1. Use a specific, stable base image
FROM node:20-slim

# 2. Security: Update OS packages to patch vulnerabilities
# Also, clean up after install to keep the image small
RUN apt-get update && apt-get upgrade -y && \
    apt-get install -y libatomic1 && \
    rm -rf /var/lib/apt/lists/*

# 3. Create app directory
WORKDIR /usr/src/app

# 4. Copy package files and install
COPY package*.json ./
# Use 'npm ci' for faster, reliable builds in CI/CD
RUN npm ci --only=production

# 5. Copy the rest of the code
COPY . .

# 6. Security: Switch to a non-privileged user
# Node images come with a user named 'node' by default
USER node

# 7. Expose the port
EXPOSE 5000

# 8. Start the server
CMD ["node", "backend/server.js"]