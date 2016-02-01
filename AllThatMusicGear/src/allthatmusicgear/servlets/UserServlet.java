package allthatmusicgear.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import allthatmusicgear.constants.UserConstants;
import allthatmusicgear.model.Question;


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
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		try {			
			Context context = new InitialContext();
    		BasicDataSource ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
    		Connection conn = ds.getConnection();
    		String uri = request.getRequestURI();
    		
    		/* First Case - It deals with Updating User Ration */
    		if (uri.indexOf(UserConstants.UPDATE_UR) != -1)
    		{
				try {
					PreparedStatement pstmt;
					String strUserName =  request.getParameter("userNickName");
    				pstmt = conn.prepareStatement(UserConstants.GET_USER_AVG_Q_SCORES); 
    				pstmt.setString(1, strUserName);
    				double avgQRating = 0;
    				ResultSet rs = pstmt.executeQuery();
					while (rs.next())
					{
						avgQRating = rs.getObject(1) == null ? 0 : rs.getDouble(1);
						
					}
					rs.close();
					pstmt.close();
					
					PreparedStatement pstmt2;
					pstmt2 = conn.prepareStatement(UserConstants.GET_USER_AVG_A_SCORES); 
    				pstmt2.setString(1, strUserName);
    				double avgARating = 0;
    				ResultSet rs2 = pstmt2.executeQuery();
					while (rs2.next())
					{
						avgARating = rs2.getObject(1) == null ? 0 : rs2.getDouble(1);
						
					}
					rs2.close();
					pstmt2.close();
					
					double newRatingScore = 0.2 * avgQRating + 0.8 * avgARating;
					PreparedStatement updateUserPstmt;
					updateUserPstmt = conn.prepareStatement(UserConstants.UPDATE_UR_QUERY);
					updateUserPstmt.setDouble(1, newRatingScore);
					updateUserPstmt.setString(2, strUserName);
					updateUserPstmt.executeUpdate();
					conn.commit();
					updateUserPstmt.close();
				} catch (SQLException e) {
					getServletContext().log("Error while updating User Rating", e);
					response.sendError(500);//internal server error
				}		
    		}
    		else if (uri.indexOf(UserConstants.USER_EXPERTISE) != -1)
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
					conn.close();
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
