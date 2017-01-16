package textSummarizer.helper;

/**
 * This class is used for computing three types of semantic distances: Conrath-Jiang distance,
 * Resnik distance and Lin distance
 * 
 * @author Andrei Janca
 *
 */
public class Distance {
	
	public static final int CONRATH_JIANG = 0;
	public static final int RESNIK = 1;
	public static final int LIN = 2;
	
	/** This method is used to compute the semantic distance for two given words
	 *   
	 * @param root         The {@link String} representing the root-word of the two words for which the distance is computed
	 * @param word1		   The first word	
	 * @param word2		   The second word	
	 * @param distance     The distance used - Conrath-Jiang, Resnik or Lin
	 * @return			   The semantic numeric distance between the two words
	 */
	public static double getDistance(String root, String word1, String word2, int distance)
	{
		long rootCount = WordCounter.getCount(root);
		long wordCount1 = WordCounter.getCount(word1);
		long wordCount2 = WordCounter.getCount(word2);
		return getDistance(rootCount, wordCount1, wordCount2, distance);

	}
	
	/**
	 * Method that calculated the required distance using the appropiate formula. 
	 * 
	 * @param rootCount 		The number of occurences for the root-word
	 * @param wordCount1		The number of occurences for the first word
	 * @param wordCount2		The number of occurences for the second word
	 * @param distance			The distance used - Conrath-Jiang, Resnik or Lin
	 * @return					The semantic numeric distance between the two words
	 */
	public static double getDistance(long rootCount, long wordCount1, long wordCount2, int distance)
	{
		if (distance == CONRATH_JIANG)
			return (2 * Math.log10(rootCount) - Math.log10(wordCount1) - Math.log10(wordCount2)) / Math.log10(2);
		if (distance == RESNIK)
			return Math.log10(rootCount) / Math.log10(2);
		
		return 2 * Math.log10(rootCount) / (Math.log10(wordCount1) + Math.log10(wordCount2));//LIN distance

	}
}
