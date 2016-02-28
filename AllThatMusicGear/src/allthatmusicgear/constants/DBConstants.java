package allthatmusicgear.constants;

/**
 * This interface holds the constants for DB access<br>
 * see SQLSchema.sql for more details.
 * @author Adam Ma'or, Vadim Xaxam
 */
public interface DBConstants {

	//derby constants
	public final String DB_NAME = "AllThatMusicGearDB";
	public final String DB_DATASOURCE = "java:comp/env/jdbc/AllThatMusicGearDatasource";													  
	public final String PROTOCOL = "jdbc:derby:"; 
}
