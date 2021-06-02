import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ResourceDao 
{
	    //Get the userinfo resources with help of access token
		public static CreateAccModel getUsers(int uid) throws SQLException, ClassNotFoundException
		{
			Connection conn=DatabaseConnect.connect();
			PreparedStatement checktok=conn.prepareStatement("select * from users where uid=?");
			checktok.setInt(1, uid);
			ResultSet rscheck=checktok.executeQuery();
			rscheck.next();
			CreateAccModel users=new CreateAccModel();
			users.setName(rscheck.getString("name"));
			users.setEmail(rscheck.getString("email"));
			users.setPhone(rscheck.getString("phone"));
			return users;
		}
		
}
