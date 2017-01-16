package textSummarizer;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

import textSummarizer.helper.TaggedSenseWord;
import textSummarizer.helper.TaggedTextWord;
import textSummarizer.helper.WordNetHelper;
import textSummarizer.machineLearning.*;
import weka.core.Instances;

public class Main {
	

	public static void main(String args[]) throws Exception
	{
		int argc = args.length;
		System.out.println(args[0]);
		System.out.println(argc);
		String usageMessage = "Wrong usage!\n" + "Usage: OPTIONS [--debug-print] [--output-all] \n" + "Use --help for more information\n";
		switch (argc)
		{
			case 2:
				if (args[0].equals("-h") || args[0].equals("--help"))
				{
					System.out.println("OPTIONS:");
					System.out.println("-train train_file.......................................train classifier on the training set");
					System.out.println("-test test_file..............................test classifier on the test file");
					System.out.println("-summarize in_file ..........................run classifier on the input file");
					
					
				}
				else if (args[0].equals("-train"))
				{
					//to train the classifier on the input set
					System.out.println("Inside training");
					String summFile = "";
					String textFile = ""; 
					String posOutputFile = "testing_pos.txt";
					String wsdOutputFile = "testing_wsd.txt";
					String lcOutputFile = "testing_lc.txt";
					
					for(int j = 0; args[1].charAt(j)!='.'; j++)
					{
						summFile += Character.toString(args[1].charAt(j));
						textFile += Character.toString(args[1].charAt(j));
						
					}
					textFile += ".txt";
					summFile += "_summ.txt";
					
					
					
					Vector<TaggedTextWord> headingWords = new Vector<TaggedTextWord>();
					Vector<String> sentences=new Vector<String>();
					WordNetHelper.getInstance().initialize(SystemParam.getInstance().getParam("WORDNET_PROPERTIES"));
					
					
					
					Vector<Vector<TaggedTextWord>> sentenceWords = Preprocess.structureText(textFile, true, headingWords, sentences, posOutputFile);
					Vector<Vector<Double>> fv = buildTrainingData.analyseTrainingCorpus(sentenceWords,headingWords);
					Vector<TaggedSenseWord> wsdWords = Preprocess.WSDFromWords(sentenceWords, true, wsdOutputFile);
					System.out.println(wsdWords);
					Vector<Vector<TaggedTextWord>> representativeMembers = LexicalChain.lexicalChainsFromWSD(wsdWords, lcOutputFile);
					Vector<Double> lexScore = LexicalChain.calculateLexicalScores(sentenceWords,representativeMembers);
					for(int k=0;k<fv.size();k++)
					{
						fv.elementAt(k).add(lexScore.elementAt(k));
					}
					
					//build training file for training the classifier 
					learningClassifier.buildFile(summFile,fv);
					
					
				}
				else
				{
					System.out.println(usageMessage);
				}
				break;
			case 4:
				
				if((args[0].equals("-test")==false) && (args[0].equals("-summarize")==false))
				{
					System.out.println(usageMessage);
				}
				else
				{
					
					if(args[0].equals("-test"))
						
					{
						//to test the classifier on the input test set
						
						
						String summFile = "";
						String outputFile1 = "";
						String outputFile2 = "";
						String textFile = ""; 
						String posOutputFile = "testing_pos.txt";
						String wsdOutputFile = "testing_wsd.txt";
						String lcOutputFile = "testing_lc.txt";
						for(int j = 0; args[1].charAt(j)!='.'; j++)
						{
							summFile += Character.toString(args[1].charAt(j));
							textFile += Character.toString(args[1].charAt(j));
							outputFile1 += Character.toString(args[1].charAt(j));
							outputFile2 += Character.toString(args[1].charAt(j));
						}
						
						textFile += ".txt";
						summFile += "_summ.txt";
						outputFile1 += "_nb.txt";
						outputFile2 += "_j48.txt";
						Vector<TaggedTextWord> headingWords = new Vector<TaggedTextWord>();
						Vector<String> sentences=new Vector<String>();
						WordNetHelper.getInstance().initialize(SystemParam.getInstance().getParam("WORDNET_PROPERTIES"));
						
						
						
						Vector<Vector<TaggedTextWord>> sentenceWords = Preprocess.structureText(textFile, true, headingWords, sentences, posOutputFile);
						Vector<Vector<Double>> fv = buildTrainingData.analyseTrainingCorpus(sentenceWords,headingWords);
						Vector<TaggedSenseWord> wsdWords = Preprocess.WSDFromWords(sentenceWords, true, wsdOutputFile);
						Vector<Vector<TaggedTextWord>> representativeMembers = LexicalChain.lexicalChainsFromWSD(wsdWords, lcOutputFile);
						Vector<Double> lexScore = LexicalChain.calculateLexicalScores(sentenceWords,representativeMembers);
						for(int k=0;k<fv.size();k++)
						{
							fv.elementAt(k).add(lexScore.elementAt(k));
						}
						
						//build testing file for testing the classifier 
						Vector<Double> clsLabel = learningClassifier.buildFile(summFile,fv);
						
						String file = "C:/corpus1/training/training.arff";
						
						//to test the build classifier on the input file
						Instances data = learningClassifier.loadInstances(file);//contains instances of training file
						Instances data1 = learningClassifier.loadInstances(summFile+".arff");//contains instances of test file
						
						//to test using NaiveBayesClassifier 
						learningClassifier.classifyNB(data, data1, clsLabel, outputFile1, sentences);
						
						//to test using J48 tree , i.e. C4.5 tree implementation of weka
						learningClassifier.classifyJ48(data,data1, clsLabel, outputFile2, sentences);
						
						
						
						
						
					}
					else if(args[0].equals("-summarize"))
					{
						// to summarize the input
						String summFile = "";
						String outputFile1 = "";
						String outputFile2 = "";
						String textFile = ""; 
						String posOutputFile = "testing_pos.txt";
						String wsdOutputFile = "testing_wsd.txt";
						String lcOutputFile = "testing_lc.txt";
						for(int j = 0; args[1].charAt(j)!='.'; j++)
						{
							summFile += Character.toString(args[1].charAt(j));
							textFile += Character.toString(args[1].charAt(j));
							outputFile1 += Character.toString(args[1].charAt(j));
							outputFile2 += Character.toString(args[1].charAt(j));
						}
						
						textFile += ".txt";
						outputFile1 += "_nb.txt";
						outputFile2 += "_j48.txt";
						Vector<TaggedTextWord> headingWords = new Vector<TaggedTextWord>();
						Vector<String> sentences=new Vector<String>();
						WordNetHelper.getInstance().initialize(SystemParam.getInstance().getParam("WORDNET_PROPERTIES"));
						
						
						
						Vector<Vector<TaggedTextWord>> sentenceWords = Preprocess.structureText(textFile, true, headingWords, sentences, posOutputFile);
						Vector<Vector<Double>> fv = buildTrainingData.analyseTrainingCorpus(sentenceWords,headingWords);
						Vector<TaggedSenseWord> wsdWords = Preprocess.WSDFromWords(sentenceWords, true, wsdOutputFile);
						Vector<Vector<TaggedTextWord>> representativeMembers = LexicalChain.lexicalChainsFromWSD(wsdWords, lcOutputFile);
						Vector<Double> lexScore = LexicalChain.calculateLexicalScores(sentenceWords,representativeMembers);
						for(int k=0;k<fv.size();k++)
						{
							fv.elementAt(k).add(lexScore.elementAt(k));
						}
						
						//build testing file for testing the classifier 
						learningClassifier.buildFile(textFile,fv);
						
						String file = "C:/corpus1/training/training.arff";
						
						//to test the build classifier on the input file
						Instances data = learningClassifier.loadInstances(file);//contains instances of training file
						Instances data1 = learningClassifier.loadInstances(textFile+".arff");//contains instances of test file
						
						//to test using NaiveBayesClassifier 
						learningClassifier.evaluateNB(data, data1, outputFile1, sentences);
						
						//to test using J48 tree , i.e. C4.5 tree implementation of weka
						learningClassifier.evaluateJ48(data, data1, outputFile2, sentences);
						
						
						
					}
					
				}
				
					
				break;
			default:
				System.out.println(args[0]);
				if ((args[0].equals("-train")==false) && (args[0].equals("-test")==false) &&
				   (args[0].equals("-summarize")==false))
				{
					System.out.println(usageMessage);
				}
				break;
			
			
		}
				
	}
}
