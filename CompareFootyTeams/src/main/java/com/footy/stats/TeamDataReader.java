/**
 * @author SridevBalakrishnan
 *
 */

package com.footy.stats;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.footy.stats.domain.TeamHistory;


public class TeamDataReader {

	static final Logger log = Logger.getLogger(TeamDataReader.class.getName());

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
			search.sendKeys(teamName + " stats WhoScored.com");

			// Maximize browser
			driver.manage().window().maximize();

			// And submit		
			search.submit();

			driverWait.until(ExpectedConditions.visibilityOf(driver.findElement
					(By.partialLinkText(("WhoScored.com")))));
			driver.findElement(By.partialLinkText("WhoScored.com")).click();	

			driverWait.until(ExpectedConditions.visibilityOf(driver.findElement
					(By.xpath("//a[contains(text(),'Fixtures')]//parent::li"))));
			driver.findElement(By.xpath("//a[contains(text(),'Fixtures')]//parent::li")).click();

			/*driverWait.until(ExpectedConditions.visibilityOf(driver.findElement
					(By.xpath("//div[contains(@class,'col12-xs-12 col12-s-12 col12-m-12 col12-lg-12 ws-panel')]"))));*/
			try {
				driverWait =  new WebDriverWait(driver, 10);

				ExpectedCondition<Boolean> expectation = new ExpectedCondition<Boolean>() {
					public Boolean apply(WebDriver driverjs) {
						JavascriptExecutor js = (JavascriptExecutor) driverjs;
						return js.executeScript(websiteLoadCheckString).equals(websiteLoadCheckBoolean);
					}
				};
				driverWait.until(expectation);
			}
			catch (TimeoutException e) {
				log.error("TimeoutException while reading data from stats table: {}", e);
			}
			catch (Exception e) {
				log.error("Exception while waiting to load webpage: {}", e);
			}
			
