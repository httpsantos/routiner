FROM openjdk:8-jre-alpine

EXPOSE 7000
ENTRYPOINT ["/usr/bin/java", "-jar", "/usr/share/routiner/routiner.jar"]

# Add Maven dependencies (not shaded into the artifact; Docker-cached)
#ADD target/lib           /usr/share/myservice/lib

ARG JAR_FILE
ADD target/${JAR_FILE} /usr/share/routiner/routiner.jar