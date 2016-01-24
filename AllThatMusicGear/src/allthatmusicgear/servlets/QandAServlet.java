package allthatmusicgear.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
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
import allthatmusicgear.model.Answer;
import allthatmusicgear.model.Question;
import allthatmusicgear.model.User;

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
    		boolean bISQuestRelated = false;
    		
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
    					getServletContext().log("Error while querying for New Questions", e);
    					response.sendError(500);//internal server error
    				}				
    			}
    			
    			else if (uri.indexOf(QAndAConstants.INSERT_QUESTION) != -1){
    				PreparedStatement pstmt;
    				pstmt = conn.prepareStatement(QAndAConstants.INSERT_NEW_QUESTION); 
    				pstmt.setString(1, request.getParameter("userNickName"));
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
    			}
    			
    			else if (uri.indexOf(QAndAConstants.UPDATE_QUESTION) != -1){
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
    			}
    		}
    		/* Second Big Case - It deals with Answers */
    		else if (uri.indexOf(QAndAConstants.ANSWER) != -1)
    		{
    			
    		}
    		
    		
    		conn.close();
    		Gson gson = new Gson();
    		String JsonRes;
    		if (bISQuestRelated){
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
