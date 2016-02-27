package allthatmusicgear.constants;

import java.lang.reflect.Type;
import java.util.Collection;

import com.google.gson.reflect.TypeToken;

import allthatmusicgear.model.Answer;
import allthatmusicgear.model.Question;
import allthatmusicgear.model.QuestionAnswerPair;
import allthatmusicgear.model.QuestionWithAnswers;
import allthatmusicgear.model.TopicQRatingPair;

/**
 * @author Adam Ma'or, Vadim Xaxam
 * 
 * This interface holds the constants for all Question and Answer handling
 * @see SQLSchema.sql
 */
public interface QAndAConstants {

	/* constants for URL in servlet */
	public final String QUESTION = "Question";
	public final String ANSWER = "Answer";
	public final String NEW_QUESTIONS = "NewQuestions";
	public final String ALL_QUESTIONS = "AllQuestions";
	public final String INSERT_QUESTION = "InsertQuestion";
	public final String INSERT_ANSWER = "InsertAnswer";
	public final String UPDATE_QUESTION = "UpdateQuestion";
	public final String UPDATE_ANSWER = "UpdateAnswer";
	public final String QUESTION_ANS = "AnswersOfQ";
	public final String USER_LAST_ASKED = "UserLastAskedQuestions";
	public final String USER_LAST_ANSWERED = "UserLastAnswerdAnswers";
	public final String TOPIC_BY_TPOP = "QuestionTopicsByTpop";
	public final String QUESTIONS_BY_TOPIC = "QuestionsByTopic";
	
	/* for GSON */
	public final Type QUESTION_COLLECTION = new TypeToken<Collection<Question>>() {}.getType();
	public final Type ANSWER_COLLECTION = new TypeToken<Collection<Answer>>() {}.getType();
	public final Type QUESTION__AND_ANS_COLLECTION = new TypeToken<Collection<QuestionWithAnswers>>() {}.getType();
	public final Type TOPIC_AND_TPOP_COLLECTION = new TypeToken<Collection<TopicQRatingPair>>() {}.getType();
	public final Type QUESTION_AND_ANS_PAIR_COLLECTION = new TypeToken<Collection<QuestionAnswerPair>>() {}.getType();
	
	/* see SQLSchema.sql */
	public final String COUNT_NEW_QUESTIONS = "SELECT COUNT(*) FROM tblQuestion "
			+ "WHERE tblQuestion.QID NOT IN	(SELECT DISTINCT QuestionID FROM tblAnswer)";
	public final String COUNT_ALL_QUESTIONS = "SELECT COUNT(*) FROM tblQuestion";
	public final String COUNT_ALL_TOPICS = "SELECT COUNT(DISTINCT tblQuestionTopics.Topic) FROM tblQuestionTopics";
	public final String COUNT_ALL_TOPIC_QUESTIONS = "SELECT COUNT(tblQuestionTopics.QID) FROM tblQuestionTopics WHERE tblQuestionTopics.Topic = ? ";
		
	public final String INSERT_NEW_QUESTION = "INSERT INTO tblQuestion (QUNickName, QText) VALUES (?,?)";
	public final String INSERT_TOPIC_TO_LATEST_QUESTION = "INSERT INTO tblQuestionTopics VALUES (IDENTITY_VAL_LOCAL(), ?)";
	public final String INSERT_NEW_ANSWER = "INSERT INTO tblAnswer (QuestionID, AUNickName, AText) VALUES (?,?,?)";
	
	public final String GET_NEW_QUESTIONS = "SELECT tblQuestion.*, tblUser.PhotoURL, tblUser.UserRating " 
										+ "FROM tblQuestion JOIN tblUser ON tblQuestion.QUNickName = tblUser.NickName "
										+ "WHERE tblQuestion.QID NOT IN (SELECT DISTINCT QuestionID FROM tblAnswer) "
										+ "ORDER BY tblQuestion.QSubmissionTime DESC "
										+ "OFFSET ? ROWS FETCH NEXT 20 ROWS ONLY ";
	
