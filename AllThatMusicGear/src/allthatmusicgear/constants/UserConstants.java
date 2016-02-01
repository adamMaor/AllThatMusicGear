/**
 * 
 */
package allthatmusicgear.constants;

import java.lang.reflect.Type;
import java.util.Collection;

import com.google.gson.reflect.TypeToken;

import allthatmusicgear.model.Question;

/**
 * @author Adam
 *
 */
public interface UserConstants {
	
	public final String UPDATE_UR = "UpdateUserRating";
	public final String USER_EXPERTISE = "UserExpertise";

	
	
	public final Type TOPIC_LIST = new TypeToken<Collection<String>>() {}.getType();
	
	public final String GET_USER_AVG_Q_SCORES = "SELECT AVG(app.tblQuestion.QRating) FROM app.tblQuestion WHERE app.tblQuestion.QUNickName = ? ";
	public final String GET_USER_AVG_A_SCORES = "SELECT AVG(app.tblAnswer.AVotingScore) FROM app.tblAnswer WHERE app.tblAnswer.AUNickName = ? ";
	public final String UPDATE_UR_QUERY = "UPDATE app.tblUser SET app.tblUser.UserRating = ? WHERE app.tblUser.NickName=?";
	
	public final String GET_USER_EXPERTISE = "SELECT app.tblQuestionTopics.topic as Topic,"
			+ "COUNT(app.tblAnswer.AVotingScore) as TotalVotes"
			+ "FROM app.tblAnswer LEFT OUTER JOIN app.tblQuestionTopics ON app.tblAnswer.QuestionID = app.tblQuestionTopics.QID"
			+ "WHERE app.tblAnswer.AUNickName = ?"
			+ "GROUP BY app.tblQuestionTopics.topic"
			+ "ORDER BY TotalVotes DESC";
	

}
