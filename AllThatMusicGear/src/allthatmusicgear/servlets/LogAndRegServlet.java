package allthatmusicgear.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

import com.google.gson.Gson;

import allthatmusicgear.constants.DBConstants;
import allthatmusicgear.constants.LogAndRegConstants;
import allthatmusicgear.model.User;

/**
 * Servlet implementation class LogAndRegServlet
 */
public class LogAndRegServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LogAndRegServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {			
			Context context = new InitialContext();
    		BasicDataSource ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
    		Connection conn = ds.getConnection();
    		String uri = request.getRequestURI();
    		Collection<User> userCollection = new ArrayList<User>();
    		
    		/* First Case - Fetch all Users and passwords to match later */
    		if (uri.indexOf(LogAndRegConstants.ALL_USERS) != -1)
    		{
    			Statement stmt;
    			try {
    				stmt = conn.createStatement();
					ResultSet rs = stmt.executeQuery(LogAndRegConstants.GET_ALL_USERS);
					while (rs.next())
					{
						String desc = rs.getObject(4) == null ? "" : rs.getString(4);
						String photo = rs.getObject(5) == null ? "" : rs.getString(5);
						userCollection.add(new User(rs.getString(1), rs.getString(2), rs.getString(3), desc, photo, rs.getDouble(6)));
					}
					rs.close();
					stmt.close();
    			} catch (SQLException e) {
    				getServletContext().log("Error while querying for all users", e);
    	    		response.sendError(500);//internal server error
    			}
    		}
    		
    		else if (uri.indexOf(LogAndRegConstants.REGISTER) != -1)
    		{
    			PreparedStatement pstmt;		
    			try {
    				
    				pstmt = conn.prepareStatement(LogAndRegConstants.REGISTER_USER); 
    				
    				String desc = request.getParameter("description") == null ? "" : request.getParameter("description");
					String photo = request.getParameter("phtoUrl") == null ? "" : request.getParameter("phtoUrl");
    				
	    			pstmt.setString(1, request.getParameter("userName"));
	    			pstmt.setString(2, request.getParameter("password"));
	    			pstmt.setString(3, request.getParameter("nickName"));
	    			pstmt.setString(4, desc);
	    			pstmt.setString(5, photo);
	    			pstmt.setInt(6, 0);
	    			pstmt.executeUpdate();
	    			
	    			//commit update
	    			conn.commit();
	    			//close statements
	    			pstmt.close();
	    			
	    			
	    			User createdUser = new User(request.getParameter("userName"), 
						    					request.getParameter("password"),
						    					request.getParameter("nickName"),
						    					request.getParameter("descreption"),
						    					request.getParameter("phtoUrl"),
						    					0);
	    			userCollection.add(createdUser);

    			} catch (SQLException e) {
    				getServletContext().log("Error while querying for all users", e);
    	    		response.sendError(500);//internal server error
    			}
    		}
    		
    		
    		conn.close();
    		Gson gson = new Gson();
    		String userJsonRes = gson.toJson(userCollection, LogAndRegConstants.USER_COLLECTION);
    		
    		PrintWriter writer = response.getWriter();
    		writer.println(userJsonRes);
    		writer.close();
    		
		} catch (SQLException | NamingException e)
		{
			getServletContext().log("Error while closing connection", e);
    		response.sendError(500);//internal server error
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
