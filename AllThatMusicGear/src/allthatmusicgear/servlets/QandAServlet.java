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
 * Servlet implementation class QandAServlet<br>
 * This servlet will handle all DB accesses related to questions and answers
 */
public class QandAServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public QandAServlet() {
        super();
    }
    
    /**
     * This Utility method is used to check if exception was caused because of duplicate key 
     * used for user's votes 
     * @param e - exception thrown
     * @return - true if exception is of duplicated key creation in table
     */
    private boolean userAlreadyVoted(SQLException e) {
        boolean voted;
        if(e.getSQLState().equals("23505")) {
        	voted = true;
        } else {
        	voted = false;
        }
        return voted;
    }
    	
	/** 
	 * This Utility method is used to get question topics, given a question ID
	 * @param qID - Question that topics are related to
	 * @param conn - the current connection
	 * @return - a StringList of topics
	 * @throws SQLException - to be handled by caller
	 */
	private List<String> getQuestionTopics(int qID, Connection conn) throws SQLException {
		PreparedStatement pstmt = null;
		ResultSet TopicRS = null;
		List<String> qTopicList = new ArrayList<String>();
		try {
			pstmt = conn.prepareStatement(QAndAConstants.GET_QUESTION_TOPICS); 
			pstmt.setInt(1, qID);
			TopicRS = pstmt.executeQuery();
			while (TopicRS.next())
			{
				qTopicList.add(TopicRS.getString(1));
			}
			return qTopicList;						
		} catch(SQLException e) {
			throw e;
		} finally {
			if (TopicRS != null){
				TopicRS.close();				
			}
			if (pstmt != null) {				
				pstmt.close();
			}
		}
	}
	
	/**
	 * This Utility method builds a map of QID as key, as Vote pos/neg as value for question the given user had voted for
	 * @param userNickName - user to check votes for
	 * @param conn - the current connection
	 * @return Map<QID,VoteScore> of the given user
	 * @throws SQLException - to be handled by caller
	 */
	private Map<Integer, Integer> getCurrUserQstVoteMap(String userNickName, Connection conn) throws SQLException
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Map<Integer, Integer> resMap = new HashMap<Integer, Integer>();
		try{
			pstmt = conn.prepareStatement(UserConstants.GET_USER_QST_VOTE);
			pstmt.setString(1, userNickName);
			rs = pstmt.executeQuery();
			while (rs.next()){
				resMap.put(rs.getInt(1), rs.getInt(2));
			}
			return resMap;			
		} catch(SQLException e) {
			throw e;
		} finally {
			if (rs != null) {				
				rs.close();
			}
			if (pstmt != null) {				
				pstmt.close();
			}
		}
	}
	
	/**
	 * This Utility method builds a map of AID as key, as Vote pos/neg as value for answers the given user had voted for
	 * @param userNickName - user to check votes for
	 * @param conn - the current connection
	 * @return Map<AID,VoteScore> of the given user
	 * @throws SQLException - to be handled by caller
	 */
	private Map<Integer, Integer> getCurrUserAnsVoteMap(String userNickName, Connection conn) throws SQLException
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Map<Integer, Integer> resMap = new HashMap<Integer, Integer>();
		try {
			pstmt = conn.prepareStatement(UserConstants.GET_USER_ANS_VOTE);
			pstmt.setString(1, userNickName);
			rs = pstmt.executeQuery();
			while (rs.next()){
				resMap.put(rs.getInt(1), rs.getInt(2));
			}
			return resMap;			
		} catch(SQLException e) {
			throw e;
		} finally {
			if (rs != null) {				
				rs.close();
			}
			if (pstmt != null) {
				pstmt.close();				
			}
		}
	}
	
	/**
	 * This Utility method is used to get a user vote (coded) for a specific question OR answer
	 * @param qOrAUserNickName - Q/A submitter's nickName
	 * @param loggedUserNickName - Logged in user's nickName
	 * @param userAnsOrQstMap - a map containing actual votes {@link} - getCurrUserAnsVoteMap
	 * @param ID - Q/A ID to get vote for
	 * @return (-2) if current logged in user is the Q/A submitter, 0 - if didn't vote, 1/(-1) - if voted pos/neg.
	 * @throws SQLException - to be handled by caller
	 */
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
	
	/**
	 * This Utility is used to get a List of Answers to a given question
	 * @param qID - the question ID answers need to be recovered for
	 * @param conn - the current connection
	 * @param LoggedUserNickName - used to pass to assist method {@link} getCurrUserAnsVoteMap
	 * @return A list of Answer objects related to the given question ordered by their voting score
	 * @throws SQLException
	 */
	private List<Answer> getQuestionAnswers(int qID, Connection conn, String LoggedUserNickName) throws SQLException
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<Answer> qAnswerList = new ArrayList<Answer>();
		try {
			pstmt = conn.prepareStatement(QAndAConstants.GET_ANSWERS_TO_QUESTION); 
			pstmt.setInt(1, qID);		
			Map<Integer, Integer> userAnsMap = getCurrUserAnsVoteMap(LoggedUserNickName, conn);
			rs = pstmt.executeQuery();
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
			return qAnswerList;
		} catch(SQLException e) {
			throw e;
		} finally {
			if (rs != null) {				
				rs.close();
			}
			if (pstmt != null) {
				pstmt.close();				
			}
		}		
	}
		
	/**
	 * Utility method is used to update the user rating - used at every vote, and at every Q/A submission
	 * @param userNickName - user to update
	 * @param conn - the current connection
	 * @throws SQLException - to be handled by caller
	 */
	private void updateUserRating(String userNickName, Connection conn) throws SQLException
    {
    	PreparedStatement avgQPstmt = null;
    	ResultSet avgQRS = null;
    	PreparedStatement avgAPstmt = null;
    	ResultSet avgARS = null;
    	PreparedStatement updateUserPstmt = null;
    	try {
    		// first get question scores (QVS and QRating)
    		avgQPstmt = conn.prepareStatement(UserConstants.GET_USER_AVG_Q_SCORES); 
    		avgQPstmt.setString(1, userNickName);
    		double avgQRating = 0;
    		avgQRS = avgQPstmt.executeQuery();
    		while (avgQRS.next()) {
    			avgQRating = avgQRS.getObject(1) == null ? 0 : avgQRS.getDouble(1);	
    		}
    		// now get AVG answer score 
    		avgAPstmt = conn.prepareStatement(UserConstants.GET_USER_AVG_A_SCORES); 
    		avgAPstmt.setString(1, userNickName);
    		double avgARating = 0;
    		avgARS = avgAPstmt.executeQuery();
    		while (avgARS.next()) {
    			avgARating = avgARS.getObject(1) == null ? 0 : avgARS.getDouble(1);	
    		}
    		// create the new Rating Score - and push to DB
    		double newRatingScore = 0.2 * avgQRating + 0.8 * avgARating;   		
    		updateUserPstmt = conn.prepareStatement(UserConstants.UPDATE_UR_QUERY);
    		updateUserPstmt.setDouble(1, newRatingScore);
    		updateUserPstmt.setString(2, userNickName);
    		updateUserPstmt.executeUpdate();
    		conn.commit();   		
    	} catch(SQLException e) {
			throw e;
		} finally {
			if (avgQRS != null) {
				avgQRS.close();				
			}
			if (avgQPstmt != null) {
				avgQPstmt.close();				
			}
			if (avgARS != null) {
				avgARS.close();				
			}
			if (avgAPstmt != null) {
				avgAPstmt.close();				
			}
			if (updateUserPstmt != null) {
				updateUserPstmt.close();				
			}
		}	
    }
	
	/**
	 * Utility method to get a user nickname to a given QID 
	 * @param qID - the question who's user nickname is needed
	 * @param conn - the current connection
	 * @return - User's nickName
	 * @throws SQLException - to be handled by caller
	 */
	private String getUserNickNameFromQid(int qID, Connection conn) throws SQLException
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String userNickName = null;
		try {
			pstmt = conn.prepareStatement(UserConstants.GET_USER_NICK_FROM_QUESTION);
			pstmt.setInt(1, qID);
			rs = pstmt.executeQuery();
			if (rs.next()){
				userNickName = rs.getString(1);
			}
			return userNickName;
		} catch(SQLException e) {
			throw e;
		} finally {
			if (rs != null) {				
				rs.close();
			}
			if (pstmt != null) {
				pstmt.close();				
			}
		}	
	}
	
	/**
	 * Utility method to get a user nickname to a given AID 
	 * @param aID - the answer who's user nickname is needed
	 * @param conn - the current connection
	 * @return - User's nickName
	 * @throws SQLException - to be handled by caller
	 */
	private String getUserNickNameFromAid(int aID, Connection conn) throws SQLException
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			String userNickName = null;
			pstmt = conn.prepareStatement(UserConstants.GET_USER_NICK_FROM_ANSWER);
			pstmt.setInt(1, aID);
			rs = pstmt.executeQuery();
			if (rs.next()){
				userNickName = rs.getString(1);
			}
			return userNickName;		
		} catch(SQLException e) {
			throw e;
		} finally {
			if (rs != null) {				
				rs.close();
			}
			if (pstmt != null) {
				pstmt.close();				
			}
		}	
	}
	
	/**
	 * Utility for updating Question Voting score and Rating score
	 * @param qID - Question ID to update
	 * @param votingScoreChange - the change in voting score (-1, 0 or +1)
	 * @param conn - the current connection
	 * @throws SQLException - to be handled by caller
	 */
	private void updateQuestionScores(int qID, int votingScoreChange, Connection conn) throws SQLException
	{
		PreparedStatement questionScorePstmt = null;
		ResultSet questionRS = null;
		PreparedStatement updatePstmt = null;
		try {
			questionScorePstmt = conn.prepareStatement(QAndAConstants.GET_QUESTION_SCORES); 
			questionScorePstmt.setInt(1, qID);
			questionRS = questionScorePstmt.executeQuery();			
			while (questionRS.next())
			{
				int votingScore = questionRS.getInt(1);
				votingScore += votingScoreChange;
				double answersAVGScore = questionRS.getObject(2) == null ? 0 : questionRS.getDouble(2);
				updatePstmt = conn.prepareStatement(QAndAConstants.UPDATE_QUESTION_SCORES);
				updatePstmt.setInt(1, votingScore);
				updatePstmt.setDouble(2, answersAVGScore*0.8 + votingScore*0.2);
				updatePstmt.setInt(3, qID);
				updatePstmt.executeUpdate();
				updatePstmt.close();
			}
			conn.commit();
		} catch(SQLException e) {
			throw e;
		} finally {
			if (questionRS != null) {				
				questionRS.close();
			}
			if (questionScorePstmt != null) {
				questionScorePstmt.close();				
			}
			if (updatePstmt != null) {				
				updatePstmt.close();
			}
		}		
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 * the Json String and Gson object are used in each required case
	 * handles also post requests (via call from doPost method)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {		
		Connection conn = null;
		PrintWriter writer = null;
		try {
			
			Context context = new InitialContext();
    		BasicDataSource ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
    		conn = ds.getConnection();
    		String uri = request.getRequestURI();
    		String loggedUserNickName = (String) request.getSession().getAttribute("LoggedInUserNickName");
    		Gson gson = new Gson();
    		String JsonRes = "";  		
    		
			/* Get newly submitted questions - 20 each time */
			if (uri.indexOf(QAndAConstants.NEW_QUESTIONS) != -1) {
				Statement countQuestions = null;
				ResultSet questionCountRs = null;
				PreparedStatement pstmt = null;
				ResultSet questionRS = null;
				try {
					// first count all questions for pages and offset
					Integer totalQuestionCount = 0;
					countQuestions = conn.createStatement();
					questionCountRs = countQuestions.executeQuery(QAndAConstants.COUNT_NEW_QUESTIONS);
					if (questionCountRs.next()){
						totalQuestionCount = questionCountRs.getInt(1);
					}									
					pstmt = conn.prepareStatement(QAndAConstants.GET_NEW_QUESTIONS);
					// offset by page number
					int offset = 20 * (Integer.parseInt(request.getParameter("pageNum")) - 1);
					pstmt.setInt(1, offset);
					questionRS = pstmt.executeQuery();
					Map<Integer, Integer> userQstVoteMap = getCurrUserQstVoteMap(loggedUserNickName, conn); 
					
					Collection<QuestionWithAnswers> questCollection = new ArrayList<QuestionWithAnswers>();
					while (questionRS.next())
					{
						int qID = questionRS.getInt(1);
						String questionUserNickName = questionRS.getString(2);
						int currUserVoted = getUserVote(questionUserNickName, loggedUserNickName, userQstVoteMap, qID);
						// get question topics
						List<String> qTopicList = getQuestionTopics(qID, conn);  						    						
						Question newQuestion = new Question(qID, 
														questionUserNickName, 
					    								questionRS.getString(3), 
					    								questionRS.getTimestamp(4).getTime(), 
					    								questionRS.getInt(5), 
					    								questionRS.getDouble(6), 
					    								qTopicList,
					    								questionRS.getString(7),
					    								questionRS.getDouble(8),
					    								currUserVoted);
						// no answers here - empty list
						List<Answer> qAnswers = new ArrayList<Answer>();
						questCollection.add(new QuestionWithAnswers(newQuestion, qAnswers));
					}
					// put in Json
					JsonRes = "{\"numQuestion\":" + totalQuestionCount.toString() + ", \"questions\":";
					JsonRes += gson.toJson(questCollection, QAndAConstants.QUESTION__AND_ANS_COLLECTION);
					JsonRes += "}";
					
				} catch (SQLException e) {
					getServletContext().log("Error while querying for New Questions", e);
					response.sendError(500);//internal server error
				} finally {
					try{
						if (countQuestions != null){
							countQuestions.close();
						}
						if (questionCountRs != null){
							questionCountRs.close();
						}
						if (questionRS != null){
							questionRS.close();
						}
						if (pstmt != null){
							pstmt.close();
						}					
					} catch(Exception e){
						e.printStackTrace();
					}
				}
			}
			
			/* Get Top Questions - by QRating - 20 each time */
			else if (uri.indexOf(QAndAConstants.ALL_QUESTIONS) != -1) {
				Statement countQuestions = null;
				ResultSet questionCountRs = null;
				PreparedStatement pstmt = null;
				ResultSet questionRS = null;
				try {
					// first count all questions for pages and offset
					Integer totalQuestionCount = 0;
					countQuestions = conn.createStatement();
					questionCountRs = countQuestions.executeQuery(QAndAConstants.COUNT_ALL_QUESTIONS);
					if (questionCountRs.next()){
						totalQuestionCount = questionCountRs.getInt(1);
					}						
					pstmt = conn.prepareStatement(QAndAConstants.GET_ALL_QUESTIONS);
					// offset by page number
					int offset = 20 * (Integer.parseInt(request.getParameter("pageNum")) - 1);
					pstmt.setInt(1, offset);
					Map<Integer, Integer> userQstVoteMap = getCurrUserQstVoteMap(loggedUserNickName, conn); 
					Collection<QuestionWithAnswers> questCollection = new ArrayList<QuestionWithAnswers>();
					questionRS = pstmt.executeQuery();

					while (questionRS.next())
					{
						int qID = questionRS.getInt(1);
						String questionUserNickName = questionRS.getString(2);
						int currUserVoted = getUserVote(questionUserNickName, loggedUserNickName, userQstVoteMap, qID);
						// get topics
						List<String> qTopicList = getQuestionTopics(qID, conn); 
						Question newQuestion = new Question(qID, 
														questionUserNickName, 
					    								questionRS.getString(3), 
					    								questionRS.getTimestamp(4).getTime(), 
					    								questionRS.getInt(5), 
					    								questionRS.getDouble(6), 
					    								qTopicList,
					    								questionRS.getString(7),
					    								questionRS.getDouble(8),
					    								currUserVoted);
						// get answers for this question
						List<Answer> qAnswers = getQuestionAnswers(qID, conn, loggedUserNickName);
						questCollection.add(new QuestionWithAnswers(newQuestion, qAnswers));
					}
					// put in Json
					JsonRes = "{\"numQuestion\":" + totalQuestionCount.toString() + ", \"questions\":";
					JsonRes += gson.toJson(questCollection, QAndAConstants.QUESTION__AND_ANS_COLLECTION);
					JsonRes += "}";

				} catch (SQLException e) {
					getServletContext().log("Error while querying for All Questions", e);
					response.sendError(500);//internal server error
				} finally {
					try{
						if (countQuestions != null){
							countQuestions.close();
						}
						if (questionCountRs != null){
							questionCountRs.close();
						}
						if (questionRS != null){
							questionRS.close();
						}
						if (pstmt != null){
							pstmt.close();
						}					
					} catch(Exception e){
						e.printStackTrace();
					}
				}				
			}
			
			/* Insert a new question */
			else if (uri.indexOf(QAndAConstants.INSERT_QUESTION) != -1) {
				PreparedStatement pstmt = null;
				PreparedStatement topicPstmt = null;
				try{
					pstmt = conn.prepareStatement(QAndAConstants.INSERT_NEW_QUESTION);
					pstmt.setString(1, loggedUserNickName);
					pstmt.setString(2, request.getParameter("qText"));
					pstmt.executeUpdate();
					conn.commit();
					String[] topicList = request.getParameter("topicList").split(",");
					// insert the topics - locked by default derby locks
					for (int i = 0; i < topicList.length; ++i)
					{
						topicPstmt = conn.prepareStatement(QAndAConstants.INSERT_TOPIC_TO_LATEST_QUESTION);
						topicPstmt.setString(1, topicList[i]);
						topicPstmt.executeUpdate();		    			
						conn.commit();;
					}	
					// now to update user rating - a new question -> a new UserRating
					updateUserRating(loggedUserNickName, conn);					
				}  catch (SQLException e) {
					getServletContext().log("Error while Inserting a New Question", e);
					response.sendError(500);//internal server error
				} finally {
					try{
						if (topicPstmt != null){
							topicPstmt.close();
						}
						if (pstmt != null){
							pstmt.close();
						}					
					} catch(Exception e){
						e.printStackTrace();
					}
				}	   				
			}
			
			/* Update question after vote */
			else if (uri.indexOf(QAndAConstants.UPDATE_QUESTION) != -1) {
				PreparedStatement saveVoteStmt = null;
				try {
					// first: user has voted and we need to save the vote
					int qId = Integer.parseInt(request.getParameter("qId"));
					int votingScoreChange = Integer.parseInt(request.getParameter("changeVS"));
					saveVoteStmt = conn.prepareStatement(QAndAConstants.ADD_QUESTION_VOTE);
					saveVoteStmt.setInt(1, qId);
					saveVoteStmt.setString(2, loggedUserNickName);
					saveVoteStmt.setInt(3, votingScoreChange);
					saveVoteStmt.executeUpdate();
					
					// now update the question scores
					updateQuestionScores(qId, votingScoreChange, conn);    					 
					conn.commit(); 								
					 					
					// now to update user rating
					String questionSubmitter = getUserNickNameFromQid(qId, conn);
					updateUserRating(questionSubmitter, conn);
					
				} catch (SQLException e) {
					conn.rollback();
					/* handle a case where user already voted - also handled in client side */
					if (userAlreadyVoted(e)) {
						JsonRes = "{\"failed\":\"true\",\"error\":\"Cannot vote question twice\"}";
					}
					else {
						getServletContext().log("Error while Updating Question", e);
						response.sendError(500);//internal server error    						
					}
				} finally {
					try{
						if (saveVoteStmt != null){
							saveVoteStmt.close();
						}				
					} catch(Exception e){
						e.printStackTrace();
					}
				}	
			}
			
			/* Get user's 5 last asked questions */
			else if (uri.indexOf(QAndAConstants.USER_LAST_ASKED) != -1)	{
				PreparedStatement pstmt = null;
				ResultSet questionRS = null;
    			try {
					String strUserName =  request.getParameter("userNickName");
    				pstmt = conn.prepareStatement(QAndAConstants.GET_USER_LAST_QUESTION); 
    				pstmt.setString(1, strUserName);
    				Collection<Question> questCollection = new ArrayList<Question>();
    				Map<Integer, Integer> userQstVoteMap = getCurrUserQstVoteMap(loggedUserNickName, conn);
    				questionRS = pstmt.executeQuery();

					while (questionRS.next())
					{
						int qID = questionRS.getInt(1);
						String questionUserNickName = questionRS.getString(2);
						int currUserVoted = getUserVote(questionUserNickName, loggedUserNickName, userQstVoteMap, qID);
						List<String> qTopicList = getQuestionTopics(qID, conn);  						    						
						questCollection.add(new Question(qID, 
														questionUserNickName, 
					    								questionRS.getString(3), 
					    								questionRS.getTimestamp(4).getTime(), 
					    								questionRS.getInt(5), 
					    								questionRS.getDouble(6), 
					    								qTopicList,
					    								questionRS.getString(7),
					    								questionRS.getDouble(8),
					    								currUserVoted));
						
					}
					JsonRes = gson.toJson(questCollection, QAndAConstants.QUESTION_COLLECTION); 
				} catch (SQLException e) {
					getServletContext().log("Error while fetching User last questions", e);
					response.sendError(500);//internal server error
				} finally {
					try{
						if (questionRS != null){
							questionRS.close();
						}
						if (pstmt != null){
							pstmt.close();
						}					
					} catch(Exception e){
						e.printStackTrace();
					}
				}	
    		}
			
			/* Get topic ordered by Tpop  - number of entries is set by "listSize" */
			else if (uri.indexOf(QAndAConstants.TOPIC_BY_TPOP) != -1) {
				Statement getTopicCount = null;
				ResultSet topicRs = null;
				PreparedStatement pstmt = null;
				ResultSet rs = null;
				try {
					Integer totalTopics = 0;
					getTopicCount = conn.createStatement();
					topicRs = getTopicCount.executeQuery(QAndAConstants.COUNT_ALL_TOPICS);
					if (topicRs.next()){
						totalTopics = topicRs.getInt(1);
					}
    				pstmt = conn.prepareStatement(QAndAConstants.GET_TOPICS_BY_POPULARITY);
    				// offset by page number
    				pstmt.setInt(1, Integer.parseInt(request.getParameter("offset")));
    				pstmt.setInt(2, Integer.parseInt(request.getParameter("listSize")));
    				// the topic list
    				List<TopicQRatingPair> topicList = new ArrayList<TopicQRatingPair>();
    				rs = pstmt.executeQuery();
					while (rs.next())
					{
						topicList.add(new TopicQRatingPair(rs.getString(1), rs.getDouble(2)));   						
					}
					// put in Json
					JsonRes = "{\"numTopics\":" + totalTopics.toString() + ", \"topics\":";
					JsonRes += gson.toJson(topicList, QAndAConstants.TOPIC_AND_TPOP_COLLECTION);
					JsonRes += "}";
					
				} catch (SQLException e) {
					getServletContext().log("Error while fetching Topics", e);
					response.sendError(500);//internal server error
				} finally {
					try{
						if (getTopicCount != null){
							getTopicCount.close();
						}
						if (topicRs != null){
							topicRs.close();
						}
						if (rs != null){
							rs.close();
						}
						if (pstmt != null){
							pstmt.close();
						}					
					} catch(Exception e){
						e.printStackTrace();
					}
				}	
			}
			
			/* Get question related to topic */
			else if (uri.indexOf(QAndAConstants.QUESTIONS_BY_TOPIC) != -1) {
				PreparedStatement countStmt = null;
				ResultSet rs = null;
				PreparedStatement pstmt = null;
				ResultSet questionRS = null;
				try {
					// offset by page number
					int offset = 20 * (Integer.parseInt(request.getParameter("pageNum")) -1);
					String topic = request.getParameter("topic");

					/* first Count all for pages */
					Integer totalQuestionsForTopic = 0;
					countStmt = conn.prepareStatement(QAndAConstants.COUNT_ALL_TOPIC_QUESTIONS);
					countStmt.setString(1, topic);
					rs = countStmt.executeQuery();
					if (rs.next()){
						totalQuestionsForTopic = rs.getInt(1);
					}
					
					/* now get the questions and answers */
    				pstmt = conn.prepareStatement(QAndAConstants.GET_QUESTIONS_BY_TOPIC);
    				pstmt.setString(1, topic);
    				pstmt.setInt(2, offset);
					Collection<QuestionWithAnswers> questCollection = new ArrayList<QuestionWithAnswers>();
    				Map<Integer, Integer> userQstVoteMap = getCurrUserQstVoteMap(loggedUserNickName, conn);
    				questionRS = pstmt.executeQuery();
					while (questionRS.next())
					{
						int qID = questionRS.getInt(1);
						String questionUserNickName = questionRS.getString(2);
						int currUserVoted = getUserVote(questionUserNickName, loggedUserNickName, userQstVoteMap, qID);
						List<String> qTopicList = getQuestionTopics(qID, conn);  						    						
						Question newQuestion = new Question(qID, 
														questionUserNickName, 
					    								questionRS.getString(3), 
					    								questionRS.getTimestamp(4).getTime(), 
					    								questionRS.getInt(5), 
					    								questionRS.getDouble(6), 
					    								qTopicList,
					    								questionRS.getString(7),
					    								questionRS.getDouble(8),
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
				} finally {
					try{
						if (countStmt != null){
							countStmt.close();
						}
						if (rs != null){
							rs.close();
						}
						if (questionRS != null){
							questionRS.close();
						}
						if (pstmt != null){
							pstmt.close();
						}					
					} catch(Exception e){
						e.printStackTrace();
					}
				}	
			}
    					
			/* Insert a new answer */
			else if (uri.indexOf(QAndAConstants.INSERT_ANSWER) != -1) {
				PreparedStatement pstmt = null;
				try{
					String loggedInUser = (String)request.getSession().getAttribute("LoggedInUserNickName");
					int qId = Integer.parseInt(request.getParameter("qID"));
					pstmt = conn.prepareStatement(QAndAConstants.INSERT_NEW_ANSWER); 
					pstmt.setInt(1, qId);
					pstmt.setString(2, loggedInUser);
					pstmt.setString(3, request.getParameter("aText"));
					pstmt.executeUpdate();
					
					//commit update
					conn.commit();
					
					// now we need to update both users - question submitter and answer submitter 
					// answer user
					updateUserRating(loggedInUser, conn);
					
					// question and user
					updateQuestionScores(qId, 0, conn);
					String questionUser = getUserNickNameFromQid(qId, conn);
					updateUserRating(questionUser, conn);
					
					// put new answers collection in response - saves a request from js
					Collection<Answer> ansCollection = getQuestionAnswers(qId, conn, loggedUserNickName);
	    			JsonRes = gson.toJson(ansCollection, QAndAConstants.ANSWER_COLLECTION);
					
				}  catch (SQLException e) {
					getServletContext().log("Error while Inserting a New Answer", e);
					response.sendError(500);//internal server error
				} finally {
					try{
						if (pstmt != null){
							pstmt.close();
						}					
					} catch(Exception e){
						e.printStackTrace();
					}
				}	
	
			}
			
			/** Update answer after vote */
			else if (uri.indexOf(QAndAConstants.UPDATE_ANSWER) != -1) {
				PreparedStatement saveVoteStmt = null;
				PreparedStatement pstmt = null;
				try {
					// Save Vote
					int aId = Integer.parseInt(request.getParameter("aID"));
					int qId = Integer.parseInt(request.getParameter("qID"));
					int newVote = Integer.parseInt(request.getParameter("changeVS"));
					saveVoteStmt = conn.prepareStatement(QAndAConstants.ADD_ANSWER_VOTE);
					saveVoteStmt.setInt(1, aId);
					saveVoteStmt.setString(2, loggedUserNickName);
					saveVoteStmt.setInt(3, newVote);
					saveVoteStmt.executeUpdate();
					saveVoteStmt.close();
					
					pstmt = conn.prepareStatement(QAndAConstants.VOTE_ANSWER); 
					pstmt.setInt(1, newVote);
					pstmt.setInt(2, aId);
					pstmt.executeUpdate();
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
				} finally {
					try{
						if (saveVoteStmt != null){
							saveVoteStmt.close();
						}
						if (pstmt != null){
							pstmt.close();
						}
					} catch(Exception e){
						e.printStackTrace();
					}
				}		
			}
			
			/** Get User's last 5 Answers with questions */
			else if (uri.indexOf(QAndAConstants.USER_LAST_ANSWERED) != -1) {
				PreparedStatement pstmt = null;
				ResultSet rs = null;
				try {
					String strUserName =  request.getParameter("userNickName");
					pstmt = conn.prepareStatement(QAndAConstants.GET_USER_LAST_ANSWERS); 
					pstmt.setString(1, strUserName);
		    		Collection<QuestionAnswerPair> questAndAnsPairCollection = new ArrayList<QuestionAnswerPair>();
		    		Map<Integer, Integer> userQstVoteMap = getCurrUserQstVoteMap(loggedUserNickName, conn);
		    		Map<Integer, Integer> userAnsVoteMap = getCurrUserQstVoteMap(loggedUserNickName, conn);
		    		rs = pstmt.executeQuery();

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
	    			JsonRes = gson.toJson(questAndAnsPairCollection, QAndAConstants.QUESTION_AND_ANS_PAIR_COLLECTION);
	    			
				} catch (SQLException e) {
					getServletContext().log("Error while fetching User's last answers", e);
					response.sendError(500);//internal server error
				} finally {
					try{
						if (rs != null){
							rs.close();
						}
						if (pstmt != null){
							pstmt.close();
						}
					} catch(Exception e){
						e.printStackTrace();
					}
				}		
			}
   		   	if (!JsonRes.isEmpty()) {
   		   		writer = response.getWriter();
   		   		writer.println(JsonRes);
   		   	}
    		    		
		} catch (SQLException | NamingException e) {
			getServletContext().log("Error while closing connection", e);
    		response.sendError(500);//internal server error
    	// finally close connection and writer 
		} finally {
			try {
				if (conn != null){
					conn.close();
				}
				if (writer != null) {
					writer.close();
				}
			} catch (Exception e) {
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
