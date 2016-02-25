package allthatmusicgear.constants;

import java.lang.reflect.Type;
import java.util.Collection;

import com.google.gson.reflect.TypeToken;

import allthatmusicgear.model.User;

public interface LogAndRegConstants {

	public final String REGISTER = "Register";
	public final String ALL_USERS = "Users";
	public final String LOGIN = "Login";
	
	public final Type USER_COLLECTION = new TypeToken<Collection<User>>() {}.getType();
	
	
	public final String REGISTER_USER = "INSERT INTO tblUser (UserName, Password, NickName, Descreption, PhotoURL, UserRating) VALUES (?,?,?,?,?,?)";
	public final String GET_ALL_USERS = "SELECT * FROM tblUser";
	public final String LOGIN_USER = "SELECT tblUser.NickName, tblUser.PhotoURL FROM tblUser "
			+ "WHERE tblUser.UserName = ? AND tblUser.Password = ? ";
	public final String CHECK_EXISTING_USER = "SELECT tblUser.NickName FROM tblUser "
			+ "WHERE tblUser.UserName = ? OR tblUser.NickName = ? ";
}
