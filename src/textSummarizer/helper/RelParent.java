package textSummarizer.helper;

import net.didion.jwnl.data.*;

/**
 * Helper class that stores the length of a WordNet Relationship and the parent {@link Synset}
 * @author Andrei
 *
 */
public class RelParent {
	private int length;
	private Synset parent;
	private PointerType type;
	/**
	 * Constructor for a RelParent object. 
	 * @param parent		The parent sense 
	 * @param length		The length of the link between two words
	 */
	public RelParent(Synset parent,int length,PointerType type)
	{
		this.parent = parent;
		this.length = length;
		this.type = type;
	}
	public Synset getParentSynset()
	{
		return parent;
	}
	public int getLength()
	{
		return length;
	}
	public PointerType getType()
	{
		return type;
	}
	public String toString()
	{
		if (parent != null)
			return parent.toString()+"\n"+length;
		return String.valueOf(length);
	}
}
