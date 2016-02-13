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
import allthatmusicgear.constants.LogAndRegConstants;
import allthatmusicgear.constants.QAndAConstants;
import allthatmusicgear.constants.UserConstants;
import allthatmusicgear.model.Question;
import allthatmusicgear.model.User;


/**
 * Servlet implementation class UserServlet
 */
public class UserServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UserServlet() {
        super();
        // TODO Auto-generated constructor stub
    }
    /**
     * A service method for getting user expertise
     * @param nickName - The User
     * @param conn - the current connection
     * @return
     * @throws SQLException - will throw the exception
     */
    private List<String> getUserExp(String nickName, Connection conn) throws SQLException
    {
    	List<String> resList = new ArrayList<String>();
    	PreparedStatement pstmt;
    	pstmt = conn.prepareStatement(UserConstants.GET_USER_EXPERTISE);
    	pstmt.setString(1, nickName);
    	ResultSet rs = pstmt.executeQuery();
    	while (rs.next()){
    		resList.add(rs.getString(1));
    	}  	
    	return resList;
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
    		
    		/* First Case - It deals with Updating User Ration */
    		if (uri.indexOf(UserConstants.USER_EXPERTISE) != -1)
    		{
    			List<String> topicList = new ArrayList<String>();
    			try {
					PreparedStatement pstmt;
					String strUserName =  request.getParameter("userNickName");
    				pstmt = conn.prepareStatement(UserConstants.GET_USER_EXPERTISE); 
    				pstmt.setString(1, strUserName);
    				ResultSet rs = pstmt.executeQuery();
					while (rs.next())
					{
						topicList.add(rs.getString(1));
						
					}
					rs.close();
					pstmt.close();
		    		Gson gson = new Gson();
		    		String userJsonRes = gson.toJson(topicList, UserConstants.TOPIC_LIST);
		    		
		    		PrintWriter writer = response.getWriter();
		    		writer.println(userJsonRes);
		    		writer.close();					
				} catch (SQLException e) {
					getServletContext().log("Error while fetching User topics", e);
					response.sendError(500);//internal server error
				}
    		}
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
    		else if (uri.indexOf(UserConstants.LOGOUT) != -1) {
    			try {
    				request.getSession().invalidate();
    			}
    			catch (Exception e){
    				getServletContext().log("Error while logging out", e);
					response.sendError(500);//internal server error
    			}
    		}
    		
    		else if (uri.indexOf(UserConstants.GET_USER_INFO) != -1) {
    			try {
	    			String nickName = request.getParameter("userNickName");
	    			PreparedStatement pstmt;
    				pstmt = conn.prepareStatement(UserConstants.GET_USER_INFO_QUERY); 
    				pstmt.setString(1, nickName);
    				ResultSet rs = pstmt.executeQuery();
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
    				rs.close();
    				pstmt.close();
    				
    	    		Gson gson = new Gson();
    	    		String JsonRes = gson.toJson(user);
    	    		PrintWriter writer = response.getWriter();
    	    		writer.println(JsonRes);
    	    		writer.close();
    			}
    			catch (Exception e){
    				getServletContext().log("Error while fetching user info", e);
					response.sendError(500);//internal server error
    			}
    		}
    		else if (uri.indexOf(UserConstants.GET_ALL_USERS_INFO) != -1) {
    			try {
    				Statement stmt = conn.createStatement();
					ResultSet allUsersRS = stmt.executeQuery(UserConstants.GET_ALL_USERS_INFO_QUERY);
					Collection<User> userCollection = new ArrayList<User>();
					while (allUsersRS.next())
					{
						List<String> expertise = getUserExp(allUsersRS.getString(1), conn);
						userCollection.add(new User(allUsersRS.getString(1), 
						    						allUsersRS.getString(2),
						    						allUsersRS.getString(3),
						    						allUsersRS.getDouble(4),
						    						expertise));
					}
					allUsersRS.close();
					stmt.close();
    				
    	    		Gson gson = new Gson();
    	    		String JsonRes = gson.toJson(userCollection, UserConstants.USER_COLLECTION);  
    	    		PrintWriter writer = response.getWriter();
    	    		writer.println(JsonRes);
    	    		writer.close();
    			}
    			catch (Exception e){
    				getServletContext().log("Error while fetching all user info", e);
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
