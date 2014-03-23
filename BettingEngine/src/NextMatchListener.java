import java.util.ArrayList;


public interface NextMatchListener {
	
	  /**
	   * A new line has been added to the tailed log file
	   * 
	   * @param line  next match deails
	   */

	public void fireNextMatchDetailsAvailable(ArrayList<Game> gameList);
}
