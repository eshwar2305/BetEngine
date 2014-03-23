import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;


public class Cricinfo extends Thread{

	  /* How frequently to check for next match; 2hrs
	   */
	  private long m_sampleInterval = 1000 * 60 * 2;
	  
	  private int m_onGoingMatchNumber = 1; // set it to fiurst game initailly
	  // to query for the results of this match
	  
	  private Game m_game;
	  /**
	   * Set of listeners
	   */
	  private Set m_listeners = new HashSet();

	  
	Elements m_potClass,m_potScores,m_potStatus; 
	
	  public void addCricinfoListener( CricinfoListener cc )
	  {
	    this.m_listeners.add( cc );
	  }

	  public void removeCricinfoListener( CricinfoListener cc )
	  {
	    this.m_listeners.remove( cc );
	  }
	  
	  protected void fireMatchResultAvailable( Game g )
	  {
	    for( Iterator i=this.m_listeners.iterator(); i.hasNext(); )
	    {
	    	CricinfoListener cc = ( CricinfoListener )i.next();
	    	cc.fireMatchResultAvailable( g );
	    }
	  }
	  
	  public void run()
	  {
		  
		while(true){
			
			//chk if the match is complete
			if(isMatchComplete(m_onGoingMatchNumber)){
				m_game.setGameId(m_onGoingMatchNumber);
				m_game.setWinTeam(getMatchWinner(m_onGoingMatchNumber));
				m_game.setTeamA(getTeamA(m_onGoingMatchNumber));
				m_game.setTeamB(getTeamB(m_onGoingMatchNumber));
				fireMatchResultAvailable(m_game);
				
				// increment the match count number to start querying for next match details
				m_onGoingMatchNumber++; 
			}else{
				
				//sleep for 2hrs and then check for the on going match status
				try {
					sleep(m_sampleInterval);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}	    
	  }
	public Cricinfo(String url) {
		 org.jsoup.nodes.Document doc;
		try {
			doc = Jsoup.connect(url).get();
			 m_potClass = doc.getElementsByClass("potMatchHeading");
			 m_potScores = doc.getElementsByClass("mat_scores");
			 m_potStatus = doc.getElementsByClass("mat_status");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getMatchInfo(int i) {
		StringBuffer sb = new StringBuffer();
		Element match = m_potClass.get(i-1);
		Element score = m_potScores.get(i-1);
		Element status = m_potStatus.get(i-1);
		
		TextNode matchNumber = match.textNodes().get(0);
		TextNode matchDate = match.textNodes().get(1);
		TextNode matchDesc = match.child(0).child(0).textNodes().get(0);
		
		String matchStatusStr,matchScoreStr;
		 
		List matchScoreNodes = score.childNodes();
		if(matchScoreNodes.size() != 0){
			TextNode matchScore = (TextNode) matchScoreNodes.get(0); 
			matchScoreStr = matchScore.text() ;
		}else{
			matchScoreStr = "N/A";
		}
	
		TextNode matchStatus;
		List matchStatusNodes;
		List childElements = status.children();
		if(childElements.size() == 0){
			matchStatusNodes = status.textNodes();
		  	if(matchStatusNodes.size() != 0){
		  		matchStatus = (TextNode) matchStatusNodes.get(0); 
		  		matchStatusStr = matchStatus.text() ;
		  	}else{
		  		matchStatusStr = "N/A";
		  	}
		}else{
			Element childEle = (Element) childElements.get(0);
			matchStatusNodes = childEle.textNodes();
		  	if(matchStatusNodes.size() != 0){
		  		matchStatus = (TextNode) matchStatusNodes.get(0); 
		  		matchStatusStr = matchStatus.text() ;
		  	}else{
		  		matchStatusStr = "N/A";
			  	}
		  	}
		  //System.out.println(matchDate.text() + matchNumber.text() + matchDesc.text() + "-" + matchScoreStr + "-" + matchStatusStr );
		  sb.append(matchDate.text()).append(matchNumber.text()).append(matchDesc.text()).append(matchScoreStr).append(matchStatusStr).append("\n");
		  return sb.toString();
	}
	
	public String printCompleteSchedule(){
		StringBuffer sb = new StringBuffer();
		 for(int i = 0 ;i< m_potClass.size() ; i++){
			Element match = m_potClass.get(i);
			Element score = m_potScores.get(i);
			Element status = m_potStatus.get(i);
			
			TextNode matchNumber = match.textNodes().get(0);
			TextNode matchDate = match.textNodes().get(1);
			TextNode matchDesc = match.child(0).child(0).textNodes().get(0);
			
			String matchStatusStr;
			String matchScoreStr;
			 
			List matchScoreNodes = score.childNodes();
			if(matchScoreNodes.size() != 0){
				TextNode matchScore = (TextNode) matchScoreNodes.get(0); 
				matchScoreStr = matchScore.text() ;
			}else{
				matchScoreStr = "N/A";
			}
		
			TextNode matchStatus;
			List matchStatusNodes;
			List childElements = status.children();
			if(childElements.size() == 0){
				matchStatusNodes = status.textNodes();
			  	if(matchStatusNodes.size() != 0){
			  		matchStatus = (TextNode) matchStatusNodes.get(0); 
			  		matchStatusStr = matchStatus.text() ;
			  	}else{
			  		matchStatusStr = "N/A";
			  	}
			}else{
				Element childEle = (Element) childElements.get(0);
				matchStatusNodes = childEle.textNodes();
			  	if(matchStatusNodes.size() != 0){
			  		matchStatus = (TextNode) matchStatusNodes.get(0); 
			  		matchStatusStr = matchStatus.text() ;
			  	}else{
			  		matchStatusStr = "N/A";
				  	}
			 }
			  //System.out.println(matchDate.text() + matchNumber.text() + matchDesc.text() + "-" + matchScoreStr + "-" + matchStatusStr );
			  sb.append(matchDate.text()).append(matchNumber.text()).append(matchDesc.text()).append(matchScoreStr).append(matchStatusStr);
		  	}
		 
		 return sb.toString();
	}

	public boolean isMatchComplete(int i) {
		String str = getMatchStatus(i);
		
		if(str.contains("N/A") || str.contains("scheduled")){
			return false;
		}else{
			return true;
		}
	}

	public String getMatchWinner(int i) {
		if(isMatchComplete(i)){
			String str = getMatchStatus(i);
			int index = str.indexOf("won");
			str = str.substring(0, index-1);
			return str.trim();
		}else{
			return "match yet to be played";
		}
	}

	public String getMatchStatus(int i) {
		Element status = m_potStatus.get(i-1);
		String matchStatusStr;
		
		TextNode matchStatus;
		List matchStatusNodes;
		List childElements = status.children();
		if(childElements.size() == 0){
			matchStatusNodes = status.textNodes();
		  	if(matchStatusNodes.size() != 0){
		  		matchStatus = (TextNode) matchStatusNodes.get(0); 
		  		matchStatusStr = matchStatus.text() ;
		  	}else{
		  		matchStatusStr = "N/A";
		  	}
		}else{
			Element childEle = (Element) childElements.get(0);
			matchStatusNodes = childEle.textNodes();
		  	if(matchStatusNodes.size() != 0){
		  		matchStatus = (TextNode) matchStatusNodes.get(0); 
		  		matchStatusStr = matchStatus.text() ;
		  	}else{
		  		matchStatusStr = "N/A";
			  	}
		 }
		
		return matchStatusStr;
	}

	public String getMatchTeams(int i) {
		Element match = m_potClass.get(i-1);
		TextNode matchDesc = match.child(0).child(0).textNodes().get(0);
		String str = matchDesc.text();
		
		int index = str.indexOf(" at ");
		str = str.substring(0, index);
		return str.trim();
	}

	public String getTeamA(int i) {
		String teamA = getMatchTeams(i);
		
		int index = teamA.indexOf(" v ");
		
		teamA = teamA.substring(0, index);
		
		return teamA.trim();
	}

	public String getTeamB(int i) {
		String teamB = getMatchTeams(i);
		
		int index = teamB.indexOf(" v ");
		
		teamB = teamB.substring(index+2);
		
		return teamB.trim();
	}

	public String getPlace(int i) {
		Element match = m_potClass.get(i-1);
		TextNode matchDesc = match.child(0).child(0).textNodes().get(0);
		String str = matchDesc.text();
		
		int index = str.indexOf(" at ");
		str = str.substring(index+3);
		return str.trim();
	}

	public String getScoreSheet(int i) {
		Element score = m_potScores.get(i-1);
		String matchScoreStr;
		 
		List matchScoreNodes = score.childNodes();
		if(matchScoreNodes.size() != 0){
			TextNode matchScore = (TextNode) matchScoreNodes.get(0); 
			matchScoreStr = matchScore.text() ;
		}else{
			matchScoreStr = "N/A";
		}
		
		return matchScoreStr;
	}

	public String getMatchDate(int i) {
		Element match = m_potClass.get(i-1);
		TextNode matchDate = match.textNodes().get(1);
		String str = matchDate.text();
		str = str.substring(2);
		return str;
	}


	
}
