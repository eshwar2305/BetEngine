package com.engine.dataaccess;

import com.engine.dataobject.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


public class TryDBAccess
{
	Connection conn = null;
	private PreparedStatement playerInsert = null;
	private PreparedStatement bettingInsert = null;
	private PreparedStatement scoreUpdate = null;
	private PreparedStatement gameWinUpdate = null;
	private PreparedStatement gameStatusUpdate = null;
	private PreparedStatement gamesForBettingGetOpenWindow = null;	
	private PreparedStatement gamesForBettingGetRunningWindow = null;	
	private PreparedStatement gameGet = null;
	private PreparedStatement bettingGet = null;	
	private PreparedStatement scoreGet = null;
	private PreparedStatement currentBettingGet = null;
	private PreparedStatement playerAllGet = null;
	private PreparedStatement gameStatusUpdateToStarted = null;
	private PreparedStatement gameStatusUpdateToDone = null;
	static private ArrayList<Game>gamesForBetting = null; 

			
	public TryDBAccess(){
		getConnection();
		refreshGameStatus();
		if(gamesForBetting == null){
			gamesForBetting = new ArrayList<Game>();
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
		         playerAllGet = conn.prepareStatement("select * from player");
		         bettingInsert = conn.prepareStatement("insert into betting(playerId, gameId, betTeam, score, betDate) values (?,?,?,0,?)");
		         gamesForBettingGetOpenWindow = conn.prepareStatement("select * from game where startdate in (select startdate from game where status = ? order by startdate asc fetch first 1 row only )");
		         gamesForBettingGetRunningWindow = conn.prepareStatement(new StringBuffer ("select * from game where status ='").
		        		 append(StaticValues.GAME_STATUS_BETTING).append("'").toString());
		         gameGet = conn.prepareStatement("select * from game where gameId = ?");
		         scoreUpdate = conn.prepareStatement("update betting set score =?, haswon = ? where bettingId = ?");
		         gameWinUpdate = conn.prepareStatement("update game set winTeam = ? , status = ? where gameId = ?");
		         gameStatusUpdate = conn.prepareStatement("update game set status = ? where gameId = ?");
		         bettingGet = conn.prepareStatement("select * from betting where gameId = ?");
		         currentBettingGet = conn.prepareStatement("select name, betteam, gameid from betting as b, player as p where gameid=? and p.playerid = b.playerid order by name");
		         scoreGet = conn.prepareStatement("select name, sum(score), sum(haswon) from betting as b, player as p where b.playerid = p.playerid group by name order by name");
		         StringBuffer tmp = new StringBuffer("update game set status ='").append(StaticValues.GAME_STATUS_STARTED).
		        		 	append("' where cast(Startdate as date)= CURRENT_DATE");
		         gameStatusUpdateToStarted = conn.prepareStatement(tmp.toString());//started
		         tmp = new StringBuffer(). append("update game set status= '").append(StaticValues.GAME_STATUS_DONE).
		        		 append("' where cast(startdate as date) < CURRENT_DATE"); //Done
		         gameStatusUpdateToDone = conn.prepareStatement(tmp.toString());
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
	
	public void refreshGameStatus(){
		try {
			gameStatusUpdateToStarted.executeUpdate();
			gameStatusUpdateToDone.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
		
	public ArrayList<Game> getGamesForBetting(){	
		Game g = new Game();
		Timestamp currentTimestamp = new Timestamp(new java.util.Date().getTime());
		
		Iterator<Game> iter = gamesForBetting.iterator();
		
		//DitchCache or not
		boolean ditchCache = false;
		ditchCache = !iter.hasNext(); //1. Cache Empty
		while(iter.hasNext()){ //2. any of the elements has gone old,
			g = (Game) iter.next();	
			ditchCache = currentTimestamp.after(g.getStartDate());//corrupt ditch it	- Ranjana - hits null					
		}
		if(!ditchCache) return gamesForBetting; //we are good 
		gamesForBetting.clear();
		
		
		//Get Status=betting
		forceGetGamesForBetting();
		
		if(gamesForBetting.isEmpty()){
			try {
				this.gameStatusUpdateToStarted.execute();
				openGamesForBetting();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		return gamesForBetting;		
	}
		
	private void forceGetGamesForBetting(){
		gamesForBetting.clear();
		try {			
			ResultSet rs = gamesForBettingGetRunningWindow.executeQuery();
			while (rs.next()){				
				Game gg = new Game(rs.getInt(1),rs.getString(2), rs.getString(3), rs.getString(4), rs.getTimestamp(5),rs.getString(6), rs.getString(7),rs.getString(8));
				gamesForBetting.add(gg);				
			}			
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return;		
	}
	
	
	private ArrayList<Game> openGamesForBetting(){
		gamesForBetting.clear();
		try {			
			gamesForBettingGetOpenWindow.setString(1, StaticValues.GAME_STATUS_NOT_STARTED);
			ResultSet rs = gamesForBettingGetOpenWindow.executeQuery();
			while (rs.next()){				
				Game gg = new Game(rs.getInt(1),rs.getString(2), rs.getString(3), rs.getString(4), rs.getTimestamp(5),rs.getString(6), rs.getString(7),rs.getString(8));
				gamesForBetting.add(gg);
				gameStatusUpdate.setString(1,StaticValues.GAME_STATUS_BETTING );
				gameStatusUpdate.setInt(2, gg.getGameId());
				gameStatusUpdate.executeUpdate();
			}			
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return gamesForBetting;		
	}
	
	
	private int validateBet(String betTeam){
		int ret = -1;		
		//lookup on the cache
		getGamesForBetting(); //refresh gamesForBetting
		Iterator <Game> iter = gamesForBetting.iterator();
		while(iter.hasNext()){
			Game g = iter.next();
			if(g.getTeamA().equalsIgnoreCase(betTeam) || g.getTeamB().equalsIgnoreCase(betTeam)){
				return g.getGameId();
			}
		}
		return ret;
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
	
	public HashMap<String,String> getPlayers(){
		HashMap <String, String> hm = new HashMap<String,String>();
		try {
			ResultSet rs = playerAllGet.executeQuery();
			while(rs.next()){
				hm.put(rs.getString(1), rs.getString(2));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return hm;
	}
	
	
	public boolean insertBetting(String playerId, String betTeam){
		boolean ret = false;
		Timestamp currentTimestamp = new Timestamp(new java.util.Date().getTime());
		int gameId = validateBet(betTeam);
		
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
				g = new Game(rs.getInt(1),rs.getString(2), rs.getString(3), rs.getString(4), rs.getTimestamp(5),rs.getString(6), rs.getString(7),rs.getString(8));
			}
			rs.close();
			
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
				Betting b = new Betting(rsBet.getInt(1),rsBet.getString(2), rsBet.getInt(3), rsBet.getString(4), rsBet.getFloat(5),rsBet.getTimestamp(6));
				if(winResult.equalsIgnoreCase("DRAW") || b.getBetTeam().equalsIgnoreCase(winResult)){
					winList.add(b);
				}else{
					loseList.add(b);
				}
			}
			rsBet.close();
			
			
			float totalScore = StaticValues.TOTALPLAYERS * StaticValues.SCOREPERPLAYER;
			float winScore;
			
			if(winList.isEmpty())winScore = 0; //all losers				
			else winScore = totalScore/winList.size();
			
			
			scoreUpdate.setFloat(1, winScore);
			scoreUpdate.setInt(2,1);
			Iterator <Betting> iter = winList.iterator();
			while(iter.hasNext()){
				Betting b = iter.next();				
				scoreUpdate.setInt(3,b.getBettingId());
				scoreUpdate.executeUpdate();				
			}				
			
			
			//Update Games table			
			gameWinUpdate.setString(1, winResult);
			gameWinUpdate.setString(2, StaticValues.GAME_STATUS_DONE);
			gameWinUpdate.setInt(3, gameId);
			gameWinUpdate.executeUpdate();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ret;
	}
	
	public String displayCurrentBet(){
		String ret = "No Bet Available";		
	
		this.getGamesForBetting();//refreshes the todaysGame if needed
		Iterator <Game> iter = gamesForBetting.iterator();
		StringBuffer buf = new StringBuffer("---------\n");
		while(iter.hasNext()){
			Game g = iter.next();
			buf.append("Game ").append(g.getGameId()).append(" : ").append(displayCurrentBet(g.getGameId())).append("-------\n");
		}						
		ret = buf.toString();
		return ret;
	}
	
	public String displayCurrentBet(int gameId){
		String ret = "No Bet Available for game " + gameId; 
		String tmp;
		try {
			this.getGamesForBetting();//refreshes the todaysGame if needed			
			Iterator <Game> iter = gamesForBetting.iterator();
			StringBuffer buf = new StringBuffer();
			currentBettingGet.setInt(1,gameId);
			ResultSet rs = currentBettingGet.executeQuery();
			Game g = new Game();
			while(iter.hasNext()){
				g = iter.next();
				if(g.getGameId() != gameId); //do nothing
				else break;
			}
			currentBettingGet.setInt(1,g.getGameId());				
			StringBuffer bufA= new StringBuffer(g.getTeamA()).append(" ( ");
			StringBuffer bufB = new StringBuffer(g.getTeamB()).append(" ( ");
			while(rs.next()){
				tmp = rs.getString(2);//betTeam
				if(tmp.equalsIgnoreCase(g.getTeamA()))
					bufA.append(rs.getString(1)).append(", ");
				else
					bufB.append(rs.getString(1)).append(", ");						
			}
			buf.append(bufA).append(")\n").append(bufB).append(")\n");		
			ret  = buf.toString();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;	
	}
	
	public String displayScoreBoard(){
		String ret = "No ScoreBoard Available!";
		
		try {
			ResultSet rs = scoreGet.executeQuery();
			List<ScoreObject> soList = new ArrayList<ScoreObject>();
			StringBuffer buf = new StringBuffer();
			while(rs.next()){
				//buf.append(rs.getString(1)).append("\t: ").append(rs.getFloat(2)).append("\t: ").append(rs.getInt(3)).append("\n");
				ScoreObject so = new ScoreObject(rs.getString(1),rs.getFloat(2),rs.getInt(3));
				soList.add(so);
			}
			Collections.sort(soList);
			Iterator<ScoreObject> it  = soList.iterator();
			int i = 1;
			while(it.hasNext()){
				buf.append(i++).append("\t:").append(it.next());
			}
			rs.close();		
			ret = buf.toString();
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
			if(gamesForBettingGetRunningWindow != null && !gamesForBettingGetRunningWindow.isClosed()){
				gamesForBettingGetRunningWindow.close();
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			if(gamesForBettingGetOpenWindow != null && !gamesForBettingGetOpenWindow.isClosed()){
				gamesForBettingGetOpenWindow.close();
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
	
    public static void main(String[] args){
    	TryDBAccess dbA = new TryDBAccess();
    	
    //	dbA.insertPlayer("ranj", "ranj");
    	
    
    	/*ArrayList<Game> gList = dbA.forceGetTodaysGame(new Timestamp((new java.util.Date().getTime())));
    	Iterator<Game> iter = gList.iterator();
    	while(iter.hasNext()){
    		Game g = iter.next();
    		System.out.println("Game On : " + g.getGameId() + " " + g.getTeamA() + "  " + g.getTeamB());
    	}
    
    */	
    	
    //	dbA.insertBetting("eshu123", "India");
    //	dbA.winTrigger(5, "India");
    
    	ArrayList<Game> gList = dbA.getGamesForBetting();
    	Iterator <Game> iter = gList.iterator();
    	while(iter.hasNext()){
    		Game g = (Game) iter.next();
    		System.out.println("Game " + g.getGameId() + " : " + g.getTeamA() );
    	}
    
    	
    	System.out.println(dbA.displayCurrentBet());
    	System.out.println(dbA.displayScoreBoard());
    	dbA.close();    
    }

	public int getLastMatchnumberWithResult() {
		// TODO Auto-generated method stub
		// Ranjana - Need the last match which was completed and result updated in database
		return 16;
	}
    
  	
}
