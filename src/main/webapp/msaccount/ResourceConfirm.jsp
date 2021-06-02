<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Insert title here</title>
</head>
<body>
<center>
<h1>Confirmation from the Resource Owner</h1>
<h3>Do you want to access the following resources</h3>
<% 
     String scope=(String)session.getAttribute("scopename");
      out.print("Client going to access the ");
      out.print(scope+"\n");
%>
<br><br><br>
<a href="codeortoksent"><input type="submit" value="Allow"></a><br><br>
<a href="errorcode"><input type="submit" value="Deny"></a>
</center>
</body>
</html>