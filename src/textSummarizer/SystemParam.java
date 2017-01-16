package textSummarizer;

public class SystemParam {

	private static SystemParameters sys;
	static
	{
		sys = new SystemParameters();
		sys.init();
	}
	
	public static SystemParameters getInstance ()
	{
		return sys;
	}
}
