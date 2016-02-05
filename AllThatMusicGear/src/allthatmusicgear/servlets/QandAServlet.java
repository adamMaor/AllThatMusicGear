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

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


//		public final String NEW_QUESTION = "NewQuestions";
//		public final String ALL_QUESTION = "AllQuestions";
//		public final String QUESTION_ANSWERS = "AnswersOfQuestion";
//		public final String INSERT_QUESTION = "InsertQuestion";
//		public final String INSERT_ANSWER = "InsertAnswer";
//		public final String UPDATE_QUESTION = "UpdateQuestion";
//		public final String UPDATE_ANSWER = "UpdateAnswer";
		
		try {
//			System.out.println("In Servlet doGet: URI = " + request.getRequestURI());
			
			Context context = new InitialContext();
    		BasicDataSource ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
    		Connection conn = ds.getConnection();
    		String uri = request.getRequestURI();
    		Collection<Question> questCollection = new ArrayList<Question>();
    		Collection<Answer> ansCollection = new ArrayList<Answer>();
    		Collection<QuestionAnswerPair> questAndAnsPairCollection = new ArrayList<QuestionAnswerPair>();
    		boolean bISQuestRelated = false;
    		boolean bIsQuestwithAns = false;
    		
    		/* First Big Case - It deals with Questions */
    		if (uri.indexOf(QAndAConstants.QUESTION) != -1)
    		{
    			bISQuestRelated = true;
    			/* First Question case - get New submitted questions */
    			if (uri.indexOf(QAndAConstants.NEW_QUESTIONS) != -1)
    			{
    				Statement stmt;
    				try {
    					stmt = conn.createStatement();
    					ResultSet QuestionRS = stmt.executeQuery(QAndAConstants.GET_NEW_QUESTIONS);
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
    				}  catch (SQLException e) {
    					getServletContext().log("Error while Inserting a New Question", e);
    					response.sendError(500);//internal server error
    				}	
    				
    			}
    			
    			else if (uri.indexOf(QAndAConstants.UPDATE_QUESTION) != -1){
    				try {
    					PreparedStatement pstmt;
    					pstmt = conn.prepareStatement(QAndAConstants.GET_QUESTION_SCORES); 
    					pstmt.setInt(1, Integer.parseInt(request.getParameter("qId")));
    					int votingScoreChange = Integer.parseInt(request.getParameter("changeVS"));
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
    						updatePstmt.setInt(3, Integer.parseInt(request.getParameter("qId")));
    						updatePstmt.executeUpdate();
    						//commit update
    						conn.commit();
    						updatePstmt.close();   						
    					}
    					
    					QuestionRS.close();
    					//close statements
    					pstmt.close();    			    					
    				} catch (SQLException e) {
    					getServletContext().log("Error while Updating Question", e);
    					response.sendError(500);//internal server error
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
    					while (rs.next())
    					{
    						int qID = rs.getInt(1);
    						List<String> qTopicList = getQuestionTopics(qID, conn);  						    						
    						questCollection.add(new Question(rs.getInt(1), 
						    								rs.getString(2), 
						    								rs.getString(3), 
						    								rs.getTimestamp(4).getTime(), 
						    								rs.getInt(5), 
						    								rs.getDouble(6), 
						    								qTopicList));
    						
    					}
    					rs.close();
    					pstmt.close();				
    				} catch (SQLException e) {
    					getServletContext().log("Error while fetching User last questions", e);
    					response.sendError(500);//internal server error
    				}
        		}
    		}
    		/* Second Big Case - It deals with Answers */
    		else if (uri.indexOf(QAndAConstants.ANSWER) != -1)
    		{
    			bISQuestRelated = false;
    			if (uri.indexOf(QAndAConstants.QUESTION_ANS) != -1) {    				    				
    				try{
    					PreparedStatement pstmt;
    					pstmt = conn.prepareStatement(QAndAConstants.GET_ANSWERS_TO_QUESTION); 
    					pstmt.setInt(1, Integer.parseInt(request.getParameter("qID")));
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
    				}  catch (SQLException e) {
    					getServletContext().log("Error while querying for Answers to Question", e);
    					response.sendError(500);//internal server error
    				}	
    			}
    			
    			
    			else if (uri.indexOf(QAndAConstants.INSERT_ANSWER) != -1){
    				try{
    					PreparedStatement pstmt;
    					pstmt = conn.prepareStatement(QAndAConstants.INSERT_NEW_ANSWER); 
    					pstmt.setInt(1, Integer.parseInt(request.getParameter("qID")));
    					pstmt.setString(2, (String)request.getSession().getAttribute("LoggedInUserNickName"));
    					pstmt.setString(3, request.getParameter("aText"));
    					pstmt.executeUpdate();
    					
    					//commit update
    					conn.commit();
    					//close statements
    					pstmt.close();	    				   					
    				}  catch (SQLException e) {
    					getServletContext().log("Error while Inserting a New Answer", e);
    					response.sendError(500);//internal server error
    				}	
    			}
    			
    			else if (uri.indexOf(QAndAConstants.UPDATE_ANSWER_POS) != -1) {
    				try{
    					PreparedStatement pstmt;
    					pstmt = conn.prepareStatement(QAndAConstants.VOTE_ANSWER_POS); 
    					pstmt.setInt(1, Integer.parseInt(request.getParameter("aID")));
    					pstmt.executeUpdate();
    					
    					//commit update
    					conn.commit();
    					//close statements
    					pstmt.close();	    				   					
    				}  catch (SQLException e) {
    					getServletContext().log("Error while Updating Pos Answer Vote", e);
    					response.sendError(500);//internal server error
    				}	
    			}
    			
    			else if (uri.indexOf(QAndAConstants.UPDATE_ANSWER_NEG) != -1) {
    				try{
    					PreparedStatement pstmt;
    					pstmt = conn.prepareStatement(QAndAConstants.VOTE_ANSWER_NEG); 
    					pstmt.setInt(1, Integer.parseInt(request.getParameter("aID")));
    					pstmt.executeUpdate();
    					
    					//commit update
    					conn.commit();
    					//close statements
    					pstmt.close();	    				   					
    				}  catch (SQLException e) {
    					getServletContext().log("Error while Updating Neg Answer Vote", e);
    					response.sendError(500);//internal server error
    				}
    			}
    				else if (uri.indexOf(QAndAConstants.USER_LAST_ANSWERED) != -1)
    				{
    					bIsQuestwithAns = true;
    					try {
    						PreparedStatement pstmt;
    						String strUserName =  request.getParameter("userNickName");
    						pstmt = conn.prepareStatement(QAndAConstants.GET_USER_LAST_ANSWERS); 
    						pstmt.setString(1, strUserName);
    						ResultSet rs = pstmt.executeQuery();
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
    					} catch (SQLException e) {
    						getServletContext().log("Error while fetching User's last answers", e);
    						response.sendError(500);//internal server error
    					}
    			}
    		}
    		
    		
    		conn.close();
    		Gson gson = new Gson();
    		String JsonRes;
    		if (bIsQuestwithAns) {
    			JsonRes = gson.toJson(questAndAnsPairCollection, QAndAConstants.QUESTION__AND_ANS_COLLECTION);
    		}
    		else if (bISQuestRelated){
    			JsonRes = gson.toJson(questCollection, QAndAConstants.QUESTION_COLLECTION);  			
    		}
    		else{
    			JsonRes = gson.toJson(ansCollection, QAndAConstants.ANSWER_COLLECTION);
    		}
    		
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

}
