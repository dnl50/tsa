FROM node:18-alpine AS build
WORKDIR /build
COPY . .
RUN wget -O /etc/apk/keys/adoptium.rsa.pub https://packages.adoptium.net/artifactory/api/security/keypair/public/repositories/apk && \
    echo 'https://packages.adoptium.net/artifactory/apk/alpine/main' >> /etc/apk/repositories && \
    apk add temurin-17-jre && \
    npm ci && \
    npm run build && \
    npm ci --omit dev

FROM node:18-alpine
WORKDIR /app
COPY package.json .
COPY --from=build /build/dist .
COPY --from=build /build/node_modules ./node_modules
EXPOSE 3000
ENTRYPOINT ["node", "index.js"]