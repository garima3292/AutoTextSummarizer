package textSummarizer.wsd;
import java.util.*;

import textSummarizer.*;
import textSummarizer.helper.*;
import net.didion.jwnl.*;
import net.didion.jwnl.data.*;
class Score
{
	private double total;
	private double length;
	private double grad;
	public Score(double total,double length,double grad)
	{
		this.total = total;
		this.length = length;
		this.grad = grad;
		
	}
	public double getTotal()
	{
		return total;
	}
	public double getLength()
	{
		return length;
	}
	public double getGrad()
	{
		return grad;
	}
	public static int compare(Score s1,Score s2)
	{
		if (s1.getTotal() < s2.getTotal())
			return -1;
		if (s1.getTotal() > s2.getTotal())
			return 1;
		return 0;
	}
	public String toString()
	{
		return new String(total+" "+length+" "+grad);
	}
}
public class WSDisambiguator {
	private  Vector<Vector<Synset>> senseList;
	private  SynsetMatrix synMat;
	private  int nrOfWords;
	private  Vector<Integer> wordsIndex;
	private  Score min;
	private  Vector<Integer> sol;
	private  Vector<TaggedTextWord> utt;
	private Vector<Vector<Integer>> valid;
	public WSDisambiguator(Vector<TaggedTextWord> utt) throws JWNLException
	{
		synMat = new SynsetMatrix(utt);
		synMat.getSynonymWords(utt);
		senseList = synMat.getSenseList();
		valid = synMat.getValid();
		nrOfWords = utt.size();
		wordsIndex = new Vector<Integer>();
		sol = new Vector<Integer>();
		for (int i = 0; i < nrOfWords; i++)
		{
			wordsIndex.add(-1);
			sol.add(-1);
		}
		min = new Score(Double.MAX_VALUE,1,Double.MAX_VALUE);
		this.utt = utt;
	}
	public  Score computeScore(Vector<Integer> grad,Vector<Integer> distance)
	{
		double meanLength = 0;
		double meanGrad = 0;
		double stdDevGrad = 0;
		double stdDevLength = 0;
		
		for (int i = 0; i < nrOfWords; i++)
		{
			meanGrad = meanGrad + grad.elementAt(i);
			
		}
		for (int i = 0; i < distance.size(); i++)
		{
			meanLength = meanLength + distance.elementAt(i);
		}
		
		meanLength = (distance.size() > 0 ? meanLength/distance.size() : 0);
		meanGrad = meanGrad / nrOfWords;
		for (int i = 0; i < nrOfWords; i++)
		{
			stdDevGrad = stdDevGrad + Math.pow(grad.elementAt(i) - meanGrad, 2);
		}
		stdDevGrad = Math.sqrt(stdDevGrad / nrOfWords);
		for (int i = 0; i < distance.size(); i++)
		{
			stdDevLength = stdDevLength + Math.pow(distance.elementAt(i) - meanLength, 2);
		}
		stdDevLength = (distance.size() > 0 ? Math.sqrt(stdDevLength/distance.size()) : 0);
		
		return new Score(meanGrad * stdDevGrad + meanLength , stdDevLength, stdDevGrad);
		
	}
	
	public  void back(int k)
	{
		if (k == nrOfWords)
		{
			Vector<Integer> distance = new Vector<Integer>();
			Vector<Integer> grad = new Vector<Integer>();
			for (int i = 0; i < nrOfWords; i++)
			{
				grad.add(0);
			}
			for (int i = 0; i < nrOfWords - 1; i++)
			{
				for (int j = i + 1; j < nrOfWords; j++)
				{
					int v = synMat.getRelLength(i, wordsIndex.elementAt(i), j, wordsIndex.elementAt(j));
					distance.add(v);
					if (v < 100)
					{
						grad.set(i, grad.elementAt(i) + 1);
						grad.set(j, grad.elementAt(j) + 1);
					}
				}
			}
			
			Score score = computeScore(grad, distance);
			if ( Score.compare(score, min) == -1)  //score is less than minimum 
			{
				min = new Score(score.getTotal(),score.getLength(),score.getGrad());
				for (int i = 0; i < nrOfWords; i++)
				{
					sol.set(i, wordsIndex.elementAt(i));
				}
			}
		}
		else
		{
			for (int i = 0; i < senseList.elementAt(k).size(); i++)
			{
				if (valid.elementAt(k).elementAt(i) == 1)
				{
					wordsIndex.set(k, i);
					back(k+1);
				}
			}
		}
	}
	public  Vector<TaggedSenseWord> wsd()
	{
		if (Preprocess.DEBUG_PRINT)
			System.out.println("here wsd");
		
		back(0);
		Vector<TaggedSenseWord> sdWords = new Vector<TaggedSenseWord>();
		
		for (int i = 0; i < nrOfWords; i++)
		{
			int k = sol.elementAt(i);
			
			Vector<Synset> senses = senseList.elementAt(i);
			Synset sense  = senses.elementAt(k);
			SynsetMatrix.senseCache.put(sense.getOffset(), 1);
			
			sdWords.add(new TaggedSenseWord(utt.elementAt(i),sense)); 
		}
		if (Preprocess.DEBUG_PRINT)
			System.out.println("Score = " + min);
		
		return sdWords;
		
	}
}
