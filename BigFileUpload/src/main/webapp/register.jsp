<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<link rel="stylesheet" href="login.css"/>
<title>register</title>
</head>
<body>
	<div id="login_frame">
	<form action="registerSuccess.jsp" method="post">
		<div align="center" style="font-size:20px;margin-bottom:40px">用户注册</div>
		<p class="input-field"><label class="label_input">用户名</label><input type="text" name=name autofocus="autofocus" class="text_field"></p>
		<p class="input-field"><label class="label_input">密码</label><input type="text" name=password autofocus="autofocus" class="text_field"></p>
		<p class="input-field"><label class="label_input">确认密码</label><input type="text" name=refill autofocus="autofocus" class="text_field"></p>
		 <div id="login_control">
            <input type="submit" id="btn_login" value="注册" name=register/>
            <input type="reset" id="btn_login"  value="重填" name=refill style="position: relative;
    float: right;">
        </div>
		<!-- <table align="center">
			<tr align="right">
		 		<td>请输入用户名:</td>
				<td><input type="text" name=name autofocus="autofocus"></td>
			</tr>
			<tr align="right">
				<td>请输入密码:</td>
				<td><input type="text" name=password></td>
			</tr>
			<tr align="right">
				<td>请输入确认密码:</td>
				<td><input type="text" name=refill></td>
			</tr>
		</table>
			<input type="submit" name=register value="注册" >
			<input type="reset" name=refill value="重填" > -->
	</form>
	</div>
	
	 
		
</body>
</html>