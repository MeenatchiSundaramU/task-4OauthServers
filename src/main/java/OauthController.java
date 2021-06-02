import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


@WebServlet(value="/")
public class OauthController extends HttpServlet
{
	protected void service(HttpServletRequest req,HttpServletResponse resp) throws IOException
	{
           String serverEndpt=req.getServletPath();
           switch(serverEndpt)
           {
           
           //When client gets registered in developer console this case will called
           case "/devdb":                                   try {
					                                        devDetails(req,resp);
					                                        } catch (ClassNotFoundException | IOException | SQLException e1) {
					                                        e1.printStackTrace();}
                                                            break;
                                       
          //When user clicks on sign in with mano this case will called
                                       
           case "/msaccount/accounts":                      signInWithMano(req,resp);
                       	                                    break;
           
           //When user create an account on mano accounts server this case will called
           case "/msaccount/createAcc" :                    try {
					                                        createAcc(req,resp);} catch (ClassNotFoundException | SQLException e) {
					                                        e.printStackTrace();}
                                                            break;
           
           //When user logs in their mano's accounts on server this case will called
                                        
           case "/msaccount/login" :                        try {
					                                        LogVerified(req,resp);
				                                            } catch (ClassNotFoundException | SQLException | IOException e) {
					                                        e.printStackTrace();}
                                                            break;
             
            //When the resource owner grants permission for the resources this case will called
            
           case "/msaccount/codeortoksent" :                try {
				                                            issueCodeOrTokSent(req,resp);} 
                                                            catch (ClassNotFoundException | SQLException | IOException e) {
				                                            e.printStackTrace();
				                                            }
                                                            break;
           
           //If the resource owner denied the authorization permission this case will called
           case "/msaccount/errorcode"   :                  deniedGrantResponse(req,resp);
                                                            break;
                                                            
            //Endpoint for Code Exchange for accesstoken(Auth Flow) or Generating Access Token or Refresh Token for client flow,ropc flow
           case "/msaccount/tokensent":                     try {
				                                            issueAccRefToken(req,resp);
			                                                } catch (ClassNotFoundException | SQLException | ParseException | IOException e) {
				                                            e.printStackTrace();}
                                                            break;
                                                            
                 //Token response endpoint(Issued Access Token and Refresh Token)
           case "/token/response":                          RedirectUriResp(req,resp);
                                                            break;
                                                            
                 //To get userinfo resources this case will gets called AI call for user profile access
           case "/msresource/userinfo" :                    try {
					                                        getUserProfileDetails(req,resp);
				                                            } catch (NumberFormatException | ClassNotFoundException | SQLException | ParseException e) {
					                                        e.printStackTrace();}
                                                            break;
                                                            
             //To revoke the refresh token in which we no need longer accessing of data,This case called
                                                            
           case "/revoke" :                                 try {
					                                        RevokeToken(req,resp);} catch (ClassNotFoundException | SQLException | IOException e) {
					                                        e.printStackTrace();}
                                                            break;
           
           }
	}
	
	          //Store developer console details to developerdb table
              void devDetails(HttpServletRequest req,HttpServletResponse resp) throws IOException, ClassNotFoundException, SQLException
                 {
            	  
            	    //create one developermodel obj for clubed the developer details
	                DeveloperModel newdev=new DeveloperModel();
	                newdev.setClientId(randomStringGenerator());
	                newdev.setClientSecret(randomStringGenerator());
	                newdev.setAppName(req.getParameter("appname"));
	                newdev.setRedirectUri(req.getParameter("url1"));
	    
	                //Here we have concatenate the multiple redirected uri's and each sepearated by commas.
	                //When validate the redirected uris we split up based on commas and stored in arrayList and validation made easier.
	    
	                if((req.getParameter("url2").contains("null"))==false)
	                {
	                  //Concatenate with URL 1 each of us seperated by commas
	    	          newdev.setRedirectUri(newdev.getRedirectUri().concat(','+req.getParameter("url2")));
	                }
	                if((req.getParameter("url3").contains("null"))==false)
	                {
	                  //Concatenate with URL 1 each of us seperated by commas
	    	          newdev.setRedirectUri(newdev.getRedirectUri().concat(','+req.getParameter("url3")));
	                }
	                
	                //Uploaded the details to developerdb table in database.
	                DeveloperDao.InsertUser(newdev);
	                
	                //Redirect to sigin with mano main page
	                resp.sendRedirect("clientsigin.jsp");
                 }
    
