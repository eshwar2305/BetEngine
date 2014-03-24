package com.engine.app;
import java.io.File;
import com.engine.dataaccess.*;
import com.engine.dataobject.*;

import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class NextMatchDetails extends Thread{
	/**
	   * How frequently to check for next match; 4hrs
	   */
	  private long m_sampleInterval = 1000 * 60 * 60 * 4;
	  
	  private boolean m_bettingWindowChanged = false;
	  
	  private ArrayList<Game> currentBettingGameList = new ArrayList<Game>();
	 
	  
	  private TryDBAccess m_dbHandle;
	  /**
	   * Set of listeners
	   */
	  private Set m_listeners = new HashSet();



	  public NextMatchDetails(TryDBAccess dbHandle) {
		  m_dbHandle = dbHandle;
	  }

	  public void addNextMatchListener( NextMatchListener nm )
	  {
	    this.m_listeners.add( nm );
	  }

	  public void removeNextMatchListener( NextMatchListener nm )
	  {
	    this.m_listeners.remove( nm );
	  }
	  
	  
	  protected void fireNextMatchDetailsAvailable( ArrayList<Game> gameList )
	  {
	    for( Iterator i=this.m_listeners.iterator(); i.hasNext(); )
	    {
	      NextMatchListener nm = ( NextMatchListener )i.next();
	      nm.fireNextMatchDetailsAvailable( gameList );
	    }
	  }
	  
	  /***Eshu -  **/
	  public void run()
	  {
		ArrayList <Game> newBettingGameList = new ArrayList<Game>();
		fireNextMatchDetailsAvailable(this.getBettingGameList()); //initialize the current games first
		while(true){			
			newBettingGameList = this.getBettingGameList();	
			System.out.println(" get games ");
			m_bettingWindowChanged = this.isBettingWindowChanged(newBettingGameList);
			System.out.println(" changed ?  " + m_bettingWindowChanged);
			if(m_bettingWindowChanged){				
				fireNextMatchDetailsAvailable(currentBettingGameList);				
			}		
			
			//Once you get the next matches to bet for - go to sleep and wake up after 4hrs 
			//sleep for 4Hrs;
			try {
				sleep(m_sampleInterval);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	    
	  }

	private ArrayList<Game> getBettingGameList() {
		//Ranju - Query DB to get the today matches
		return m_dbHandle.getGamesForBetting();
	}
	
	private boolean isBettingWindowChanged(ArrayList<Game> newBettingGameList){
		boolean ret = false;
		if((currentBettingGameList != null && newBettingGameList == null) ||
				(currentBettingGameList == null && newBettingGameList != null)||
				currentBettingGameList.size() != newBettingGameList.size())
				ret = true;
		
		List<Game> oldL = currentBettingGameList;
		List<Game> newL = newBettingGameList;
		
		newL.removeAll(currentBettingGameList);
		
		if(oldL.isEmpty())
			return false;		
		
		//refresh current
		currentBettingGameList.clear();
		currentBettingGameList.addAll(newBettingGameList);
		
		return ret;
	}

	private boolean checkIfMatchStarted(int i) {
		// TODO Auto-generated method stub
		boolean flag;
		int matchNumber = i;
		
		// Ranju - Query DB to check if match i has started - return true or false;
		
		return true;
	}
	
}
