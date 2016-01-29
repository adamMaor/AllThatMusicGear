package allthatmusicgear.model;

public class User {
	public User (String vName, String vPassword, String vNickName, String vDesc, String vPhoto, double RatingScore)
	{
		userName = vName;
		password = vPassword;
		nickName = vNickName;
		descreption = vDesc;
		photoURL = vPhoto;
		userRating = RatingScore;
	}
	
	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
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
	private String userName, password, nickName, descreption, photoURL;
	private double userRating;
	
}
