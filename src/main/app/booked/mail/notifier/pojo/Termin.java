package booked.mail.notifier.pojo;

import java.util.Comparator;

import booked.main.notifier.parse.Parser;


public class Termin implements Comparator<Termin> {

	private String time; // 13:30
	private String state; // OPEN / RESERVE
	private int timeNum;
	private boolean ready;
	private boolean doing;
	private String link;
	
	public Termin() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Termin(String time, String state) {
		super();
		this.time = time;
		this.state = state;
		this.timeNum = Parser.timeToMin(time);
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@Override
	public String toString() {
		return "Time - " + time + ", state - " + state + (ready?" (clicked 'READY')":"");
	}
	
	public int compare(Termin t1, Termin t2) {
		return t1.timeNum - t2.timeNum;
	}

	public int getTimeNum() {
		return timeNum;
	}

	public boolean isReady() {
		return ready;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}

	public boolean isDoing() {
		return doing;
	}

	public void setDoing(boolean doing) {
		this.doing = doing;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}


}
