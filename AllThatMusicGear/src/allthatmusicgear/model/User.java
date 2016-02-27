package allthatmusicgear.model;

import java.util.List;

/**
 * this model class is used to represent a User in the system
 * @author Adam Ma'or, Vadim Xaxam
 *
 */
public class User {
	/**
	 * @param vNickName - User's NickName
	 * @param vDesc - User's Description
	 * @param vPhoto - User's photoURL Link
	 * @param RatingScore - User's Rating score
	 * @param vExpertise - User's expertise list
	 */
	public User (String vNickName, String vDesc, String vPhoto, double RatingScore, List<String> vExpertise)
	{
		nickName = vNickName;
		descreption = vDesc;
		photoURL = vPhoto;
		userRating = RatingScore;
		expertise = vExpertise;
	}
	
	/* Members */
	@SuppressWarnings("unused")
	private String nickName, descreption, photoURL;
	@SuppressWarnings("unused")
	private double userRating;
	List<String> expertise;
	
}
