# First stage: complete build environment
FROM cprd-doc-reg01.sberins.ru:10019/platform/jdk-18-maven-3.8.8 AS builder

ADD ./pom.xml pom.xml
ADD ./src src/
ENV MAVEN_OPTS "-Dhttps.protocols=TLSv1.2"
#RUN mvn clean package
RUN mkdir -p /tmp/target && \
    mvn -B -f ./pom.xml dependency:go-offline -DoutputDirectory=/tmp/target && mvn -B -T 1C package -DskipTests -DoutputDirectory=/tmp/target

FROM cprd-doc-reg01.sberins.ru:10010/openjdk:18-slim-buster

ARG Env

USER root

COPY certs/SberIns_Root_CA.cer /usr/local/share/ca-certificates/
COPY certs/SberIns_Issuing_CA.cer /usr/local/share/ca-certificates/

RUN keytool -keystore -cacerts -storepass changeit -noprompt -trustcacerts -importcert -alias SberIns_Root_CA -file /usr/local/share/ca-certificates/SberIns_Root_CA.cer
RUN keytool -keystore -cacerts -storepass changeit -noprompt -trustcacerts -importcert -alias SberIns_Issuing_CA -file /usr/local/share/ca-certificates/SberIns_Issuing_CA.cer
RUN update-ca-certificates

RUN echo | openssl s_client -showcerts -connect $Env:443 2>/dev/null | \
  openssl x509 -outform PEM > /usr/local/share/ca-certificates/sberins.pem && \
  keytool -import -file /usr/local/share/ca-certificates/sberins.pem -trustcacerts -storepass changeit -noprompt -keystore ${JAVA_HOME}/lib/security/cacerts
 

WORKDIR /app
COPY --from=builder /tmp/target/*.jar ./app-SNAPSHOT.jar
EXPOSE 8080

RUN chown -R 1000:1000 /app && \
  chmod o+w -R /var/log/
USER 1000

CMD [ "java", "-Xms4096m  -Xmx8g ", "-Djava.util.concurrent.ForkJoinPool.common.parallelism=2048", "-jar", "/app/app-SNAPSHOT.jar" ]
