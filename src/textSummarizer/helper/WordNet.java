package textSummarizer.helper;



import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;


import net.didion.jwnl.*;
import net.didion.jwnl.data.*;
import net.didion.jwnl.data.list.*;
import net.didion.jwnl.data.relationship.*;
import net.didion.jwnl.dictionary.*;
import java.util.Hashtable;

/**
 * This is a helper class used for interacting with WordNet JWNL API.
 * Do not use this class directly, use the singleton class WordNetHelper
 * @author Andrei
 *
 */
public class WordNet {

	/**
	 * The WordNet dictionary object used for retrieving WN information
	 */
    public Dictionary wordnet;
    public static final int DEPTH_LIMIT = 10;

    /**
     * Initialize the WordNet database, using the property XML provided as an argument
     * @param propsFile			Path of the property file needed by JWNL
     */
    public void initialize(String propsFile) {
        //String propsFile = "file_properties.xml";
        try {
            JWNL.initialize(new FileInputStream(propsFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JWNLException e) {
            e.printStackTrace();
        }
        // Create dictionary object
        wordnet = Dictionary.getInstance();
    }


    /**
     *  Returns an array of POS objects for a given String
     * @param s			The word for all which POS values are returned
     * @return			An array of {@link POS} objects for that word
     * @throws JWNLException
     */
    public POS[] getPOS(String s) throws JWNLException {
        // Look up all IndexWords (an IndexWord can only be one POS)
        IndexWordSet set = wordnet.lookupAllIndexWords(s);
        
        // Turn it into an array of IndexWords
        IndexWord[] words = set.getIndexWordArray();
        // Make the array of POS
        POS[] pos = new POS[words.length];
        for (int i = 0; i < words.length; i++) {
            pos[i] = words[i].getPOS();
        }
        return pos;
    }

    /**
     * Given a Wordnet {@link Relationship} object, returns the lowest common ancestor
     * @param rel			The WN {@code Relationship} object
     * @return 				A WN {@code Synset} object, representing the lowest common ancestor
     * @throws JWNLException
     */
    public Synset getLCA(Relationship rel) throws JWNLException
    {
    	if(rel==null) return null;
    	Synset s1 = rel.getSourceSynset();//meaning of the first word
    	Synset s2 = rel.getTargetSynset();//meaning of the second word
    	PointerType pt = rel.getType();
    	Hashtable<Synset,Integer> hash = new Hashtable<Synset,Integer>();
    	int depth = rel.getDepth();
    	PointerTargetTree tree1,tree2;
    	if (depth == 0)
    	{
    		return rel.getSourceSynset();
    	}
    	if (pt == PointerType.HYPERNYM) 
		{
        	tree1 = PointerUtils.getInstance().getHypernymTree(s1,depth);
        	tree2 = PointerUtils.getInstance().getHypernymTree(s2, depth);
		}
    	else if (pt == PointerType.HYPONYM)
    		{
    			tree1 = PointerUtils.getInstance().getHyponymTree(s1,depth);
            	tree2 = PointerUtils.getInstance().getHyponymTree(s2, depth);
    		}
    	
    	else if(pt == PointerType.ANTONYM)
	    	{
	    		tree1 = PointerUtils.getInstance().getIndirectAntonyms(s1,depth);
	        	tree2 = PointerUtils.getInstance().getIndirectAntonyms(s2, depth);
	     	}
    	else
			{
				tree1 = PointerUtils.getInstance().getSynonymTree(s1,depth);
	        	tree2 = PointerUtils.getInstance().getSynonymTree(s2, depth);
	        	
			}
    	
        	ArrayList<PointerTargetNodeList> lst1 = (ArrayList<PointerTargetNodeList>)tree1.toList();
        	ArrayList<PointerTargetNodeList> lst2 = (ArrayList<PointerTargetNodeList>)tree2.toList();
        	PointerTargetNodeList list1 = (PointerTargetNodeList)lst1.get(0);
        	PointerTargetNodeList list2 = (PointerTargetNodeList)lst2.get(0);
        	for (int i = 0; i < list1.size(); i++)
        	{
        		Synset sense1 = ((PointerTargetNode)list1.get(i)).getSynset();
        		hash.put(sense1, 0);
        	}
        	for (int i = 0; i < list2.size(); i++)
        	{
        		Synset sense2 = ((PointerTargetNode)list2.get(i)).getSynset();
        		if (hash.get(sense2) != null)
        			return sense2;
        	}
        	
 
	    	return null;
    }   
 
    
    public void printRelationship(Relationship rel) throws JWNLException
    {
    	ArrayList a = getRelationshipSenses(rel);
    	 for (int i = 0; i < a.size(); i++) {
             Synset s = (Synset) a.get(i);
             Word[] words = s.getWords();
             //System.out.print(i + ": ");
             for (int j = 0; j < words.length; j++ ) {
                 //System.out.print(words[j].getLemma());
                 if (j != words.length-1) System.out.print(", ");
             }
             //System.out.println();
         }
    }
    // gaseste legatura cu distanta cea mai mica pentru 2 cuvinte date 
    public Relationship getShortestPath (IndexWord start, IndexWord end ) throws JWNLException {
        // All the start senses
        Synset[] startSenses = start.getSenses();
        // All the end senses
        Synset[] endSenses = end.getSenses();
       
        // Check all against each other to find a relationship
        int min = Integer.MAX_VALUE;
        PointerType[] ptArray = {PointerType.SIMILAR_TO,PointerType.HYPERNYM,PointerType.HYPONYM,PointerType.ANTONYM};
        Relationship sol = null;
        for (int i = 0; i < startSenses.length; i++) {
            for (int j = 0; j < endSenses.length; j++) {
            	for (int t = 0; t < ptArray.length; t++)
            	{
            		RelationshipList list = RelationshipFinder.getInstance().findRelationships(startSenses[i], endSenses[j], ptArray[t]);
            		if (list.size() > 0)
            		{
            			for (int k = 0; k < list.size(); k++)
            			{
            				Relationship rel = (Relationship)list.get(k);
            				if (rel.getDepth() < min)
            				{
            					min = rel.getDepth();
            					sol = rel;
            				}
            			}
            		}
            	}
            }
        }
        
        return sol;
    }

    public Synset intersect(PointerTargetNodeList list1,PointerTargetNodeList list2, PointerType type)
    {
    	for (int i = 0; i < list1.size(); i++)
    	{
    		for (int j = 0; j < list2.size(); j++)
    		{
    			PointerTargetNode node1 = (PointerTargetNode)list1.get(i);
    			Synset s1 = node1.getSynset();
    			//System.out.println(node1.getType());
    			PointerTargetNode node2 = (PointerTargetNode)list2.get(j);
    			Synset s2 = node2.getSynset();
    			//System.out.println(node1.getType());
    			if (s1.equals(s2)){
    				type = node1.getType();
    				return s1;
    			}
    				

    		}
    	}
    	return null;

    }
    public PointerTargetNodeList concat(PointerTargetNodeList list1,PointerTargetNodeList list2)
    {
    	for (int i = 0; i < list2.size(); i++)
    	{
    		if (list1.contains(list2.get(i))==false)
    		list1.add(list2.get(i));
    	}
    	return list1;
    }
    public PointerTargetNodeList expandSynset(Synset s) throws JWNLException
    {
    	PointerTargetNodeList rezList = new PointerTargetNodeList();
    	PointerTargetNodeList relList = PointerUtils.getInstance().getDirectHypernyms(s);
		rezList = concat(rezList, relList);
		relList =  PointerUtils.getInstance().getSynonyms(s);
		rezList = concat(rezList,relList);
		relList = PointerUtils.getInstance().getDirectHyponyms(s);
		rezList = concat(rezList,relList);
		relList = PointerUtils.getInstance().getAntonyms(s);
		rezList = concat(rezList,relList);
		relList = PointerUtils.getInstance().getHolonyms(s);
		rezList = concat(rezList,relList);
		relList = PointerUtils.getInstance().getMeronyms(s);
		rezList = concat(rezList,relList);
		
		
		// recommendation to account for as many relationships 
	
		
		/*relList = PointerUtils.getInstance().getAlsoSees(s);
		rezList = concat(rezList,relList);
		
		relList = PointerUtils.getInstance().getDerived(s);
		rezList = concat(rezList,relList);
		 
		relList = PointerUtils.getInstance().getHolonyms(s);
		rezList = concat(rezList,relList);
		relList = PointerUtils.getInstance().getEntailedBy(s);
		rezList = concat(rezList,relList);
		relList = PointerUtils.getInstance().getMemberHolonyms(s);
		rezList = concat(rezList,relList);
		relList = PointerUtils.getInstance().getMemberMeronyms(s);
		rezList = concat(rezList,relList);
		relList = PointerUtils.getInstance().getMeronyms(s);
		rezList = concat(rezList,relList);
		relList = PointerUtils.getInstance().getEntailments(s);
		rezList = concat(rezList,relList);
		relList = PointerUtils.getInstance().getAlsoSees(s);
		rezList = concat(rezList,relList);
		relList = PointerUtils.getInstance().getPartMeronyms(s);
		rezList = concat(rezList,relList);
		relList = PointerUtils.getInstance().getSubstanceHolonyms(s);
		rezList = concat(rezList,relList);
		relList = PointerUtils.getInstance().getSubstanceMeronyms(s);
		rezList = concat(rezList,relList);*/
		return rezList;
    }
    public PointerTargetNodeList expandList(PointerTargetNodeList list) throws JWNLException
    {
    	PointerTargetNodeList rezList = new PointerTargetNodeList();
    	//rezList = concat(rezList, list);
    	for (int i = 0; i < list.size(); i++)
    	{
    		PointerTargetNode node = (PointerTargetNode)list.get(i);
			Synset s = node.getSynset();
			PointerTargetNodeList relList = expandSynset(s);
			rezList = concat(rezList,relList);

    	}
    	
    	return rezList;
    }
    public RelParent areRelated(Synset start,Synset end) throws JWNLException
    {
    	if (start.equals(end))
    	{
    		//System.out.println(start.getWord(0).getLemma());
    		return new RelParent(start,0,PointerType.SIMILAR_TO);
    	}
    	PointerTargetNodeList relatedList1 = expandSynset(start);
    	PointerTargetNodeList relatedList2 = expandSynset(end);
    	PointerType type = null;
    	for (int i = 0; i <= 1; i++)
    	{
    		Synset s = intersect(relatedList1, relatedList2, type); 
    		if (s != null)
    		{
    			//System.out.println(s.getWord(0).getLemma());
    			return new RelParent(s,i+1,type);
    		}
    		if (i < 1)
    		{
    			relatedList1 = expandList(relatedList1);
    			relatedList2 = expandList(relatedList2);
    		}
    	}
    	return new RelParent(null,100,type);

    	
    }
    
    /*
     * My addition : to see the relationship b/w two senses 
     */
    public RelParent areRelated1(Synset Start, Synset End) throws JWNLException
    {
    	int min = Integer.MAX_VALUE;
        PointerType[] ptArray = {PointerType.SIMILAR_TO,PointerType.HYPERNYM,PointerType.HYPONYM,PointerType.ANTONYM};
        Relationship sol = null;
        PointerType type = null;
            	for (int t = 0; t < ptArray.length; t++)
            	{
            		RelationshipList list = RelationshipFinder.getInstance().findRelationships(Start, End, ptArray[t]);
            		if (list.size() > 0)
            		{
            			for (int k = 0; k < list.size(); k++)
            			{
            				Relationship rel = (Relationship)list.get(k);
            				if (rel.getDepth() < min)
            				{
            					min = rel.getDepth();
            					sol = rel;
            				}
            			}
            		}
            	}
            	if(sol == null) {
            		return new RelParent(null,min,null);
            	}
                return new RelParent(getLCA(sol),min,sol.getType());
            	
    }
    
    public RelParent areRelated(IndexWord word1, IndexWord word2) throws JWNLException
    {
    	Synset[] startSenses = word1.getSenses();
        Synset[] endSenses = word2.getSenses();
        
        for (int i = 0; i < startSenses.length; i++) {
            for (int j = 0; j < endSenses.length; j++) {
            	RelParent rel = areRelated(startSenses[i], endSenses[j]); 
            	if (rel.getParentSynset()!=null)
            	{
            		return rel; 
            	}
            }
        }
        return new RelParent(null,100,null);
    }
    public  RelParent getShortestPathLCA(IndexWord word1, IndexWord word2) throws JWNLException
    {
    	Synset[] startSenses = word1.getSenses();
        Synset[] endSenses = word2.getSenses();
        int min = 99;
        RelParent lca = new RelParent(null,100, null);
        for (int i = 0; i < startSenses.length; i++) {
            for (int j = 0; j < endSenses.length; j++) {
            	RelParent rel = areRelated(startSenses[i], endSenses[j]); 
            	if (rel.getLength() < min)
            	{
            		min = rel.getLength();
            		lca = rel;
            	}
            }
        }
        return lca;
    }
    public ArrayList getRelationshipSenses (Relationship rel) throws JWNLException {
        ArrayList a = new ArrayList();
        PointerTargetNodeList nodelist = rel.getNodeList();
        Iterator i = nodelist.iterator();
        while (i.hasNext()) {
            PointerTargetNode related = (PointerTargetNode) i.next();
            a.add(related.getSynset());
        }
        return a;
    }

    // Get the IndexWord object for a String and POS
    public IndexWord getWord(POS pos, String s) throws JWNLException {
        IndexWord word = wordnet.getIndexWord(pos,s);
        return word;
    }

    public void printRelationship (String s1, POS pos1, String s2, POS pos2) throws JWNLException
    {
    	IndexWord iw1 = getWord(pos1, s1);
    	IndexWord iw2 = getWord (pos2, s2);
    	
    	RelParent rel = areRelated(iw1, iw2);
    	//System.out.println(rel.toString());
    	
    	
    }
}
