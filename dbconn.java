package gui;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class dbconn {
	
	 
	    private static final String URL = "jdbc:oracle:thin:@localhost:1521:xe";
	    private static final String USER = "system";
	    private static final String PASSWORD = "root";

	    public static Connection getConnection() throws ClassNotFoundException, SQLException {
	        
	        Class.forName("oracle.jdbc.driver.OracleDriver");
	        return DriverManager.getConnection(URL, USER, PASSWORD);
	    }

	    public static void main(String[] args) {
	        System.out.println("Testing DB connection...");
	        try (Connection conn = getConnection()) {
	            if (conn != null && !conn.isClosed()) {
	                System.out.println(" Connected to Oracle DB successfully.");
	            } else {
	                System.out.println(" Connection returned null/closed.");
	            }
	        } catch (ClassNotFoundException cnfe) {
	            System.err.println("Oracle JDBC Driver not found. Add ojdbc8.jar to build path.");
	            cnfe.printStackTrace();
	        } catch (SQLException se) {
	            System.err.println("SQL error: " + se.getMessage());
	            se.printStackTrace();
	        }
	    }
	}