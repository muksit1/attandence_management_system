package ams.db;

import java.sql.*;

public final class Database {
  private Database() { }
  public static Connection connect() throws SQLException {
    try { Class.forName("com.mysql.cj.jdbc.Driver"); }
    catch (ClassNotFoundException e) { throw new SQLException("MySQL Connector/J is missing. Add it via Maven or your IDE.", e); }
    String url = System.getenv().getOrDefault("AMS_DB_URL", "jdbc:mysql://localhost:3306/attendance_management?serverTimezone=UTC");
    String user = System.getenv().getOrDefault("AMS_DB_USER", "root");
    String password = System.getenv().getOrDefault("AMS_DB_PASSWORD", "");
    return DriverManager.getConnection(url, user, password);
  }
}
