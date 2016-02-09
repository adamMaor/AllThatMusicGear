package allthatmusicgear.model;

public class User {
	public User (String vNickName, String vDesc, String vPhoto, double RatingScore)
	{
		nickName = vNickName;
		descreption = vDesc;
		photoURL = vPhoto;
		userRating = RatingScore;
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
	
}
