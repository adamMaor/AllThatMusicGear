/**
 * 
 */
package allthatmusicgear.constants;

import java.lang.reflect.Type;
import java.util.Collection;

import com.google.gson.reflect.TypeToken;

import allthatmusicgear.model.Question;
import allthatmusicgear.model.User;
import allthatmusicgear.model.QuestionAnswerPair;

/**
 * @author Adam
 *
 */
public interface UserConstants {
	
	public final String UPDATE_UR = "UpdateUserRating";
	public final String USER_EXPERTISE = "UserExpertise";
	public final String GET_SESSION_INFO = "GetSessionInfo";
	public final String LOGOUT = "LogOut";
	public final String GET_USER_INFO = "GetUserInfo";
	public final String GET_ALL_USERS_INFO = "GetAllUsersInfo";
	
	
	public final Type TOPIC_LIST = new TypeToken<Collection<String>>() {}.getType();
	public final Type USER_COLLECTION = new TypeToken<Collection<User>>() {}.getType();
	
	public final String COUNT_ALL_USERS = "SELECT COUNT(*) FROM app.tblUser";

	
	public final String GET_USER_NICK_FROM_QUESTION = "SELECT app.tblQuestion.QUNickName FROM app.tblQuestion WHERE app.tblQuestion.qID = ?";
	public final String GET_USER_NICK_FROM_ANSWER = "SELECT app.tblAnswer.AUNickName FROM app.tblAnswer WHERE app.tblAnswer.aID = ?";
		
	public final String GET_USER_AVG_Q_SCORES = "SELECT AVG(app.tblQuestion.QRating) FROM app.tblQuestion WHERE app.tblQuestion.QUNickName = ? ";
	public final String GET_USER_AVG_A_SCORES = "SELECT AVG(app.tblAnswer.AVotingScore) FROM app.tblAnswer WHERE app.tblAnswer.AUNickName = ? ";
	public final String UPDATE_UR_QUERY = "UPDATE app.tblUser SET app.tblUser.UserRating = ? WHERE app.tblUser.NickName=?";
	
	public final String GET_USER_ANS_VOTE = "SELECT app.tblQuestionVotes.QID, app.tblQuestionVotes.Vote "
										+ "FROM app.tblQuestionVotes "
										+ "WHERE app.tblQuestionVotes.UNickName = ? ";
	public final String GET_USER_QST_VOTE = "SELECT app.tblAnswerVotes.AID, app.tblAnswerVotes.Vote "
										+ "FROM app.tblAnswerVotes "
										+ "WHERE app.tblAnswerVotes.UNickName = ? ";
	
	public final String GET_USER_EXPERTISE = "SELECT app.tblQuestionTopics.topic as Topic, "
			+ "SUM(app.tblAnswer.AVotingScore) as TotalVotes "
			+ "FROM app.tblAnswer JOIN app.tblQuestionTopics "
			+ "ON app.tblAnswer.QuestionID = app.tblQuestionTopics.QID "
			+ "WHERE app.tblAnswer.AUNickName = ? "
			+ "GROUP BY app.tblQuestionTopics.topic "
			+ "ORDER BY TotalVotes DESC "
			+ "FETCH FIRST 5 ROWS ONLY ";
	
	public final String GET_USER_INFO_QUERY = "SELECT NICKNAME,DESCREPTION,PHOTOURL,USERRATING " +
			"FROM app.tblUser WHERE app.tblUser.NickName=?";
	public final String GET_ALL_USERS_INFO_QUERY = "SELECT NICKNAME,DESCREPTION,PHOTOURL,USERRATING " +
			"FROM app.tblUser ORDER BY USERRATING DESC";


}
