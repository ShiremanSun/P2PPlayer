package com.itheima.ck.web;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.itheima.ck.bean.MovieBean;
import com.itheima.ck.bean.MovieDao;

import net.sf.json.JSONArray;



/**
 * Servlet implementation class SearchServlet
 */
@WebServlet("/SearchServlet")
public class SearchServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SearchServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		String paramString = request.getParameter("movie_name");
		
		List<MovieBean> list = new ArrayList<MovieBean>();
		try {
			list = MovieDao.getInstance().query(paramString);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		
		for (MovieBean movieBean : list) {
			System.out.println(movieBean.name);
			System.out.println(movieBean.datasourcePath);
			System.out.println(movieBean.details);
		}
		JSONArray jsonArray = JSONArray.fromObject(list.get(0));
		System.out.println(jsonArray+"---" + list.size());
		PrintWriter writer = response.getWriter();
		writer.write(jsonArray.toString());
		writer.close();
		
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stubsu
		doGet(request, response);
	}

}
