FROM eclipse-temurin:17-jre-alpine as builder
WORKDIR application
ARG JAR_FILE=app/build/libs/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM eclipse-temurin:17-jre-alpine
WORKDIR application
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./
EXPOSE 80
EXPOSE 318
EXPOSE 8080
VOLUME ["/application/db/data"]
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
