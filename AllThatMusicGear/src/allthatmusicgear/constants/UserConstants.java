/**
 * 
 */
package allthatmusicgear.constants;

import java.lang.reflect.Type;
import java.util.Collection;

import com.google.gson.reflect.TypeToken;

import allthatmusicgear.model.User;

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
	
	public final String COUNT_ALL_USERS = "SELECT COUNT(*) FROM tblUser";

	
	public final String GET_USER_NICK_FROM_QUESTION = "SELECT tblQuestion.QUNickName FROM tblQuestion WHERE tblQuestion.qID = ?";
	public final String GET_USER_NICK_FROM_ANSWER = "SELECT tblAnswer.AUNickName FROM tblAnswer WHERE tblAnswer.aID = ?";
		
	public final String GET_USER_AVG_Q_SCORES = "SELECT AVG(tblQuestion.QRating) FROM tblQuestion WHERE tblQuestion.QUNickName = ? ";
	public final String GET_USER_AVG_A_SCORES = "SELECT AVG(tblAnswer.AVotingScore) FROM tblAnswer WHERE tblAnswer.AUNickName = ? ";
	public final String UPDATE_UR_QUERY = "UPDATE tblUser SET tblUser.UserRating = ? WHERE tblUser.NickName=?";
	
	public final String GET_USER_QST_VOTE = "SELECT tblQuestionVotes.QID, tblQuestionVotes.Vote "
										+ "FROM tblQuestionVotes "
										+ "WHERE tblQuestionVotes.UNickName = ? ";
	public final String GET_USER_ANS_VOTE = "SELECT tblAnswerVotes.AID, tblAnswerVotes.Vote "
										+ "FROM tblAnswerVotes "
										+ "WHERE tblAnswerVotes.UNickName = ? ";
	
	public final String GET_USER_EXPERTISE = "SELECT tblQuestionTopics.topic as Topic, "
											+ "SUM(tblAnswer.AVotingScore) as TotalVotes "
											+ "FROM tblAnswer JOIN tblQuestionTopics "
											+ "ON tblAnswer.QuestionID = tblQuestionTopics.QID "
											+ "WHERE tblAnswer.AUNickName = ? "
											+ "GROUP BY tblQuestionTopics.topic "
											+ "ORDER BY TotalVotes DESC "
											+ "FETCH FIRST 5 ROWS ONLY ";
	
	public final String GET_USER_INFO_QUERY = "SELECT NICKNAME,DESCREPTION,PHOTOURL,USERRATING " +
			"FROM tblUser WHERE tblUser.NickName=?";
	public final String GET_ALL_USERS_INFO_QUERY = "SELECT NICKNAME,DESCREPTION,PHOTOURL,USERRATING " 
												+ "FROM tblUser ORDER BY USERRATING DESC "
												+ "OFFSET ? ROWS FETCH NEXT 20 ROWS ONLY ";


}
