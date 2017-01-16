package textSummarizer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Vector;

import textSummarizer.helper.TaggedTextWord;

import textSummarizer.LexicalChain;
import textSummarizer.helper.RelParent;
import textSummarizer.helper.ScorePos;
import textSummarizer.helper.TaggedSenseWord;
import textSummarizer.helper.TaggedTextWord;
import textSummarizer.helper.TaggedTextWord;
import textSummarizer.helper.TaggedTextWord;
import textSummarizer.helper.WordNetHelper;
import textSummarizer.DistinctChains;

import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.relationship.Relationship;
import textSummarizer.*;
import textSummarizer.helper.*;


class MyComp1 implements Comparator
{
	public int compare(Object o1, Object o2)
	{
		if (((ScorePos)o1).getScore() < ((ScorePos)o2).getScore())
			return 1;
		if (((ScorePos)o1).getScore() > ((ScorePos)o2).getScore())
			return -1;
		return 0;
	}
}

/*
 * This class performs all the functioning related to lexical chains 
 * 
 */
public class LexicalChain {
	
	public static final int LENGTH_LIMIT = 3;
	public static final double FRACTION = 0.9;
	/**
	 * This method finds the lexical chains based on an array of sense disambiguated words 
	 * and writes the output in a text file.
	 * 
	 * 
	 * @param words      				A 2-d array of TaggedSenseWord
	 * 
	 * @param outputFileName 			The name of a file name in which the output will be written
	 * 
	 * @return 							returns a list of representative members for each chain 
	 * 
	 * @throws IOException
	 */
	
