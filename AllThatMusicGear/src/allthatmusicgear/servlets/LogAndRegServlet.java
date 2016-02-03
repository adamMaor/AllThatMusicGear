package allthatmusicgear.servlets;

import java.io.IOException;
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
    		
    		/* First Case - Fetch all Users and passwords to match later */
    		if (uri.indexOf(LogAndRegConstants.LOGIN) != -1)
     		{
			 PreparedStatement pstmt;
     			try {
     				pstmt = conn.prepareStatement(LogAndRegConstants.LOGIN_USER);
     				pstmt.setString(1, request.getParameter("userName"));
	    			pstmt.setString(2, request.getParameter("password"));
	    			ResultSet rs = pstmt.executeQuery();
	    			if (rs.next()){
	    				request.getSession().setAttribute("LoggedInUserNickName", rs.getString(1));
		    			request.getSession().setAttribute("LoggedInUserPhotoURL", rs.getString(2));
	    			}
	    			else {
	    				response.getWriter().println("Invalid username/password combination");
	    			}
 					rs.close();
 					pstmt.close();
     			} catch (SQLException e) {
     				getServletContext().log("Error On Login", e);
     	    		response.sendError(500);//internal server error
     			}
     		}
    		   		
    		else if (uri.indexOf(LogAndRegConstants.REGISTER) != -1)
    		{
    			PreparedStatement pstmt, pstmt2;		
    			try {
    				String userName = request.getParameter("userName");
    				String password = request.getParameter("password");
    				String nickName = request.getParameter("nickName");
    				String desc = request.getParameter("description") == null ? "" : request.getParameter("description");
    				String photo = request.getParameter("phtoUrl") == null ? "" : request.getParameter("phtoUrl");
    				
     				pstmt = conn.prepareStatement(LogAndRegConstants.CHECK_EXISTING_USER);
     				pstmt.setString(1, request.getParameter("userName"));
	    			pstmt.setString(2, request.getParameter("nickName"));
	    			ResultSet rs = pstmt.executeQuery();
	    			if (rs.next()){
	    				response.getWriter().println("Existing username or nickname");
	    				rs.close();
	 					pstmt.close();
	 					return;
	    			}

 					rs.close();
 					pstmt.close();
         			   								
    				pstmt2 = conn.prepareStatement(LogAndRegConstants.REGISTER_USER); 					
	    			pstmt2.setString(1, userName);
	    			pstmt2.setString(2, password);
	    			pstmt2.setString(3, nickName);
	    			pstmt2.setString(4, desc);
	    			pstmt2.setString(5, photo);
	    			pstmt2.setInt(6, 0);
	    			pstmt2.executeUpdate();	    			
	    			//commit update
	    			conn.commit();
	    			//close statements
	    			pstmt2.close();    			
	    			request.getSession().setAttribute("LoggedInUserNickName", nickName);
	    			request.getSession().setAttribute("LoggedInUserPhotoURL", photo);

    			} catch (SQLException e) {
    				getServletContext().log("Error while registering new user", e);
    	    		response.sendError(500);//internal server error
    			}
    		}
    		   		
    		conn.close();
    		
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


