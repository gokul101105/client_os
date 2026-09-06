# Module 18. VITE_API_BASE_URL is a BUILD-TIME value, not a runtime one --
# Vite inlines import.meta.env.VITE_* into the compiled JS bundle when
# `npm run build` runs, so it has to be passed as a build ARG (see
# docker-compose.yml's `build.args`), not as a container environment
# variable the way backend/ai-service's config works. Changing it means
# rebuilding this image, not just restarting the container.
FROM node:20-slim AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
ARG VITE_API_BASE_URL=http://localhost:8080/api
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
