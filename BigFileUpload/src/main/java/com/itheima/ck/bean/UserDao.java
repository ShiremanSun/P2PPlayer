package com.itheima.ck.bean;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao {
	private Connection connection;
	private UserDao() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			
			e.printStackTrace();
		}
	}
	
	private static class UserHolder {
		public static final UserDao INSTANCE_DAO = new UserDao();
	}
	
	public static UserDao getInstance() {
		return UserHolder.INSTANCE_DAO;
	}
	
	public void createTable() throws SQLException{
		 String creatsql = "CREATE TABLE IF NOT EXISTS user(" + "user_name varchar(100) not null  PRIMARY KEY,"
		           + "password varchar(100)" 
		           + ")charset=utf8";
		 PreparedStatement statement = getConnection().prepareStatement(creatsql);
		 statement.execute();
	}
	public Connection getConnection() throws SQLException{
		if (connection == null) {
			String dbUrl = "jdbc:mysql://localhost:3306/movies?useSSL=false";
			String username = "root";
			String password = "1234";
			connection = DriverManager.getConnection(dbUrl, username, password);
		}
		
		return connection;
	}

	//注册新用户
	public void addUser(UserBean user) throws SQLException {
		String insert = "insert into user(user_name,password) value(?,?)";
		PreparedStatement preparedStatement = getConnection().prepareStatement(insert);
		preparedStatement.setString(1, user.userName);
		preparedStatement.setString(2, user.password);
		preparedStatement.execute();
		preparedStatement.close();
	}
	
	//查询用户名是否存在
	public boolean query(String userName) throws SQLException{
		String query = "select * from user where user_name=?";
		PreparedStatement statement = getConnection().prepareStatement(query);
		statement.setString(1, userName);
		ResultSet resultSet = statement.executeQuery();
		return resultSet.next();
		
	}
	
	public boolean queryPass(String userName, String password) throws SQLException {
		String query = "select password from user where user_name=?";
		PreparedStatement statement = getConnection().prepareStatement(query);
		statement.setString(1, userName);
		ResultSet resultSet = statement.executeQuery();
		while (resultSet.next()) {
			String passwordString = resultSet.getString(1);
			if (password.equals(passwordString)) {
				return true;
			}
		}
		
		return false;
	}
}
