package textSummarizer.machineLearning;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;


import weka.classifiers.*;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.trees.J48;

import weka.core.Instances;

public class learningClassifier {
	
	
	public static Vector<Double> buildFile(String summFile,Vector<Vector<Double>> fv) throws IOException
	{
		
		Vector<Double> clsLabel = new Vector<Double>();
		BufferedReader in = new BufferedReader (new FileReader(summFile));
		String line = new String();
		String text = new String();
		String delim = " ,.:!?)([]{}\";/\\+=-_@#$%^&*|";
		int lineCount = 0;
		while ((line = in.readLine())!=null)
		{
			StringTokenizer st = new StringTokenizer(line, delim);
			String token = new String();			
			while (st.hasMoreTokens())
			{
				token = st.nextToken();
			}
			if(token.startsWith("1"))
			{
				fv.elementAt(lineCount).add((double)1);
				clsLabel.add((double)1);
			}
			else
			{
				fv.elementAt(lineCount).add((double)0);
				clsLabel.add((double)0);
			}
			lineCount++;
		}
		
		//build ARFF 
		
		String sNL = "\r\n";
		String sAtt = "@attribute ";
		
		//write relation name
		String sARFF = "@relation " + "Text_Summarizer" + sNL + sNL;
		
		sARFF += sAtt + "posFeature" + " NUMERIC" + sNL;
		sARFF += sAtt + "lengthFeature" + " NUMERIC" + sNL;
		sARFF += sAtt + "tf" + " NUMERIC" + sNL;
		sARFF += sAtt + "tfisf" + " NUMERIC" + sNL;
		sARFF += sAtt + "overlap" + " NUMERIC" + sNL;
		sARFF += sAtt + "lexFeature" + " NUMERIC" + sNL;
		sARFF += sAtt + "IN_SUMMARY" + "{0.0,1.0}" + sNL;
		
		// add the @data tag
		sARFF += sNL + "@data" + sNL;
		
		
		for(int k=0;k<fv.size();k++)
		{
			
			String sData = "";
			for(int m=0;m<6;m++)
			{
				sData += fv.elementAt(k).elementAt(m) + ",";
			}
			//add the class label
			sData += fv.elementAt(k).elementAt(6) + sNL;
			
			//add this line of data to the arff string
			sARFF += sData;
			
		}
		
		Utilities.writeStringToFile(sARFF, summFile+ ".arff");
		return clsLabel;
		
	}
	/*
	 * This function is for building unlabelled input file , where 
	 * the sentences have not been already labelled as to whether they belong to summary or not
	 */
	public static void buildInputFile(String file, Vector<Vector<Double>> fv) throws IOException 
	{
		
		
		//build ARFF 
		
		String sNL = "\r\n";
		String sAtt = "@attribute ";
		
		//write relation name
		String sARFF = "@relation " + "Text_Summarizer" + sNL + sNL;
		
		sARFF += sAtt + "posFeature" + " NUMERIC" + sNL;
		sARFF += sAtt + "lengthFeature" + " NUMERIC" + sNL;
		sARFF += sAtt + "tf" + " NUMERIC" + sNL;
		sARFF += sAtt + "tfisf" + " NUMERIC" + sNL;
		sARFF += sAtt + "overlap" + " NUMERIC" + sNL;
		sARFF += sAtt + "lexFeature" + " NUMERIC" + sNL;
		sARFF += sAtt + "IN_SUMMARY" + "{0.0,1.0}" + sNL;
		
		// add the @data tag
		sARFF += sNL + "@data" + sNL;
		
		
		for(int k=0;k<fv.size();k++)
		{
			
			String sData = "";
			for(int m=0;m<5;m++)
			{
				sData += fv.elementAt(k).elementAt(m) + ",";
			}
			//add the class label
			sData += fv.elementAt(k).elementAt(5) + sNL;
			
			//add this line of data to the arff string
			sARFF += sData;
			
		}
		
		Utilities.writeStringToFile(sARFF, file+ ".arff");
		
		
	}
	
