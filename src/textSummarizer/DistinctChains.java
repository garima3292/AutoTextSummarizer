package textSummarizer;
import java.util.*;

import textSummarizer.helper.*;

/**
 * This class stores the list of lexical chains as well as the list of the distinct lexical chains.
 *
 */

public class DistinctChains
{
	private Vector<Vector<TaggedTextWord>> chainListDistinct;
	private Vector<Vector<TaggedTextWord>> chainList;
	private Hashtable<String, Integer> numberHash;
	private Vector<Double> chainScores;

	
	public DistinctChains(Vector chainsDistinct, Hashtable<String, Integer> numberHash, Vector<Double> chainScores, Vector chains)
	{
		this.chainListDistinct = chainsDistinct;
		this.numberHash = numberHash;
		this.chainList = chains;
		this.chainScores = chainScores;
	}
	/**
	 * Returns the list of distinct lexical chains
	 * @return		The list of distinct lexical chains
	 */
	public Vector<Vector<TaggedTextWord>> getChainsDistinct()
	{
		return chainListDistinct;
	}
	
	/**
	 * Returns the list of lexical chains
	 * @return		The list of all lexical chains
	 */
	public Vector<Vector<TaggedTextWord>> getChains()
	{
		return chainList;
	}
	public Hashtable<String, Integer> getWordCount()
	{
		return numberHash;
	}
	
	public Vector<Double> getChainScores()
	{
		return chainScores;
	}
}