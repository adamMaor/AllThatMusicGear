package allthatmusicgear.constants;

public interface CreateTablesConstants {
	
	
	public final String CREATE_USER_TABLE = "CREATE TABLE app.tblUser "
	+ "(UserName VARCHAR(10) PRIMARY KEY,"
	+ "Password VARCHAR(8) NOT NULL,"
	+ "NickName VARCHAR(20) UNIQUE NOT NULL,"
	+ "Descreption	VARCHAR(50) DEFAULT '',"
	+ "PhotoURL VARCHAR(100) DEFAULT '', "/*we will check for valid URL in javascript*/
	+ "UserRating DOUBLE DEFAULT 0 )" ;
		
	public final String CREATE_QUESTION_TABLE = "CREATE TABLE app.tblQuestion"
	+ "(QID INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"
	+ "QUNickName VARCHAR(20) NOT NULL,"
	+ "QText VARCHAR(300) NOT NULL,"
	+ "QSubmissionTime TIMESTAMP DEFAULT TIMESTAMP(CURRENT_DATE,CURRENT_TIME),"
	+ "QVotingScore INT DEFAULT 0,"
	+ "QRating DOUBLE DEFAULT 0,"
	+ "FOREIGN KEY(QUNickName) REFERENCES app.tblUser(NickName))";
	
	public final String CREATE_QUESTION_TOPIC_TABLE = "CREATE TABLE app.tblQuestionTopics"
	+ "(QID INT,"
	+ "Topic VARCHAR(50),"
	+ "PRIMARY KEY (QID, Topic),"
	+ "FOREIGN KEY (QID) REFERENCES app.tblQuestion(QID))";
	
	public final String CREATE_ANSWER_TABLE = "CREATE TABLE app.tblAnswer" 
	+ "(AID INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"
	+ "QuestionID INT NOT NULL,"
	+ "AUNickName VARCHAR(20) NOT NULL,"
	+ "AText VARCHAR(300) NOT NULL,"
	+ "ASubmissionTime TIMESTAMP DEFAULT TIMESTAMP(CURRENT_DATE,CURRENT_TIME),"
	+ "AVotingScore INT DEFAULT 0,"
	+ "FOREIGN KEY(QuestionID) REFERENCES app.tblQuestion(QID),"
	+ "FOREIGN KEY(AUNickName) REFERENCES app.tblUser(NickName))";
	
	public final String CREATE_QUESTION_VOTES_TABLE = "CREATE TABLE app.tblQuestionVotes"
			+ "(QID INT, UNickName VARCHAR(20),"
			+ "PRIMARY KEY (QID, UNickName),"
			+ "FOREIGN KEY(QID) REFERENCES app.tblQuestion(QID),"
			+ "FOREIGN KEY(UNickName) REFERENCES app.tblUser(NickName))";

	public final String CREATE_ANSWER_VOTES_TABLE = "CREATE TABLE app.tblAnswerVotes"
			+ "(AID INT, UNickName VARCHAR(20),"
			+ "PRIMARY KEY (AID, UNickName),"
			+ "FOREIGN KEY(AID) REFERENCES app.tblAnswer(AID),"
			+ "FOREIGN KEY(UNickName) REFERENCES app.tblUser(NickName))";
	
}
