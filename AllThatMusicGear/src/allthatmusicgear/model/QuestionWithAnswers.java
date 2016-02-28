package allthatmusicgear.model;

import java.util.Collection;

/**
 * This model class is used to return a Question with all it's answers.<br>
 * Used for Json/Gson only.
 * @author Adam Ma'or, Vadim Xaxam
 */
public class QuestionWithAnswers {
	/**
	 * @param qst - the Question
	 * @param ans - a List of Answers sorted by Rating (votes)
	 */
	public QuestionWithAnswers(Question qst, Collection<Answer> ans) {
		this.qst = qst;
		this.ans = ans;
	}
	@SuppressWarnings("unused")
	private Question qst;
	@SuppressWarnings("unused")
	private Collection<Answer> ans;
}