            //Participate Oauth Flows in this Fn---->Authorization code flow,Implicit flow
              
           //Signin with mano stores the URL details and redirected to authentication Page
           void signInWithMano(HttpServletRequest req,HttpServletResponse resp) throws IOException
            {
 	           HttpSession session=req.getSession();
 	           
 	           //Stored the URI details for further process
 	           String responseType=req.getParameter("response_type");
   	           session.setAttribute("client_id", req.getParameter("client_id"));
   	           session.setAttribute("scope",req.getParameter("scope"));
   	           session.setAttribute("redirect_uri",req.getParameter("redirect_uri"));
   	           session.setAttribute("response_type",responseType);
   	           
   	           //We are fetching the details based on the response_type
   	           //code---->may contains access_type,send_refresh (Additional Parameters which influenced the issuing of refresh token to client)
   	           if(responseType.equals("code")==true)
   	           {
   	        	   if(req.getParameter("access_type")!=null)
   	        	   {
   	        		   String access_type=req.getParameter("access_type");
   	        		   session.setAttribute("access_type",access_type);
   	        		   if(req.getParameter("send_refresh")!=null)
   	        		   {
   	        			session.setAttribute("send_refresh","true"); 
   	        		   }
   	        	   }
   	        	   else
   	        	   {
   	        		session.setAttribute("access_type","online"); 
   	        	   }
   	           }
   	           
   	           //Redirected to Authentication Page
   	           resp.sendRedirect("ManoLogin.jsp");
            }
           
           //Participate Oauth Flows in this Fn---->Authorization code flow,Implicit flow
           //Create an new account and upload usersdetails to users table
           void createAcc(HttpServletRequest req,HttpServletResponse resp) throws ClassNotFoundException, SQLException, IOException
           {
        	   String refreshTokens="";
        	   //Create one object in CreateAccModel and stored the value into it
        	   
        	   CreateAccModel newuser=new CreateAccModel();
        	   newuser.setName(req.getParameter("crename"));
        	   newuser.setEmail(req.getParameter("cremail"));
        	   newuser.setPassword(req.getParameter("crepass"));
        	   newuser.setLocation(req.getParameter("creloc"));
        	   newuser.setPhone(req.getParameter("cremobile"));
        	   
        	 //Generate 20 refresh token for per accounts which helps during refreshing the access tokens.
        	   for(int i=1;i<=20;i++)
        	   {
        		   //Each tokens will seperated by commas.
        		   refreshTokens=refreshTokens.concat(randomStringGenerator());
        		   if(i<20)
        		   {
        			   refreshTokens=refreshTokens.concat(",");  
        		   }
        	   }
        	   
        	   //Stored the new users details in users table and 20 refreshTokens in refTokenHolder table
        	   AuthenticateAuthorizeDao.InsertUser(newuser, refreshTokens);
        	   resp.sendRedirect("ManoLogin.jsp");
           }
              
