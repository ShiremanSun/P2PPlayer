<%@page import="com.itheima.ck.bean.UserDao"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title></title>
</head>
<body>
<%
	String name = request.getParameter("name").trim();
	String password = request.getParameter("password").trim();
	if(UserDao.getInstance().query(name)) {
		//用户名存在
		if(UserDao.getInstance().queryPass(name, password)) {
			Cookie newCookie = new Cookie("userName", name);
			response.addCookie(newCookie);
			response.sendRedirect("index.jsp");
		} else {
			out.print("密码输入错误！！！<br>"+"重新<a href=\"login.jsp\">登录</a>");
		}
	} else {
		out.print("<font color=red>"+name+"</font>用户不存在！！！<br>"+"请点击<a href=\"register.jsp\">注册</a>");
	}

%>
</body>
</html>