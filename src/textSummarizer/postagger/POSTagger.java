package textSummarizer.postagger;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import net.didion.jwnl.data.POS;
import textSummarizer.*;
import textSummarizer.helper.*;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
public class POSTagger {
	public static POS getPosFromTag(TaggedTextWord word)
	{
		if (word.tag().length()<2)
			return null;
		if (word.tag().substring(0, 2).equals("JJ"))
		{
			return POS.ADJECTIVE;
		}
		if (word.tag().substring(0, 2).equals("NN"))
		{
			return POS.NOUN;
		}
		if (word.tag().substring(0, 2).equals("VB"))
		{
			return POS.VERB;
		}
		if (word.tag().substring(0, 2).equals("RB"))
		{
			return POS.ADVERB;
		}
		return null;
	}
	public static Vector<Vector<TaggedTextWord>> tagWords (String text, String head, Vector<TaggedTextWord> taggedHeadingWords, Vector<String> sentencesText) throws Exception
	{
		
		Vector<Vector<TaggedTextWord>> taggedSentenceWords = new Vector<Vector<TaggedTextWord>>();
		MaxentTagger tagger = new MaxentTagger(SystemParam.getInstance().getParam("TAGGER_LIB"));
		
		/*if (LexChain.DEBUG_PRINT)
			System.out.println(text.length());*/

		int i = 0;
		List<List<HasWord>> heading = MaxentTagger.tokenizeText(new BufferedReader(new StringReader(head)));
		for (List<HasWord> sentence : heading) 
		{
			ArrayList<TaggedWord> tSentence = tagger.tagSentence(sentence);
			
			//taggedSentenceWords.add(new Vector<TaggedTextWord>());
			String tempSentence="";
			for (int j = 0; j < tSentence.size(); j++)
				{
					//System.out.println(tSentence.get(j).beginPosition() + " " + tSentence.get(j).endPosition());
				    //System.out.print(tSentence.get(j).value()+" ");
					TaggedTextWord tw = new TaggedTextWord(tSentence.get(j));
					tempSentence+=tSentence.get(j).value().toString();
					tempSentence+=" ";
					POS pos = getPosFromTag(tw);
					if (pos!=null){
						
							taggedHeadingWords.add(tw);
							
					}
				}
			
			//System.out.println(tempSentence);
			sentencesText.add(tempSentence);
			
			
		}

		
		List<List<HasWord>> sentences = MaxentTagger.tokenizeText(new BufferedReader(new StringReader(text)));
		
		for (List<HasWord> sentence : sentences) 
			{
				ArrayList<TaggedWord> tSentence = tagger.tagSentence(sentence);
				
				taggedSentenceWords.add(new Vector<TaggedTextWord>());
				String tempSentence="";
				for (int j = 0; j < tSentence.size(); j++)
					{
						//System.out.println(tSentence.get(j).beginPosition() + " " + tSentence.get(j).endPosition());
					    //System.out.print(tSentence.get(j).value()+" ");
						TaggedTextWord tw = new TaggedTextWord(tSentence.get(j));
						tempSentence+=tSentence.get(j).value().toString();
						tempSentence+=" ";
						POS pos = getPosFromTag(tw);
						if (pos!=null){
							
							taggedSentenceWords.elementAt(taggedSentenceWords.size()-1).add(tw);
								
						}
					}
				
				//System.out.println(tempSentence);
				sentencesText.add(tempSentence);
				
				
			}
		return taggedSentenceWords;
		
	}
	
}