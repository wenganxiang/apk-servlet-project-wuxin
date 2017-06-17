<%@page import="java.sql.DriverManager"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.Statement"%>
<%@page import="com.wuxin.enroll.DbHelper"%>
<%@page import="java.sql.Connection"%>
<html>
<body>
<h2>Hello World!</h2>
<h3>
<%
Connection conn=(Connection)application.getAttribute(DbHelper.CONN);
Statement stmt=conn.createStatement();
String sql = "SELECT control FROM score_lines";
ResultSet rs = stmt.executeQuery(sql);
rs.first();
out.println("<td>"+rs.getString(1)+"</td>");
%>
</h3>
</body>
</html>
