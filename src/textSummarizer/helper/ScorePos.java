package textSummarizer.helper;

	/*
	 * This class defines the object of how sentence score is saved along with its position
	 */
	public class ScorePos
	{
		private double score;
		private int pos;
		
		public ScorePos(double score,int pos)
		{
			this.score=score;
			this.pos=pos;
		}
		
		public double getScore()
		{
			return this.score;
		}
		
		public int getPos()
		{
			return this.pos;
			
		}
		
		public void setScore(double score)
		{
			this.score=score;
		}
		
		public void setPos(int pos)
		{
			this.pos=pos;
		}
	}