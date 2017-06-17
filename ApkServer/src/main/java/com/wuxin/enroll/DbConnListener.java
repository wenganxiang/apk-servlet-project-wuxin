package com.wuxin.enroll;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author ForeverApp 监听 应用启动 ，获取数据库连接
 */
public class DbConnListener implements ServletContextListener {
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		try {
			ServletContext application = sce.getServletContext();
			String driver = application.getInitParameter("mysqlDriver");
			String url = application.getInitParameter("wuxinUrl");
			String user = application.getInitParameter("user");
			String pass = application.getInitParameter("pass");
			Class.forName(driver);// register driver
			Connection conn = DriverManager.getConnection(url, user, pass);
			application.setAttribute(DbHelper.CONN, conn);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("DbConnListener:" + e.toString());
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		ServletContext application = sce.getServletContext();
		Connection conn = (Connection) application.getAttribute(DbHelper.CONN);
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

}
