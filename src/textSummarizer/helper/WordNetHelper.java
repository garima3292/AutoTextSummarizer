package textSummarizer.helper;

/**
 * This is a helper class used for interacting with WordNet JWNL API.
 * Use this class directley, by calling WordNetHelper.getInstance()
 * @author Andrei
 *
 */
public class WordNetHelper {

	private static WordNet wn;
	static
	{
		wn = new WordNet ();
		
	}
	public static WordNet getInstance ()
	{
		return wn;
	}
}
