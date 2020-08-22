/**
 * @author SridevBalakrishnan
 * @purpose Fetches the stats of requested football team; returns opponent, match results, goals scored, etc.
 */

package com.footy.stats;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import java.io.IOException;
import java.io.InputStream;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.footy.stats.domain.TeamHistory;

public class TeamDataReader {

	static final Logger log = LogManager.getLogger(TeamDataReader.class.getName());

	static String chromeDriver = "";
	static String chromeDriverPath = "";
	static String homeWebsiteURL = "";
	static String footyWebsiteURL = "";
	static String websiteLoadCheckString = "";
	static String websiteLoadCheckBoolean = "";
	static String outputFileName = "";
	static String outputWorksheetName = "";
	static String teamName = "";
	static int index;
	static String statsWebsiteLink = "WhoScored.com";
	static String xPathFixtures = "//a[contains(text(),'Fixtures')]//parent::li";
	static String xPathPanel = "//div[contains(@class,'col12-xs-12 col12-s-12 col12-m-12 col12-lg-12 ws-panel')]";
	static String xPathFormFixtures = ".//div[contains(@class,'col12-lg-1 col12-m-1 col12-s-1 col12-xs-1 divtable-data "
									  + "form-fixtures')]";
	static String xPathLeague = ".//div[contains(@class,'col12-lg-1 col12-m-1 col12-s-1 col12-xs-1 tournament divtable-data')]";
	static String xPathOpponent = ".//div[contains(@class,'horizontal-match-display team')]";
	static String homeTeam = "";
	static String xPathMatchDate = ".//div[contains(@class,'col12-lg-1 col12-m-1 col12-s-0 col12-xs-0 date fourth-col-date "
									+ "divtable-data')]";
	static String xPathResults = ".//div[contains(@class,'col12-lg-1 col12-m-1 col12-s-0 col12-xs-0 divtable-data result')]";
	static String xPathTable = "//div[contains(@class,'col12-lg-12 col12-m-12 col12-s-12 col12-xs-12 item divtable-row ')]";
			
	static List<TeamHistory> teamHistoryLst =  new ArrayList<>();

	private TeamDataReader() {
		// Utility class
	}

	public static void main(String... args) throws IOException {

		Scanner in = new Scanner(System.in);  
		System.out.print("Enter football team name: ");
		teamName = in.nextLine();
		System.out.print("Enter number of matches to pull stats for: ");    
		index = Integer.parseInt(in.nextLine());
		in.close();

		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yy");
		int win = 0;
		int draw = 0;
		int lost = 0;
		StringBuilder form = new StringBuilder();
		int bothSidesScored = 0;
		int overTotGoals = 0;
		int goalsFor = 0;
		int goalsAgainst = 0;

		webPageSurf();
		teamHistoryLst.sort(Comparator.comparing(
				TeamHistory::getDate, (s1, s2) -> {
					try {
						return dateFormat.parse(s2).compareTo(dateFormat.parse(s1));
					} catch (ParseException e) {
						log.error("Exception while sorting by date: {}", e);
					}
					return 0;
				}));

		if (index > teamHistoryLst.size())
			index = teamHistoryLst.size();

		for(int i=0; i<index; i++) {

			form.append(teamHistoryLst.get(i).getMatchResult());

			goalsFor = goalsFor + teamHistoryLst.get(i).getHomeGoals();
			goalsAgainst =  goalsAgainst + teamHistoryLst.get(i).getAwayGoals();

			if (teamHistoryLst.get(i).getMatchResult().equalsIgnoreCase("W"))
				win++;
			else if (teamHistoryLst.get(i).getMatchResult().equalsIgnoreCase("D"))
				draw++;
			else if (teamHistoryLst.get(i).getMatchResult().equalsIgnoreCase("L"))
				lost++;

			if (teamHistoryLst.get(i).isBothSidesScored())
				bothSidesScored++;

			if (teamHistoryLst.get(i).isMoreTotGoals())
				overTotGoals++;

			log.info(teamHistoryLst.get(i).toString());
		}

		log.info("[ Team form ] "+form);
		log.info("[ Match Result ] "+win+"W "+draw+"D "+lost+"L");
		log.info("[ Total Matches - Both sides scored ] "+bothSidesScored+ "/"+index);
		log.info("[ Total Matches - Over 2.5 goals ] "+overTotGoals+ "/"+index);
		log.info("[ Goals For ] "+goalsFor+ " [ Goals Against ] "+goalsAgainst);
		if (!teamHistoryLst.isEmpty())
			DisplayDataTable.getData(homeTeam, teamHistoryLst, index);
		System.exit(0);
	}
	/**
	 * @throws IOException 
	 * 
	 */
	public static void webPageSurf() throws IOException {

		Properties prop = new Properties();
		InputStream inStream = null;
		try {
			inStream = TeamDataReader.class.getClassLoader().
					getResourceAsStream("application.properties");
			if (inStream == null) {
				log.error("Unable to find application.properties");
			}

			prop.load(inStream);

			chromeDriver = prop.getProperty("chrome.driver");
			chromeDriverPath = prop.getProperty("chrome.driver.location");
			homeWebsiteURL = prop.getProperty("home.website.url");
			websiteLoadCheckString = prop.getProperty("whoscored.website.load.check.string");
			websiteLoadCheckBoolean = prop.getProperty("whoscored.website.load.check.boolean");
		}
		catch (Exception e) {
			log.error("Exception in reading config file: {}", e);
		}
		finally {
			if (inStream != null)
				inStream.close();
		}
		System.setProperty(chromeDriver, chromeDriverPath);

		// Initialize browser
		WebDriver driver = new ChromeDriver();

		// Open Google 
		driver.get(homeWebsiteURL);		

		try {
			WebDriverWait driverWait =  new WebDriverWait(driver, 10);

			driverWait.until(ExpectedConditions.visibilityOfElementLocated(By.name("q")));

			// Find search bar
			WebElement search = driver.findElement(By.name("q"));

			// Enter football team's name in search bar
			search.sendKeys(teamName + " stats "+statsWebsiteLink);

			// Maximize browser
			driver.manage().window().maximize();

			// And submit		
			search.submit();

			driverWait.until(ExpectedConditions.visibilityOf(driver.findElement
					(By.partialLinkText((statsWebsiteLink)))));
			driver.findElement(By.partialLinkText(statsWebsiteLink)).click();	

			driverWait.until(ExpectedConditions.visibilityOf(driver.findElement
					(By.xpath(xPathFixtures))));
			driver.findElement(By.xpath(xPathFixtures)).click();

			driverWait.until(ExpectedConditions.visibilityOf(driver.findElement
					(By.xpath(xPathPanel))));

			populateData(driver);
		}
		catch (TimeoutException e) {
			log.error("Timedout reading elements: {}", e);
		}
		catch (Exception e) {
			log.error("Exception while reading data from load webpage: {}", e);
			log.info("Make sure team name is correct and there are no typos");
		}
		finally {
			log.info("Driver closed");			
			driver.close();
		}
	}

