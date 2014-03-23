package com.engine.dataobject;
import java.sql.Timestamp;

public class Game{
	
		private int gameId;
		private String teamA;
		private String teamB;
		private String winTeam;
		private Timestamp startDate;
		private String status;
		private String description;
		private String place;
		
		
		public String getStatus() {
			return status;
		}
		public void setStatus(String status) {
			this.status = status;
		}
		public String getPlace() {
			return place;
		}
		public void setPlace(String place) {
			this.place = place;
		}
		public Game(){};
		public Game(int gameId, String teamA, String teamB, String winTeam, Timestamp startDate, String status, String description, String place){
			this.gameId = gameId;
			this.teamA= teamA;
			this.teamB = teamB;
			this.winTeam = winTeam;
			this.startDate = startDate;
			this.place = place;
			this.description = description;
			this.status = status;
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
		
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
	}