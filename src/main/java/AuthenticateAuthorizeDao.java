import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AuthenticateAuthorizeDao 
{
	//Insert new users
	public static void InsertUser(CreateAccModel creuser,String refreshTokens) throws ClassNotFoundException, SQLException
    {
   	 Connection conn=DatabaseConnect.connect();
   	 PreparedStatement st=conn.prepareStatement("insert into users(name,email,mobile,password,location) values(?,?,?,?,?)");
   	 st.setString(1,creuser.getName());
   	 st.setString(2,creuser.getEmail());
   	 st.setString(3,creuser.getPhone());
   	 st.setString(4,creuser.getPassword());
   	 st.setString(5, creuser.getLocation());
   	 st.executeUpdate();
     java.sql.Statement stm=(java.sql.Statement)conn.createStatement();
     ResultSet rst=stm.executeQuery("select max(uid) as UID from users");
     int uids=rst.getInt("UID");
    
     //Insert uids into UsersAPIindex table to acknowledge,the user's resources will hold by the server
     st=conn.prepareStatement("insert into usersAPIindex(uid,profile,contacts,socialMedia) values(?,?,0,0)");
     st.setInt(1, uids);
	 st.setInt(2,1);
	 st.executeUpdate();
	 
	 //Insert the 20 refresh Tokens for that Users Accounts which helpful for refreshing the access tokens during API calls
	 st=conn.prepareStatement("insert into refTokenHolder(uid,refreshTokens) values(?,?)");
	 st.setInt(1, uids);
	 st.setString(2,refreshTokens);
	 st.executeUpdate();
	 st.close();
	 conn.close();
    }
	       //Check the users credentials when logging the accounts
		public static int checkUser(String email,String pass) throws ClassNotFoundException, SQLException
		{
			 System.out.print("Entered Databse");
			Connection conn=DatabaseConnect.connect();
			PreparedStatement st=conn.prepareStatement("select uid from users where email=? and password=?");
			st.setString(1, email);
			st.setString(2, pass);
			ResultSet rs=st.executeQuery();
			if(rs.next()==false)
			{
				  st.close();
				  conn.close();
			      return 0;
			}
			else
			{
				System.out.print(rs.getInt("uid"));
			    int uids=rs.getInt("uid");
			    st.close();
			    conn.close();
			    return uids;
			}
		}
		
		//Check the whether the resource owner have resources(which mentioned in the url) on mano's server
		public static boolean checkScope(int uids,String scopename) throws SQLException, ClassNotFoundException
		{
			int check_scope_flag=1;
			//split the list of scopes to check whether the resource is avail or not
			String[] scopeSegregates=scopename.split(",");
			Connection conn=DatabaseConnect.connect();
			PreparedStatement st;
			st=conn.prepareStatement("select * from usersAPIindex where uid=?");
			st.setInt(1, uids);
			ResultSet rs=st.executeQuery();
			return checkResultSet(rs, conn, st, scopename);
		}
		
		//Check the whether the clients have resources(which mentioned in the url) on mano's server
		public static boolean checkClientScope(String clientid,String scopename) throws SQLException, ClassNotFoundException
		{
			Connection conn=DatabaseConnect.connect();
			PreparedStatement st;
			st=conn.prepareStatement("select * from clientAPIindex where clientid=?");
			st.setString(1, clientid);
			ResultSet rs=st.executeQuery();
			return checkResultSet(rs, conn, st, scopename);
		}
		
		//Resuablitity function for checking the resources on the resource server
		public static boolean checkResultSet(ResultSet rs,Connection conn,Statement st,String scopename) throws SQLException
		{
			int check_scope_flag=1;
			String[] scopeSegregates=scopename.split(",");
			if(rs.next()==false)
			{
				rs.close();
				st.close();
				conn.close();
			    return false;
			}
			else
			{
				for(int i=0;i<scopeSegregates.length;i++)
				{
					if(rs.getInt(scopeSegregates[i])==0)
					{
						check_scope_flag=0;
						break;
					}	
				}
				if(check_scope_flag==1)
				{
				rs.close();
				st.close();
				conn.close();
				return true;
				}
				else
				{
					rs.close();
					st.close();
					conn.close();
					return false;
				}
			}
		}
		
		//Save access token
		public static void saveAccessTokens(AccessTokenModel newAccessToken) throws SQLException, ClassNotFoundException
		{
			Connection conn=DatabaseConnect.connect();
			PreparedStatement savetok=conn.prepareStatement("insert into issuedAccessToken(clientid,uid,accesstoken,scope,timestamp)values(?,?,?,?,?)");
			savetok.setString(1, newAccessToken.getClientId());
			savetok.setInt(2, newAccessToken.getUid());
			savetok.setString(3, newAccessToken.getAccessToken());
			savetok.setString(4,newAccessToken.getScope());
			savetok.setString(5,newAccessToken.getTimeStamp());
			savetok.executeUpdate();
			savetok.close();
			conn.close();
		}
		
		//Check refresh Token existing for uids or Valid refresh Tokens or not
		public static boolean checkAlreadyIssueRefresh(int uids) throws SQLException, ClassNotFoundException
		{
				Connection conn=DatabaseConnect.connect();
				PreparedStatement validTok=conn.prepareStatement("select * from issuedRefreshToken where uid=?");
			    validTok.setInt(1, uids);
			    ResultSet rs=validTok.executeQuery();
			    if(rs.next()==true)
			    {
			    	return true;
			    }
			    else
			    {
			    	return false;
			    }
		}
		//Stored the Authorization grant code 
		public static void saveGrantCode(grantCodeModel newCode) throws SQLException, ClassNotFoundException
		{
			 Connection conn=DatabaseConnect.connect();
		   	 PreparedStatement st=conn.prepareStatement("insert into grantcodelog(clientid,uid,grantcode,timestamp,scope,refreshissued) values(?,?,?,?,?,?)");
		   	 st.setString(1,newCode.getClientId());
		   	 st.setInt(2,newCode.getUid());
		   	 st.setString(3,newCode.getGrantCode());
		   	 st.setString(4,newCode.getTimeStamp());
		   	 st.setString(5,newCode.getScope());
		   	 st.setInt(6,newCode.getRefresh_issued());
		   	 st.executeUpdate();
		   	 st.close();
		   	 conn.close();
		}
		
		//Validation the grant code for generation of access token
		public static ArrayList<Integer> validateGrandCode(String grantcode) throws SQLException, ClassNotFoundException, ParseException
		{
			ArrayList<Integer> uidrefstatus=new ArrayList();
			 Connection conn=DatabaseConnect.connect();
		   	 PreparedStatement st=conn.prepareStatement("select * from grantcodelog where grantcode=?");
		   	 st.setString(1,grantcode);
		   	 ResultSet rs=st.executeQuery();
		   	 //Check if the code is avail or not
		   	 if(rs.next()==false)
		   		  return uidrefstatus; 
		   	 else 
		   	 {
		     
		   	 String grandtoktime=rs.getString("timestamp");
		   	 
		   	 //Indicates 0--->not issued refresh token , 1--> issued refresh token
		   	 int refresh_issued=rs.getInt("refreshissued");
		   	 int uid=rs.getInt("uid");
		   	 uidrefstatus.add(uid);
		   	 uidrefstatus.add(refresh_issued);
		   	 
		   	 Calendar tokcal = Calendar.getInstance();
		   	 SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
		   	 tokcal.setTime((sdf.parse(grandtoktime)));
		   	 Calendar currtime= Calendar.getInstance();
		   	 st=conn.prepareStatement("delete from grantcodelog where uid=? and grantcode=?");
		   	 st.setInt(1, rs.getInt("uid"));
		   	 st.setString(2, grantcode);
		   	 st.executeUpdate();
		   	 
		   	 //Check for expiration of grantcode
		   	 if(tokcal.compareTo(currtime)>0)
		   	 {
		   		st.close();
				conn.close();
		   	    return uidrefstatus;
		   	 }
		   	 else
		   	 {
		   		st.close();
				conn.close();
		   		return uidrefstatus;
		   	 }
		   	 }
		}
		//Save Refresh Token
		public static String saveRefreshToken(AccessTokenModel access_token,RefreshTokenModel refresh_token) throws ClassNotFoundException, SQLException
		{
			saveAccessTokens(access_token);
			int tok_ind=0;
			String refreshTokens;
			Connection conn=DatabaseConnect.connect();
			//Get max index of refresh token issued.
			PreparedStatement checkRefAvail=conn.prepareStatement("select max(tokenindex) as REMAIN from issuedRefreshToken where clientid=? and uid=?");
			checkRefAvail.setString(1, access_token.getClientId());
			checkRefAvail.setInt(2, access_token.getUid());
			ResultSet tokconsumes=checkRefAvail.executeQuery();
			if(tokconsumes.next()==false)
			{
				//If this is a first refresh token.
				refreshTokens=generateRefreshToken(refresh_token.getUid(),0, conn);
				refresh_token.setTokenindex(0);;
				refresh_token.setRefreshToken(refreshTokens);
				saveRefreshTokens(refresh_token,conn);
			}
			else
			{
				//It is used for providing the exact 20 refresh token,if 20 crossed,it will again issued the first refresh token which issued earlier.
				tok_ind=(((tokconsumes.getInt("REMAIN"))%19));
				if(tok_ind!=0)
					tok_ind+=1;
				refresh_token.setTokenindex(tok_ind);
				refreshTokens=generateRefreshToken(refresh_token.getUid(),tok_ind,conn);
				refresh_token.setRefreshToken(refreshTokens);
				saveRefreshTokens(refresh_token,conn);
			}
			conn.close();
		    return refreshTokens;
		}
		
		//Pick up and returned the Refresh Tokens which issued for respective accounts when the accounts was first created
		public static String generateRefreshToken(int uid,int tokind,Connection conn) throws SQLException
		{
			
			PreparedStatement getRefreshTok=conn.prepareStatement("select * from refTokenHolder where uid=?");
			getRefreshTok.setInt(1, uid);
			ResultSet refTok=getRefreshTok.executeQuery();
			refTok.next();
			String refTokens=refTok.getString("refreshTokens");
			String[] tokSegregate=refTokens.split(",");
			return tokSegregate[tokind];
		}
		
		//Save Refresh Tokens after pickup from the refTokenHolder
		public static void saveRefreshTokens(RefreshTokenModel refToken,Connection conn) throws SQLException
		{
			PreparedStatement saveReftok=conn.prepareStatement("insert into issuedRefreshToken(clientid,uid,refreshtoken,scope,tokenindex)values(?,?,?,?,?)");
			saveReftok.setString(1, refToken.getClientId());
			saveReftok.setInt(2, refToken.getUid());
			saveReftok.setString(3, refToken.getRefreshToken());
			saveReftok.setString(4,refToken.getScope());
			saveReftok.setInt(5, refToken.getTokenindex());
			saveReftok.executeUpdate();
			saveReftok.close();
		}
		
		//Validate the access tokens for API call
		public static boolean ValidateAccessToken(String accesstoken,int uid,String scope) throws ClassNotFoundException, SQLException, ParseException
		{
			Connection conn=DatabaseConnect.connect();
			// To check whether this accesstoken is valid and scope mentioned in the URL should gets matched
			PreparedStatement checktok=conn.prepareStatement("select * from issuedAccesstoken where accesstoken=? and uid=?");
			checktok.setString(1, accesstoken);
			checktok.setInt(2, uid);
			ResultSet rscheck=checktok.executeQuery();
			if(rscheck.next()==false)
				return false;
			else
			{
				String actime=rscheck.getString("timestamp");
				 Calendar cal = Calendar.getInstance();
			   	 SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
			   	 cal.setTime((sdf.parse(actime)));
			   	 System.out.print(cal.getTime().toString());
			   	 Calendar cal2= Calendar.getInstance();
			   	 //if the access token is valid
			   	 if(cal.compareTo(cal2)>0)
			   	 {
			   		 //Check the scope of issued accesstoken and scope mentioned in URL
			   		 if(rscheck.getString("scope").contains(scope)==true)
			   		 {
			   			checktok.close();
			   			rscheck.close();
						conn.close();
				   	    return true;
			   		 }
			   		 else
			   		 {
			   			checktok.close();
			   			rscheck.close();
						conn.close();
				   	    return true;
			   		 } 
			   	 }
			   	 else
			   	 {
			   		checktok.close();
					conn.close();
			   		return false;
			   	 }
			}
		}
		//Revoke the Refresh Token
		public static boolean DeleteToken(String refreshtoken,int uid,String clientid) throws ClassNotFoundException, SQLException
		{
			Connection conn=DatabaseConnect.connect();
			PreparedStatement checktok=conn.prepareStatement("delete from issuedRefreshToken where refreshtoken=? and uid=? and clientid=?");
			checktok.setString(1, refreshtoken);
			checktok.setInt(2, uid);
			checktok.setString(3, clientid);
			int checkdel=checktok.executeUpdate();
			conn.close();
			if(checkdel==0)
				return false;
			else
		        return true;
		}
		}
