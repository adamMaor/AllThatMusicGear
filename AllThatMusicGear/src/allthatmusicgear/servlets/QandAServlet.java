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
import allthatmusicgear.constants.QAndAConstants;
import allthatmusicgear.constants.UserConstants;
import allthatmusicgear.model.Answer;
import allthatmusicgear.model.Question;
import allthatmusicgear.model.QuestionAnswerPair;
import allthatmusicgear.model.TopicQRatingPair;

/**
 * Servlet implementation class QandAServlet
 */
public class QandAServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public QandAServlet() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    private boolean userAlreadyVoted(SQLException e) {
        boolean voted;
        if(e.getSQLState().equals("23505")) {
        	voted = true;
        } else {
        	voted = false;
        }
        return voted;
    }
    	
	private List<String> getQuestionTopics(int qID, Connection conn) throws SQLException{
		List<String> qTopicList = new ArrayList<String>();
		PreparedStatement pstmt;
		pstmt = conn.prepareStatement(QAndAConstants.GET_QUESTION_TOPICS); 
		pstmt.setInt(1, qID);
		ResultSet TopicRS = pstmt.executeQuery();
		while (TopicRS.next())
		{
			qTopicList.add(TopicRS.getString(1));
		}
		TopicRS.close();
		pstmt.close();
		return qTopicList;
	}
	
	private void updateUserRating(String userNickName, Connection conn) throws SQLException
    {
    	PreparedStatement pstmt;
		pstmt = conn.prepareStatement(UserConstants.GET_USER_AVG_Q_SCORES); 
		pstmt.setString(1, userNickName);
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
		pstmt2.setString(1, userNickName);
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
		updateUserPstmt.setString(2, userNickName);
		updateUserPstmt.executeUpdate();
		conn.commit();
		updateUserPstmt.close();
    }
	
	private String getUserNickNameFromQid(int qID, Connection conn) throws SQLException
	{
		String userNickName = null;
		PreparedStatement pstmt;
		pstmt = conn.prepareStatement(UserConstants.GET_USER_NICK_FROM_QUESTION);
		pstmt.setInt(1, qID);
		ResultSet rs = pstmt.executeQuery();
		if (rs.next()){
			userNickName = rs.getString(1);
		}
		return userNickName;
	}
	
	private String getUserNickNameFromAid(int aID, Connection conn) throws SQLException
	{
		String userNickName = null;
		PreparedStatement pstmt;
		pstmt = conn.prepareStatement(UserConstants.GET_USER_NICK_FROM_ANSWER);
		pstmt.setInt(1, aID);
		ResultSet rs = pstmt.executeQuery();
		if (rs.next()){
			userNickName = rs.getString(1);
		}
		return userNickName;
	}
	
	private void updateQuestionScores(int qID, int votingScoreChange, Connection conn) throws SQLException
	{
		PreparedStatement pstmt;
		pstmt = conn.prepareStatement(QAndAConstants.GET_QUESTION_SCORES); 
		pstmt.setInt(1, qID);
		ResultSet QuestionRS = pstmt.executeQuery();
		
		while (QuestionRS.next())
		{
			int votingScore = QuestionRS.getInt(1);
			votingScore += votingScoreChange;
			double answersAVGScore = QuestionRS.getObject(2) == null ? 0 : QuestionRS.getDouble(2);
			PreparedStatement updatePstmt;
			updatePstmt = conn.prepareStatement(QAndAConstants.UPDATE_QUESTION_SCORES);
			updatePstmt.setInt(1, votingScore);
			updatePstmt.setDouble(2, answersAVGScore*0.8 + votingScore*0.2);
			updatePstmt.setInt(3, qID);
			updatePstmt.executeUpdate();
			updatePstmt.close();
		}
		conn.commit();
		QuestionRS.close();   					
		//close statements
		pstmt.close();
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
    		Gson gson = new Gson();
    		String JsonRes = null;  		
    		
    		/* First Big Case - It deals with Questions */
    		if (uri.indexOf(QAndAConstants.QUESTION) != -1)
    		{
    			/* First Question case - get New submitted questions */
    			if (uri.indexOf(QAndAConstants.NEW_QUESTIONS) != -1)
    			{
    				Statement stmt;
    				try {
    					stmt = conn.createStatement();
    					ResultSet QuestionRS = stmt.executeQuery(QAndAConstants.GET_NEW_QUESTIONS);
    					Collection<Question> questCollection = new ArrayList<Question>();
    					while (QuestionRS.next())
    					{
    						int qID = QuestionRS.getInt(1);
    						List<String> qTopicList = getQuestionTopics(qID, conn);  						    						
    						questCollection.add(new Question(QuestionRS.getInt(1), 
						    								QuestionRS.getString(2), 
						    								QuestionRS.getString(3), 
						    								QuestionRS.getTimestamp(4).getTime(), 
						    								QuestionRS.getInt(5), 
						    								QuestionRS.getDouble(6), 
						    								qTopicList));
    					}
    					QuestionRS.close();
    					JsonRes = gson.toJson(questCollection, QAndAConstants.QUESTION_COLLECTION); 
    					stmt.close();
    				} catch (SQLException e) {
    					getServletContext().log("Error while querying for New Questions", e);
    					response.sendError(500);//internal server error
    				}				
    			}
    			
    			else if (uri.indexOf(QAndAConstants.ALL_QUESTIONS) != -1)
    			{
    				Statement stmt;
    				try {
    					stmt = conn.createStatement();
    					ResultSet QuestionRS = stmt.executeQuery(QAndAConstants.GET_ALL_QUESTIONS);
    					Collection<Question> questCollection = new ArrayList<Question>();

    					while (QuestionRS.next())
    					{
    						int qID = QuestionRS.getInt(1);
    						List<String> qTopicList = getQuestionTopics(qID, conn);  						    						
    						questCollection.add(new Question(QuestionRS.getInt(1), 
						    								QuestionRS.getString(2), 
						    								QuestionRS.getString(3), 
						    								QuestionRS.getTimestamp(4).getTime(), 
						    								QuestionRS.getInt(5), 
						    								QuestionRS.getDouble(6), 
						    								qTopicList));
    					}
    					QuestionRS.close();
    					JsonRes = gson.toJson(questCollection, QAndAConstants.QUESTION_COLLECTION); 

    					stmt.close();
    				} catch (SQLException e) {
    					getServletContext().log("Error while querying for All Questions", e);
    					response.sendError(500);//internal server error
    				}				
    			}
    			
    			else if (uri.indexOf(QAndAConstants.INSERT_QUESTION) != -1){
    				try{
    					PreparedStatement pstmt;
    					pstmt = conn.prepareStatement(QAndAConstants.INSERT_NEW_QUESTION);
    					String nickName = (String) request.getSession().getAttribute("LoggedInUserNickName");
    					pstmt.setString(1, nickName);
    					pstmt.setString(2, request.getParameter("qText"));
    					pstmt.executeUpdate();
    					
    					//commit update
    					conn.commit();
    					//close statements
    					pstmt.close();
    					String[] topicList = request.getParameter("topicList").replaceAll("^[,\\s]+", "").split("[,\\s]+");
    					for (int i = 0; i < topicList.length; ++i)
    					{
    						PreparedStatement topicPstmt;
    						topicPstmt = conn.prepareStatement(QAndAConstants.INSERT_TOPIC_TO_LATEST_QUESTION);
    						topicPstmt.setString(1, topicList[i]);
    						topicPstmt.executeUpdate();		    			
    						//commit update
    						conn.commit();
    						//close statements
    						topicPstmt.close();
    					}	
    					// now to update user rating
    					updateUserRating(nickName, conn);
    					
    				}  catch (SQLException e) {
    					getServletContext().log("Error while Inserting a New Question", e);
    					response.sendError(500);//internal server error
    				}    				
    			}
    			
    			else if (uri.indexOf(QAndAConstants.UPDATE_QUESTION) != -1){
    				try {
    					// first: user has voted and we need to save the vote
    					PreparedStatement saveVoteStmt;
    					int qId = Integer.parseInt(request.getParameter("qId"));
    					String nickName = (String) request.getSession().getAttribute("LoggedInUserNickName");
    					saveVoteStmt = conn.prepareStatement(QAndAConstants.ADD_QUESTION_VOTE);
    					saveVoteStmt.setInt(1, qId);
    					saveVoteStmt.setString(2, nickName);
    					saveVoteStmt.executeUpdate();
    					// commit is here - if failed will roll back!!!
    					saveVoteStmt.close();
    					int votingScoreChange = Integer.parseInt(request.getParameter("changeVS"));
    					
    					// now update the question scores
    					updateQuestionScores(qId, votingScoreChange, conn);    					 
    					conn.commit(); 								
    					 					
    					// now to update user rating
    					String questionSubmitter = getUserNickNameFromQid(qId, conn);
    					updateUserRating(questionSubmitter, conn);
    					
    				} catch (SQLException e) {
    					conn.rollback();
    					if (userAlreadyVoted(e)) {
    						JsonRes = "{\"failed\":\"true\",\"error\":\"Cannot vote question twice\"}";
    					}
    					else {
    						getServletContext().log("Error while Updating Question", e);
    						response.sendError(500);//internal server error    						
    					}
    				}	
    			}
    			else if (uri.indexOf(QAndAConstants.USER_LAST_ASKED) != -1)
        		{
        			try {
    					PreparedStatement pstmt;
    					String strUserName =  request.getParameter("userNickName");
        				pstmt = conn.prepareStatement(QAndAConstants.GET_USER_LAST_QUESTION); 
        				pstmt.setString(1, strUserName);
        				ResultSet rs = pstmt.executeQuery();
    					Collection<Question> questCollection = new ArrayList<Question>();

    					while (rs.next())
    					{
    						int qID = rs.getInt(1);
    						List<String> qTopicList = getQuestionTopics(qID, conn);  						    						
    						questCollection.add(new Question(qID, 
						    								rs.getString(2), 
						    								rs.getString(3), 
						    								rs.getTimestamp(4).getTime(), 
						    								rs.getInt(5), 
						    								rs.getDouble(6), 
						    								qTopicList));
    						
    					}
    					rs.close();
    					pstmt.close();
    					JsonRes = gson.toJson(questCollection, QAndAConstants.QUESTION_COLLECTION); 

    				} catch (SQLException e) {
    					getServletContext().log("Error while fetching User last questions", e);
    					response.sendError(500);//internal server error
    				}
        		}
    			else if (uri.indexOf(QAndAConstants.TOPIC_BY_TPOP) != -1){
    				try {
    					PreparedStatement pstmt;
        				pstmt = conn.prepareStatement(QAndAConstants.GET_TOPICS_BY_POPULARITY);
        				List<TopicQRatingPair> topicList = new ArrayList<TopicQRatingPair>();
        				ResultSet rs = pstmt.executeQuery();
    					while (rs.next())
    					{
    						topicList.add(new TopicQRatingPair(rs.getString(1), rs.getDouble(2)));   						
    					}
    					rs.close();
    					pstmt.close();	
    					JsonRes = gson.toJson(topicList, QAndAConstants.TOPIC_AND_TPOP_COLLECTION);
    				} catch (SQLException e) {
    					getServletContext().log("Error while fetching User last questions", e);
    					response.sendError(500);//internal server error
    				}
    			}
    			else if (uri.indexOf(QAndAConstants.QUESTIONS_BY_TOPIC) != 1){
    				try {
    					PreparedStatement pstmt;
        				pstmt = conn.prepareStatement(QAndAConstants.GET_QUESTIONS_BY_TOPIC);
        				pstmt.setString(1, request.getParameter("topic"));
        				Collection<Question> questCollection = new ArrayList<Question>();        				ResultSet rs = pstmt.executeQuery();
    					while (rs.next())
    					{
    						int qID = rs.getInt(1);
    						List<String> qTopicList = getQuestionTopics(qID, conn);  						    						
    						questCollection.add(new Question(qID, 
						    								rs.getString(2), 
						    								rs.getString(3), 
						    								rs.getTimestamp(4).getTime(), 
						    								rs.getInt(5), 
						    								rs.getDouble(6), 
						    								qTopicList));   						
    					}
    					rs.close();
    					pstmt.close();	
    					JsonRes = gson.toJson(questCollection, QAndAConstants.QUESTION_COLLECTION);  
    					} catch (SQLException e) {
    					getServletContext().log("Error while fetching User last questions", e);
    					response.sendError(500);//internal server error
    				}
    			}
    		}
    		/* Second Big Case - It deals with Answers */
    		else if (uri.indexOf(QAndAConstants.ANSWER) != -1)
    		{
    			if (uri.indexOf(QAndAConstants.QUESTION_ANS) != -1) {    				    				
    				try{
    					PreparedStatement pstmt;
    					pstmt = conn.prepareStatement(QAndAConstants.GET_ANSWERS_TO_QUESTION); 
    					pstmt.setInt(1, Integer.parseInt(request.getParameter("qID")));
    		    		Collection<Answer> ansCollection = new ArrayList<Answer>();

    					ResultSet rs = pstmt.executeQuery();
    					while (rs.next()){
    						ansCollection.add(new Answer(rs.getInt(1), 
					    								rs.getInt(2), 
					    								rs.getString(3), 
					    								rs.getString(4), 
					    								rs.getTimestamp(5).getTime(), 
					    								rs.getInt(6)));
    					}
    					rs.close();
    					pstmt.close();
    	    			JsonRes = gson.toJson(ansCollection, QAndAConstants.ANSWER_COLLECTION);

    				}  catch (SQLException e) {
    					getServletContext().log("Error while querying for Answers to Question", e);
    					response.sendError(500);//internal server error
    				}	
    			}
    			
    			
    			else if (uri.indexOf(QAndAConstants.INSERT_ANSWER) != -1){
    				try{
    					PreparedStatement pstmt;
    					String loggedInUser = (String)request.getSession().getAttribute("LoggedInUserNickName");
    					int qId = Integer.parseInt(request.getParameter("qID"));
    					pstmt = conn.prepareStatement(QAndAConstants.INSERT_NEW_ANSWER); 
    					pstmt.setInt(1, qId);
    					pstmt.setString(2, loggedInUser);
    					pstmt.setString(3, request.getParameter("aText"));
    					pstmt.executeUpdate();
    					
    					//commit update
    					conn.commit();
    					//close statements
    					pstmt.close();
    					
    					// now we need to update both users - question submitter and answer submitter 
    					// answer user
    					updateUserRating(loggedInUser, conn);
    					
    					// question and question user
    					updateQuestionScores(qId, 0, conn);
    					String questionUser = getUserNickNameFromQid(qId, conn);
    					updateUserRating(questionUser, conn);
    					
    					
    				}  catch (SQLException e) {
    					getServletContext().log("Error while Inserting a New Answer", e);
    					response.sendError(500);//internal server error
    				}	
    			}
    			
    			else if (uri.indexOf(QAndAConstants.UPDATE_ANSWER) != -1) {
    				try{
    					// Save Vote
    					PreparedStatement saveVoteStmt;
    					int aId = Integer.parseInt(request.getParameter("aID"));
    					int qId = Integer.parseInt(request.getParameter("qID"));
    					String nickName = (String) request.getSession().getAttribute("LoggedInUserNickName");
    					saveVoteStmt = conn.prepareStatement(QAndAConstants.ADD_ANSWER_VOTE);
    					saveVoteStmt.setInt(1, aId);
    					saveVoteStmt.setString(2, nickName);
    					saveVoteStmt.executeUpdate();
    					saveVoteStmt.close();
    					
    					PreparedStatement pstmt;
    					pstmt = conn.prepareStatement(QAndAConstants.VOTE_ANSWER); 
    					pstmt.setInt(1, Integer.parseInt(request.getParameter("changeVS")));
    					pstmt.setInt(2, aId);
    					pstmt.executeUpdate();
    					//close statements
    					pstmt.close();
    					conn.commit();
    					
    					// now we need to update answer submitter User Rating and the question rating and user rating  					
    					updateQuestionScores(qId, 0, conn);
    					String questionUser = getUserNickNameFromQid(qId, conn);
						updateUserRating(questionUser, conn);    						
			
    					String answerUser = getUserNickNameFromAid(aId, conn);
    					if (!answerUser.equals(questionUser)){
    						updateUserRating(answerUser, conn);
    					}
    				   					  					
    				}  catch (SQLException e) {
    					conn.rollback();
    					if (userAlreadyVoted(e)){
    						JsonRes = "{\"failed\":\"true\",\"error\":\"Cannot vote answer twice\"}";
    					}
    					else{
    						getServletContext().log("Error while Updating Pos Answer Vote", e);
    						response.sendError(500);//internal server error    						
    					}
    				}	
    			}
    			
    			else if (uri.indexOf(QAndAConstants.USER_LAST_ANSWERED) != -1)
    				{
    					try {
    						PreparedStatement pstmt;
    						String strUserName =  request.getParameter("userNickName");
    						pstmt = conn.prepareStatement(QAndAConstants.GET_USER_LAST_ANSWERS); 
    						pstmt.setString(1, strUserName);
    						ResultSet rs = pstmt.executeQuery();
    			    		Collection<QuestionAnswerPair> questAndAnsPairCollection = new ArrayList<QuestionAnswerPair>();

    						while (rs.next()){
    							int qID = rs.getInt(1);
        						List<String> qTopicList = getQuestionTopics(qID, conn);  						    						
        						Question qst = new Question(	rs.getInt(1), 
    						    								rs.getString(2), 
    						    								rs.getString(3), 
    						    								rs.getTimestamp(4).getTime(), 
    						    								rs.getInt(5), 
    						    								rs.getDouble(6), 
    						    								qTopicList);
        						Answer ans = new Answer(	rs.getInt(7), 
    					    								rs.getInt(8), 
    					    								rs.getString(9), 
    					    								rs.getString(10), 
    					    								rs.getTimestamp(11).getTime(), 
    					    								rs.getInt(12));
        						questAndAnsPairCollection.add(new QuestionAnswerPair(qst, ans));
        					}
    						rs.close();
    						pstmt.close();
    		    			JsonRes = gson.toJson(questAndAnsPairCollection, QAndAConstants.QUESTION__AND_ANS_COLLECTION);

    					} catch (SQLException e) {
    						getServletContext().log("Error while fetching User's last answers", e);
    						response.sendError(500);//internal server error
    					}
    			}
    		}
    		
    		
    		conn.close();
    		   		
    		PrintWriter writer = response.getWriter();
    		writer.println(JsonRes);
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
