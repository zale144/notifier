package booked.main.notifier.parse;

import java.awt.AWTException;
import java.awt.Toolkit;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import booked.mail.notifier.mail.MailSender;
import booked.mail.notifier.pojo.Termin;
import booked.mail.notifier.pojo.Termini;

public class Parser {

	static String htmlContent;
	static Document document;
	static Elements open;
	static Elements reserve;
	static String baseUrl = "https://tutor.eikaiwa.dmm.com/accounts/login";
//	 static String baseUrl = "file:D:/TEST.html";
	static HtmlUnitDriver driver;
	static Tray tray;
	static Termini termini = new Termini();
	static HashMap<String, Termin> recordedOpen = new HashMap<String, Termin>();
	static boolean run = true;
	static boolean ready = false;
	static String link;
	private static int lessonStartsIn;
	
	static {
		driver = new HtmlUnitDriver(false);
		login();
		parse();
		recordOpen();
		
		try {
			tray = new Tray();
			tray.loadTray();
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// MAIN
	public static void main(String[] args) throws AWTException {
		try {
			checker();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		driver.close();
	}

	// RECORD THE OPEN SLOTS
	public static void recordOpen() {
		recordedOpen.clear();
		for (Termin t : termini.getOpen().values()) {
			recordedOpen.put(t.getTime(), t);
		}
	}

	// SHOW CURRENT
	public static void showCurrent() {
		parse();
		popup(termini.toString(), "Schedule Today", true);
	}

	// CHECK FOR NEW RESERVED SLOTS
	public static void checker() throws InterruptedException, AWTException {
		int x = 100;
		int timeNow = 0;
		do {
			timeNow = currentTime();
			if (timeNow >= 0 && timeNow < 720) {
				tray.swapIconPause();
				System.out.println("Paused");
				Thread.sleep((720 - timeNow)* 60 * 1000); // SLEEP FROM 16:30 TO 12:00 NEXT DAY
				tray.swapIconResume();
				System.out.println("Resuming");
			} 
			
			if (timeNow > 990) {
				tray.swapIconPause();
				System.out.println("Paused");
				Thread.sleep((2160 - timeNow)* 60 * 1000); // SLEEP FROM 16:30 TO 12:00 NEXT DAY
				tray.swapIconResume();
				System.out.println("Resuming");
			}
			parse();
			if (termini.getOpen() != null && termini.getReserve() != null) {
				boolean flag = false;
				for (Termin cur : termini.getReserve().values()) {
					if (recordedOpen.containsKey(cur.getTime())) {
						flag = true;
						break;
					}
				}
				if (flag) {
					try {
						MailSender.generateAndSendEmail(termini.toEmail());
					} catch (AddressException e) {
						e.printStackTrace();
					} catch (MessagingException e) {
					}
					popup(termini.toString(), "A lesson has been booked!", true); // NO POPUP - BECAUSE IT PAUSES THE CURRENT THREAD
				}
				System.out.println(termini);
				
				if(ready) {
					clickReady(link); 
					try {
						MailSender.generateAndSendEmail("'READY' button has been clicked! \n"
								+ " Lesson starts in " + lessonStartsIn + " minutes --- \n" + termini.toEmail());
					} catch (AddressException e) {
						e.printStackTrace();
					} catch (MessagingException e) {
					}
					popup("Lesson starts in " + lessonStartsIn + " minutes",
							"The 'READY' button has been clicked!", true);
					ready = false;
					link = null;
					lessonStartsIn = 0;
				}
			}
			recordOpen();
			Thread.sleep(x * 1000);
		} while (run);
	}

	// PARSING THE TODAY LESSON PAGE
	public static void parse() {
		driver.findElement(By.linkText("Schedule Today")).click();
//		 driver.get(baseUrl);
		htmlContent = driver.getPageSource();
		document = Jsoup.parse(htmlContent);
		open = document.getElementsMatchingOwnText("OPEN");
		reserve = document.getElementsMatchingOwnText("RESERVE");
		manageOpenHash(open);
		manageReserveHash(reserve);
	}

	// MANAGE THE OPEN SLOTS
	public static void manageOpenHash(Elements open) {
		termini.getOpen().clear();
		HashMap<String, Termin> ret = toHashMap(open);
		for (Termin t : ret.values()) {
			termini.getOpen().put(t.getTime(), t);
		}
	}

	// MANAGE THE RESERVED SLOTS
	public static void manageReserveHash(Elements reserve) {
		HashMap<String, Termin> ret = toHashMap(reserve);
		ArrayList<String> toRemove = new ArrayList<String>();
		for (Termin r : termini.getReserve().values()) {
			if (!ret.containsKey(r.getTime())) {
				toRemove.add(r.getTime());
			}
			
		}
		for (int i = 0; i < toRemove.size(); i++) {
			termini.getReserve().remove(toRemove.get(i));
		}

		for (Termin t : ret.values()) {
			String time = t.getTime();
			if (!termini.getReserve().containsKey(t.getTime())) {
				termini.getReserve().put(time, t);
			}
		}
		for (Termin t : termini.getReserve().values()) {
			int timeBeforeFirstReserved = checkReady(t.getTime());

			if (!t.isReady() && timeBeforeFirstReserved > 0) {
				clickReady(t.getLink()); 
				ready = true;
				link = t.getLink();
				// POSTPONE AFTER EMAIL
				t.setReady(true);
				lessonStartsIn = timeBeforeFirstReserved;
			}
			if (t.isReady() && !t.isDoing() && timeBeforeFirstReserved <= 6) {
				try {
					MailSender.generateAndSendEmail("Lesson starts in " + timeBeforeFirstReserved + " minutes!");
				} catch (AddressException e) {
					e.printStackTrace();
				} catch (MessagingException e) {
				}
				popup("Lesson starts in " + timeBeforeFirstReserved + " minutes", "Get ready!", true);
				t.setDoing(true);
			}

		}
	}

	// ELEMENTS TO HASHMAP CONVERSION
	public static HashMap<String, Termin> toHashMap(Elements e) {
		String time = null;
		String state = null;
		String link = null;
		HashMap<String, Termin> ret = new HashMap<String, Termin>();
		
		if (e.size() > 0) {
			for (int i = 0; i < e.size(); i++) {    
				time = e.get(i).parent().parent().getElementsByAttributeValue(
						"style", "background-color:#DDBAFF; text-align: center")
						.text();
				if(!time.equals("")) {
					time = time.substring(11);
				}

				if (time.equals("")) {
					time = e.get(i).parent().parent().parent()
							.getElementsByAttributeValue(
									"style", "background-color:#DDBAFF; text-align: center")
							.text();
				}
				
				if(!time.equals("") && time.length() > 11) {
					time = time.substring(11);
				}
				state = e.get(i).ownText();
				link = "https://tutor.eikaiwa.dmm.com" /* "file:D:/" */ + e.get(i).parent().attributes().get("href");
				Termin t = new Termin(time, state);
				if (!link.equals("")) { 
					t.setLink(link);
				}
				ret.put(time, t);
			}
		}
		return ret;
	}

	// CURRENT TIME
	public static int currentTime() {
		return timeToMin(new SimpleDateFormat("HH:mm").format(new Date()));
	}

	// CHECK FOR READY RESERVED SLOTS
	public static int checkReady(String curRes) {
		int timeNow = currentTime();
		int nextReserveTime = timeToMin(curRes);
		int diff = -1;
		if (nextReserveTime > timeNow && (nextReserveTime - timeNow <= 30)) {
			diff = nextReserveTime - timeNow;
		}
		return diff;
	}

	// POPUP MESSAGE AND SOUND ALERT
	public static void popup(String msg, String title, boolean sound) {
		if (sound) {
			Toolkit.getDefaultToolkit().beep();
		}
		JOptionPane optionPane = new JOptionPane();
		optionPane.setMessage(msg);
		optionPane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
		JDialog dialog = optionPane.createDialog(null, title);
		dialog.setVisible(true);
		dialog.setAlwaysOnTop(true);

	}

	// CLICK ON THE READY BUTTON
	private static void clickReady(String link) {
		HtmlUnitDriver driver = new HtmlUnitDriver(false);
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
		driver.get(link);
		WebElement ready = null;
		String element = null;
		try {
			ready = driver.findElement(By.id("lessonstart"));
			element = ready.toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			driver.findElement(By.cssSelector("*[class^='color blue button']")).click();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		driver.close();
		System.out.println(element);
	}

	// CONVERT TIME TO MINUTES
	public static int timeToMin(String s) {
		String[] sTime = s.split(":");
		int[] time = { Integer.parseInt(sTime[0]), // {14, 30}
				Integer.parseInt(sTime[1]) };
		int ret = time[0] * 60 + time[1];
		return ret;
	}

	// LOGIN ON TUTOR PAGE
	public static void login() {
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
		driver.get(baseUrl);
		driver.findElement(By.id("loginid")).clear();
		driver.findElement(By.id("loginid")).sendKeys("381607515715");
		driver.findElement(By.id("password")).clear();
		driver.findElement(By.id("password")).sendKeys("*******");
		driver.findElement(By.className("btn")).click();
	}
}
