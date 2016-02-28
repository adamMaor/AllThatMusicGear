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
import java.util.List;

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
import allthatmusicgear.constants.UserConstants;
import allthatmusicgear.model.User;


/**
 * Servlet implementation class UserServlet<br>
 * This servlet is responsible for all interaction involed with User details and attributes
 */
public class UserServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UserServlet() {
        super();
    }
    /**
     * A Utility method for getting user expertise
     * @param nickName - The User
     * @param conn - the current connection
     * @return - a List of size 5 with user top Topics.
     * @throws SQLException - will throw the exception
     */
    private List<String> getUserExp(String nickName, Connection conn) throws SQLException
    {
    	PreparedStatement pstmt = null;
    	ResultSet rs = null;
    	try {
    		List<String> resList = new ArrayList<String>();
        	pstmt = conn.prepareStatement(UserConstants.GET_USER_EXPERTISE);
        	pstmt.setString(1, nickName);
        	rs = pstmt.executeQuery();
        	while (rs.next()){
        		resList.add(rs.getString(1));
        	}  	
        	return resList;
    	} catch (SQLException e) {
    		throw e;
    	} finally {
    		if (pstmt != null){    			
    			pstmt.close();
    		}
    		if (rs != null){    			
    			rs.close();
    		}
    	}
    }  
    
    /**
     * This Utility method is used to count all the users in the system
     * @param conn - the current connection
     * @return int - the number of total users in the system
     * @throws SQLException
     */
    private int countUsers(Connection conn) throws SQLException
    {
    	Statement stmt = null;
    	ResultSet rs = null;
    	try {
    		int count = 0;
    		stmt = conn.createStatement();
    		rs = stmt.executeQuery(UserConstants.COUNT_ALL_USERS);
    		if (rs.next()){
    			count = rs.getInt(1);
    		}
    		return count;    		
    	} catch (SQLException e) {
    		throw e;
    	} finally {
    		if (stmt != null){    			
    			stmt.close();
    		}
    		if (rs != null){    			
    			rs.close();
    		}
    	}
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
    		Gson gson = new Gson();
    		String JsonRes = ""; 
    		String uri = request.getRequestURI();
    		
    		/* Get user's expertise */
    		if (uri.indexOf(UserConstants.USER_EXPERTISE) != -1)
    		{
    			PreparedStatement pstmt = null;
    			ResultSet rs = null;
    			List<String> topicList = new ArrayList<String>();
    			try {
					String strUserName =  request.getParameter("userNickName");
    				pstmt = conn.prepareStatement(UserConstants.GET_USER_EXPERTISE); 
    				pstmt.setString(1, strUserName);
    				rs = pstmt.executeQuery();
					while (rs.next())
					{
						topicList.add(rs.getString(1));	
					}
		    		// put in Json
					JsonRes = gson.toJson(topicList, UserConstants.TOPIC_LIST);
			
				} catch (SQLException e) {
					getServletContext().log("Error while fetching User topics", e);
					response.sendError(500);//internal server error
				} finally {
					try {
						if (rs != null){
							rs.close();
						}
						if (pstmt != null){
							pstmt.close();
						}
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
    		}
    		/* Get session info - logged user nickname and photo */
    		else if (uri.indexOf(UserConstants.GET_SESSION_INFO) != -1) {
    			try {
	    			String nickName = (String) request.getSession().getAttribute("LoggedInUserNickName");
	    			String photoURL = (String) request.getSession().getAttribute("LoggedInUserPhotoURL");
	    			response.getWriter().println("{\"nickName\":\"" + nickName + "\", \"photoURL\":\"" + photoURL + "\"}" );
    			}
    			catch (Exception e){
    				getServletContext().log("Error while fetching user session info", e);
					response.sendError(500);//internal server error
    			}
    		}
    		
    		/* LogOut - simply invalidates the session */
    		else if (uri.indexOf(UserConstants.LOGOUT) != -1) {
    			try {
    				request.getSession().invalidate();
    			}
    			catch (Exception e){
    				getServletContext().log("Error while logging out", e);
					response.sendError(500);//internal server error
    			}
    		}
    		
    		/* get user info (model) by nickname */
    		else if (uri.indexOf(UserConstants.GET_USER_INFO) != -1) {
    			PreparedStatement pstmt = null;
    			ResultSet rs = null;
    			try {
	    			String nickName = request.getParameter("userNickName");
    				pstmt = conn.prepareStatement(UserConstants.GET_USER_INFO_QUERY); 
    				pstmt.setString(1, nickName);
    				rs = pstmt.executeQuery();
    				User user = null ;
    				if (rs.next()){
    					List<String> expertise = getUserExp(rs.getString(1), conn);
    					user = new User(rs.getString(1), 
							rs.getString(2),
							rs.getString(3),
							rs.getDouble(4),
							expertise
							);				
    				}   				
    	    		JsonRes = gson.toJson(user);		
    			}
    			catch (Exception e){
    				getServletContext().log("Error while fetching user info", e);
					response.sendError(500);//internal server error
    			} finally {
					try {
						if (rs != null){
							rs.close();
						}
						if (pstmt != null){
							pstmt.close();
						}
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
    		}
    		
    		/* get all users info - 20 each time */
    		else if (uri.indexOf(UserConstants.GET_ALL_USERS_INFO) != -1) {
    			PreparedStatement pstmt = null;
    			ResultSet allUsersRS = null;
    			try {
    				Integer allUsersCount = countUsers(conn);
    				// offset by page number
    				int offset = 20 * (Integer.parseInt(request.getParameter("pageNum")) -1);
    				Collection<User> userCollection = new ArrayList<User>();

    				pstmt = conn.prepareStatement(UserConstants.GET_ALL_USERS_INFO_QUERY); 
    				pstmt.setInt(1, offset);
					allUsersRS = pstmt.executeQuery();
					while (allUsersRS.next())
					{
						List<String> expertise = getUserExp(allUsersRS.getString(1), conn);
						userCollection.add(new User(allUsersRS.getString(1), 
						    						allUsersRS.getString(2),
						    						allUsersRS.getString(3),
						    						allUsersRS.getDouble(4),
						    						expertise));
					}
					// put in Json
    	    		JsonRes = "{\"numUsers\":" + allUsersCount.toString() + ", \"users\":";
					JsonRes += gson.toJson(userCollection, UserConstants.USER_COLLECTION);
					JsonRes += "}";
    			}
    			catch (Exception e){
    				getServletContext().log("Error while fetching all user info", e);
					response.sendError(500);//internal server error
    			} finally {
					try {
						if (allUsersRS != null){
							allUsersRS.close();
						}
						if (pstmt != null){
							pstmt.close();
						}
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
    		}
    		
    		if (!JsonRes.isEmpty()) {
    			writer = response.getWriter();
    			writer.println(JsonRes);    			
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
