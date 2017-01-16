package textSummarizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.StringTokenizer;


public class SystemParameters {

	public Hashtable<String, String> params;

	public void init() 
	{
		params = new Hashtable<String, String>();
		try
		{
			InputStream is = this.getClass().getClassLoader().getResourceAsStream("textSummarizer/conf/lexchain.conf");
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String line = "";
			while ((line = in.readLine()) != null)
			{
				StringTokenizer st = new StringTokenizer(line, "# ");
				String val = in.readLine();
				if (val == null)
				{
					System.out.println("Error in lexchain.conf : Value espected for parameter");
					System.exit(-1);
				}
				String param = st.nextToken();
				
				params.put(param, val);
			}
			in.close();
		}
		catch (IOException e) {System.out.println("Conf file not found!");}
		
	}
	
	public String getParam (String p)
	{
		return params.get(p);
	}
}
