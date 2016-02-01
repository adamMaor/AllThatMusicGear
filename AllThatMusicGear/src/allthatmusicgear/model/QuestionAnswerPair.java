/**
 * 
 */
package allthatmusicgear.model;

/**
 * @author Laptop
 *	This class is implemented because we need to get a question with and attached answer more than once
 */
public class QuestionAnswerPair {
	
	/**
	 * @param qst
	 * @param ans
	 */
	public QuestionAnswerPair(Question qst, Answer ans) {
		this.qst = qst;
		this.ans = ans;
	}
	public Question qst;
	public Answer ans;
}
