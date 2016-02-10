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
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

import allthatmusicgear.constants.CreateTablesConstants;
import allthatmusicgear.constants.DBConstants;;
/**
 * Application Lifecycle Listener implementation class LogAndRegListener
 *
 */
public class LogAndRegListener implements ServletContextListener, ServletContextAttributeListener {

    /**
     * Default constructor. 
     */
    public LogAndRegListener() {
        // TODO Auto-generated constructor stub
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
         // TODO Auto-generated method stub
    	System.setProperty("derby.system.home", "c:/Web/DerbyDBFolder");
    	ServletContext cntx = event.getServletContext();
    	
    	try{
    		
    		//obtain CustomerDB data source from Tomcat's context
    		Context context = new InitialContext();
    		BasicDataSource ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
    		Connection conn = ds.getConnection();
    		
    		
    		
    		List<String> dmlList = new ArrayList<String>();
 
//    		If you want to remove all the tables uncomment this and comment the next 4 Lines - remeber to comment them again
    		
//    		dmlList.add("DROP TABLE app.tblAnswer");
//    		dmlList.add("DROP TABLE app.tblQuestionTopics");
//    		dmlList.add("DROP TABLE app.tblQuestion");
//    		dmlList.add("DROP TABLE app.tblUser");
//    		dmlList.add("DROP TABLE app.tblQuestionVotes");
//    		dmlList.add("DROP TABLE app.tblAnswerVotes");
    		
    		
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
    					throw e;//re-throw the exception so it will be caught in the
    					//external try..catch and recorded as error in the log
    				}
    			}
    		}
    		
    		//close connection
    		conn.close();

    	} catch (/*IOException | */SQLException | NamingException e) {
    		//log error 
    		cntx.log("Error during database initialization",e);
    	}
    }
    
    /**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent event)  { 
         // TODO Auto-generated method stub
    	 try {
 			DriverManager.getConnection(DBConstants.PROTOCOL + DBConstants.DB_NAME +";shutdown=true");
 		} catch (SQLException e) {
 			ServletContext cntx = event.getServletContext();
 			cntx.log("Error shutting down database",e);
 		}
    }

	/**
     * @see ServletContextAttributeListener#attributeAdded(ServletContextAttributeEvent)
     */
    public void attributeAdded(ServletContextAttributeEvent arg0)  { 
         // TODO Auto-generated method stub
    }

	/**
     * @see ServletContextAttributeListener#attributeRemoved(ServletContextAttributeEvent)
     */
    public void attributeRemoved(ServletContextAttributeEvent arg0)  { 
         // TODO Auto-generated method stub
    }

	

	/**
     * @see ServletContextAttributeListener#attributeReplaced(ServletContextAttributeEvent)
     */
    public void attributeReplaced(ServletContextAttributeEvent arg0)  { 
         // TODO Auto-generated method stub
    }


	
}