	public static Instances loadInstances(String file) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		Instances data = new Instances(reader);
		reader.close();
		data.setClassIndex(data.numAttributes() - 1);
		return data;
	}
	
	public static void classifyNB (Instances data,Instances data1, Vector<Double> cl, String outputFile, Vector<String> sentences) throws Exception
	{
		NaiveBayes nb = new NaiveBayes();
		nb.buildClassifier(data);
		Evaluation eval = new Evaluation(data);
		eval.crossValidateModel(nb, data, 2, new Random(1), new Object[] { });
		System.out.println("Evaluation of the build model using Training File ");
		System.out.println(eval.toSummaryString("\nResults\n======\n", false));

		eval = new Evaluation(data1);
		eval.evaluateModel(nb, data1, new Object[] { } );
		
		System.out.println("Evaluation of the build model using Test File ");
		System.out.println(eval.toSummaryString("\nResults\n======\n", false));
		String text = sentences.elementAt(0)+"\n";
		
		for(int i = 0; i < data1.numInstances(); i++)
		{
			double clsLabel = nb.classifyInstance(data1.instance(i));
			System.out.print(clsLabel+"     "); 
			System.out.println(cl.elementAt(i));
			if(clsLabel == 1.0)
			{
				text += sentences.elementAt(i+1)+"\n";
			}
			
		}
		
		text += eval.toSummaryString("\nResults\n======\n", false);
		
		Utilities.writeStringToFile(text, outputFile);
		
		
		
	}
	
	
	public static void evaluateNB (Instances data,Instances data1, String outputFile, Vector<String> sentences) throws Exception
	{
		NaiveBayes nb = new NaiveBayes();
		nb.buildClassifier(data);
		Evaluation eval = new Evaluation(data);
		eval.crossValidateModel(nb, data, 2, new Random(1), new Object[] { });
		System.out.println("Evaluation of the build model using Training File ");
		System.out.println(eval.toSummaryString("\nResults\n======\n", false));

		
		System.out.println("Classfication of the build input file  ");
		String text = sentences.elementAt(0)+"\n";
		
		for(int i = 0; i < data1.numInstances(); i++)
		{
			double clsLabel = nb.classifyInstance(data1.instance(i));
			System.out.print(clsLabel+"     "); 
			
			if(clsLabel == 1.0)
			{
				text += sentences.elementAt(i+1)+"\n";
			}
			
		}
		
		Utilities.writeStringToFile(text, outputFile);
		
		
		
	}
	
	
	public static void classifyJ48 (Instances data, Instances data1, Vector<Double> cl, String outputFile, Vector<String> sentences) throws Exception
	{
		
		 String[] options = new String[1];
		 options[0] = "-U";
		 J48 tree = new J48();         // new instance of tree
		 tree.setOptions(options);     // set the options
		 tree.buildClassifier(data);   // build classifier
		 
	 	Evaluation eval = new Evaluation(data);
		eval.crossValidateModel(tree, data, 2, new Random(1), new Object[] { });
		System.out.println("Evaluation of the build model using Training File ");
		System.out.println(eval.toSummaryString("\nResults\n======\n", false));

		eval = new Evaluation(data1);
		eval.evaluateModel(tree, data1, new Object[] { } );
		
		System.out.println("Evaluation of the build model using Test File ");
		System.out.println(eval.toSummaryString("\nResults\n======\n", false));
		
		for(int i = 0; i < data1.numInstances(); i++)
		{
			double clsLabel = tree.classifyInstance(data1.instance(i));
			System.out.print(clsLabel+"     "); 
			System.out.println(cl.elementAt(i));
		}
		
		String text = sentences.elementAt(0)+"\n";
		
		for(int i = 0; i < data1.numInstances(); i++)
		{
			double clsLabel = tree.classifyInstance(data1.instance(i));
			System.out.print(clsLabel+"     "); 
			System.out.println(cl.elementAt(i));
			if(clsLabel == 1.0)
			{
				text += sentences.elementAt(i+1)+"\n";
			}
			
		}
		
		text += eval.toSummaryString("\nResults\n======\n", false);
		
		Utilities.writeStringToFile(text, outputFile);
		

		
	}
	
	public static void evaluateJ48 (Instances data, Instances data1, String outputFile, Vector<String> sentences) throws Exception
	{
		
		 String[] options = new String[1];
		 options[0] = "-U";            // unpruned tree
		 J48 tree = new J48();         // new instance of tree
		 tree.setOptions(options);     // set the options
		 tree.buildClassifier(data);   // build classifier
		 
	 	Evaluation eval = new Evaluation(data);
		eval.crossValidateModel(tree, data, 2, new Random(1), new Object[] { });
		System.out.println("Evaluation of the build model using Training File ");
		System.out.println(eval.toSummaryString("\nResults\n======\n", false));

		System.out.println("Evaluation of the build input file ");
		
		
		for(int i = 0; i < data1.numInstances(); i++)
		{
			double clsLabel = tree.classifyInstance(data1.instance(i));
			System.out.print(clsLabel+"     "); 
			
		}
		
		String text = sentences.elementAt(0)+"\n";
		
		for(int i = 0; i < data1.numInstances(); i++)
		{
			double clsLabel = tree.classifyInstance(data1.instance(i));
			System.out.print(clsLabel+"     "); 
			
			if(clsLabel == 1.0)
			{
				text += sentences.elementAt(i+1)+"\n";
			}
			
		}
		
		text += eval.toSummaryString("\nResults\n======\n", false);
		
		Utilities.writeStringToFile(text, outputFile);
		

		
	}
	
	
	
	
}



