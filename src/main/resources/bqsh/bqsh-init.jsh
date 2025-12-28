import org.springframework.jdbc.core.simple.JdbcClient;

var ds = bx.sql.duckdb.DuckDataSource.createInMemory();
var jdbc = JdbcClient.create(ds)



