/**
 * 
 */
package allthatmusicgear.model;


/**
 * @author Adam
 *
 */
public class Answer {
		
	/**
	 * @param aID
	 * @param qID
	 * @param aUserNickName
	 * @param aText
	 * @param aSubDate
	 * @param aVotingScore
	 */
	public Answer(int aID, int qID, String aUserNickName, String aText, long submissionTimeStamp, int aVotingScore) {
		super();
		this.aID = aID;
		this.qID = qID;
		this.aUserNickName = aUserNickName;
		this.aText = aText;
		this.aSubDate = submissionTimeStamp;
		this.aVotingScore = aVotingScore;
	}
	/**
	 * @return the aVotingScore
	 */
	public int getaVotingScore() {
		return aVotingScore;
	}
	/**
	 * @param aVotingScore the aVotingScore to set
	 */
	public void setaVotingScore(int aVotingScore) {
		this.aVotingScore = aVotingScore;
	}
	/**
	 * @return the aText
	 */
	public String getaText() {
		return aText;
	}
	/**
	 * @param aText the aText to set
	 */
	public void setaText(String aText) {
		this.aText = aText;
	}
	/**
	 * @return the aID
	 */
	public int getaID() {
		return aID;
	}
	/**
	 * @return the qID
	 */
	public int getqID() {
		return qID;
	}
	/**
	 * @return the aSubDate
	 */
	public long getaSubDate() {
		return aSubDate;
	}
	/**
	 * @return the aUserNickName
	 */
	public String getaUserNickName() {
		return aUserNickName;
	}
	private int aID, qID, aVotingScore;
	private long aSubDate;
	private String aUserNickName, aText;
}