//Class of basic utitlities
//Note: this was reused from a 572 lab.
class Utilities 
{
//In Microsoft's J++, the console window can close up before you get a chance to read it,
//so this method can be used to wait until you're ready to proceed.
public static void waitHere(String msg)
{
System.out.println("");
System.out.print(msg);
try { System.in.read(); }
catch(Exception e) {} // Ignore any errors while reading.
}
		
//This method will read the contents of a file, returning it as a string.  
//Note, this was taken from source code given in Lab 1
public static synchronized String getFileContents(String fileName)
{ File File = new File(fileName);
DataInputStream inputDataStream;
String results = null;

	//debug
	//System.out.println("");
	//System.out.println("Open file " + fileName + " ...");

try
{ int length = (int)File.length(), bytesRead;
  byte byteArray[] = new byte[length];

  ByteArrayOutputStream bytesBuffer = new ByteArrayOutputStream(length);
  FileInputStream       InputStream = new FileInputStream(File);
  bytesRead = InputStream.read(byteArray);
  bytesBuffer.write(byteArray, 0, bytesRead);
  InputStream.close();

  results = bytesBuffer.toString();
}
catch(Exception e)
{
  System.out.println("Exception in getFileContents(" + fileName + "), msg=" + e);
}

return results;
}

//Note: this does NOT add a final "linefeed" to the file.  Might not want to
//get that upon a subsequent "read" of the file.
static synchronized boolean writeStringToFile(String contents, String fileName)  
{
try
{ java.io.File        File   = new java.io.File(fileName);       // Want this to be an auto-flush file.
  java.io.PrintWriter stream = new java.io.PrintWriter(new java.io.FileOutputStream(File), true);

  stream.print(contents);
  stream.close();
  return true;
}
catch(Exception ioe)
{
  //Error("Exception writing to " + fileName + ".  Error msg: " + ioe);
  System.out.println("Exception writing to " + fileName + ".  Error msg: " + ioe);
  return false;
}
} 

} // end class Utilities


