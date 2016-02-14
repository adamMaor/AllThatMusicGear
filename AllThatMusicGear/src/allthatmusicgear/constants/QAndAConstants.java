package allthatmusicgear.constants;

import java.lang.reflect.Type;
import java.util.Collection;

import com.google.gson.reflect.TypeToken;

import allthatmusicgear.model.Answer;
import allthatmusicgear.model.Question;
import allthatmusicgear.model.QuestionAnswerPair;
import allthatmusicgear.model.QuestionWithAnswers;
import allthatmusicgear.model.TopicQRatingPair;

public interface QAndAConstants {

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
	
	public final Type QUESTION_COLLECTION = new TypeToken<Collection<Question>>() {}.getType();
	public final Type ANSWER_COLLECTION = new TypeToken<Collection<Answer>>() {}.getType();
	public final Type QUESTION__AND_ANS_COLLECTION = new TypeToken<Collection<QuestionWithAnswers>>() {}.getType();
	public final Type TOPIC_AND_TPOP_COLLECTION = new TypeToken<Collection<TopicQRatingPair>>() {}.getType();
	public final Type QUESTION_AND_ANS_PAIR_COLLECTION = new TypeToken<Collection<QuestionAnswerPair>>() {}.getType();
	
	
	public final String COUNT_NEW_QUESTIONS = "SELECT COUNT(*) FROM app.tblQuestion "
			+ "WHERE app.tblQuestion.QID NOT IN	(SELECT DISTINCT QuestionID FROM app.tblAnswer)";
	public final String COUNT_ALL_QUESTIONS = "SELECT COUNT(*) FROM app.tblQuestion";
	public final String COUNT_ALL_TOPICS = "SELECT COUNT(DISTINCT app.tblQuestionTopics.Topic) FROM app.tblQuestionTopics";
	public final String COUNT_ALL_TOPIC_QUESTIONS = "SELECT COUNT(app.tblQuestionTopics.QID) FROM app.tblQuestionTopics WHERE app.tblQuestionTopics.Topic = ? ";
	
	
	
	public final String INSERT_NEW_QUESTION = "INSERT INTO app.tblQuestion (QUNickName, QText) VALUES (?,?)";
	public final String INSERT_TOPIC_TO_LATEST_QUESTION = "INSERT INTO app.tblQuestionTopics VALUES (IDENTITY_VAL_LOCAL(), ?)";
	public final String GET_LATEST_QUESTION = "SELECT * FROM app.tblQuestion WHERE QID = IDENTITY_VAL_LOCAL()";
	public final String INSERT_NEW_ANSWER = "INSERT INTO app.tblAnswer (QuestionID, AUNickName, AText) VALUES (?,?,?)";
	
	public final String GET_NEW_QUESTIONS = "SELECT app.tblQuestion.*, app.tblUser.PhotoURL, app.tblUser.UserRating " 
										+ "FROM app.tblQuestion JOIN app.tblUser ON app.tblQuestion.QUNickName = app.tblUser.NickName "
										+ "WHERE app.tblQuestion.QID NOT IN (SELECT DISTINCT QuestionID FROM app.tblAnswer) "
										+ "ORDER BY app.tblQuestion.QSubmissionTime DESC "
										+ "OFFSET ? ROWS FETCH NEXT 20 ROWS ONLY ";
	
	public final String GET_ALL_QUESTIONS = "SELECT app.tblQuestion.*, app.tblUser.PhotoURL, app.tblUser.UserRating "
										+ "FROM app.tblQuestion JOIN app.tblUser ON app.tblQuestion.QUNickName = app.tblUser.NickName "
										+ "ORDER BY app.tblQuestion.QRating DESC "
										+ "OFFSET ? ROWS FETCH NEXT 20 ROWS ONLY ";
	
	public final String GET_ANSWERS_TO_QUESTION = "SELECT app.tblAnswer.*, app.tblUser.PhotoURL, app.tblUser.UserRating "
											+ "FROM app.tblAnswer JOIN app.tblUser ON app.tblAnswer.AUNickName = app.tblUser.NickName "
											+ "WHERE app.tblAnswer.QuestionID = ? "
											+ "ORDER BY app.tblAnswer.AVotingScore DESC";
	
