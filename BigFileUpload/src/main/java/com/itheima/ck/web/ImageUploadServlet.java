package com.itheima.ck.web;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itheima.ck.bean.FileUploadBean;
import com.itheima.ck.bean.MovieBean;
import com.itheima.ck.bean.MovieDao;
import com.itheima.ck.utils.FileUtils;

/**
 * Servlet implementation class ImageUploadServlet
 */
@WebServlet("/ImageUploadServlet")
public class ImageUploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private Logger logger = LoggerFactory.getLogger(UploadController.class);
	
	private static String finalDirPath = "/var/www/html/images/";
	private static String ipAddressString = "http://192.168.43.68/images/";
       
	private String movieName;
	private String movieDetails;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ImageUploadServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	    
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//拿到图片
		boolean multipartContent = ServletFileUpload.isMultipartContent(request);
		if (multipartContent) {

            // 创建工厂（这里用的是工厂模式）
            DiskFileItemFactory factory = new DiskFileItemFactory();
            //获取汽车零件清单与组装说明书（从ServletContext中得到上传来的数据）
            ServletContext servletContext = this.getServletConfig().getServletContext();
            // 临时文件目录
            File repository = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
            //工厂把将要组装的汽车的参数录入工厂自己的系统，因为要根据这些参数开设一条生产线（上传来的文件的各种属性）
            factory.setRepository(repository);
            //此时工厂中已经有了汽车的组装工艺、颜色等属性参数（上传来的文件的大小、文件名等）
            //执行下面的这一行代码意味着根据组装工艺等开设了一条组装生产线
            ServletFileUpload upload = new ServletFileUpload(factory);
            List<FileItem> list ;
           
            try {
				list = upload.parseRequest(request);
				//拿到movie的标题和描述
                for (FileItem item : list) {
					if ("moviename".equals(item.getFieldName())) {
						movieName = item.getString("utf-8");
					}
					if ("details".equals(item.getFieldName())) {
						movieDetails = item.getString("utf-8");
					}
				}
				
			} catch (FileUploadException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
            
            FileUploadBean bean = new FileUploadBean(list, logger);
            String filenameString = bean.getName();
            String dirString = finalDirPath + filenameString;
            Path path = Paths.get(dirString);
            FileUtils.authorizationAll(path);
            byte[] fileData = FileUtils.readInputStream(bean.getFile(), 2048);
            try {
            	Files.write(path, fileData, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            }catch (Exception e) {
				e.printStackTrace();
			}finally {
				//不管图片存不存在都插入
				try {
					if (MovieDao.getInstance().getConnection() != null) {
						//连接成功
						String ipString ="";
						try {
							ipString = UploadController.getLocalHostLANAddress().getHostAddress();
						} catch (Exception e) {
							e.printStackTrace();
						}
						
						
						if (!MovieDao.getInstance().ifExsts(movieName)) {
							//插入表
							System.out.println("图片插入表");
							MovieBean movieBean = new MovieBean();
							movieBean.name = movieName;
							movieBean.details = "";
							movieBean.datasourcePath = "";
							movieBean.imagePathString = "http://" + ipString +"/images/"+filenameString;
							movieBean.torrentpathString = "";
							MovieDao.getInstance().addMovie(movieBean);
						}else {
							//更新表格
							String sqlString = "update movie set imagePathString=? where name=?";
							PreparedStatement preparedStatement = MovieDao.getInstance().getConnection().prepareStatement(sqlString);
							preparedStatement.setString(1, "http://" + ipString +"/images/" + filenameString);
							preparedStatement.setString(2, movieName);
							preparedStatement.executeUpdate();
							preparedStatement.close();
						}
						
					}else {
						System.out.println("连接失败");
					}
					
				} catch (SQLException e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
            //插入数据库
           
		}
	}

}
