package allthatmusicgear.constants;

/**
 * @author Adam Ma'or, Vadim Xaxam
 * 
 * This interface holds the constants for DB access
 *
 */
public interface DBConstants {

	//derby constants
	public final String DB_NAME = "AllThatMusicGearDB";
	public final String DB_DATASOURCE = "java:comp/env/jdbc/AllThatMusicGearDatasource";													  
	public final String PROTOCOL = "jdbc:derby:"; 
}
