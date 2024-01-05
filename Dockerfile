FROM eclipse-temurin:17-jdk-alpine

RUN adduser --system --uid 1001 app

# Create deployment directory
RUN mkdir -p /app

WORKDIR /app

RUN cp -r /app-build/app.jar /app/

RUN chown -R 1001:0 /app \  
    && chmod -R g+=wrx /app

USER app

EXPOSE 8080

CMD java -Djava.security.egd=file:/dev/./urandom -jar /app/app.jar $APP_OPTIONS
