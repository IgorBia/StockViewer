# Stage 1: Build Angular
FROM node:22 AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci --legacy-peer-deps
RUN npm install npm@latest -g
COPY . .
RUN npm run build -- --project stock_frontend --configuration development --optimization=false --source-map=true

# Stage 2: Nginx
FROM nginx:stable-alpine
COPY --from=build /app/dist/stock_frontend/browser /usr/share/nginx/html
RUN chmod -R 755 /usr/share/nginx/html
COPY ./nginx/default.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
