package textSummarizer.helper;
import edu.stanford.nlp.ling.TaggedWord;
import net.didion.jwnl.data.*;
/**
 * Class used to store information regarding a POS-tagged word. Information regarding the utterance ID is
 * also stored.
 * @author Andrei
 *
 */
public class TaggedTextWord extends TaggedWord
{
	private IndexWord iw;
	private Position pos;
	
	public TaggedTextWord(String value,String tag,IndexWord iw)
	{
		super(value,tag);

		this.iw = iw;
		this.pos = null;
	}
	public TaggedTextWord(TaggedWord tw)
	{
		super(tw.value(), tw.tag());

	}
	public TaggedTextWord(TaggedWord tw,IndexWord iw)
	{
		super(tw.value(),tw.tag());
		this.iw = iw;
	}
	public TaggedTextWord(TaggedTextWord tw)
	{
		super(tw.value(),tw.tag());
		this.iw=tw.getIndexWord();
	}
	public IndexWord getIndexWord()
	{
		return iw;
	}
	public void setIndexWord(IndexWord iw)
	{
		this.iw = iw;
	}
	
	public void setPosition (int line, int index)
	{
		this.pos = new Position(line, index);
	}
	public Position getPosition()
	{
		return this.pos;
	}
}
