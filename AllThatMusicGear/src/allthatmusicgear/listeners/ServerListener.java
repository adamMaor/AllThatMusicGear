package allthatmusicgear.listeners;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

import allthatmusicgear.constants.CreateTablesConstants;
import allthatmusicgear.constants.DBConstants;

/**
 * Application Lifecycle Listener implementation class ServerListener<br>
 * we used the listener to initialize our tables upon initialize and shut down the DB upon exit (destroy).
 * @author Adam Ma'or, Vadim Xaxam
 *
 */
public class ServerListener implements ServletContextListener {

    /**
     * Default constructor. 
     */
    public ServerListener() {
    }
    
  // utility that checks whether the exception was thrown due to table already exists
    private boolean tableAlreadyExists(SQLException e) {
        boolean exists;
        if(e.getSQLState().equals("X0Y32")) {
            exists = true;
        } else {
            exists = false;
        }
        return exists;
    }

	/**
	 * we used this implicitly called method o initialize our DB tables
	 * SCHEMA ATMG is auto-generated as we defined in the context.xml file
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent event)  { 
    	System.setProperty("derby.system.home", "c:/Web/DerbyDBFolder");
    	ServletContext cntx = event.getServletContext();
    	Connection conn = null;
    	
    	try{
    		
    		//obtain CustomerDB data source from Tomcat's context
    		Context context = new InitialContext();
    		BasicDataSource ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
    		conn = ds.getConnection();  		
    		List<String> dmlList = new ArrayList<String>();
 
//    		If you want to remove all the tables uncomment the next 7 lines - remember to comment them again
    		
//    		dmlList.add("DROP TABLE tblQuestionVotes");
//    		dmlList.add("DROP TABLE tblAnswerVotes");
//    		dmlList.add("DROP TABLE tblAnswer");
//    		dmlList.add("DROP TABLE tblQuestionTopics");
//    		dmlList.add("DROP TABLE tblQuestion");
//    		dmlList.add("DROP TABLE tblUser");
//    		dmlList.add("DROP SCHEMA ATMG RESTRICT");

    		dmlList.add(CreateTablesConstants.CREATE_USER_TABLE);
    		dmlList.add(CreateTablesConstants.CREATE_QUESTION_TABLE);
    		dmlList.add(CreateTablesConstants.CREATE_QUESTION_TOPIC_TABLE);
    		dmlList.add(CreateTablesConstants.CREATE_ANSWER_TABLE);
    		dmlList.add(CreateTablesConstants.CREATE_QUESTION_VOTES_TABLE);
    		dmlList.add(CreateTablesConstants.CREATE_ANSWER_VOTES_TABLE);
    		
    		Statement stmt = null;
    		
    		for (String currCreateStat : dmlList)
    		{    			
    			boolean created = false;
    			try{
    				stmt = conn.createStatement();
    				stmt.executeUpdate(currCreateStat);
    				conn.commit();
    			} catch (SQLException e){
    				//check if exception thrown since table was already created (so we created the database already 
    				//in the past
    				created = tableAlreadyExists(e);
    				if (!created){
    					throw e;//re-throw the exception so it will be caught in the
    					//external try..catch and recorded as error in the log
    				}
    			} finally {
    				try {
     					if (stmt != null) {
     						stmt.close();
     					}
     				} catch (Exception e) {
     					e.printStackTrace();
     				}
    			}
    		}

    	} catch (SQLException | NamingException e) {
    		//log error 
    		cntx.log("Error during database initialization",e);
    	} finally {
    		try {
    			if (conn != null) {
    				conn.close();
    			}    			
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	}
    }

	/**
	 * we used this implicitly called method to shut down our DB 
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent event)  { 
    	try { 
			DriverManager.getConnection(DBConstants.PROTOCOL + DBConstants.DB_NAME +";shutdown=true");
 		} catch (SQLException e) {
 			String state = e.getSQLState();
 			// if one of these errors - it ok - no need to print the exception details
 			if (state.equals("XJ015") || state.equals("08006")){
 				// garbage collector
 				System.gc();
 			}
 			else {
 				ServletContext cntx = event.getServletContext();
 				cntx.log("Error shutting down database",e); 				
 			}
 		}
    }
	
}