            //Login verifying
           void LogVerified(HttpServletRequest req,HttpServletResponse resp) throws ClassNotFoundException, SQLException, IOException
           {
        	   //Check the login credentials and if users exists,it will returns the uid of that users.
        	   HttpSession session=req.getSession();
        	   String email=req.getParameter("logmail");
        	   String password=req.getParameter("logpass");
        	   String clientid=(String) session.getAttribute("client_id");
		       String redirecturi=(String) session.getAttribute("redirect_uri");
		       String scope=(String) session.getAttribute("scope");
		       String allScopes=scopeExtraction(scope,1);
		       
		       //Create an Login object for validation using loginResuability function
        	   ReuseLoginValidModel validLogCredentials=new ReuseLoginValidModel(email, password, clientid, redirecturi, allScopes);
        	   
        	   //Verified Login credentials
        	   if(reuseLoginValidity(req,resp,validLogCredentials)==true)
        	   {
        		 //It is used to print in JSP which tells the resource owner which scope does client requests.
	    		   session.setAttribute("scopename", allScopes);
	    		   
	    		   //It will redirected to Authorization requests
	    		   resp.sendRedirect("ResourceConfirm.jsp");
        	   }
        	   else
        	   {
        		   //If the login credentials are not matched,it will gets called
        		   resp.sendRedirect("ManoLogin.jsp");
        	   }
           }
           
           //Participate Oauth Flows in this Fn---->Authorization code flow,Implicit flow,ROPC flow
           //Resuablility function for Login Validity
           public static boolean reuseLoginValidity(HttpServletRequest req,HttpServletResponse resp,ReuseLoginValidModel logModel) throws ClassNotFoundException, SQLException, IOException
           {
        	   
        	   HttpSession session=req.getSession();
        	   
        	   //Check the login credentials and extracts the user uid for further process
               int uids=AuthenticateAuthorizeDao.checkUser(logModel.getEmail(),logModel.getPass());
		       session.setAttribute("uids", uids);
		       
        	    if(uids!=0)
        		   {
        		       //Check for verified Client ID and Redirect URI
        		       if(DeveloperDao.verifyDeveloper(logModel.getClientid(),logModel.getRedirecturi())==true)
        		       {
        		    	   //Then verified the scope whether the resource owner have the resources on that server
        		    	   if(AuthenticateAuthorizeDao.checkScope(uids,logModel.getScope())==true)
        		    	   {
        		    		   return true;
        		    	   }
        		    	   else
        		    	   {
        		    		   //If the mentioned scope resources are not availble for that users it displayed it.
        		    		   resp.getWriter().print("You are not allowed to access the scope");
        		    		   return false;
        		    	   }
        		       }
        		       else
        		       {
                           //If the clientId and redirectUri are mismatched,it will gets called
        		    	   resp.getWriter().print("Invalid Client Id and redirect Uri");
        		    	   return false;
        		       }
        		   }
        	    else
        	    {
        	    	//Invalid Login Credentials
        	    	resp.getWriter().print("Invalid Login Credentials");
        	    	return false;
        	    }
           }
           
         //Participate Oauth Flows in this Fn---->Authorization code flow,Implicit flow
           //When client request for Authorization Code or Access Token Request(Implicit flow)
          public static void issueCodeOrTokSent(HttpServletRequest req,HttpServletResponse resp) throws ClassNotFoundException, SQLException, IOException
          {
        	  //Here response will depends on the type of flow,
        	  HttpSession session=req.getSession();
        	  int uid=(int) session.getAttribute("uids");
        	  String response_type=(String) session.getAttribute("response_type");
        	  String clientid=(String) session.getAttribute("client_id");
        	  String scope=(String) session.getAttribute("scope");
        	  
        	  //If the flow is implicit flow,the below if will work
        	  if(response_type.contains("token")==true)
        	  {
        		  //Create one access token and returned to client if the flow is an implicit flow
        		  AccessTokenModel newAccToken=reuseAccessTokenCode(req,uid, clientid, scope);        		  
        		  //It will send the access token along with redirected uri's
        		  resp.sendRedirect(session.getAttribute("redirect_uri")+"?access_token="+newAccToken.getAccessToken()+"&scope="+scope+"&expires=3600");
        	  }
        	  
        	//If the flow is Authorization code flow,the below if will work
        	  else if(response_type.contains("code")==true)
        	  {
        		int refresh_issued=0;
        		String timeStamp=timeGenerator(2);
        		String code=randomStringGenerator();
        		String access_type=(String) session.getAttribute("access_type");
        		
        		//If the accesstype==offline and send_refresh==true---->we need to issued refresh token along with access token. 
        		if((access_type.contains("offline")==true)&&(session.getAttribute("send_refresh")!=null)==true)
        		{
        			// So we made the refresh_issued_token=1 for issued refresh token along with access token
        			refresh_issued=1;
        		}
        		
        		//If the accesstype==offline and if the users is a new user,we can issued refresh token along with access token
        		//else we need to issued only access token as response
        		else if(access_type.contains("offline")==true)
        		{
        			//To check if the user is already get the refresh token or not which tells us we need to issued refresh token along with access token or not
        			if(AuthenticateAuthorizeDao.checkAlreadyIssueRefresh(uid)==false)
        			{
        				refresh_issued=1;
        			}
        		}
        		//Create one object for the grantcode and insert the values
        		grantCodeModel newGrantCode=new grantCodeModel(clientid,code,timeStamp,scope,uid,refresh_issued);
        		
        		//insert the grantCode object to the grantcodelog table
        		AuthenticateAuthorizeDao.saveGrantCode(newGrantCode);
        		
        		//It will send the auth code along with redirected uri's
        		resp.sendRedirect(session.getAttribute("redirect_uri")+"?code="+code+"&scope="+scope+"&expires=120");
        	  }
          }
          