			try {
				/*List<WebElement> statsLst = driver.findElements(By.xpath("//div[contains(@class,"
						+ "'col12-lg-12 col12-m-12 col12-s-12 col12-xs-12 item divtable-row alt')]"));*/
				
				/*for(WebElement e: statsLst) {
				
					TeamHistory th = new TeamHistory();
					String homeGoal = null;
					String awayGoal = null;
					
					try {
						th.setMatchResult(e.findElement(By.xpath(".//div[contains(@class,"
								+ "'col12-lg-1 col12-m-1 col12-s-1 col12-xs-1 divtable-data form-fixtures')]")).getText());
					}
					catch (NoSuchElementException ex) {
						log.error("Exception while reading elements: {}", ex);
						continue;
					}
					th.setLeague(e.findElement(By.xpath(".//div[contains(@class,"
							+ "'col12-lg-1 col12-m-1 col12-s-1 col12-xs-1 tournament divtable-data')]")).getText());			    	
					List<WebElement> rivals = e.findElements(By.xpath(".//div[contains(@class,"
							+ "'horizontal-match-display team')]"));
					for(WebElement r: rivals) {
						if (!r.getText().equalsIgnoreCase(teamName))
							th.setOpponent(r.getText());
					}
					th.setDate(e.findElement(By.xpath(".//div[contains(@class,"
							+ "'col12-lg-1 col12-m-1 col12-s-0 col12-xs-0 date fourth-col-date divtable-data')]")).getText());
				
					String score = e.findElement(By.xpath(".//div[contains(@class,"
							+ "'col12-lg-1 col12-m-1 col12-s-0 col12-xs-0 divtable-data result')]")).getText();	
					score = score.replaceAll("\\*", "").replaceAll("\\s", "");
					String[] scores = score.split(":");
					for (int i=0 ; i < scores.length; i++) {
						if (i==0)
							homeGoal = scores[i];
						else if (i==1)
							awayGoal = scores[i];
					}
					if ((th.getMatchResult().equalsIgnoreCase("D")) || 
							((Integer.parseInt(homeGoal) > Integer.parseInt(awayGoal)) && th.getMatchResult().equalsIgnoreCase("W")) ||
							((Integer.parseInt(homeGoal) < Integer.parseInt(awayGoal)) && th.getMatchResult().equalsIgnoreCase("L"))) {
						th.setHomeGoals(Integer.parseInt(homeGoal));
						th.setAwayGoals(Integer.parseInt(awayGoal));
					}
					else if (((Integer.parseInt(homeGoal) > Integer.parseInt(awayGoal)) && th.getMatchResult().equalsIgnoreCase("L"))
							|| ((Integer.parseInt(homeGoal) < Integer.parseInt(awayGoal)) && th.getMatchResult().equalsIgnoreCase("W"))) {
						th.setHomeGoals(Integer.parseInt(awayGoal));
						th.setAwayGoals(Integer.parseInt(homeGoal));
					}
				
					if ((Integer.parseInt(homeGoal) > 0)  && (Integer.parseInt(awayGoal) > 0))
						th.setBothSidesScored(true);
				
					if ((Integer.parseInt(homeGoal) + Integer.parseInt(awayGoal)) > 2.5 )
						th.setMoreTotGoals(true);
					
					teamHistoryLst.add(th);
				}*/
				
				List<WebElement> statsLst = driver.findElements(By.xpath("//div[contains(@class,"
						+ "'col12-lg-12 col12-m-12 col12-s-12 col12-xs-12 item divtable-row ')]"));
				
				for(WebElement e: statsLst) {

					TeamHistory th = new TeamHistory();
					String homeGoal = null;
					String awayGoal = null;
					
					try {
						th.setMatchResult(e.findElement(By.xpath(".//div[contains(@class,"
							+ "'col12-lg-1 col12-m-1 col12-s-1 col12-xs-1 divtable-data form-fixtures')]")).getText());
					}
					catch (NoSuchElementException ex) {
						log.error("Exception while reading elements: {}", ex);
						continue;
					}
					th.setLeague(e.findElement(By.xpath(".//div[contains(@class,"
							+ "'col12-lg-1 col12-m-1 col12-s-1 col12-xs-1 tournament divtable-data')]")).getText());
					List<WebElement> rivals = e.findElements(By.xpath(".//div[contains(@class,"
							+ "'horizontal-match-display team')]"));
					for(WebElement r: rivals) {
						if (!r.getText().equalsIgnoreCase(teamName))
							th.setOpponent(r.getText());
					}
					th.setDate(e.findElement(By.xpath(".//div[contains(@class,"
							+ "'col12-lg-1 col12-m-1 col12-s-0 col12-xs-0 date fourth-col-date divtable-data')]")).getText());

					String score = e.findElement(By.xpath(".//div[contains(@class,"
							+ "'col12-lg-1 col12-m-1 col12-s-0 col12-xs-0 divtable-data result')]")).getText();
					score = score.replaceAll("\\*", "").replaceAll("\\s", "");
					String[] scores = score.split(":");
					for (int i=0 ; i < scores.length; i++) {
						if (i==0)
							homeGoal = scores[i];
						else if (i==1)
							awayGoal = scores[i];
					}
					if ((th.getMatchResult().equalsIgnoreCase("D")) || 
							((Integer.parseInt(homeGoal) > Integer.parseInt(awayGoal)) && th.getMatchResult().equalsIgnoreCase("W")) ||
							((Integer.parseInt(homeGoal) < Integer.parseInt(awayGoal)) && th.getMatchResult().equalsIgnoreCase("L"))) {
						th.setHomeGoals(Integer.parseInt(homeGoal));
						th.setAwayGoals(Integer.parseInt(awayGoal));
					}
					else if (((Integer.parseInt(homeGoal) > Integer.parseInt(awayGoal)) && th.getMatchResult().equalsIgnoreCase("L"))
							|| ((Integer.parseInt(homeGoal) < Integer.parseInt(awayGoal)) && th.getMatchResult().equalsIgnoreCase("W"))) {
						th.setHomeGoals(Integer.parseInt(awayGoal));
						th.setAwayGoals(Integer.parseInt(homeGoal));
					}
					
					if ((Integer.parseInt(homeGoal) > 0)  && (Integer.parseInt(awayGoal) > 0))
						th.setBothSidesScored(true);

					if ((Integer.parseInt(homeGoal) + Integer.parseInt(awayGoal)) > 2.5 )
						th.setMoreTotGoals(true);
					
					teamHistoryLst.add(th);
				}

			}
			catch (NoSuchElementException e) {
				log.error("Exception while reading elements: {}", e);
			}

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
			//driver.close();
		}
	}

}