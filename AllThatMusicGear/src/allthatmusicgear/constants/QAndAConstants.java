package allthatmusicgear.constants;

import java.lang.reflect.Type;
import java.util.Collection;

import com.google.gson.reflect.TypeToken;

import allthatmusicgear.model.Answer;
import allthatmusicgear.model.Question;
import allthatmusicgear.model.QuestionAnswerPair;

public interface QAndAConstants {

	public final String QUESTION = "Question";
	public final String ANSWER = "Answer";
	public final String NEW_QUESTIONS = "New"+QUESTION+"s";
	public final String ALL_QUESTIONS = "All"+QUESTION+"s";
	public final String INSERT_QUESTION = "Insert"+QUESTION;
	public final String INSERT_ANSWER = "Insert"+ANSWER;
	public final String UPDATE_QUESTION = "Update"+QUESTION;
	public final String UPDATE_ANSWER = "Update"+ANSWER;
	public final String QUESTION_ANS = ANSWER+"sOfQ";
	public final String UPDATE_ANSWER_NEG = "Update"+ANSWER+"Neg";
	public final String USER_LAST_ASKED = "UserLastAsked"+QUESTION+"s";
	public final String USER_LAST_ANSWERED = "UserLastAnswerd"+ANSWER+"s";
	
	public final Type QUESTION_COLLECTION = new TypeToken<Collection<Question>>() {}.getType();
	public final Type ANSWER_COLLECTION = new TypeToken<Collection<Answer>>() {}.getType();
	public final Type QUESTION__AND_ANS_COLLECTION = new TypeToken<Collection<QuestionAnswerPair>>() {}.getType();
	
	
	public final String INSERT_NEW_QUESTION = "INSERT INTO app.tblQuestion (QUNickName, QText) VALUES (?,?)";
	public final String INSERT_TOPIC_TO_LATEST_QUESTION = "INSERT INTO app.tblQuestionTopics VALUES (IDENTITY_VAL_LOCAL(), ?)";
	public final String GET_LATEST_QUESTION = "SELECT * FROM app.tblQuestion WHERE QID = IDENTITY_VAL_LOCAL()";
	public final String INSERT_NEW_ANSWER = "INSERT INTO app.tblAnswer (QuestionID, AUNickName, AText) VALUES (?,?,?)";
	
	public final String GET_NEW_QUESTIONS = "SELECT * FROM app.tblQuestion "
										+ "WHERE app.tblQuestion.QID NOT IN	(SELECT DISTINCT QuestionID FROM app.tblAnswer) "
										+ "ORDER BY app.tblQuestion.QSubmissionTime DESC ";
//										TODO: when you'd like to limit the rows you fetch
//										+ "OFFSET ? ROWS FETCH NEXT 20 ROWS ONLY";
//	
//	SELECT COUNTRY , count(*) as frequency
//	FROM CUSTOMERS
//	GROUP BY COUNTRY
//	order by count(*) desc
	
	public final String GET_ALL_QUESTIONS = "SELECT * FROM app.tblQuestion ORDER BY app.tblQuestion.QRating DESC";
	
	public final String GET_ANSWERS_TO_QUESTION = "SELECT * FROM app.tblAnswer "
											+ "WHERE app.tblAnswer.QuestionID = ? "
											+ "ORDER BY app.tblAnswer.AVotingScore DESC";
	
	final public String GET_QUESTION_TOPICS = "SELECT Topic FROM app.tblQuestionTopics WHERE app.tblQuestionTopics.QID = ?";
	
	//final public String GET_QUESTIONS_BY_TOPICS = ""
	
	public final String GET_QUESTION_SCORES = "SELECT app.tblQuestion.QVotingScore, AVG(app.tblAnswer.AVotingScore) as AVGAnswerScore"
											+ " FROM app.tblQuestion LEFT OUTER JOIN app.tblAnswer ON app.tblQuestion.QID = app.tblAnswer.QuestionID"
											+ " WHERE app.tblQuestion.QID=?"
											+ " GROUP BY app.tblQuestion.QVotingScore";
	
	public final String UPDATE_QUESTION_SCORES = "UPDATE app.tblQuestion SET app.tblQuestion.QVotingScore = ?, app.tblQuestion.QRating = ? WHERE app.tblQuestion.QID=?";
	
	public final String VOTE_ANSWER = "UPDATE app.tblAnswer SET AVotingScore = AVotingScore + ? WHERE AID=?";
		
	public final String GET_USER_LAST_QUESTION = "SELECT * FROM app.tblQuestion WHERE app.tblQuestion.QUNickName = ?"
			+ "ORDER BY app.tblQuestion.QSubmissionTime DESC FETCH FIRST 5 ROWS ONLY ";
	
	public final String GET_USER_LAST_ANSWERS = "SELECT * FROM app.tblQuestion JOIN app.tblAnswer WHERE app.tblAnswer.AUNickName = ?"
			+ "ORDER BY app.tblAnswer.ASubmissionTime DESC FETCH FIRST 5 ROWS ONLY";

	
}
