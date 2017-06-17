package com.wuxin.enroll;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbHelper {

	public final static String CONN = "wuxin_db_conn";
	public final static String DB_OPERATE = "db_operate";

	private Connection conn;

	public DbHelper(Connection conn) {
		this.conn = conn;
	}

	public Connection getConnection() throws Exception {
		return conn;
	}

	public boolean insert(String sql, Object... args) throws Exception {
		PreparedStatement pstmt = getConnection().prepareStatement(sql);
		for (int i = 0; i < args.length; i++)
			pstmt.setObject(i + 1, args[i]);
		int count = pstmt.executeUpdate();
		pstmt.close();
		return count < 1 ? false : true;
	}

	public boolean create(String sql, Object... args) throws Exception {
		PreparedStatement pstmt = getConnection().prepareStatement(sql);
		for (int i = 0; i < args.length; i++)
			pstmt.setObject(i + 1, args[i]);
		int count = pstmt.executeUpdate();
		pstmt.close();
		return count != 1 ? false : true;
	}

	public ResultSet query(String sql, Object... args) throws Exception {
		PreparedStatement pstmt = getConnection().prepareStatement(sql);
		for (int i = 0; i < args.length; i++)
			pstmt.setObject(i + 1, args[i]);
		return pstmt.executeQuery();
	}

	public ResultSet query(String sql) throws Exception {
		Statement stmt = getConnection().createStatement();
		return stmt.executeQuery(sql);
	}
	
	public void modify(String sql, Object... args) throws Exception {
		PreparedStatement pstmt = getConnection().prepareStatement(sql);
		for (int i = 0; i < args.length; i++)
			pstmt.setObject(i + 1, args[i]);
		pstmt.executeUpdate();
		pstmt.close();
	}

	public void delete(String sql, Object... args) throws Exception {
		PreparedStatement pstmt = getConnection().prepareStatement(sql);
		for (int i = 0; i < args.length; i++)
			pstmt.setObject(i + 1, args[i]);
		pstmt.execute();
		pstmt.close();
	}
	
	public void closeConn() throws Exception {
		if (conn != null && !conn.isClosed())
			conn.close();
	}

}
