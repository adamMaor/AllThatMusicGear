package allthatmusicgear.model;

/**
 * this model class is used to return a topic and it's Tpop (as required in specs.)
 * @author Adam & Vadim
 *
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
