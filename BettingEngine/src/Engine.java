import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;


public class Engine implements LogFileTailerListener,CricinfoListener,NextMatchListener{

	private LogFileTailer m_tailer;
	private NextMatchDetails m_nmDetails;
	private Cricinfo m_cricinfo;
	private TryDBAccess m_dbHandle;
	private HashMap<String, String> m_mp = new HashMap<>();
	private String m_teamA,m_teamB,m_teamC,m_teamD,m_teamE,m_teamF;
	private int m_m1,m_m2,m_m3;
	private String m_m1Place,m_m2Place,m_m3Place;
	private int m_noOfMatches;
	private ArrayList<Game> m_gamelist;
	
	private String[] players = {"Eshwar","Abhi","Amruth","Anuranjan","Apurva","Ashwin",
								"Chaitra","DN","PM","Prashanth","Rajiv","Ranju","Ravi",
								"Shashi","Sinchana","Sindhu","Swaroop","Veena","Hemanth","Pradeep"
								};
	String[] m_cmd = {
	        "python",
	        "Writer_Yowsup\\yowsup-cli",
	        "-c",
	        "Writer_Yowsup\\config.example",
	        "-s",
	        "13026901224", //Send msg to Eshu number
	        //"13026901224-1333685017", -- BG family
	        //"919686811944-1394712848", //- BAri talk
	        "test"
	    };
	
	
	public void spark() {
		loadPhoneNumbers();
	    m_tailer = new LogFileTailer( new File( "Reader_Yowsup\\Examples\\out.txt" ), 1000, false );
	    m_tailer.addLogFileTailerListener( this );
	    m_tailer.start();
	    
	    /*m_nmDetails = new NextMatchDetails(m_dbHandle);
	    m_nmDetails.addNextMatchListener(this);
	    m_nmDetails.start();
	    
	    m_cricinfo =  new Cricinfo("http://www.espncricinfo.com/world-t20/engine/series/628368.html");
		m_cricinfo.addCricinfoListener(this);
		m_cricinfo.start();*/
	}
	
	@Override
	public void fireNextMatchDetailsAvailable(ArrayList<Game> gameList) {
		//initialize all string to empty
		 
		m_teamA =m_teamB =m_teamC =m_teamD =m_teamE =m_teamF ="";
		m_noOfMatches = gameList.size();
		for(int i=0;i<m_noOfMatches;i++){
			Game g = gameList.get(0);
			if(i == 1){
				m_teamA = g.getTeamA();
				m_teamB = g.getTeamB();
				m_m1 = g.getGameId();
				m_m1Place = g.getPlace();
			}else if(i == 2){
				m_teamC = g.getTeamA();
				m_teamD = g.getTeamB();
				m_m2 = g.getGameId();
				m_m2Place = g.getPlace();
			}else if(i == 3){
				m_teamE = g.getTeamA();
				m_teamF = g.getTeamB();
				m_m3 = g.getGameId();
				m_m3Place = g.getPlace();
			}
		}
		
		
	}

	@Override
	public void fireMatchResultAvailable(Game g) {
		// call the win trigger for DB update
		m_dbHandle.winTrigger(g.getGameId(), g.getWinTeam());
		
		//sleep for 5s - let DB get updated with new calculation
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		StringBuffer sendMsg = new StringBuffer();
		sendMsg.append("Winner of M");
		sendMsg.append(g.getGameId());
		sendMsg.append(": ");
		sendMsg.append(g.getWinTeam());
		sendMsg.append("Scorecard: ");
		sendMsg.append(getCurrentScoreSheet());
    	fireMsg(sendMsg.toString());
	}

