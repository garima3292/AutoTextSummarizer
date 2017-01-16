package textSummarizer.helper;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.*;
/**
 * Class used for retrieving the probability of encountering a given word. Since a relevant corpus was
 * not available for this task, the number of hits returned by a Google search of that word is returned.
 * This information is needed by the semantic distance computation methods.
 * 
 * @author Andrei
 *
 */
// metoda propriu-zisa
	public class WordCounter
	{
		/**
		 * Returns the hits of Google search of the given string
		 * @param word			The string representing the word to search for
		 * @return				The number of Google results
		 */
		
	public static long getCount(String word){ // is connected to google , read the information on the page and returns the number of hits for the word ' word '

		String page;
		
		page = "http://www.google.com/search?hl=en&q=" + word+ "&btnG=Search"; // create link google to search word ' Word '
		//page = "http://www.google.ro/search?hl=ro&source=hp&q="+word+"google&btnG=Cautare+Google&meta=&aq=f&oq=";
		long numarLong = -1; // number of hits to be returned by the method 

		try{
			URL url = new URL(page); // create the URL to  connect to 
			HttpURLConnection huc = (HttpURLConnection) url.openConnection(); // realize connection
			
			String line = null; // reads one line of the page
			String pag = new String(""); // string containing that page
			int index; //  index used to read the number of hits
			String numar=""; // numarul in format string
			
			
			huc.setRequestMethod("GET"); // sunt trimise header-ele
			huc.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; JVM)");
			huc.setRequestProperty("Pragma", "no-cache");
			huc.connect(); // conectarea efectiva

			BufferedReader reader = new BufferedReader(new InputStreamReader(huc.getInputStream())); //citesc informatia primita
			//pag = "";
			
			while ((line = reader.readLine()) != null)
				pag = pag + line; // memorez informatia
			System.out.println(pag);
		
			huc.disconnect(); // ma deconectez

			//return pag; // am memorat continutul paginii

			index = pag.indexOf("About"); // I positioned the page to get the result 
			//index = pag.indexOf("of about",index);
			//index = pag.indexOf("<b>",index);
			index = index + 6;
			while (pag.charAt(index)!=' ') // iau cuvantul sub forma string (< vine de la </b>)
			{
				numar = numar + pag.charAt(index);
				index ++;
			}
			
			numar = numar.replace(",",""); // elimin virgulele
			numar = numar.replace(".",""); // elimin punctele

			try{ 
				if(!numar.isEmpty())
				{
				numarLong = Long.parseLong(numar); // convert word in - a long
				System.out.println(numarLong);
				return numarLong;
				}
			} catch (Exception e) {
				System.out.println(" does not work in word processing - a long!");
				e.printStackTrace();
				//System.exit(-1);
				}

			return -1;

		} catch (Exception e) { 
			System.out.println("Connection to google cannot be established");
			e.printStackTrace();
			//System.exit(-1);
			}
		
		return -1;
		//return 1;
	}
	}
