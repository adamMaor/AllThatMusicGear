package allthatmusicgear.model;

import java.util.List;

public class User {
	public User (String vNickName, String vDesc, String vPhoto, double RatingScore, List<String> vExpertise)
	{
		nickName = vNickName;
		descreption = vDesc;
		photoURL = vPhoto;
		userRating = RatingScore;
		expertise = vExpertise;
	}
	
	public String getNickName() {
		return nickName;
	}

	public String getDescreption() {
		return descreption;
	}

	public String getPhotoURL() {
		return photoURL;
	}

	public double getUserRating() {
		return userRating;
	}

	/* Members */
	private String nickName, descreption, photoURL;
	private double userRating;
	List<String> expertise;
	
}