	@Override
	public void newLogFileLine(String line) {

		String number,name,timestamp,msg;

	    System.out.println("out.txt - " + line );
		
	    number = line.substring(0, 12);
	    //System.out.println(number);
	    
	    name = m_mp.get(number); // name of the better
	    //System.out.println(name);
	    
	    timestamp = line.substring(13,31);
	    
	    msg = line.substring(32);
	    msg = msg.toLowerCase();
	    //System.out.println(msg);
	    
	    if(name != null){
	    	fireMsg(msg);
	    	/*StringBuffer sendMsg = new StringBuffer();
	    	if(msg.contains("score")){
		    	sendMsg = getCurrentScoreSheet();
		    	
		    	fireMsg(sendMsg.toString());  // Latest scoresheet
		    	return;
	    	}else if(msg.contains("bet")){
	    		sendMsg = getCurrentBetSheet();
		    	
		    	fireMsg(sendMsg.toString());  // Latest Betsheet
		    	return;
	    	}

	    	ArrayList<String> noOfBetsInMsg = getTeamNamesFromMsg(msg);
	    	if(noOfBetsInMsg == null){
	    		sendMsg.append(name);
	    		sendMsg.append(" please specify correct team names.");
	    		fireMsg(sendMsg.toString());  // indiviual bet error msg
	    		 
	    		return;
	    	}else{
	    		sendMsg.append(name);
	    		sendMsg.append(": ");
		    	for(int i=0;i<noOfBetsInMsg.size();i++){
		    		String team = noOfBetsInMsg.get(i);
		    		
		    		if(msg.contains("Proxy")){
		    			String proxyName = getProxyName(msg);
		    			if(proxyName == null) return;
		    			name = proxyName + "";
		    		}

		    		placeBetForName(name,team);
		    		sendMsg.append(team);
		    		sendMsg.append("-");
		    	}
		    	fireMsg(sendMsg.toString());  // indiviual bet confirmation
	    	}
	    	
	    	sendMsg = getCurrentBetSheet();
	    	
	    	fireMsg(sendMsg.toString());  // Latest betsheet
	    	*/
	    	
	    }else{
	    	//Do nothing
	    	return;
	    }
	}

	private String getProxyName(String msg) {
		for(int i=0;i<players.length;i++){
			if(msg.contains(players[i])){
				return players[i];
			}
		}
		return null;
	}

	private StringBuffer getCurrentScoreSheet() {
		// Ranju - Query DB to give me the current Score sheet 
		return null;
	}


	private void fireMsg(String sendMsg) {
		m_cmd[6] = sendMsg.toString();
    	try {
    		Process p = Runtime.getRuntime().exec(m_cmd);
    		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
    		while(reader.readLine() != null){
    			System.out.println(reader.readLine());
    		}
    		
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
	}

	private StringBuffer getCurrentBetSheet() {
		// Ranju - Query DB to give me the current Bet for all the matches whose betting is going on
		
		//dbHandle.getCurrentBetSheet();
		StringBuffer strBuf = new StringBuffer();
		return strBuf;
	}

	private void placeBetForName(String name, String team) {
		// TODO Auto-generated method stub
		// Ranju - Input to DB the bets
		m_dbHandle.insertBetting(name, team);
	}

	private ArrayList<String> getTeamNamesFromMsg(String msg) {
		ArrayList<String> teams = new ArrayList<String>();
		
		if(msg.contains(m_teamA)){
			teams.add(m_teamA);
		}else if(msg.contains(m_teamB)){
			teams.add(m_teamB);
		}else if(msg.contains(m_teamC)){
			teams.add(m_teamC);
		}else if(msg.contains(m_teamD)){
			teams.add(m_teamD);
		}else if(msg.contains(m_teamE)){
			teams.add(m_teamE);
		}else if(msg.contains(m_teamF)){
			teams.add(m_teamF);
		}
		return teams;
	}

	private void loadPhoneNumbers() {
		m_mp.put("13026901224@", "Eshwar");
		m_mp.put("919886558406", "Abhi");
		m_mp.put("919845208308", "Amruth");
		m_mp.put("919686433880", "Anuranjan");
		m_mp.put("919902315159", "Apurva");
		m_mp.put("919686811944", "Ashwin");
		m_mp.put("919611360852", "Chaitra");
		m_mp.put("919845632873", "DN");
		m_mp.put("919912091116", "PM");
		m_mp.put("919845407435", "Prashanth");
		m_mp.put("918008677337", "Rajiv");
		m_mp.put("917799889885", "Ranju");
		m_mp.put("919886741295", "Ravi");
		m_mp.put("919916394597", "Shashi");
		m_mp.put("919164100066", "Sinchana");
		m_mp.put("919945564212", "Sindhu");
		m_mp.put("919880716292", "Swaroop");
		m_mp.put("919701550588", "Veena");
		m_mp.put("919164444598", "Hemanth");
		m_mp.put("919880338008", "Pradeep");
	}

}
