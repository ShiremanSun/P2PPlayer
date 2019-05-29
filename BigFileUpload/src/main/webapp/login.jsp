<%@page import="com.itheima.ck.bean.UserDao"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>login</title>
<meta charset="utf-8"/>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
<link rel="stylesheet" href="login.css"/>
</head>
<body>

<% UserDao.getInstance().createTable(); %>
<div id="login_frame">

   <div align="center" style="font-size:20px;margin-bottom:40px">用户登录</div>
	<form action="success.jsp" method="post">
	<p class="input-field"><label class="label_input">用户名</label><input type="text" id="username" class="text_field" name="name"/></p>
    <p class="input-field"><label class="label_input">密码</label><input type="text" id="password" class="text_field" name="password"/></p>
        <div id="login_control">
            <input type="submit" id="btn_login" value="登录" />
            <a class="forget_pwd" href="register.jsp">注册新用户</a>
        </div>

	</form>
</div>


</body>
</html>