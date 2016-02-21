package allthatmusicgear.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

import allthatmusicgear.constants.DBConstants;
import allthatmusicgear.constants.LogAndRegConstants;

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
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Connection conn = null;
		PrintWriter writer = null;
		try {			
			Context context = new InitialContext();
    		BasicDataSource ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
    		conn = ds.getConnection();
    		String uri = request.getRequestURI();
    		
    		/* Log in to server */
    		if (uri.indexOf(LogAndRegConstants.LOGIN) != -1)
     		{
			 PreparedStatement pstmt = null;
			 ResultSet rs = null;
     			try {
     				writer = response.getWriter();
     				pstmt = conn.prepareStatement(LogAndRegConstants.LOGIN_USER);
     				pstmt.setString(1, request.getParameter("userName"));
	    			pstmt.setString(2, request.getParameter("password"));
	    			rs = pstmt.executeQuery();
	    			if (rs.next()){
	    				// if a user matches we update session details
	    				request.getSession().setAttribute("LoggedInUserNickName", rs.getString(1));
		    			request.getSession().setAttribute("LoggedInUserPhotoURL", rs.getString(2));
		    			// and write "success" in the writer
		    			writer.println("{\"success\":\"true\"}");
	    			}
	    			else {
	    				// if no user found then we send the error via Json
	    				writer.println("{\"success\":\"false\", \"errorMsg\":\"Invalid username/password combination\"}");
	    			}
     			} catch (SQLException e) {
     				getServletContext().log("Error On Login", e);
     	    		response.sendError(500);//internal server error
     			} finally {
     				try {
     					if (pstmt != null) {
     						pstmt.close();
     					}
     					if (rs != null) {
     						rs.close();
     					}
     				} catch (Exception e) {
     					e.printStackTrace();
     				}
     			}
     		}
    		  
    		/* Register a new user */
    		else if (uri.indexOf(LogAndRegConstants.REGISTER) != -1)
    		{
    			PreparedStatement checkUserPstmt = null, regUserPstmt = null;
    			ResultSet checkUserRS = null;
    			try {
    				writer = response.getWriter();
    				String userName = request.getParameter("userName");
    				String password = request.getParameter("password");
    				String nickName = request.getParameter("nickName");
    				String desc = request.getParameter("description") == null ? "" : request.getParameter("description");
    				String photo = request.getParameter("phtoUrl") == null ? "media/defaultIcon.png" : request.getParameter("phtoUrl");
    				
     				checkUserPstmt = conn.prepareStatement(LogAndRegConstants.CHECK_EXISTING_USER);
     				checkUserPstmt.setString(1, userName);
	    			checkUserPstmt.setString(2, nickName);
	    			checkUserRS = checkUserPstmt.executeQuery();
	    			if (checkUserRS.next()){
	    				writer.println("{\"success\":\"false\", \"errorMsg\":\"Existing username or nickname\"}");
	 					return;
	    			}
         			   								
    				regUserPstmt = conn.prepareStatement(LogAndRegConstants.REGISTER_USER); 					
	    			regUserPstmt.setString(1, userName);
	    			regUserPstmt.setString(2, password);
	    			regUserPstmt.setString(3, nickName);
	    			regUserPstmt.setString(4, desc);
	    			regUserPstmt.setString(5, photo);
	    			regUserPstmt.setInt(6, 0);
	    			regUserPstmt.executeUpdate();	    			
	    			//commit update
	    			conn.commit();
	    			writer.println("{\"success\":\"true\"}");
	    			request.getSession().setAttribute("LoggedInUserNickName", nickName);
	    			request.getSession().setAttribute("LoggedInUserPhotoURL", photo);

    			} catch (SQLException e) {
    				getServletContext().log("Error while registering new user", e);
    	    		response.sendError(500);//internal server error
    			} finally {
     				try {
     					if (checkUserPstmt != null) {
     						checkUserPstmt.close();
     					}
     					if (checkUserRS != null) {
     						checkUserRS.close();
     					}
     					if (regUserPstmt != null) {
     						regUserPstmt.close();
     					}
     				} catch (Exception e) {
     					e.printStackTrace();
     				}
     			}
    		}
    		   		    		
		} catch (SQLException | NamingException e)
		{
			getServletContext().log("Error while closing connection", e);
    		response.sendError(500);//internal server error
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
				if (writer != null) {
					writer.close();
				}
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 * Simply call doGet
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}


