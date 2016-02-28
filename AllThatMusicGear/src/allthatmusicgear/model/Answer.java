package allthatmusicgear.model;


/**
 * This model class represents an Answer.<br>
 * Used for Json/Gson only.
 * @author Adam Ma'or, Vadim Xaxam
 */
public class Answer {	
	/**
	 * @param aID - the auto generating Answer ID
	 * @param qID - the question ID this answer relates to
	 * @param aUserNickName - Submiter's nickName
	 * @param aText - the actual answer
	 * @param submissionTimeStamp - Auto-generated submission date and time
	 * @param aVotingScore - the Voting Score for this answer
	 */
	public Answer(int aID, int qID, String aUserNickName, String aText, long submissionTimeStamp, int aVotingScore, 
			String usrPhoto, double usrRating, int lgdUsrVote) {
		super();
		this.aID = aID;
		this.qID = qID;
		this.aUserNickName = aUserNickName;
		this.aText = aText;
		this.aSubDate = submissionTimeStamp;
		this.aVotingScore = aVotingScore;
		this.userPhoto = usrPhoto;
		this.userRating = usrRating;
		this.loggedUserVote = lgdUsrVote;
	}

	@SuppressWarnings("unused")
	private int aID, qID, aVotingScore;
	@SuppressWarnings("unused")
	private long aSubDate;
	@SuppressWarnings("unused")
	private String aUserNickName, aText;
	@SuppressWarnings("unused")
	private String userPhoto;
	@SuppressWarnings("unused")
	private double userRating;
	@SuppressWarnings("unused")
	private int loggedUserVote;
}
