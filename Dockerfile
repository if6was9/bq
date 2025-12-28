FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY target/stage/lib/ /app/lib/
COPY target/*.jar /app/lib/
COPY bqsh /bin/
COPY bq /bin/


COPY src/main/script/entrypoint.sh /entrypoint.sh
RUN chmod +x /bin/bqsh && chmod +x /entrypoint.sh && chmod +x /bin/bq


ENTRYPOINT [ "/entrypoint.sh" ]
CMD ["bq"]