	public static Vector<Vector<TaggedTextWord>> lexicalChainsFromWSD(Vector<TaggedSenseWord> words, String outputFileName) throws IOException
	{
		Hashtable<String,Integer> numberHash = new Hashtable<String, Integer>();
		
		for (int i = 0 ; i < words.size(); i++)
		{
			
			String lema = words.elementAt(i).getIndexWord().getLemma().toLowerCase();
			
			if (numberHash.get(lema) == null)
			{
				numberHash.put(lema, 1);
			}
			else
			{
				Integer count = numberHash.get(lema);
				numberHash.remove(lema);
				numberHash.put(lema, count + 1);
			}
			
		}

		Hashtable<String, Integer> auxnum = new Hashtable<String, Integer>(numberHash);
		Vector<Vector<TaggedTextWord>> representativeMembers=new Vector<Vector<TaggedTextWord>>();// for representative members of each lexical chain
		try
		{
			DistinctChains chains  = createWSDChains(words, numberHash);
			Vector<Vector<TaggedTextWord>> chainList = chains.getChainsDistinct();
			DistinctChains relevantChains = contextRelevantChains(chains);
		    representativeMembers = representativeMember(relevantChains);
			
			BufferedWriter  out1 = new BufferedWriter(new FileWriter(outputFileName));
			out1.write(printChainsDistinct(chains));
			out1.close();
			numberHash = new Hashtable<String, Integer>(auxnum);
			
		}
		catch (JWNLException e){e.printStackTrace();}
		
		return representativeMembers;
	}
	
	
	
	
	 /*
	  * Algorithm for building lexical chains from the 2D array of TaggedSenseWords
	  *  
	  */
	
	
	public static DistinctChains createWSDChains(Vector<TaggedSenseWord> uttwords,Hashtable<String, Integer> numberHash) throws JWNLException,IOException
	{
		
		Vector<Vector<TaggedSenseWord>> chainList = new Vector<Vector<TaggedSenseWord>>();
		Vector<Vector<Integer>> chainpoz = new Vector<Vector<Integer>>();
		Vector<Vector<TaggedSenseWord>> chainListDistinct = new Vector<Vector<TaggedSenseWord>>();
		Vector<Double> chainScores = new Vector<Double>();
		int i,j,k;
		Synset[] indSyns = new Synset[uttwords.size()];
		
		Hashtable<String,Integer> pathHash = new Hashtable<String,Integer>();
		Hashtable<String,Integer> chainHash = new Hashtable<String,Integer>();
		Hashtable<String,Double> scoreHash = new Hashtable<String,Double>();
		
		long t1 = System.currentTimeMillis();
		//if (Preprocess.DEBUG_PRINT)
			System.out.println("Extracting lexical features, Building Chains .......");
		double chainScore=0;
		double tempScore=0;
		
		for ( i = 0; i < uttwords.size(); i++)
		{
			
			indSyns[i] = uttwords.elementAt(i).getSense();
			
		}
		for ( i = 0; i < uttwords.size(); i++)
		{
			
			
			int ok = 0; //no lexical chain appropriate , has been found 
			Integer chainIndex = chainHash.get(uttwords.elementAt(i).getIndexWord().getLemma().toLowerCase());
			//to check if this word is already found in some chain
			if (chainIndex != null)
			{
				
				ok = 1;
				chainList.elementAt(chainIndex).add(uttwords.elementAt(i));
				chainpoz.elementAt(chainIndex).add(i);
				chainScores.set(chainIndex,(chainScores.get(chainIndex)+1.0));// adding 1 score to the chain for the relation of SIMILAR_TO
				//System.out.println("Adding the word to a chain where it already exists with new score as "+chainScores.get(chainIndex));
				
			}
			else
			{
				
				//System.out.println("Going through all the created chains ....");
				for (k = 0; k < chainList.size(); k++)
				{
					//System.out.println("Chain "+k);
		  			Vector<TaggedSenseWord> chain = chainList.elementAt(k);
					int found = 0;
					chainScore=0;
					for (int l = 0; l < chain.size(); l++)
					{
						tempScore=0;
						
						Relationship rel;
						RelParent relp;
						int wordIndex = chainpoz.elementAt(k).elementAt(l);
						
						String word1lema = uttwords.elementAt(i).getIndexWord().getLemma();
						String word2lema = uttwords.elementAt(wordIndex).getIndexWord().getLemma();
						String word1 = uttwords.elementAt(i).getIndexWord().getLemma()+" "+uttwords.elementAt(i).getIndexWord().getPOS()+" "+uttwords.elementAt(i).getSense().getOffset();
						String word2 = uttwords.elementAt(wordIndex).getIndexWord().getLemma()+" "+uttwords.elementAt(wordIndex).getIndexWord().getPOS()+" "+uttwords.elementAt(wordIndex).getSense().getOffset();
						String key = word1 + " " + word2;
						String key1 = word2 +" " + word1;//the 2 strings concatenated in both possible ways
						
						int splength = Integer.MAX_VALUE;
						
						if (!pathHash.containsKey(key))//to calculate the path distance b/w two words 
						{
						
							relp = WordNetHelper.getInstance().areRelated(indSyns[i], indSyns[wordIndex]);
							//---if (rel!=null)
							if (relp.getParentSynset() != null)
							{
								splength = relp.getLength();
								//System.out.println(key +" "+ splength);
								/*
								 * Calculating the score according to the distance b/w two senses 
								 */
								tempScore = (1+splength);
								tempScore = 1/tempScore; 
								
								//System.out.println("found a relation with its member with weight "+tempScore);
								
								pathHash.put(key, splength);
								pathHash.put(key1,splength);
								
								scoreHash.put(key, tempScore);
								scoreHash.put(key1, tempScore);
								
								chainScore+=tempScore;
								
							}
							else
							{
								pathHash.put(key, Integer.MAX_VALUE);
								pathHash.put(key1,Integer.MAX_VALUE);
								
								scoreHash.put(key1, Double.MIN_VALUE);
								scoreHash.put(key, Double.MIN_VALUE);
							}
						}
						else
						{
							splength = pathHash.get(key);
							tempScore = scoreHash.get(key);
							chainScore += tempScore;
							//System.out.println("found a relation with its member with weight "+tempScore);
						}
						
						if ((splength <= 1) || (splength <= LENGTH_LIMIT))
						{
								found++;
						}
						if ((splength <= 1) || ((word2lema.length() > 2) && (word1lema.startsWith(word2lema)))
								|| ((word2lema.startsWith(word1lema)) && (word1lema.length() > 2)))
						{
								
								found = Integer.MAX_VALUE;
								l = chain.size();
						}
						
					}
					
					if (found >= chainList.elementAt(k).size() * FRACTION)
					{
						chainList.elementAt(k).add(uttwords.elementAt(i));
						chainpoz.elementAt(k).add(i);
						chainListDistinct.elementAt(k).add(uttwords.elementAt(i));
						chainHash.put(uttwords.elementAt(i).getIndexWord().getLemma(), k);
						chainScores.set(k,chainScores.get(k)+chainScore);
						//System.out.println("Finally the updated chain score is "+chainScores.get(k));
						k = chainList.size();
						ok = 1;
						
					}
				}
				
				if (ok == 0 )
				{
					chainList.add(new Vector<TaggedSenseWord>());
					chainList.elementAt(chainList.size() - 1).add(uttwords.elementAt(i));
					chainListDistinct.add(new Vector<TaggedSenseWord>());
					chainListDistinct.elementAt(chainList.size() - 1).add(uttwords.elementAt(i));
					chainpoz.add(new Vector<Integer>());
					chainpoz.elementAt(chainList.size() - 1).add(i);
					chainHash.put(uttwords.elementAt(i).getIndexWord().getLemma(), chainList.size() - 1);
					chainScores.add((double)0);
					//System.out.println("Creating a new chain with score "+chainScores.elementAt(chainList.size()-1));
					
				}
			}
		}
		
		long t2 = System.currentTimeMillis();
		
		//if (Preprocess.DEBUG_PRINT)
			System.out.println("time for chaining : " + (t2-t1));
		
		return new DistinctChains(chainListDistinct,numberHash,chainScores,chainList);
	}

