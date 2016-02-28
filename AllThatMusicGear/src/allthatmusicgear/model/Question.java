package allthatmusicgear.model;

import java.util.List;

/**
 * This model class represents a Question.<br>
 * Used for Json/Gson only.
 * @author Adam Ma'or, Vadim Xaxam
 */
public class Question {

	/**
	 * @param qID - the auto generating Question ID
	 * @param qUserNickName - submitter's nickName
	 * @param qText - the actual question
	 * @param submissionTimeStamp - Auto-generated submission date and time
	 * @param qVotingScore - Question Voting score
	 * @param qRating - Question Rating
	 * @param qTopics - a list of topic related to this question
	 */
	public Question(int qID, String qUserNickName, String qText, long submissionTimeStamp, int qVotingScore, double qRating,
			List<String> qTopics, String usrPhoto, double usrRating, int lgdUsrVote) {
		super();
		this.qID = qID;
		this.qUserNickName = qUserNickName;
		this.qText = qText;
		this.qSubDate = submissionTimeStamp;
		this.qVotingScore = qVotingScore;
		this.qRating = qRating;
		this.qTopics = qTopics;
		this.userPhoto = usrPhoto;
		this.userRating = usrRating;
		this.loggedUserVote = lgdUsrVote;
	}
	
	/**
	 * @param qID - the auto generating Question ID
	 * @param qRating - Question's Rating
	 * @param qSubDate - Auto-generated submission date and time
	 * @param qUserNickName - submitter's nickName
	 * @param qText - the actual question
	 * @param qVotingScore - Question Voting score
	 * @param qTopics - a list of topic related to this question
	 */

	@SuppressWarnings("unused")
	private int qID, qVotingScore;
	@SuppressWarnings("unused")
	private long qSubDate;
	@SuppressWarnings("unused")
	private String qUserNickName, qText;
	@SuppressWarnings("unused")
	private double qRating;
	@SuppressWarnings("unused")
	private List<String> qTopics;
	@SuppressWarnings("unused")
	private String userPhoto;
	@SuppressWarnings("unused")
	private double userRating;
	@SuppressWarnings("unused")
	private int loggedUserVote;
	
	
}
