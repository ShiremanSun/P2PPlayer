<%@page import="com.itheima.ck.bean.UserBean"%>
<%@page import="com.itheima.ck.bean.UserDao"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>注册成功</title>
</head>
<body>
<%
String name = new String(request.getParameter("name").getBytes("iso-8859-1"), "utf-8").trim();
String password=new String(request.getParameter("password").getBytes("iso-8859-1"), "utf-8").trim();
String refill=new String(request.getParameter("refill").getBytes("iso-8859-1"), "utf-8").trim();
if(name!=null && name.length()>0) {
	if(UserDao.getInstance().query(name)){
		out.print("用户已经存在  "+"请<a href=\"register.jsp\">注册</a>");
	}else {
		if(password!=null) {
			if(!password.equals(refill)) {
				out.print("密码输入不一致!!!<br>"+"重新<a href=\"register.jsp\">注册</a>");
			}else {
				UserBean user = new UserBean();
				user.userName = name;
				user.password = password;
				UserDao.getInstance().addUser(user);
				%>
				注册成功！！！<br>
				<a href="login.jsp">点击登录</a>！！！
				<% 	
			}
		}else {
			out.print("密码不能为空");
		}
	}
}else {
	out.print("用户名不能为空");
}
%>

</body>
</html>