	public final String GET_ALL_QUESTIONS = "SELECT tblQuestion.*, tblUser.PhotoURL, tblUser.UserRating "
										+ "FROM tblQuestion JOIN tblUser ON tblQuestion.QUNickName = tblUser.NickName "
										+ "ORDER BY tblQuestion.QRating DESC "
										+ "OFFSET ? ROWS FETCH NEXT 20 ROWS ONLY ";
	
	public final String GET_ANSWERS_TO_QUESTION = "SELECT tblAnswer.*, tblUser.PhotoURL, tblUser.UserRating "
											+ "FROM tblAnswer JOIN tblUser ON tblAnswer.AUNickName = tblUser.NickName "
											+ "WHERE tblAnswer.QuestionID = ? "
											+ "ORDER BY tblAnswer.AVotingScore DESC";
	
	final public String GET_QUESTION_TOPICS = "SELECT Topic FROM tblQuestionTopics "
			+ "WHERE tblQuestionTopics.QID = ?";
	
	final public String GET_QUESTIONS_BY_TOPIC = "SELECT tblQuestion.*, tblUser.PhotoURL, tblUser.UserRating "
			+ "FROM tblQuestionTopics "
			+ "JOIN tblQuestion ON tblQuestionTopics.QID = tblQuestion.QID "
			+ "JOIN tblUser ON tblQuestion.QUNickName = tblUser.NickName "
			+ "WHERE tblQuestionTopics.Topic = ? "
			+ "ORDER BY tblQuestion.QRating DESC "
			+ "OFFSET ? ROWS FETCH NEXT 20 ROWS ONLY ";
	
	final public String GET_TOPICS_BY_POPULARITY = "SELECT tblQuestionTopics.Topic, SUM(tblQuestion.QRating) AS TPop "
			+ "FROM tblQuestionTopics JOIN tblQuestion "
			+ "ON tblQuestionTopics.QID = tblQuestion.QID "
			+ "GROUP BY tblQuestionTopics.Topic "
			+ "ORDER BY TPop DESC "
			+ "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY ";
		
	
	public final String GET_QUESTION_SCORES = "SELECT tblQuestion.QVotingScore, AVG(Cast(tblAnswer.AVotingScore as DOUBLE)) as AVGAnswerScore"
											+ " FROM tblQuestion LEFT OUTER JOIN tblAnswer ON tblQuestion.QID = tblAnswer.QuestionID"
											+ " WHERE tblQuestion.QID = ?"
											+ " GROUP BY tblQuestion.QVotingScore";
	
	public final String UPDATE_QUESTION_SCORES = "UPDATE tblQuestion "
			+ "SET tblQuestion.QVotingScore = ?, tblQuestion.QRating = ? "
			+ "WHERE tblQuestion.QID = ?";
	
	public final String ADD_QUESTION_VOTE = "INSERT INTO tblQuestionVotes VALUES(?,?,?)";
	
	public final String ADD_ANSWER_VOTE = "INSERT INTO tblAnswerVotes VALUES(?,?,?)";
	
	public final String VOTE_ANSWER = "UPDATE tblAnswer SET AVotingScore = AVotingScore + ? WHERE AID = ?";
		
	public final String GET_USER_LAST_QUESTION = "SELECT tblQuestion.*, tblUser.PhotoURL, tblUser.UserRating "
			+ "FROM tblQuestion JOIN tblUser ON tblQuestion.QUNickName = tblUser.NickName "
			+ "WHERE tblQuestion.QUNickName = ? "
			+ "ORDER BY tblQuestion.QSubmissionTime DESC "
			+ "FETCH FIRST 5 ROWS ONLY ";
	
	public final String GET_USER_LAST_ANSWERS = "SELECT tblQuestion.*, qUser.PhotoURL, qUser.UserRating, tblAnswer.*, aUser.PhotoURL, aUser.UserRating "
			+ "FROM tblQuestion JOIN tblUser qUser ON tblQuestion.QUNickName = qUser.NickName "
			+ "JOIN tblAnswer ON tblQuestion.QID = tblAnswer.QuestionID "
			+ "JOIN tblUser aUser ON tblAnswer.AUNickName = aUser.NickName "
			+ "WHERE tblAnswer.AUNickName = ? "
			+ "ORDER BY tblAnswer.ASubmissionTime DESC "
			+ "FETCH FIRST 5 ROWS ONLY ";

	
}