        //Participate Oauth Flows in this Fn---->Implicit flow,Client Credential Flow,ROPC Flow
          //It is called when issued tokens response will depends on types of flow
          public static void issueAccRefToken(HttpServletRequest req,HttpServletResponse resp) throws ClassNotFoundException, SQLException, ParseException, IOException
          {
        	  HttpSession session=req.getSession();
        	  String clientId=req.getParameter("client_id");
              String clientSecret=req.getParameter("client_secret");
              String scope=req.getParameter("scope");
              String redirecturi=req.getParameter("redirect_uri");
              String grant_type=req.getParameter("grant_type");
              
              //If the flow is Authorization code flow
              if(grant_type.contains("auth_code")==true)
              {
            	  //Get grant code from the URL parameter
            	  String grantcode=req.getParameter("code");
            	  
            	  //Check whether the grantcode is valid or not and whether we need to issued refresh token along access token or not
            	  
            	  //The below function returned two values one is uid of the user.Next status of refresh token issued 
            	   //0--->Not issued refresh token along with access token,1---->issued refresh token along with access token
            	  
            	  ArrayList<Integer> refreshissued=AuthenticateAuthorizeDao.validateGrandCode(grantcode);
            	  
            	  //Check if the grandcode is valid or not
            	  if(refreshissued.get(0)!=0)
            	  {
            		//No refresh token issued if refresh_issued status==0
            		AccessTokenModel newAccToken=reuseAccessTokenCode(req, refreshissued.get(0), clientId, scope);
            		
            		// issued refresh token along with access token if refresh_issued status==1
            		if(refreshissued.get(1)==1)
            		{
            		     reuseRefreshTokenCode(req,refreshissued.get(0), clientId, scope, newAccToken);
            		}
            		resp.sendRedirect(redirecturi);
            	  }
            	  else
            	  {
            		  resp.getWriter().print("Invalid Grant Code");
            	  }
              }
              //If the flow is Client Credential Code flow
              else if(grant_type.contains("client_flow")==true)
              {
            	  //Check for verified Client ID and Redirect URI
            	  if(DeveloperDao.verifyDeveloper(clientId,redirecturi)==true)
            	  {
            		  //Extracts the individual scopes for that client
            		  //Mode-2---> for to gets Clients Resources values
            		  String allScopes=scopeExtraction(scope,2);
            		  
            		  //Check whether the client have the resources on server or not
            		  if(AuthenticateAuthorizeDao.checkClientScope(clientId, allScopes)==true)
            		  {
            			//set uid==-1 represents it is an client Access Tokens
            			//Generate the access Token for the client
            			  reuseAccessTokenCode(req,-1, clientId, allScopes);
            		  }
            		  else
            		  {
            			  resp.getWriter().print("Invalid Scope");
            		  }
            	  }
            	  else
            	  {
            		  resp.getWriter().print("Invalid ClientId Credentials");;
            	  }
              }
               //If the flow is Resource Owner Password Credential flow
              else if(grant_type.contains("password")==true)
              {
               //Extracts the login credentials from the uri itself for validation
               String email=req.getParameter("logmail");
           	   String password=req.getParameter("logpass");
   		       String allScopes=scopeExtraction(scope,1);
   		       
           	   ReuseLoginValidModel validLogCredentials=new ReuseLoginValidModel(email, password,clientId, redirecturi, allScopes);
           	   
               //Check the login credentials
           	   if(reuseLoginValidity(req,resp,validLogCredentials)==true)
           	   {
           		int uid=(int) session.getAttribute("uids");
           		
           		//Issued only Access Tokens for access_type==online
           		AccessTokenModel newAccToken=reuseAccessTokenCode(req,uid, clientId, allScopes);
           		
           	    //issued refresh token along with access tokens for access_type==offline
           		if(req.getParameter("access_type").contains("offline")==true)
           		{
           			reuseRefreshTokenCode(req,uid, clientId,allScopes, newAccToken);
           		}
  			    resp.sendRedirect(redirecturi);
           	   }
           	   else
           	   {
           		     deniedGrantResponse(req, resp);
           	   }
              }
          }
          
