package allthatmusicgear.constants;

/**
 * @author Adam Ma'or, Vadim Xaxam
 * 
 * This interface holds the constants to build our tables
 * @see SQLSchema.sql
 */
public interface CreateTablesConstants {
	
	public final String CREATE_USER_TABLE = "CREATE TABLE tblUser "
			+ "(UserName VARCHAR(10) PRIMARY KEY,"
			+ "Password VARCHAR(8) NOT NULL,"
			+ "NickName VARCHAR(20) UNIQUE NOT NULL,"
			+ "Descreption VARCHAR(50) DEFAULT '',"
			+ "PhotoURL VARCHAR(300) DEFAULT '', "
			+ "UserRating DOUBLE DEFAULT 0 )" ;
		
	public final String CREATE_QUESTION_TABLE = "CREATE TABLE tblQuestion"
			+ "(QID INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"
			+ "QUNickName VARCHAR(20) NOT NULL,"
			+ "QText VARCHAR(300) NOT NULL,"
			+ "QSubmissionTime TIMESTAMP DEFAULT TIMESTAMP(CURRENT_DATE,CURRENT_TIME),"
			+ "QVotingScore INT DEFAULT 0,"
			+ "QRating DOUBLE DEFAULT 0,"
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
			+ "ASubmissionTime TIMESTAMP DEFAULT TIMESTAMP(CURRENT_DATE,CURRENT_TIME),"
			+ "AVotingScore INT DEFAULT 0,"
			+ "FOREIGN KEY(QuestionID) REFERENCES tblQuestion(QID),"
			+ "FOREIGN KEY(AUNickName) REFERENCES tblUser(NickName))";
	
	public final String CREATE_QUESTION_VOTES_TABLE = "CREATE TABLE tblQuestionVotes"
			+ "(QID INT, UNickName VARCHAR(20), Vote INT, "
			+ "PRIMARY KEY (QID, UNickName),"
			+ "FOREIGN KEY(QID) REFERENCES tblQuestion(QID),"
			+ "FOREIGN KEY(UNickName) REFERENCES tblUser(NickName))";

	public final String CREATE_ANSWER_VOTES_TABLE = "CREATE TABLE tblAnswerVotes"
			+ "(AID INT, UNickName VARCHAR(20), Vote INT, "
			+ "PRIMARY KEY (AID, UNickName),"
			+ "FOREIGN KEY(AID) REFERENCES tblAnswer(AID),"
			+ "FOREIGN KEY(UNickName) REFERENCES tblUser(NickName))";
	
}
