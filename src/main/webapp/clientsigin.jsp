<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Client Sign In</title>
</head>
<body>
<center>
<a href="developerform.jsp"><button>Developer Console</button><br><br></a>
<a href="msaccount/accounts?client_id=mano.dutkieszbh&scope=Manoapi.profile.READ&redirect_uri=http://localhost:8080/OauthServers/msaccount/response&access_type=offline&response_type=code"><input type="submit" value="Sign in with Mano"></a>
</center>
</body>
</html>

<!-- 
For Authorization code flow parameters = clientid,scope,redirecteduri,response_type(mandatory)
                                         access_type=online/offline,prompt=consent(optional)
                                         
For Implicit Flow parameters =  clientid,scope,redirecteduri,response_type(mandatory)
 -->