           //Participate Oauth Flows in this Fn---->Authorization code flow,Implicit flow
          //If the resource owner denied the request for access protected resource return as error to Client
          public static void deniedGrantResponse(HttpServletRequest req,HttpServletResponse resp) throws IOException
          {
       	   HttpSession session=req.getSession();
       	   String redirecturi=(String) session.getAttribute("redirect_uri");
       	   resp.sendRedirect(redirecturi+"?"+"code="+"error");
          }
          
          //Access Tokens and Refresh Tokens response to Client
          void RedirectUriResp(HttpServletRequest req,HttpServletResponse resp) throws IOException
          {
       		   HttpSession session=req.getSession();
       		   AccessTokenModel accToken=(AccessTokenModel) session.getAttribute("access_tok_obj");
       		   if(session.getAttribute("ref_tok_obj")!=null)
       		   {
       			RefreshTokenModel refToken=(RefreshTokenModel) session.getAttribute("ref_tok_obj");
       		    resp.getWriter().printf("{access_token:%s,refresh_token:%s,tokentype:%s,expires_in_sec:%d}",accToken.getAccessToken(),refToken.getRefreshToken(),"Bearer",3600);
       		   }
       		   else
       			   resp.getWriter().printf("{access_token:%s,tokentype:%s,expires_in_sec:%d}",accToken.getAccessToken(),"Bearer",3600);  
       	   }
          
           // Get user profile details(READ SCOPE) API call
           //Function called needed for individual user profile
          void getUserProfileDetails(HttpServletRequest req,HttpServletResponse resp) throws NumberFormatException, SQLException, ParseException, ClassNotFoundException, IOException
           {
        	  //Validate the access token issued with authAuthorized server
        	   if(AuthenticateAuthorizeDao.ValidateAccessToken(req.getParameter("accesstoken"),Integer.parseInt(req.getParameter("uid")),req.getParameter("scope"))==true)
        	   {
        		   //Made an API call to fetch users info
        		   CreateAccModel usersinfo=ResourceDao.getUsers(Integer.parseInt(req.getParameter("uid")));
        		   resp.getWriter().printf("{name:%s,email:%s,phone:%s}",usersinfo.getName(),usersinfo.getEmail(),usersinfo.getPhone());
        	   }
        	   else
        		   resp.getWriter().print("InValid tokens");
           }
          
        //Revoke the Refresh Token when no longer need to access the data
 
