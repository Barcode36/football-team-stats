package com.footy.stats.domain;

/**
 * @author Sridev Balakrishnan
 * @Purpose POJO to store historical data
 */
public class TeamHistory {

	private String matchResult;
	private boolean bothSidesScored;
	private boolean moreTotGoals;
	private String league;
	private String opponent;
	private String date;
	private int homeGoals;
	private int awayGoals;
	
	public TeamHistory() {
		
	}
	
	public TeamHistory (String league, String date, String opponent, String matchResult, 
						int homeGoals, int awayGoals, boolean bothSidesScored, boolean moreTotGoals) {
		this.matchResult = matchResult;
		this.date = date;
		this.bothSidesScored = bothSidesScored;
		this.moreTotGoals = moreTotGoals;
		this.league = league;
		this.opponent = opponent;
		this.homeGoals = homeGoals;
		this.awayGoals = awayGoals;
	}
	
	/**
	 * @return the matchResult
	 */
	public String getMatchResult() {
		return matchResult;
	}
	/**
	 * @param matchResult the matchResult to set
	 */
	public void setMatchResult(String matchResult) {
		this.matchResult = matchResult;
	}
	/**
	 * @return the bothSidesScored
	 */
	public boolean isBothSidesScored() {
		return bothSidesScored;
	}
	/**
	 * @param bothSidesScored the bothSidesScored to set
	 */
	public void setBothSidesScored(boolean bothSidesScored) {
		this.bothSidesScored = bothSidesScored;
	}
	/**
	 * @return the moreTotGoals
	 */
	public boolean isMoreTotGoals() {
		return moreTotGoals;
	}
	/**
	 * @param moreTotGoals the moreTotGoals to set
	 */
	public void setMoreTotGoals(boolean moreTotGoals) {
		this.moreTotGoals = moreTotGoals;
	}
	/**
	 * @return the league
	 */
	public String getLeague() {
		return league;
	}
	/**
	 * @param league the league to set
	 */
	public void setLeague(String league) {
		this.league = league;
	}
	/**
	 * @return the opponent
	 */
	public String getOpponent() {
		return opponent;
	}
	/**
	 * @param opponent the opponent to set
	 */
	public void setOpponent(String opponent) {
		this.opponent = opponent;
	}
	/**
	 * @return the date
	 */
	public String getDate() {
		return date;
	}
	/**
	 * @param date the date to set
	 */
	public void setDate(String date) {
		this.date = date;
	}
	/**
	 * @return the homeGoals
	 */
	public int getHomeGoals() {
		return homeGoals;
	}
	/**
	 * @param homeGoals the homeGoals to set
	 */
	public void setHomeGoals(int homeGoals) {
		this.homeGoals = homeGoals;
	}
	/**
	 * @return the awayGoals
	 */
	public int getAwayGoals() {
		return awayGoals;
	}
	/**
	 * @param awayGoals the awayGoals to set
	 */
	public void setAwayGoals(int awayGoals) {
		this.awayGoals = awayGoals;
	}

	@Override
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		sb.append("[ Result: " + getMatchResult() + ", ");
		sb.append("League: " + getLeague() + ", ");
		sb.append("Opponent: " + getOpponent() + ", ");
		sb.append("Date: " + getDate() + ", ");
		sb.append("Home Goals: " + getHomeGoals() + ", ");
		sb.append("Away Goals: " + getAwayGoals() + ", ");
		sb.append("Both sides scored: " + isBothSidesScored() + ", ");
		sb.append("Total Goals over 2.5: " + isMoreTotGoals() + "]");
		return sb.toString();
	}

}