	final public String GET_QUESTION_TOPICS = "SELECT Topic FROM app.tblQuestionTopics "
			+ "WHERE app.tblQuestionTopics.QID = ?";
	
	final public String GET_QUESTIONS_BY_TOPIC = "SELECT app.tblQuestion.*, app.tblUser.PhotoURL, app.tblUser.UserRating "
			+ "FROM app.tblQuestionTopics "
			+ "JOIN app.tblQuestion ON app.tblQuestionTopics.QID = app.tblQuestion.QID "
			+ "JOIN app.tblUser ON app.tblQuestion.QUNickName = app.tblUser.NickName "
			+ "WHERE app.tblQuestionTopics.Topic = ? "
			+ "ORDER BY app.tblQuestion.QRating DESC "
			+ "OFFSET ? ROWS FETCH NEXT 20 ROWS ONLY ";
	
	final public String GET_TOPICS_BY_POPULARITY = "SELECT tblQuestionTopics.Topic,  SUM(app.tblQuestion.QRating) as TPop "
			+ "FROM app.tblQuestionTopics JOIN app.tblQuestion "
			+ "ON app.tblQuestionTopics.QID = app.tblQuestion.QID "
			+ "GROUP BY tblQuestionTopics.Topic "
			+ "ORDER BY TPop DESC "
			+ "OFFSET ? ROWS FETCH NEXT 20 ROWS ONLY ";
		
	
	public final String GET_QUESTION_SCORES = "SELECT app.tblQuestion.QVotingScore, AVG(Cast(app.tblAnswer.AVotingScore as Float)) as AVGAnswerScore"
											+ " FROM app.tblQuestion LEFT OUTER JOIN app.tblAnswer ON app.tblQuestion.QID = app.tblAnswer.QuestionID"
											+ " WHERE app.tblQuestion.QID=?"
											+ " GROUP BY app.tblQuestion.QVotingScore";
	
	public final String UPDATE_QUESTION_SCORES = "UPDATE app.tblQuestion "
			+ "SET app.tblQuestion.QVotingScore = ?, app.tblQuestion.QRating = ? "
			+ "WHERE app.tblQuestion.QID=?";
	
	public final String ADD_QUESTION_VOTE = "INSERT INTO app.tblQuestionVotes VALUES(?,?,?)";
	
	public final String ADD_ANSWER_VOTE = "INSERT INTO app.tblAnswerVotes VALUES(?,?,?)";
	
	public final String VOTE_ANSWER = "UPDATE app.tblAnswer SET AVotingScore = AVotingScore + ? WHERE AID=?";
		
	public final String GET_USER_LAST_QUESTION = "SELECT app.tblQuestion.*, app.tblUser.PhotoURL, app.tblUser.UserRating "
			+ "FROM app.tblQuestion JOIN app.tblUser ON app.tblQuestion.QUNickName = app.tblUser.NickName "
			+ "WHERE app.tblQuestion.QUNickName = ? "
			+ "ORDER BY app.tblQuestion.QSubmissionTime DESC "
			+ "FETCH FIRST 5 ROWS ONLY ";
	
	public final String GET_USER_LAST_ANSWERS = "SELECT app.tblQuestion.*, qUser.PhotoURL, qUser.UserRating, app.tblAnswer.*, aUser.PhotoURL, aUser.UserRating "
			+ "FROM app.tblQuestion JOIN app.tblUser AS qUser ON app.tblQuestion.QUNickName = qUser.NickName "
			+ "JOIN app.tblAnswer ON app.tblQuestion.QID = app.tblAnswer.QuestionID "
			+ "JOIN app.tblUser AS aUser ON app.tblAnswer.AUNickName = aUser.NickName "
			+ "WHERE app.tblAnswer.AUNickName = ? "
			+ "ORDER BY app.tblAnswer.ASubmissionTime DESC "
			+ "FETCH FIRST 5 ROWS ONLY ";

	
}