          void RevokeToken(HttpServletRequest req,HttpServletResponse resp) throws ClassNotFoundException, SQLException, IOException
          {
       	   if(AuthenticateAuthorizeDao.DeleteToken(req.getParameter("refreshtoken"),Integer.parseInt(req.getParameter("uid")),req.getParameter("clientid"))==true)
       		   resp.getWriter().print("{success:true}");
       	   else
       		   resp.getWriter().print("{success:false}");
          }
          
    
    //Random String Generator for tokens and client secret and id
    public static String randomStringGenerator()
    {
   	    int lLimit = 97; 
   	    int rLimit = 122; 
   	    int targetStringLength =10;
   	    Random random = new Random();
           String generatedString = random.ints(lLimit, rLimit + 1)
   	      .limit(targetStringLength)
   	      .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
   	      .toString();
           return "mano."+generatedString;
    }
    
    
    //Extract Time used for validate the Access token and Authorization code
    public static String timeGenerator(int timeincrease) 
    {
 		      Calendar cal = Calendar.getInstance();
 		      cal.add(Calendar.MINUTE, timeincrease);
 		      System.out.println("Updated Date = " + cal.getTime());
 		      return cal.getTime().toString();
    }
    
    //Extracts the individual scopes from the list of scopes mentioned in the URI
    public static String scopeExtraction(String scopes,int mode) 
    {
    	//Mode 1--->for users Api, Mode 2 ----> for clients API
    	//Create a key value resource name and resource value 
    	HashMap<String, String>scopeAndMeaning=new HashMap();
    	if(mode==1)
    	{
    		//INDIVIDUAL USERSAPI
    	scopeAndMeaning.put("Manoapi.profile.READ","profile");
    	scopeAndMeaning.put("Manoapi.contacts.READ","contacts");
    	}
    	else
    	{
    		//CLIENTS API
    	scopeAndMeaning.put("Manoapi.allusers.READ","allusers");
    	scopeAndMeaning.put("Manoapi.alluserscontacts.READ","allusersContacts");
    	}
    	
    	//Extracts the individual scopes from the combinedscopes
    	String[] scopeSegregates=scopes.split(",");
    	String segregatedScopes="";
    	for(int i=0;i<scopeSegregates.length;i++)
    	{
    		//Find the resource value for that individual scopes
    		segregatedScopes=segregatedScopes.concat(scopeAndMeaning.get(scopeSegregates[i]));
    		if(i<scopeSegregates.length-1)
 		   {
    			segregatedScopes=segregatedScopes.concat(",");
 		   }
    	}
    	//Returned the combined scope values
    	return segregatedScopes;	
    }
    
    //Many flows use accessToken frequently,Made a reusability functionality for AcessToken Upload
    public static AccessTokenModel reuseAccessTokenCode(HttpServletRequest req,int uid,String clientId,String allScopes) throws ClassNotFoundException, SQLException
    {
    	HttpSession session=req.getSession();
    	AccessTokenModel newAccToken=new AccessTokenModel(uid,clientId,randomStringGenerator(),allScopes,timeGenerator(60));
    	//Save the access tokens
	    AuthenticateAuthorizeDao.saveAccessTokens(newAccToken);
	    
	    //Used for response
	    session.setAttribute("access_tok_obj", newAccToken);
	    return newAccToken;
    }
    //Many flows use accessToken frequently,Made a reusability functionality for AcessToken Upload
    public static void reuseRefreshTokenCode(HttpServletRequest req,int uid,String clientId,String allScopes,AccessTokenModel newAccToken) throws ClassNotFoundException, SQLException
    {
    	HttpSession session=req.getSession();
    	RefreshTokenModel newRefToken=new RefreshTokenModel(uid,-1,clientId,"",allScopes);
    	
    	//Saved the access tokens and refresh tokens
		String refreshToken=AuthenticateAuthorizeDao.saveRefreshToken(newAccToken, newRefToken);
		newRefToken.setRefreshToken(refreshToken);
		session.setAttribute("ref_tok_obj", newRefToken);
    }
}
