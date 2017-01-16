package textSummarizer.machineLearning;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import textSummarizer.helper.Position;
import textSummarizer.helper.TaggedTextWord;

public class buildTrainingData
{
	public static Vector<Vector<Double>> analyseTrainingCorpus(Vector<Vector<TaggedTextWord>> sentenceWords, Vector<TaggedTextWord> headingWords) throws IOException
	{
		
		/* Statistical Features Evaluation 
		 * posFeature = 0 index 
		 * lengthFeature = 1 index
		 * tf = 2 index
		 * tfisf = 3 index
		 * overlap = 4 index 
		 */
		Vector<Vector<Double>> featureVector = new Vector<Vector<Double>>();
		Vector<Double> lengthFeature = new Vector<Double>();
		Vector<Double> tf = new Vector<Double>();
		Vector<Double> tfisf = new Vector<Double>();
		Vector<Double> overlap = new Vector<Double>();
		int max = Integer.MIN_VALUE;
		int maxtf = Integer.MIN_VALUE;
		Hashtable<String, Integer> sentenceHash = new Hashtable<String, Integer>();
		Hashtable<String, Integer> textHash = new Hashtable<String, Integer>();
		Hashtable<String,Double> termfreq = new Hashtable<String, Double>();
		Hashtable<String, Integer> headingHash = new Hashtable<String, Integer>();
		Hashtable<String, Boolean> flg = new Hashtable<String, Boolean>();
		Hashtable<String, Boolean> flg1 = new Hashtable<String, Boolean>();
		int hdcount=0;
		for(int j=0;j<headingWords.size();j++)
		{
			String token = headingWords.elementAt(j).getIndexWord().getLemma().toLowerCase();
			
			if(headingHash.get(token)==null)
			{
				headingHash.put(token,1);
				hdcount ++;
			}
		}
		int termtf=0;
		int flag=0;
		int lc=0;
		int commons = 0;
		int j,k;
		for(j=0;j<sentenceWords.size();j++)
		{
			
			featureVector.add(new Vector<Double>());
			flag = 0;
			commons = 0;
			int tkCount = 0;
			int dtkCount = 0;//distinct token count
			String token = new String();
			Vector<TaggedTextWord> temp = sentenceWords.elementAt(j);
			for(k=0;k<temp.size();k++)
			{
				token = temp.elementAt(k).getIndexWord().getLemma().toLowerCase();
				/*
				 * for (s-terms and t-terms)
				 */
				if(headingHash.get(token) != null && flg.get(token) == null)// means this is a heading token and has been first time found in this sentence
				{
					commons++;
					flg.put(token,true);
					
				}
				
				//hash maintaining the count of token in the text
				if(textHash.get(token)==null)
				{
					textHash.put(token, 1);
				   
				}
				else
				{
					int count = textHash.get(token);
					textHash.remove(token);
					textHash.put(token, count+1);
					
				}
				
				
				termtf=textHash.get(token);
				if(termtf > maxtf)
				{
					maxtf = termtf;
				}
				
				
				if(flg1.get(token)==null)// this token has come in the sentence for the first time
				{
					if(sentenceHash.get(token)==null)
					{
						sentenceHash.put(token, 1);
						
						dtkCount++;	
						
					}
					else
					{
						int c = sentenceHash.get(token);
						sentenceHash.remove(token);
						sentenceHash.put(token, c+1);
					}
					flg1.put(token, true);
					
				}
				
			}
			/*
			 * Calculating the score of sentence according to sentence positioning
			 */
			if(j==0)//first Sentence , then score of 1
			{
				
				featureVector.elementAt(j).add(1.0);
			}
			else if(j >= 1 && j <=5)
			{
				
				featureVector.elementAt(j).add(0.8);
			}
			else
			{
				
				featureVector.elementAt(j).add(0.3);
			}
			/*
			 * Calculating the score of the sentence according to sentence length , for which , the maximum length is required 
			 */
			if(k > max)
			{
				max=k;
			}
			
			
			lengthFeature.add((double)k);
			
			/*
			 * Calculating the score of the sentence using sentence heading overlap feature 
			 */
			overlap.add(((double)commons)/((double)(hdcount+dtkCount)));
			
			
			
			//lineCount++;
		}
		lc = j;
		
		/*
		 *Calculating the score of the sentence according to sentence length 
		 */
		for(j=0;j<lc;j++)
		{
			double temp = ((lengthFeature.get(j))/max);
			lengthFeature.set(j, temp );
			featureVector.elementAt(j).add(temp);
		}
		
		/*
		 * Calculating the score of the sentence using term frequency concept 
		 */
		
		
		double tfscore = 0;
		double tfisfscore = 0;
		for(j=0;j<sentenceWords.size();j++)
		{
			Vector<TaggedTextWord> temp = sentenceWords.elementAt(j);
			tfscore=0;
			tfisfscore=0;
			String token = new String();
			double temptfScore=0;
			double temptfisfScore=0;
			for(k=0;k<temp.size();k++)
			{
				token = temp.elementAt(k).getIndexWord().getLemma().toLowerCase();
				temptfScore = textHash.get(token);
				temptfScore /= maxtf;
				if(termfreq.get(token)==null)// it stores the term frequency (normalized) value for every token/term
				{
					termfreq.put(token, temptfScore);
				}
				tfscore += temptfScore;
				
				temptfisfScore = temptfScore * (Math.log(lc/sentenceHash.get(token)));
				tfisfscore += temptfisfScore;
				
			}
			
			tfscore /= k;
			tfisfscore /= k;
			tf.add(tfscore);
			featureVector.elementAt(j).add(tfscore);
			tfisf.add(tfisfscore);
			featureVector.elementAt(j).add(tfisfscore);
			
			
		}
		
		
		for(j=0;j<overlap.size();j++)
		{
			featureVector.elementAt(j).add(overlap.get(j));
		}
			
			
	
		return featureVector;
	}
}