	private static void populateData(WebDriver driver) {
		try {
			List<WebElement> statsLst = driver.findElements(By.xpath(xPathTable));
			retrieveFixtures(driver, statsLst);
		}
		catch (NoSuchElementException e) {
			log.error("Exception while reading elements: {}", e);
		}
	}
	
	private static void retrieveFixtures(WebDriver driver, List<WebElement> statsLst) {	
		
		// Obtain home team name
		Select select = new Select(driver.findElement(By.id("teams")));
		WebElement option = select.getFirstSelectedOption();
		homeTeam = option.getText();
		
		for(WebElement e: statsLst) {

			TeamHistory th = new TeamHistory();
			int iHomeGoal = 0;
			int iAwayGoal = 0;

			try {
				th.setMatchResult(e.findElement(By.xpath(xPathFormFixtures)).getText());
			}
			catch (NoSuchElementException ex) {
				log.error("Exception while reading elements: {}", ex);
				continue;
			}

			// Populate league in bean
			th.setLeague(retrieveLeague(e));

			// Populate opponent name in bean
			th.setOpponent(retrieveOpponent(e, homeTeam));

			// Populate match date in bean
			th.setDate(retrieveMatchDate(e));

			HashMap<Integer, Integer> goals = retrieveGoals(e, th);
			iHomeGoal = goals.get(0);
			iAwayGoal = goals.get(1);
			th.setHomeGoals(iHomeGoal);
			th.setAwayGoals(iAwayGoal);

			if ((iHomeGoal > 0)  && (iAwayGoal > 0))
				th.setBothSidesScored(true);

			if ((iHomeGoal + iAwayGoal) > 2.5 )
				th.setMoreTotGoals(true);

			teamHistoryLst.add(th);
		}
	}
	
	private static String retrieveLeague(WebElement e) {		

		return (e.findElement(By.xpath(xPathLeague)).getText());
	}

	private static String retrieveOpponent(WebElement e, String homeTeam) {		

		String rival = "";
		List<WebElement> teams = e.findElements(By.xpath(xPathOpponent));
		for(WebElement r: teams) {
			if (!org.apache.commons.lang3.StringUtils.containsIgnoreCase(r.getText(), homeTeam)) {
				rival = r.getText().replaceAll("\\d","");
			}
		}
		return rival;
	}

	private static String retrieveMatchDate(WebElement e) {		

		return (e.findElement(By.xpath(xPathMatchDate)).getText());	
	}

	private static HashMap<Integer, Integer> retrieveGoals(WebElement e, TeamHistory th) {

		HashMap<Integer, Integer> goals = new HashMap<>();
		String homeGoal = null;
		String awayGoal = null;
		int iHomeGoal = 0;
		int iAwayGoal = 0;
		int iTempGoal = 0;

		String score = e.findElement(By.xpath(xPathResults)).getText();
		score = score.replaceAll("\\*", "").replaceAll("\\s", "");
		String[] scores = score.split(":");
		for (int i=0 ; i < scores.length; i++) {
			if (i==0)
				homeGoal = scores[i];
			else
				awayGoal = scores[i];
		}

		iHomeGoal = Integer.parseInt(homeGoal);
		iAwayGoal = Integer.parseInt(awayGoal);

		if (((iHomeGoal > iAwayGoal) && th.getMatchResult().equalsIgnoreCase("L"))
				|| ((iHomeGoal < iAwayGoal) && th.getMatchResult().equalsIgnoreCase("W"))) {
			iTempGoal = iHomeGoal;
			iHomeGoal = iAwayGoal;
			iAwayGoal = iTempGoal;
		}
		goals.put(0, iHomeGoal);
		goals.put(1, iAwayGoal);
		return goals;
	}

}