	/*
	 * A function that selects context relevant chains , i.e the strongest chains 
	 */
	@SuppressWarnings("unchecked")
	public static DistinctChains contextRelevantChains(DistinctChains chainList) {
		
		double length;
		double totalScore,avgScore,stdScore;
		totalScore=avgScore=stdScore=0;
		
		Vector<Vector<TaggedTextWord>> chains = chainList.getChainsDistinct();
		Hashtable<String, Integer> numberHash = chainList.getWordCount();
		Vector<Double> chainscores = chainList.getChainScores();
		Vector<ScorePos> chainScorePos = new Vector<ScorePos>();
		for(int i=0;i<chainscores.size();i++)
		{
			chainScorePos.add(new ScorePos(chainscores.elementAt(i), i));
		}
		
		Collections.sort(chainScorePos,new MyComp1());
		
		for(int j=0;j<chainScorePos.size();j++)
		{
			Vector<TaggedTextWord> temp = chains.elementAt(chainScorePos.elementAt(j).getPos());
			for(int k=0;k<temp.size();k++)
			{
				//System.out.print(temp.elementAt(k).getIndexWord().getLemma()+" ");
			}
			//System.out.println("Score : "+chainScorePos.elementAt(j).getScore());
		}
		
		int reduce = (int)(0.7 * chainscores.size());// 70% percent of chains are selected according to their score 
		
		Vector<Vector<TaggedTextWord>> strongChains=new Vector<Vector<TaggedTextWord>>();
		
		for(int j=0;j<reduce;j++)
		{
			strongChains.add(chains.elementAt(chainScorePos.elementAt(j).getPos()));
		}
		
		
		
		return new DistinctChains(strongChains, numberHash,chainscores, strongChains);
	
	}
	
	/*
	 * A method that returns the representative members of each relevant lexical chain 
	 *  
	 */
	
	public static Vector<Vector<TaggedTextWord>> representativeMember(DistinctChains chainList)
	{
		Vector<Vector<TaggedTextWord>> membersList=new Vector<Vector<TaggedTextWord>>();
		Vector<Vector<TaggedTextWord>> chains = chainList.getChainsDistinct();
		Hashtable<String, Integer> numberHash = chainList.getWordCount();
		for(int i=0;i<chains.size();i++)
		{
			Vector<TaggedTextWord> chain = chains.elementAt(i);
			int count=chain.size();
			int sum=0;
			for(int j=0;j<chain.size();j++)
			{
				TaggedTextWord wd=chain.elementAt(j);
				String val=wd.getIndexWord().getLemma();
				sum+=numberHash.get(val);
			}
			int avgCount=sum/count;
			Vector<TaggedTextWord> membersListElement=new Vector<TaggedTextWord>();
			for(int j=0;j<chain.size();j++)
			{
				
				if((numberHash.get(chain.elementAt(j).getIndexWord().getLemma()))>=avgCount)// this member of lexical chain is a representative member
				{
					membersListElement.add(chain.elementAt(j));
				}
			}
			membersList.add(membersListElement);
		}
		
		return membersList;
	}
	
	
	/**
	 * This method returns a display form of the list of distinct lexical chains , as a {@link String} 
	 * 
	 * @param chainList		The list of distinct lexical chains
	 * @return				The print form as a {@link String}
	 */
	
