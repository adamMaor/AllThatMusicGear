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
 * Application Lifecycle Listener implementation class ServerListener
 *
 */
public class ServerListener implements ServletContextListener {

    /**
     * Default constructor. 
     */
    public ServerListener() {
    }
    
  //utility that checks whether the customer tables already exists
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
 
//    		If you want to remove all the tables uncomment this and comment the next 4 Lines - remeber to comment them again
    		
    		dmlList.add("DROP TABLE app.tblQuestionVotes");
    		dmlList.add("DROP TABLE app.tblAnswerVotes");
    		dmlList.add("DROP TABLE app.tblAnswer");
    		dmlList.add("DROP TABLE app.tblQuestionTopics");
    		dmlList.add("DROP TABLE app.tblQuestion");
    		dmlList.add("DROP TABLE app.tblUser");
    		
    		
    		dmlList.add(CreateTablesConstants.CREATE_USER_TABLE);
    		dmlList.add(CreateTablesConstants.CREATE_QUESTION_TABLE);
    		dmlList.add(CreateTablesConstants.CREATE_QUESTION_TOPIC_TABLE);
    		dmlList.add(CreateTablesConstants.CREATE_ANSWER_TABLE);
    		dmlList.add(CreateTablesConstants.CREATE_QUESTION_VOTES_TABLE);
    		dmlList.add(CreateTablesConstants.CREATE_ANSWER_VOTES_TABLE);
    		
    		
    		
    		for (String currCreateStat : dmlList)
    		{    			
    			boolean created = false;
    			try{
    				Statement stmt = conn.createStatement();
    				stmt.executeUpdate(currCreateStat);
    				conn.commit();
    				stmt.close();
    			}catch (SQLException e){
    				//check if exception thrown since table was already created (so we created the database already 
    				//in the past
    				created = tableAlreadyExists(e);
    				if (!created){
    					if (conn != null) {
    	    				conn.close();
    	    			}
    					throw e;//re-throw the exception so it will be caught in the
    					//external try..catch and recorded as error in the log
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
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent event)  { 
    	try { 
			DriverManager.getConnection(DBConstants.PROTOCOL + DBConstants.DB_NAME +";shutdown=true");
 		} catch (SQLException e) {
 			String state = e.getSQLState();
 			if (state.equals("XJ015") || state.equals("08006")){
 				// if one of these errors - it ok - no need to print the exception details
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
