package allthatmusicgear.constants;

import java.lang.reflect.Type;
import java.util.Collection;

import com.google.gson.reflect.TypeToken;

import allthatmusicgear.model.User;

public interface LogAndRegConstants {

	public final String REGISTER = "Register";
	public final String ALL_USERS = "Users";
	
	public final Type USER_PASSWORD_MAP = new TypeToken<Collection<User>>() {}.getType();
	
	
	public final String REGISTER_USER = "INSERT INTO app.tblUser (UserName, Password, NickName, Descreption, PhotoURL, UserRating) VALUES (?,?,?,?,?,?)";
	public final String GET_ALL_USERS_AND_PASS = "SELECT * FROM app.tblUser";
}
