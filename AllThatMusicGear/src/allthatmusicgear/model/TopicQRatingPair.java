package allthatmusicgear.model;

/**
 * This model class is used to return a topic and it's Tpop (as required in specs.).<br>
 * Used for Json/Gson only.
 * @author Adam Ma'or, Vadim Xaxam
 */
public class TopicQRatingPair {

	/**
	 * @param topic - the Topic
	 * @param tPop - Topic's Tpop
	 */
	public TopicQRatingPair(String topic, double tPop) {
		this.topic = topic;
		this.tPop = tPop;
	}
	
	@SuppressWarnings("unused")
	private String topic;
	@SuppressWarnings("unused")
	private double tPop;
}
