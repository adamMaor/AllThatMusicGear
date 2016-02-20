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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import allthatmusicgear.model.QuestionWithAnswers;
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
	
	private int getUserVote(String qOrAUserNickName, String loggedUserNickName, Map<Integer, Integer> userAnsOrQstMap, int ID) throws SQLException
	{
		// if doesn't exist - 0
		int currUserVoteForAns = 0;
		// if belongs to current logged user - -2
		if (qOrAUserNickName.equals(loggedUserNickName)){
			currUserVoteForAns = -2;
		}
		// if voted - what value did he vote
		else if (userAnsOrQstMap.containsKey(ID))
		{
			currUserVoteForAns = userAnsOrQstMap.get(ID);
		}
		return currUserVoteForAns;
	}
	
	private List<Answer> getQuestionAnswers(int qID, Connection conn, String LoggedUserNickName) throws SQLException
	{
		List<Answer> qAnswerList = new ArrayList<Answer>();
		PreparedStatement pstmt;
		pstmt = conn.prepareStatement(QAndAConstants.GET_ANSWERS_TO_QUESTION); 
		pstmt.setInt(1, qID);		
		Map<Integer, Integer> userAnsMap = getCurrUserAnsVoteMap(LoggedUserNickName, conn);

		ResultSet rs = pstmt.executeQuery();
		while (rs.next()){
			int aID = rs.getInt(1);
			String answerUserNickName = getUserNickNameFromAid(aID, conn);
			int currUserVoteForAns = getUserVote(answerUserNickName, LoggedUserNickName, userAnsMap, aID);

			qAnswerList.add(new Answer(rs.getInt(1), 
	    								rs.getInt(2), 
	    								rs.getString(3), 
	    								rs.getString(4), 
	    								rs.getTimestamp(5).getTime(), 
	    								rs.getInt(6),
										rs.getString(7),
										rs.getDouble(8),
										currUserVoteForAns));
		}
		rs.close();
		pstmt.close();		
		return qAnswerList;
	}
	
	private Map<Integer, Integer> getCurrUserQstVoteMap(String userNickName, Connection conn) throws SQLException
	{
		Map<Integer, Integer> resMap = new HashMap<Integer, Integer>();
		PreparedStatement pstmt;
		pstmt = conn.prepareStatement(UserConstants.GET_USER_QST_VOTE);
		pstmt.setString(1, userNickName);
		ResultSet rs = pstmt.executeQuery();
		while (rs.next()){
			resMap.put(rs.getInt(1), rs.getInt(2));
		}
		return resMap;
	}
	
	private Map<Integer, Integer> getCurrUserAnsVoteMap(String userNickName, Connection conn) throws SQLException
	{
		Map<Integer, Integer> resMap = new HashMap<Integer, Integer>();
		PreparedStatement pstmt;
		pstmt = conn.prepareStatement(UserConstants.GET_USER_ANS_VOTE);
		pstmt.setString(1, userNickName);
		ResultSet rs = pstmt.executeQuery();
		while (rs.next()){
			resMap.put(rs.getInt(1), rs.getInt(2));
		}
		return resMap;
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
    		String loggedUserNickName = (String) request.getSession().getAttribute("LoggedInUserNickName");
    		Gson gson = new Gson();
    		String JsonRes = null;  		
    		

			/* First Question case - get New submitted questions */
			if (uri.indexOf(QAndAConstants.NEW_QUESTIONS) != -1) {
				try {
					Integer totalQuestionCount = 0;
					Statement countQuestions = conn.createStatement();
					ResultSet questionCountRs = countQuestions.executeQuery(QAndAConstants.COUNT_NEW_QUESTIONS);
					if (questionCountRs.next()){
						totalQuestionCount = questionCountRs.getInt(1);
					}
					
					PreparedStatement pstmt;
					pstmt = conn.prepareStatement(QAndAConstants.GET_NEW_QUESTIONS);
					int offset = 20 * (Integer.parseInt(request.getParameter("pageNum")) - 1);
					pstmt.setInt(1, offset);
					ResultSet QuestionRS = pstmt.executeQuery();
					Map<Integer, Integer> userQstVoteMap = getCurrUserQstVoteMap(loggedUserNickName, conn); 
					
					Collection<QuestionWithAnswers> questCollection = new ArrayList<QuestionWithAnswers>();
					while (QuestionRS.next())
					{
						int qID = QuestionRS.getInt(1);
						String questionUserNickName = QuestionRS.getString(2);
						int currUserVoted = getUserVote(questionUserNickName, loggedUserNickName, userQstVoteMap, qID);
						List<String> qTopicList = getQuestionTopics(qID, conn);  						    						
						Question newQuestion = new Question(qID, 
														questionUserNickName, 
					    								QuestionRS.getString(3), 
					    								QuestionRS.getTimestamp(4).getTime(), 
					    								QuestionRS.getInt(5), 
					    								QuestionRS.getDouble(6), 
					    								qTopicList,
					    								QuestionRS.getString(7),
					    								QuestionRS.getDouble(8),
					    								currUserVoted);
						// no answers here
						List<Answer> qAnswers = new ArrayList<Answer>();
						questCollection.add(new QuestionWithAnswers(newQuestion, qAnswers));
					}
					QuestionRS.close();
					pstmt.close();

					JsonRes = "{\"numQuestion\":" + totalQuestionCount.toString() + ", \"questions\":";
					JsonRes += gson.toJson(questCollection, QAndAConstants.QUESTION__AND_ANS_COLLECTION);
					JsonRes += "}";
				} catch (SQLException e) {
					getServletContext().log("Error while querying for New Questions", e);
					response.sendError(500);//internal server error
				}				
			}
			
			else if (uri.indexOf(QAndAConstants.ALL_QUESTIONS) != -1) {
				try {
					Integer totalQuestionCount = 0;
					Statement countQuestions = conn.createStatement();
					ResultSet questionCountRs = countQuestions.executeQuery(QAndAConstants.COUNT_ALL_QUESTIONS);
					if (questionCountRs.next()){
						totalQuestionCount = questionCountRs.getInt(1);
					}
					
					PreparedStatement pstmt;
					pstmt = conn.prepareStatement(QAndAConstants.GET_ALL_QUESTIONS);
					int offset = 20 * (Integer.parseInt(request.getParameter("pageNum")) - 1);
					pstmt.setInt(1, offset);
					Map<Integer, Integer> userQstVoteMap = getCurrUserQstVoteMap(loggedUserNickName, conn); 
					Collection<QuestionWithAnswers> questCollection = new ArrayList<QuestionWithAnswers>();
					ResultSet QuestionRS = pstmt.executeQuery();

					while (QuestionRS.next())
					{
						int qID = QuestionRS.getInt(1);
						String questionUserNickName = QuestionRS.getString(2);
						int currUserVoted = getUserVote(questionUserNickName, loggedUserNickName, userQstVoteMap, qID);
						List<String> qTopicList = getQuestionTopics(qID, conn); 
						Question newQuestion = new Question(qID, 
														questionUserNickName, 
					    								QuestionRS.getString(3), 
					    								QuestionRS.getTimestamp(4).getTime(), 
					    								QuestionRS.getInt(5), 
					    								QuestionRS.getDouble(6), 
					    								qTopicList,
					    								QuestionRS.getString(7),
					    								QuestionRS.getDouble(8),
					    								currUserVoted);
						List<Answer> qAnswers = getQuestionAnswers(qID, conn, loggedUserNickName);
						questCollection.add(new QuestionWithAnswers(newQuestion, qAnswers));
					}
					QuestionRS.close();
					pstmt.close();

					JsonRes = "{\"numQuestion\":" + totalQuestionCount.toString() + ", \"questions\":";
					JsonRes += gson.toJson(questCollection, QAndAConstants.QUESTION__AND_ANS_COLLECTION);
					JsonRes += "}";

				} catch (SQLException e) {
					getServletContext().log("Error while querying for All Questions", e);
					response.sendError(500);//internal server error
				}				
			}
			
			else if (uri.indexOf(QAndAConstants.INSERT_QUESTION) != -1) {
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
					String[] topicList = request.getParameter("topicList").split(",");
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
			
			else if (uri.indexOf(QAndAConstants.UPDATE_QUESTION) != -1) {
				try {
					// first: user has voted and we need to save the vote
					PreparedStatement saveVoteStmt;
					int qId = Integer.parseInt(request.getParameter("qId"));
					int votingScoreChange = Integer.parseInt(request.getParameter("changeVS"));
					saveVoteStmt = conn.prepareStatement(QAndAConstants.ADD_QUESTION_VOTE);
					saveVoteStmt.setInt(1, qId);
					saveVoteStmt.setString(2, loggedUserNickName);
					saveVoteStmt.setInt(3, votingScoreChange);
					saveVoteStmt.executeUpdate();
					// commit is here - if failed will roll back!!!
					saveVoteStmt.close();
					
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
			else if (uri.indexOf(QAndAConstants.USER_LAST_ASKED) != -1)	{
    			try {
					PreparedStatement pstmt;
					String strUserName =  request.getParameter("userNickName");
    				pstmt = conn.prepareStatement(QAndAConstants.GET_USER_LAST_QUESTION); 
    				pstmt.setString(1, strUserName);
    				Collection<Question> questCollection = new ArrayList<Question>();
    				Map<Integer, Integer> userQstVoteMap = getCurrUserQstVoteMap(loggedUserNickName, conn);
    				ResultSet QuestionRS = pstmt.executeQuery();

					while (QuestionRS.next())
					{
						int qID = QuestionRS.getInt(1);
						String questionUserNickName = QuestionRS.getString(2);
						int currUserVoted = getUserVote(questionUserNickName, loggedUserNickName, userQstVoteMap, qID);
						List<String> qTopicList = getQuestionTopics(qID, conn);  						    						
						questCollection.add(new Question(qID, 
														questionUserNickName, 
					    								QuestionRS.getString(3), 
					    								QuestionRS.getTimestamp(4).getTime(), 
					    								QuestionRS.getInt(5), 
					    								QuestionRS.getDouble(6), 
					    								qTopicList,
					    								QuestionRS.getString(7),
					    								QuestionRS.getDouble(8),
					    								currUserVoted));
						
					}
					QuestionRS.close();
					pstmt.close();
					JsonRes = gson.toJson(questCollection, QAndAConstants.QUESTION_COLLECTION); 

				} catch (SQLException e) {
					getServletContext().log("Error while fetching User last questions", e);
					response.sendError(500);//internal server error
				}
    		}
			else if (uri.indexOf(QAndAConstants.TOPIC_BY_TPOP) != -1) {
				try {
					Integer totalTopics = 0;
					Statement getTopicCount = conn.createStatement();
					ResultSet topicRs = getTopicCount.executeQuery(QAndAConstants.COUNT_ALL_TOPICS);
					if (topicRs.next()){
						totalTopics = topicRs.getInt(1);
					}
					PreparedStatement pstmt;
    				pstmt = conn.prepareStatement(QAndAConstants.GET_TOPICS_BY_POPULARITY);
    				pstmt.setInt(1, Integer.parseInt(request.getParameter("offset")));
    				pstmt.setInt(2, Integer.parseInt(request.getParameter("listSize")));
    				
    				List<TopicQRatingPair> topicList = new ArrayList<TopicQRatingPair>();
    				ResultSet rs = pstmt.executeQuery();
					while (rs.next())
					{
						topicList.add(new TopicQRatingPair(rs.getString(1), rs.getDouble(2)));   						
					}
					rs.close();
					pstmt.close();	
					
					JsonRes = "{\"numTopics\":" + totalTopics.toString() + ", \"topics\":";
					JsonRes += gson.toJson(topicList, QAndAConstants.TOPIC_AND_TPOP_COLLECTION);
					JsonRes += "}";
					
				} catch (SQLException e) {
					getServletContext().log("Error while fetching Topics", e);
					response.sendError(500);//internal server error
				}
			}
			
			else if (uri.indexOf(QAndAConstants.QUESTIONS_BY_TOPIC) != -1) {
				try {
					int offset = 20 * (Integer.parseInt(request.getParameter("pageNum")) -1);
					String topic = request.getParameter("topic");

					/* first Count all for pages */
					Integer totalQuestionsForTopic = 0;
					PreparedStatement countStmt;
					countStmt = conn.prepareStatement(QAndAConstants.COUNT_ALL_TOPIC_QUESTIONS);
					countStmt.setString(1, topic);
					ResultSet rs = countStmt.executeQuery();
					if (rs.next()){
						totalQuestionsForTopic = rs.getInt(1);
					}
					rs.close();
					countStmt.close();
					
					/* now get the questions and answers */
					PreparedStatement pstmt;
    				pstmt = conn.prepareStatement(QAndAConstants.GET_QUESTIONS_BY_TOPIC);
    				pstmt.setString(1, topic);
    				pstmt.setInt(2, offset);
					Collection<QuestionWithAnswers> questCollection = new ArrayList<QuestionWithAnswers>();
    				Map<Integer, Integer> userQstVoteMap = getCurrUserQstVoteMap(loggedUserNickName, conn);
    				ResultSet QuestionRS = pstmt.executeQuery();
					while (QuestionRS.next())
					{
						int qID = QuestionRS.getInt(1);
						String questionUserNickName = QuestionRS.getString(2);
						int currUserVoted = getUserVote(questionUserNickName, loggedUserNickName, userQstVoteMap, qID);
						List<String> qTopicList = getQuestionTopics(qID, conn);  						    						
						Question newQuestion = new Question(qID, 
														questionUserNickName, 
					    								QuestionRS.getString(3), 
					    								QuestionRS.getTimestamp(4).getTime(), 
					    								QuestionRS.getInt(5), 
					    								QuestionRS.getDouble(6), 
					    								qTopicList,
					    								QuestionRS.getString(7),
					    								QuestionRS.getDouble(8),
					    								currUserVoted);
						List<Answer> qAnswers = getQuestionAnswers(qID, conn, loggedUserNickName);
						questCollection.add(new QuestionWithAnswers(newQuestion, qAnswers));
					}
					  					
					JsonRes = "{\"numQuestion\":" + totalQuestionsForTopic.toString() + ", \"questions\":";
					JsonRes += gson.toJson(questCollection, QAndAConstants.QUESTION__AND_ANS_COLLECTION);
					JsonRes += "}";
   					} catch (SQLException e) {
					getServletContext().log("Error while fetching User last questions", e);
					response.sendError(500);//internal server error
				}
			}
    		
			else if (uri.indexOf(QAndAConstants.QUESTION_ANS) != -1) {    				    				
				try{
					int qID = Integer.parseInt(request.getParameter("qID"));
					Collection<Answer> ansCollection = getQuestionAnswers(qID, conn, loggedUserNickName);
	    			JsonRes = gson.toJson(ansCollection, QAndAConstants.ANSWER_COLLECTION);

				}  catch (SQLException e) {
					getServletContext().log("Error while querying for Answers to Question", e);
					response.sendError(500);//internal server error
				}	
			}
			
			
			else if (uri.indexOf(QAndAConstants.INSERT_ANSWER) != -1) {
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
				try {
					// Save Vote
					PreparedStatement saveVoteStmt;
					int aId = Integer.parseInt(request.getParameter("aID"));
					int qId = Integer.parseInt(request.getParameter("qID"));
					int newVote = Integer.parseInt(request.getParameter("changeVS"));
					saveVoteStmt = conn.prepareStatement(QAndAConstants.ADD_ANSWER_VOTE);
					saveVoteStmt.setInt(1, aId);
					saveVoteStmt.setString(2, loggedUserNickName);
					saveVoteStmt.setInt(3, newVote);
					saveVoteStmt.executeUpdate();
					saveVoteStmt.close();
					
					PreparedStatement pstmt;
					pstmt = conn.prepareStatement(QAndAConstants.VOTE_ANSWER); 
					pstmt.setInt(1, newVote);
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
			
			else if (uri.indexOf(QAndAConstants.USER_LAST_ANSWERED) != -1) {
				try {
					PreparedStatement pstmt;
					String strUserName =  request.getParameter("userNickName");
					pstmt = conn.prepareStatement(QAndAConstants.GET_USER_LAST_ANSWERS); 
					pstmt.setString(1, strUserName);
		    		Collection<QuestionAnswerPair> questAndAnsPairCollection = new ArrayList<QuestionAnswerPair>();
		    		Map<Integer, Integer> userQstVoteMap = getCurrUserQstVoteMap(loggedUserNickName, conn);
		    		Map<Integer, Integer> userAnsVoteMap = getCurrUserQstVoteMap(loggedUserNickName, conn);
		    		ResultSet rs = pstmt.executeQuery();

					while (rs.next()){
						int qID = rs.getInt(1);
						int aID = rs.getInt(9);
						String UserNickName = rs.getString(2); 
						int currUserQstVoted = getUserVote(UserNickName, loggedUserNickName, userQstVoteMap, qID);
						int currUserAnsVoted = getUserVote(UserNickName, loggedUserNickName, userAnsVoteMap, aID);
						List<String> qTopicList = getQuestionTopics(qID, conn);  						    						
						Question qst = new Question(	qID, 
					    								rs.getString(2), 
					    								rs.getString(3), 
					    								rs.getTimestamp(4).getTime(), 
					    								rs.getInt(5), 
					    								rs.getDouble(6), 
					    								qTopicList,
					    								rs.getString(7),
					    								rs.getDouble(8),
					    								currUserQstVoted);
						Answer ans = new Answer(	aID, 
				    								rs.getInt(10), 
				    								rs.getString(11), 
				    								rs.getString(12), 
				    								rs.getTimestamp(13).getTime(), 
				    								rs.getInt(14),
				    								rs.getString(15),
				    								rs.getDouble(16),
				    								currUserAnsVoted);
						questAndAnsPairCollection.add(new QuestionAnswerPair(qst, ans));
					}
					rs.close();
					pstmt.close();
	    			JsonRes = gson.toJson(questAndAnsPairCollection, QAndAConstants.QUESTION_AND_ANS_PAIR_COLLECTION);
	    			

				} catch (SQLException e) {
					getServletContext().log("Error while fetching User's last answers", e);
					response.sendError(500);//internal server error
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
