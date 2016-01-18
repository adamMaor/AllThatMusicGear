package allthatmusicgear.constants;

public interface LogAndRegConstants {

	public final String REGISTER_USER = "INSERT INTO tblUser (UserName, Password, NickName, Descreption, PhotoURL) VALUES (?,?,?,?,?)";
	public final String GET_USER_BY_USERNAME = "SELECT UserName, Password FROM tblUser WHERE UserName = ?";
}
