FROM eclipse-temurin:21-jdk
WORKDIR /app

RUN useradd app -m -d /app
COPY target/stage/lib/ /app/lib/
COPY src/main/resources/logback-container.xml /app/classes/logback.xml
COPY target/*.jar /app/lib/
COPY bqsh /bin/
COPY bq /bin/


COPY src/main/script/entrypoint.sh /entrypoint.sh
RUN chmod +x /bin/bqsh && chmod +x /entrypoint.sh && chmod +x /bin/bq
RUN chown app /app
USER app
ENTRYPOINT [ "/entrypoint.sh" ]
CMD ["bq"]



