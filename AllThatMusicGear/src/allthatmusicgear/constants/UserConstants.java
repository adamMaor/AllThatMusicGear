/**
 * 
 */
package allthatmusicgear.constants;

/**
 * @author Adam
 *
 */
public interface UserConstants {
	
	public final String UPDATE_UR = "UpdateUserRating";
	
	public final String GET_USER_AVG_Q_SCORES = "SELECT AVG(app.tblQuestion.QRating) FROM app.tblQuestion WHERE app.tblQuestion.QUNickName = ? ";
	public final String GET_USER_AVG_A_SCORES = "SELECT AVG(app.tblAnswer.AVotingScore) FROM app.tblAnswer WHERE app.tblAnswer.AUNickName = ? ";
	public final String UPDATE_UR_QUERY = "UPDATE app.tblUser SET app.tblUser.UserRating = ? WHERE app.tblUser.NickName=?";
}
