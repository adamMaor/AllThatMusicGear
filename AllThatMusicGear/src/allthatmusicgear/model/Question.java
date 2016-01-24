package allthatmusicgear.model;

import java.sql.Date;
import java.util.List;

public class Question {

	/**
	 * @param qID
	 * @param qUserNickName
	 * @param qText
	 * @param l
	 * @param qVotingScore
	 * @param qRating
	 * @param qTopics
	 */
	public Question(int qID, String qUserNickName, String qText, long timeStamp, int qVotingScore, double qRating,
			List<String> qTopics) {
		super();
		this.qID = qID;
		this.qUserNickName = qUserNickName;
		this.qText = qText;
		this.qSubDate = timeStamp;
		this.qVotingScore = qVotingScore;
		this.qRating = qRating;
		this.qTopics = qTopics;
	}
	/**
	 * @return the qVotingScore
	 */
	public int getqVotingScore() {
		return qVotingScore;
	}
	/**
	 * @param qVotingScore the qVotingScore to set
	 */
	public void setqVotingScore(int qVotingScore) {
		this.qVotingScore = qVotingScore;
	}
	/**
	 * @return the qText
	 */
	public String getqText() {
		return qText;
	}
	/**
	 * @param qText the qText to set
	 */
	public void setqText(String qText) {
		this.qText = qText;
	}
	/**
	 * @return the qRating
	 */
	public double getqRating() {
		return qRating;
	}
	/**
	 * @param qRating the qRating to set
	 */
	public void setqRating(double qRating) {
		this.qRating = qRating;
	}
	/**
	 * @return the qTopics
	 */
	public List<String> getqTopics() {
		return qTopics;
	}
	/**
	 * @param qTopics the qTopics to set
	 */
	public void setqTopics(List<String> qTopics) {
		this.qTopics = qTopics;
	}
	/**
	 * @return the qID
	 */
	public int getqID() {
		return qID;
	}
	/**
	 * @return the qSubDate
	 */
	public long getqSubDate() {
		return qSubDate;
	}
	/**
	 * @return the qUserNickName
	 */
	public String getqUserNickName() {
		return qUserNickName;
	}
	/**
	 * @param qID
	 * @param qRating
	 * @param qSubDate
	 * @param qUserNickName
	 * @param qText
	 * @param qVotingScore
	 * @param qTopics
	 */

	private int qID, qVotingScore;
	private long qSubDate;
	private String qUserNickName, qText;
	private double qRating;
	private List<String> qTopics;
	
}
