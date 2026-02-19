FROM eclipse-temurin:17-jre
WORKDIR /app
COPY app.jar /app/app.jar
EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC"
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
