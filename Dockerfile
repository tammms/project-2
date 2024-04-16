FROM node:21 AS ng-builder

RUN npm i -g @angular/cli

WORKDIR /ngapp

COPY frontend/package*.json .
COPY frontend/angular.json .
COPY frontend/tsconfig.* .
COPY frontend/ngsw-config.json .
COPY frontend/src src

RUN npm ci && ng build --configuration=production

# /ngapp/dist/frontend/browser/*

# Starting with this Linux server
FROM maven:3-eclipse-temurin-21 AS sb-builder

## Build the application
# Create a directory call /sbapp
# go into the directory cd /app
WORKDIR /sbapp

# everything after this is in /sbapp
COPY backend/mvnw .
COPY backend/mvnw.cmd .
COPY backend/pom.xml .
COPY backend/.mvn .mvn
COPY backend/src src
COPY --from=ng-builder /ngapp/dist/frontend/browser/ src/main/resources/static

# Build the application
RUN mvn package -Dmaven.test.skip=true

FROM maven:3-eclipse-temurin-21

WORKDIR /app 

COPY --from=sb-builder /sbapp/target/backend-0.0.1-SNAPSHOT.jar app.jar

## Run the application
# Define environment variable 
ENV PORT=8080
ENV SPRING_DATASOURCE_URL=NOT_SET
ENV SPRING_DATASOURCE_USERNAME=NOT_SET
ENV SPRING_DATASOURCE_PASSWORD=NOT_SET

ENV SPRING_DATA_MONGODB_URI=NOT_SET

ENV APP_JWT_SECRET=NOT_SET

ENV APP_FIREBASE_CONFIGURATION_FILE=NOT_SET

ENV API_EMAIL=NOT_SET
ENV API_PASSWORD=NOT_SET

# Expose the port
EXPOSE ${PORT}

# Run the program
ENTRYPOINT SERVER_PORT=${PORT} java -jar app.jar