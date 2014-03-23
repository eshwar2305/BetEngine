import java.sql.Timestamp;

public class Game{
	
		private int gameId;
		private String teamA;
		private String teamB;
		private String winTeam;
		private Timestamp startDate;
		private Timestamp endDate;
		private String description;
		private String place;
		
		public Game(){};
		public Game(int gameId, String teamA, String teamB, String winTeam, Timestamp startDate, Timestamp endDate, String description){
			this.gameId = gameId;
			this.teamA= teamA;
			this.teamB = teamB;
			this.winTeam = winTeam;
			this.startDate = startDate;
			this.endDate = endDate;
			this.description = description;
		}
		public int getGameId() {
			return gameId;
		}
		public void setGameId(int gameId) {
			this.gameId = gameId;
		}
		public String getTeamA() {
			return teamA;
		}
		public void setTeamA(String teamA) {
			this.teamA = teamA;
		}
		public String getTeamB() {
			return teamB;
		}
		public void setTeamB(String teamB) {
			this.teamB = teamB;
		}
		public String getWinTeam() {
			return winTeam;
		}
		public void setWinTeam(String winTeam) {
			this.winTeam = winTeam;
		}
		public Timestamp getStartDate() {
			return startDate;
		}
		public void setStartDate(Timestamp startDate) {
			this.startDate = startDate;
		}
		public Timestamp getEndDate() {
			return endDate;
		}
		public void setEndDate(Timestamp endDate) {
			this.endDate = endDate;
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		public String getPlace() {
			return place;
		}
		public void setPlace(String place) {
			this.place = place;
		}
	}