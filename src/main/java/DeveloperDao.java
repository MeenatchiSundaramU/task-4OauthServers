import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DeveloperDao 
{
	public static void InsertUser(DeveloperModel dev) throws ClassNotFoundException, SQLException
    {
 	      Connection conn=DatabaseConnect.connect();
 	      PreparedStatement st=conn.prepareStatement("insert into developerdb(clientid,clientsecret,appname,redirecturis) values(?,?,?,?)");
 	      st.setString(1,dev.getClientId());
 	      st.setString(2,dev.getClientSecret());
 	      st.setString(3,dev.getAppName());
 	      st.setString(4, dev.getRedirectUri());
 	      st.executeUpdate();
 	      st.close();
          conn.close();
    }
	
	//Verify the client id and redirecturi
    public static boolean verifyDeveloper(String clientid,String redirecturi) throws ClassNotFoundException, SQLException
    {
     int uri_found=0;
   	 Connection conn=DatabaseConnect.connect();
   	 PreparedStatement st=conn.prepareStatement("select * from developerdb where clientid=?");
   	 st.setString(1, clientid);
   	 ResultSet rs=st.executeQuery();
   	 if(rs.next()==true)
   	 {
   		 String redirecturis=rs.getString("redirecturis");
 
   		//Split the list of uris for verifications
   		String[] listOfUris=redirecturis.split(",");
   		for(int i=0;i<listOfUris.length;i++)
   		{
   			//Check whether the mentioned redirected uri in query param will gets matched with any of these in developer table
   			if(listOfUris[i].contains(redirecturi)==true)
   			{
   				uri_found=1;
   				break;
   			}
   		}
   		if(uri_found==1)
   		{
   			return true;
   		}
   		else
   		{
   			return false;
   		}
   	 }
   	 else
   	 {
   		 st.close();
   		 conn.close();
   		 return false;
   	 }
    }
}
