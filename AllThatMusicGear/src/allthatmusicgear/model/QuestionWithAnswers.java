/**
 * 
 */
package allthatmusicgear.model;

import java.util.Collection;

/**
 * @author Adam
 *
 */
public class QuestionWithAnswers {
	/**
	 * @param qst
	 * @param ans
	 */
	public QuestionWithAnswers(Question qst, Collection<Answer> ans) {
		super();
		this.qst = qst;
		this.ans = ans;
	}
	private Question qst;
	private Collection<Answer> ans;
}
