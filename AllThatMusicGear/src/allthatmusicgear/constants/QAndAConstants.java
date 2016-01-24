package allthatmusicgear.constants;

import java.lang.reflect.Type;
import java.util.Collection;

import com.google.gson.reflect.TypeToken;

import allthatmusicgear.model.Answer;
import allthatmusicgear.model.Question;

public interface QAndAConstants {

	public final String QUESTION = "Question";
	public final String ANSWER = "Answer";
	public final String NEW_QUESTIONS = "New"+QUESTION+"s";
	public final String ALL_QUESTIONS = "All"+QUESTION+"s";
	public final String QUESTION_ANS = "AnsOf"+QUESTION;
	public final String INSERT_QUESTION = "Insert"+QUESTION;
	public final String INSERT_ANSWER = "Insert"+ANSWER;
	public final String UPDATE_QUESTION = "Update"+QUESTION;
	public final String UPDATE_ANSWER = "Update"+ANSWER;
	
	public final Type QUESTION_COLLECTION = new TypeToken<Collection<Question>>() {}.getType();
	public final Type ANSWER_COLLECTION = new TypeToken<Collection<Answer>>() {}.getType();
	
	
	public final String INSERT_NEW_QUESTION = "INSERT INTO app.tblQuestion (QUNickName, QText) VALUES (?,?)";
	public final String INSERT_TOPIC_TO_LATEST_QUESTION = "INSERT INTO app.tblQuestionTopics VALUES (IDENTITY_VAL_LOCAL(), ?)";
	public final String GET_LATEST_QUESTION = "SELECT * FROM app.tblQuestion WHERE QID = IDENTITY_VAL_LOCAL()";
	public final String INSERT_NEW_ANSWER = "INSERT INTO app.tblAnswer (QuestionID, AUNickName, AText) VALUES (?,?,?)";
	
	public final String GET_NEW_QUESTIONS = "SELECT * FROM app.tblQuestion "
										+ "WHERE app.tblQuestion.QID NOT IN	(SELECT DISTINCT QuestionID FROM app.tblAnswer)"
										+ "ORDER BY app.tblQuestion.QSubmissionTime DESC" ;
	
	public final String GET_ALL_QUESTIONS = "SELECT * FROM app.tblQuestion ORDER BY app.tblQuestion.QRating DESC";
	
	public final String GET_ANSWERS_TO_QUESTION = "SELECT * FROM app.tblAnswer "
											+ "WHERE app.tblAnswer.QuestionID = ? "
											+ "ORDER BY app.tblAnswer.AVotingScore DESC";
	
	final public String GET_QUESTION_TOPICS = "SELECT Topic FROM app.tblQuestionTopics WHERE app.tblQuestionTopics.QID = ?";
	
	public final String GET_QUESTION_SCORES = "SELECT app.tblQuestion.QVotingScore, AVG(app.tblAnswer.AVotingScore) as AVGAnswerScore"
											+ " FROM app.tblQuestion LEFT OUTER JOIN app.tblAnswer ON app.tblQuestion.QID = app.tblAnswer.QuestionID"
											+ " WHERE app.tblQuestion.QID=?"
											+ " GROUP BY app.tblQuestion.QVotingScore";
	
	public final String UPDATE_QUESTION_SCORES = "UPDATE app.tblQuestion SET app.tblQuestion.QVotingScore = ?, app.tblQuestion.QRating = ? WHERE app.tblQuestion.QID=?";
	
	public final String VOTE_ANSWER_POS = "UPDATE tblAnswer SET AVotingScore = AVotingScore + 1 ,WHERE QID=?";
	
	public final String VOTE_ANSWER_NEG = "UPDATE tblAnswer SET AVotingScore = AVotingScore - 1 ,WHERE QID=?";

	
}
