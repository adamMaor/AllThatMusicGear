
package allthatmusicgear.model;

/**
 * This model class is implemented because we need to get a question with and a single attached answer.<br>
 * Used for Json/Gson only.
 * @author Adam Ma'or, Vadim Xaxam
 */
public class QuestionAnswerPair {
	
	/**
	 * @param qst - the Question
	 * @param ans - the Answer
	 */
	public QuestionAnswerPair(Question qst, Answer ans) {
		this.qst = qst;
		this.ans = ans;
	}
	public Question qst;
	public Answer ans;
}
