import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnect 
{
     public static Connection connect() throws ClassNotFoundException, SQLException
     {
    	 Class.forName("org.sqlite.JDBC");
    	 Connection con=DriverManager.getConnection("jdbc:sqlite:C://sqlite-tools-win32-x86-3350500//msoauth.db");
    	 return con;
     }
}
