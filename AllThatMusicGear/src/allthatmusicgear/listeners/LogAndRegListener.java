package allthatmusicgear.listeners;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.org.apache.bcel.internal.Constants;

import allthatmusicgear.constants.*;;
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
    		dmlList.add(CreateTablesConstants.CREATE_USER_TABLE);
    		dmlList.add(CreateTablesConstants.CREATE_QUESTION_TABLE);
    		dmlList.add(CreateTablesConstants.CREATE_QUESTION_TOPIC_TABLE);
    		dmlList.add(CreateTablesConstants.CREATE_ANSWER_TABLE);
    		
    		for (String currCreateStat : dmlList)
    		{    			
    			boolean created = false;
    			try{
    				//create Customers table
    				Statement stmt = conn.createStatement();
    				//stmt.executeUpdate("DROP TABLE tblUser");
    				//stmt.executeUpdate(CreateTablesConstants.CREATE_USER_TABLE);
    				//stmt.executeUpdate("DROP TABLE tblQuestion");
    				stmt.executeUpdate(currCreateStat);
    				//commit update
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
    				System.out.println("Table Already Created");
    			}
    		}
    		
    		//if no database exist in the past - further populate its records in the table
//    		if (!created){
//    			//populate customers table with customer data from json file
////    			Collection<Customer> customers = loadCustomers(cntx.getResourceAsStream(File.separator +
////    														   AppConstants.CUSTOMERS_FILE));
////    			PreparedStatement pstmt = conn.prepareStatement(AppConstants.INSERT_CUSTOMER_STMT);
////    			for (Customer customer : customers){
////    				pstmt.setString(1,customer.getName());
////    				pstmt.setString(2,customer.getCity());
////    				pstmt.setString(3,customer.getCountry());
////    				pstmt.executeUpdate();
////    			}
//    			// create admin - DELETE LATER!!!!
//    			PreparedStatement pstmt = conn.prepareStatement(LogAndRegConstants.REGISTER_USER);
//    			pstmt.setString(1, "Admin");
//    			pstmt.setString(1, "Admin");
//    			pstmt.setString(1, "Admin");
//    			pstmt.setString(1, "I'm the Admin");
//    			pstmt.setString(1, "");
//    			pstmt.setString(1, "Admin");
//    			
//    			//commit update
//    			conn.commit();
//    			//close statements
//    			pstmt.close();
//    		}
    		

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