	public static String printChainsDistinct(DistinctChains chainList)
	{
		int j;
		int Chain_score;
		int length;
		StringBuffer str = new StringBuffer("");
		Vector<Vector<TaggedTextWord>> chains = chainList.getChainsDistinct();
		Hashtable<String, Integer> numberHash = chainList.getWordCount();
		for (int i = 0;i<chains.size();i++)
		{
			
			Vector<TaggedTextWord> chain = chains.elementAt(i);
			str.append("Lexical chain " + (i + 1) + ":\n");
			for (j = 0; j < chain.size() - 1; j++)
			{
				str.append(chain.elementAt(j).value() + "(" + numberHash.get(chain.elementAt(j).getIndexWord().getLemma().toLowerCase()) + ")" + " | ");
				
			}
			str.append(chain.elementAt(chain.size() - 1).getIndexWord().getLemma().toLowerCase() + "(" +
					   numberHash.get(chain.elementAt(chain.size() - 1).getIndexWord().getLemma().toLowerCase()) + ")" + "\n");
			
			j++;
			
		}
		return str.toString();
	}
	

	/*
	 * This function assigns scores to the sentences on the basis of their inclusion of representative members of the strong chains
	 */
	public static Vector<Double> calculateLexicalScores(Vector<Vector<TaggedTextWord>> stemSentenceWords,Vector<Vector<TaggedTextWord>> representativeMembers)
	{
		Vector<ScorePos> sentenceScores = new Vector<ScorePos>();
		Vector<Double> lexScores = new Vector<Double>();
		for(int i=0;i<stemSentenceWords.size();i++)
		{
			ScorePos temp=new ScorePos(0,i);
			sentenceScores.add(temp);
			
		}
		//For Hashing the representative members of the strongest chains 
		
		Hashtable<String,Integer> representativeHash=new Hashtable<String,Integer>();
		
		for(int i=0;i<representativeMembers.size();i++)
		{
			Vector<TaggedTextWord> list=representativeMembers.elementAt(i);
			for(int j=0;j<list.size();j++)
			{
				TaggedTextWord wd=list.elementAt(j);
				String val=wd.getIndexWord().getLemma();
				if(representativeHash.get(val)==null)
				{
					representativeHash.put(val, 1);
				}
				else
				{
					int count=representativeHash.get(val);
					representativeHash.remove(val);
					representativeHash.put(val,count+1);
				}
			}
		}
		
		double max = Integer.MIN_VALUE;
		double temps = 0;
		
		for(int i=0;i<stemSentenceWords.size();i++)
		{
			Vector<TaggedTextWord> temp=stemSentenceWords.elementAt(i);
			for(int j=0;j<temp.size();j++)
			{
				if(representativeHash.get(temp.elementAt(j).getIndexWord().getLemma())!=null)
				{
					ScorePos tempScore = sentenceScores.elementAt(i);
					tempScore.setScore(tempScore.getScore()+1);
					sentenceScores.set(i,tempScore);
					
				}
				
			}
			ScorePos tempScore = sentenceScores.elementAt(i);
			temps = tempScore.getScore();
			if(temps > max)
			{
				max = temps;
			}
			lexScores.add(temps);
		}
		
		for(int i=0;i<lexScores.size();i++)
		{
			double newval = lexScores.elementAt(i);
			newval /= max;
			lexScores.set(i, newval);
		}
		
		return lexScores;
		
	}

	
 
	
	
}