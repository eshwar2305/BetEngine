package com.engine.app;

import java.util.ArrayList;
import com.engine.dataobject.*;



public interface NextMatchListener {
	
	  /**
	   * A new line has been added to the tailed log file
	   * 
	   * @param line  next match deails
	   */

	public void fireNextMatchDetailsAvailable(ArrayList<Game> gameList);
}
