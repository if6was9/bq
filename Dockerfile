FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY target/stage/lib/ /app/lib/
COPY bqsh /app/bqsh
RUN chmod +x /app/bqsh
CMD ["/app/bqsh"]



