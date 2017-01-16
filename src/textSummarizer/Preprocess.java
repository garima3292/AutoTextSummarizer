package textSummarizer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;
import textSummarizer.wsd.WSDisambiguator;
import textSummarizer.stemmer.Stemmer;
import textSummarizer.postagger.*;
import textSummarizer.helper.*;
import textSummarizer.helper.*;
public class Preprocess {
	
	
	public static boolean DEBUG_PRINT = false;
	public static int WINDOW_SIZE = 10;
	
	/**
	 *  Method used for processing a given file and outputs (if requested) the result in a 
	 *  text file. 
	
	 * @param i  				The number of the input file which is being used for training
	 
	 * @param writeRez  		{@code true} if output should be written in a text file, {@code false} otherwise
	 * @param stemHeadingWords 	An array of stemmed and tagged heading words , to be used during Statistical Feature Analysis
	 * @param sentences         A vector of strings , to be used Linguistic Feature Analysis
	 * @return 					A vector of vector of TaggedTextWords 
	 * @throws Exception
	 */
	
	public static Vector<Vector<TaggedTextWord>> structureText(String textFile, 
															   boolean writeRez, 
															   Vector<TaggedTextWord> stemHeadingWords, 
															   Vector<String> sentences, 
															   String outputFileName) throws Exception
	{
		
		Vector<Vector<TaggedTextWord>> stemSentenceWords = new Vector<Vector<TaggedTextWord>>();
		
		
		
		Vector<Vector<TaggedTextWord>> taggedSentenceWords = new Vector<Vector<TaggedTextWord>>();
		Vector<TaggedTextWord> taggedHeadingWords = new Vector<TaggedTextWord>();
		String delim = " ,.:!?)([]{}\";/\\+=-_@#$%^&*|";
		Vector<Position> positions = new Vector<Position>();	
		System.out.println(new File("").getAbsoluteFile());
		BufferedReader in = new BufferedReader (new FileReader(textFile));
		String line = new String();
		String text = new String();
		String head = new String();
		Vector<String> tokens=new Vector<String>();
		int lineCount = 0;
		head = in.readLine();
		while ((line = in.readLine())!=null)
		{
			text = text.concat(line);
			int tkCount = 0;
			StringTokenizer st = new StringTokenizer(line, delim);
			while (st.hasMoreTokens())
			{
				positions.add(new Position(lineCount, tkCount));
				tokens.add(st.nextToken());
				//st.nextToken();
				tkCount++;
			}
			lineCount++;
		}
	
		taggedSentenceWords = POSTagger.tagWords(text, head, taggedHeadingWords, sentences);
		//if (DEBUG_PRINT)
			System.out.println("tagged..");
		
		
		for(int j=0;j<taggedHeadingWords.size();j++)
		{
			TaggedTextWord tw = Stemmer.stemWord(taggedHeadingWords.elementAt(j));
			if (tw != null)
			{
				//only if the word is correct , you add
				stemHeadingWords.add(tw);
			}
		}
		for(int j=0;j<taggedSentenceWords.size();j++)
		{
			Vector<TaggedTextWord> temp=taggedSentenceWords.elementAt(j);
			stemSentenceWords.add(new Vector<TaggedTextWord>());
			for(int k=0;k<temp.size();k++)
			{
				TaggedTextWord tw=Stemmer.stemWord(temp.elementAt(k));
				//it returns a correct word 
				//or null if it was not found in the dictionary 
				
				if (tw != null)
				{
					//only if the word is correct , you add
					stemSentenceWords.elementAt(j).add(tw);
				}
				
				
			}
			
		}
		
		
		//if (DEBUG_PRINT)
			System.out.println("stemmed...");
		
		if (writeRez == true)
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(outputFileName));
			for(int j=0;j<stemHeadingWords.size();j++)
			{
				TaggedTextWord word = stemHeadingWords.elementAt(j);
				out.write(word.value().toLowerCase()+" "+word.tag()+"\n");
			}
			for(int j=0;j<stemSentenceWords.size();j++)
			{
				Vector<TaggedTextWord> temp = stemSentenceWords.elementAt(j);
				for(int k=0;k<temp.size();k++)
				{
					TaggedTextWord word = temp.elementAt(k);
					out.write(word.value().toLowerCase()+" "+word.tag()+"\n");
				}
			}
			
			
			out.close();
		}
	
		return stemSentenceWords;
	}
	
	
	
	/**
	 * Method that returns an array of objects {@link TaggedSenseWord}, which means a list of words 
	 * associated with their meaning. Receives as input vector obtained after POST-tagging the words. 
	 * If the second parameter is true, the results obtained in this stage will be written 
	 * to the file whose path is represented by the third parameter
	 * 
	 * @param stemWords 		An array of POS-tagged words
	 * @param writeRez		 	{@code true} if output should be written in a text file, {@code false} otherwise
	 * @param outputFileName    The name of a file name in which the output will be written
	 * @return An array of sense tagged words
	 * @throws Exception
	 */
	
	public static Vector<TaggedSenseWord> WSDFromWords(Vector<Vector<TaggedTextWord>> stemSentenceWords,boolean writeRez, String outputFileName) throws Exception
	{
		//if(Preprocess.DEBUG_PRINT)
			System.out.println("Starting Word Sense Disambiguation .....");
		Vector<TaggedTextWord> stemWords = new Vector<TaggedTextWord>();
		for(int i=0;i<stemSentenceWords.size();i++)
		{
			Vector<TaggedTextWord> temp = stemSentenceWords.elementAt(i);
			for(int j=0;j<temp.size();j++)
			{
				stemWords.add(temp.elementAt(j));
			}
		}
		
		int k = 0;
		
		Vector<TaggedSenseWord> wsdWords = new Vector<TaggedSenseWord>();
		//System.out.println("total words : " + stemWords.size());
		
		while (k < stemWords.size())
		{
			
			Vector<TaggedSenseWord> wsdtempWords = new Vector<TaggedSenseWord>();
			Vector<TaggedTextWord> words = new Vector<TaggedTextWord>();
			int start = k ;
			while ((k < stemWords.size()) && (k - start <= WINDOW_SIZE))
			{
				words.add(stemWords.elementAt(k));
				k++;
			}
			if (Preprocess.DEBUG_PRINT)
			{
				System.out.println("Nr of words to SD : "+words.size());
				System.out.println("At words : "+start+" "+k);
			}
			WSDisambiguator wsd = new WSDisambiguator(words);
			wsdtempWords = wsd.wsd(); 
			for (int i = 0; i < wsdtempWords.size(); i++)
			{
				wsdWords.add(wsdtempWords.elementAt(i));
			}
			
			
		}
		if (writeRez == true)
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(outputFileName));
			for (int j = 0; j < wsdWords.size(); j++)
			{
				TaggedSenseWord sdWord = wsdWords.elementAt(j);
				out.write(sdWord.value().toLowerCase()+" "+sdWord.tag()+" "+
						  sdWord.getSense().getGloss()+" "+sdWord.getSense().getOffset()+"\n");
			}
			
			out.close();		
		}
		return wsdWords;
	}
}