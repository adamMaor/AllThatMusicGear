package allthatmusicgear.constants;

import java.util.HashMap;
import java.util.Map;

public interface CreateTablesConstants {
	
	
	public final String CREATE_USER_TABLE = "CREATE TABLE tblUser "
	+ "(UserName VARCHAR(10) PRIMARY KEY,"
	+ "Password VARCHAR(8) NOT NULL,"
	+ "NickName VARCHAR(20) UNIQUE NOT NULL,"
	+ "Descreption	VARCHAR(50) DEFAULT '',"
	+ "PhotoURL VARCHAR(100) DEFAULT '', "/*we will check for valid URL in javascript*/
	+ "UserRating INT DEFAULT 0 )" ;
	
	
	
	public final String CREATE_QUESTION_TABLE = "CREATE TABLE tblQuestion"
	+ "(QID INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"
	+ "QUNickName VARCHAR(20) NOT NULL,"
	+ "QText VARCHAR(300) NOT NULL,"
	+ "QSubmissionTime DATE DEFAULT CURRENT_DATE,"
	+ "QVotingScore INT DEFAULT 0,"
	+ "QRating INT DEFAULT 0,"
	+ "FOREIGN KEY(QUNickName) REFERENCES tblUser(NickName))";
	
	public final String CREATE_QUESTION_TOPIC_TABLE = "CREATE TABLE tblQuestionTopics"
	+ "(QID INT,"
	+ "Topic VARCHAR(50),"
	+ "PRIMARY KEY (QID, Topic),"
	+ "FOREIGN KEY (QID) REFERENCES tblQuestion(QID))";
	
	public final String CREATE_ANSWER_TABLE = "CREATE TABLE tblAnswer" 
	+ "(AID INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"
	+ "QuestionID INT NOT NULL,"
	+ "AUNickName VARCHAR(20) NOT NULL,"
	+ "AText VARCHAR(300) NOT NULL,"
	+ "ASubmissionTime DATE DEFAULT CURRENT_DATE,"
	+ "AVotingScore INT DEFAULT 0,"
	+ "FOREIGN KEY (QuestionID) REFERENCES tblQuestion(QID),"
	+ "FOREIGN KEY(AUNickName) REFERENCES tblUser(NickName))";
	

}
