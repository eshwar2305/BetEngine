import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class NextMatchDetails extends Thread{
	/**
	   * How frequently to check for next match; 24hrs
	   */
	  private long m_sampleInterval = 1000 * 60 * 60 * 24;
	  
	  private boolean m_isBettingForNextMatchON = true;
	  
	  private ArrayList<Game> m_bettingGameList = new ArrayList<Game>();
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
	  
	  public void run()
	  {
		  //on the first run isBettingNextMatchON is set to true - begining of series
		while(true){
			if(m_isBettingForNextMatchON){
				m_bettingGameList = getBettingGameListFromDB(); 
				fireNextMatchDetailsAvailable(m_bettingGameList);
				m_isBettingForNextMatchON = false;
				
				//Once you get the next matches to bet for - go to sleep and wake up after 24hrs 
				//sleep for 24Hrs;
				
				try {
					sleep(m_sampleInterval);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				
				//After waking up from 24hrs sleep, query if there are any matches for today
				ArrayList todaysGameList = getTodaysGameList();
				  
				if(todaysGameList ==  null){
					// if there are no matches for today continue betting with same matches for next 24hrs
					m_isBettingForNextMatchON = false;
				}else{
					// if there are games for today
					for(int i=0;i<todaysGameList.size();i++){
						if(m_isBettingForNextMatchON == false){
							// Keep checking if any game in today has started
							// if its started then we need to get the next set of matches for betting
							// turn the isBettingForNextMatchON = true;
							Game m1 = (Game) todaysGameList.get(i);
							if(checkIfMatchStarted(i)){
								m_isBettingForNextMatchON = true; //  true if any game today is started
							}
						}
					}
				}	
			}		
		}	    
	  }

	private ArrayList<Game> getTodaysGameList() {
		//Ranju - Query DB to get the today matches
		//in DB and return a List of Games.
		// if no games today send null
		return m_dbHandle.getTodaysGame();
	}

	private ArrayList<Game> getBettingGameListFromDB() {
		//Ranju - Query DB to get the next matches(One s which state!=completed or state!=started)
		//in DB and return a List of Games. Next day s matches
		return null;
	}

	private boolean checkIfMatchStarted(int i) {
		// TODO Auto-generated method stub
		boolean flag;
		int matchNumber = i;
		
		// Ranju - Query DB to check if match i has started - return true or false;
		
		return true;
	}
	
}
