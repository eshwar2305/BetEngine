import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;




public class TryDBAccess
{
	Connection conn = null;
	private PreparedStatement playerInsert = null;
	private PreparedStatement bettingInsert = null;
	private PreparedStatement scoreUpdate = null;
	private PreparedStatement winUpdate = null;
	private PreparedStatement todayGameGet = null;	
	private PreparedStatement gameGet = null;
	private PreparedStatement bettingGet = null;	
	static private ArrayList<Game>todaysGame = null; 
	static private  int TOTALPLAYERS = 20;
	static private  int SCOREPERPLAYER = 25;
	
			
		
	public TryDBAccess(){
		getConnection();
		if(todaysGame == null){
			todaysGame = new ArrayList<Game>();
		}
	}
	
	private Connection getConnection(){
		if (conn == null){
			System.out.println("con is null");
			String driver = "org.apache.derby.jdbc.ClientDriver";
			// the database name  
			String dbName="TryDB";
			   // define the Derby connection URL to use 
			String connectionURL = "jdbc:derby://localhost:50000/" + dbName + ";create=true";
			try{
		         Class.forName(driver); 
		         System.out.println(driver + " loaded. ");
		         conn = DriverManager.getConnection(connectionURL);
		         
		         //initialize my prepstatments
		         playerInsert = conn.prepareStatement("insert into player(playerId, name) values (?,?)");		         
		         bettingInsert = conn.prepareStatement("insert into betting(playerId, gameId, betTeam, score, betDate) values (?,?,?,0,?)");
		         todayGameGet = conn.prepareStatement("select * from game where startDate < ? and endDate > ?");
		         gameGet = conn.prepareStatement("select * from game where gameId = ?");
		         scoreUpdate = conn.prepareStatement("update betting set score =? where bettingId = ?");
		         winUpdate = conn.prepareStatement("update game set winTeam = ? where gameId = ?");
		         bettingGet = conn.prepareStatement("select * from betting where gameId = ?");
		    } catch(java.lang.ClassNotFoundException e)     {
		          System.err.print("ClassNotFoundException: ");
		          System.err.println(e.getMessage());
		          System.out.println("\n    >>> Please check your CLASSPATH variable   <<<\n");
		    } catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return conn;
	}
	
	public boolean insertPlayer(String playerId, String name){		    
		boolean ret = false;
		try {
			playerInsert.setString(1, playerId);
			playerInsert.setString(2,name);
			ret = playerInsert.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return ret;
	}
	
	public ArrayList<Game> getTodaysGame(){	
		//validate the date here rather than hit the DB all the thime!!
		///TBD performance!!!!!!!
		Game g;
		Timestamp currentTimestamp = new Timestamp(new java.util.Date().getTime());
		
		Iterator<Game> iter = todaysGame.iterator();
		while(iter.hasNext()){
			g = (Game) iter.next();
			if(currentTimestamp.after(g.getStartDate()) && currentTimestamp.before(g.getEndDate())){
				//do nothing - keep it //valid match
			}else {
				iter.remove();
			}
		}
		
		if(todaysGame.isEmpty()){ //fetch from DB now		
			forceGetTodaysGame(currentTimestamp);
		}
		return todaysGame;		
	}
	
	public ArrayList<Game> forceGetTodaysGame(Timestamp currentTimestamp){
		todaysGame.clear();
		try {
			todayGameGet.setTimestamp(1,currentTimestamp);
			todayGameGet.setTimestamp(2,currentTimestamp);
			ResultSet rs = todayGameGet.executeQuery();
			while (rs.next()){				
				Game gg = new Game(rs.getInt(1),rs.getString(2), rs.getString(3), rs.getString(4), rs.getTimestamp(5),rs.getTimestamp(6), rs.getString(7));
				todaysGame.add(gg);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return todaysGame;		
	}
	
	public int validateBet(String betTeam, Timestamp currentTimestamp){
		int ret = -1;
		
		//lookup on the cache
		ArrayList<Game> todaysGame = getTodaysGame();
		Iterator <Game> iter = todaysGame.iterator();
		while(iter.hasNext()){
			Game g = iter.next();
			if(g.getTeamA().equalsIgnoreCase(betTeam) || g.getTeamB().equalsIgnoreCase(betTeam)){
				return g.getGameId();
			}
		}
		
		//Try again at the DB
		todaysGame = forceGetTodaysGame(currentTimestamp);
		iter = todaysGame.iterator();
		while(iter.hasNext()){
			Game g = iter.next();
			if(g.getTeamA().equalsIgnoreCase(betTeam) || g.getTeamB().equalsIgnoreCase(betTeam)){
				return g.getGameId();
			}
		}
		
		return ret;
	}
	
	
	public boolean insertBetting(String playerId, String betTeam){
		boolean ret = false;
		Timestamp currentTimestamp = new Timestamp(new java.util.Date().getTime());
		int gameId = validateBet(betTeam, currentTimestamp);
		
		if(gameId == -1){
			return ret;
		}
		
		try {			
			bettingInsert.setString(1,playerId);
			bettingInsert.setInt(2,gameId);
			bettingInsert.setString(3, betTeam);
			bettingInsert.setTimestamp(4, currentTimestamp);
			ret = bettingInsert.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ret;
	}
	
	public int winTrigger(int gameId, String winResult){
		int ret = -1;
		
		try {
			gameGet.setInt(1,gameId);
			ResultSet rs = gameGet.executeQuery();
			Game g = null ;
			while(rs.next()){
				g = new Game(rs.getInt(1),rs.getString(2), rs.getString(3), rs.getString(4), rs.getTimestamp(5),rs.getTimestamp(6), rs.getString(7));
			}
			
			if (g == null) return ret;
				
			if(!(winResult.equalsIgnoreCase("DRAW") ||
					winResult.equalsIgnoreCase(g.getTeamA())|| 
					winResult.equalsIgnoreCase(g.getTeamB()))){
				return -1;
			}
						
			bettingGet.setInt(1,gameId);
			ResultSet rsBet = bettingGet.executeQuery();
			ArrayList<Betting> winList = new ArrayList<Betting>();
			ArrayList<Betting> loseList = new ArrayList<Betting>();
			while(rsBet.next()){
				Betting b = new Betting(rs.getInt(1),rs.getString(2), rs.getInt(3), rs.getString(4), rs.getFloat(5),rs.getTimestamp(6));
				if(winResult.equalsIgnoreCase("DRAW") || b.getBetTeam().equalsIgnoreCase(winResult)){
					winList.add(b);
				}else{
					loseList.add(b);
				}
			}
			
					
			float totalScore = TOTALPLAYERS * SCOREPERPLAYER;
			float winScore = totalScore/winList.size();
			
			
			scoreUpdate.setFloat(1, winScore);
			Iterator <Betting> iter = winList.iterator();
			while(iter.hasNext()){
				Betting b = iter.next();				
				scoreUpdate.setInt(2,b.getBettingId());
				scoreUpdate.executeUpdate();				
			}				
			
			
			//Update Games table			
			winUpdate.setString(1, winResult);
			winUpdate.setInt(2, gameId);
			winUpdate.executeUpdate();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ret;
	}
	
	public String displayScoreBoard(int gameId){
		String ret = "No ScoreBoard";
		
		try {
			gameGet.setInt(1,gameId);
			ResultSet rs = gameGet.executeQuery();
			Game g = null ;
			while(rs.next()){
				g = new Game(rs.getInt(1),rs.getString(2), rs.getString(3), rs.getString(4), rs.getTimestamp(5),rs.getTimestamp(6), rs.getString(7));
			}
			
			if (g == null) return ret;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}	
	
	
	public void close(){
		try {
			if(playerInsert != null && !playerInsert.isClosed()){
				playerInsert.close();
			}
		} catch (SQLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		try {
			if(bettingInsert != null && !bettingInsert.isClosed())
				bettingInsert.close();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			if(gameGet != null && !gameGet.isClosed()){
				gameGet.close();
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			if(todayGameGet != null && !todayGameGet.isClosed()){
				todayGameGet.close();
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			if (conn != null && !conn.isClosed()){
				conn.close();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
    public static void main(String[] args){
    	TryDBAccess dbA = new TryDBAccess();
    	
    //	dbA.insertPlayer("ranj", "ranj");
    	
    	/*
    	ArrayList<Game> gList = dbA.forceGetTodaysGame(Timestamp.valueOf("2014-03-20 00:38:00"));
    	Iterator<Game> iter = gList.iterator();
    	while(iter.hasNext()){
    		Game g = iter.next();
    		System.out.println("Game On : " + g.gameId + " " + g.teamA + "  " + g.teamB);
    	}
    */
    	
    	dbA.insertBetting("ranj", "China");
    	
    	dbA.close();    
    }
    
     //   ## DERBY EXCEPTION REPORTING CLASSES  ## 
    /***     Exception reporting methods
    **      with special handling of SQLExceptions
    ***/
      static void errorPrint(Throwable e) {
         if (e instanceof SQLException) 
            SQLExceptionPrint((SQLException)e);
         else {
            System.out.println("A non SQL error occured.");
            e.printStackTrace();
         }   
      }  // END errorPrint 

    //  Iterates through a stack of SQLExceptions 
      static void SQLExceptionPrint(SQLException sqle) {
         while (sqle != null) {
            System.out.println("\n---SQLException Caught---\n");
            System.out.println("SQLState:   " + (sqle).getSQLState());
            System.out.println("Severity: " + (sqle).getErrorCode());
            System.out.println("Message:  " + (sqle).getMessage()); 
            sqle.printStackTrace();  
            sqle = sqle.getNextException();
         }
   }  //  END SQLExceptionPrint   	
}
