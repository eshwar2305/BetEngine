package com.engine.dataobject;

public class ScoreObject implements Comparable{
	private String playerName;
	private float score;
	private int gamesWon;
	private int rank;
	
	
	public ScoreObject(String playerName, float score, int gamesWon){
		this.playerName = playerName;
		this.score = score;
		this.gamesWon = gamesWon;		
	}
	
	public ScoreObject(String playerName, float score, int gamesWon, int rank){
		this(playerName, score, gamesWon);
		this.rank = rank;
	}
	
	@Override
	public int compareTo(Object arg0) {
		if(arg0 instanceof ScoreObject){
			ScoreObject other = (ScoreObject)arg0;
			if(other.score < this.score)
				return -1;
			else if(other.score > this.score)
				return 1;
			else
				return 0;
		}
		return -1;
	}

	@Override
	public String toString() {
		return playerName + "\t:" + score
				+ "\t:" + gamesWon + "\n";
	}
			
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(score);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScoreObject other = (ScoreObject) obj;
		if (Float.floatToIntBits(score) != Float.floatToIntBits(other.score))
			return false;
		return true;
	}

	public String getPlayerName() {
		return playerName;
	}
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
	public float getScore() {
		return score;
	}
	public void setScore(float score) {
		this.score = score;
	}
	public int getGamesWon() {
		return gamesWon;
	}
	public void setGamesWon(int gamesWon) {
		this.gamesWon = gamesWon;
	}
	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
	}

	

	
	
}
