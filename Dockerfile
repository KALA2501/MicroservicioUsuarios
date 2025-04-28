FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY wait-for-mysql.sh wait-for-mysql.sh
RUN chmod +x wait-for-mysql.sh

COPY target/app.jar app.jar

ENTRYPOINT ["./wait-for-mysql.sh", "mysql", "3306"]
