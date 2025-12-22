import org.springframework.jdbc.core.simple.JdbcClient;

var ds = bx.sql.duckdb.DuckDataSource.createInMemory();
var jdbc = JdbcClient.create(ds)

// /env --class-path /Users/rob/dev/bitquant/target/stage/lib/*.jar

