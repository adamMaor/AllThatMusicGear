
package allthatmusicgear.model;

/**
 * @author  Adam & Vadim
 *	This model class is implemented because we need to get a question with and a single attached answer 
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
