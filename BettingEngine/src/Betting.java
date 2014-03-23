import java.sql.Timestamp;


public class Betting {
	

	private int bettingId;
	private String playerId;
	private int gameId;
	private String betTeam;
	private float score;
	private Timestamp betDate;
	
	public int getBettingId() {
		return bettingId;
	}


	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public int getGameId() {
		return gameId;
	}

	public void setGameId(int gameId) {
		this.gameId = gameId;
	}

	public String getBetTeam() {
		return betTeam;
	}

	public void setBetTeam(String betTeam) {
		this.betTeam = betTeam;
	}

	public float getScore() {
		return score;
	}

	public void setScore(float score) {
		this.score = score;
	}

	public Timestamp getBetDate() {
		return betDate;
	}

	public void setBetDate(Timestamp betDate) {
		this.betDate = betDate;
	}


	
	public Betting(){
		
	}
	
	public Betting(int bettingId, String playerId, int gameId, String betTeam, float score, Timestamp betDate){
		this(playerId,gameId, betTeam, score, betDate);
		this.bettingId = bettingId;		
	}
	
	public Betting(String playerId, int gameId, String betTeam, float score, Timestamp betDate){
		this.gameId = gameId;
		this.betTeam = betTeam;
		this.score = score;
		this.betDate = betDate;		
	}
}
