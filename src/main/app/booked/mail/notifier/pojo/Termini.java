package booked.mail.notifier.pojo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

public class Termini {

	private HashMap<String, Termin> open;
	private HashMap<String, Termin> reserve;

	public Termini() {
		super();
		this.open = new HashMap<String, Termin>();
		this.reserve = new HashMap<String, Termin>();
	}
	
	public Termini(HashMap<String, Termin> open, HashMap<String, Termin> reserve) {
		super();
		this.open = open;
		this.reserve = reserve;
	}

	public HashMap<String, Termin> getOpen() {
		return open;
	}

	public void setOpen(HashMap<String, Termin> open) {
		this.open = open;
	}

	public HashMap<String, Termin> getReserve() {
		return reserve;
	}

	public void setReserve(HashMap<String, Termin> reserve) {
		this.reserve = reserve;
	}

	@Override
	public String toString() {
		HashMap<String, Termin> termini = new HashMap<String, Termin>();
		for (Termin t : open.values()) {
			termini.put(t.getTime(), t);
		}
		for (Termin t : reserve.values()) {
			termini.put(t.getTime(), t);
		}

		ArrayList<Termin> terminiArray = new ArrayList<Termin>(termini.values());
		Collections.sort(terminiArray, new Termin());
		String ret = "";
		for (Termin t : terminiArray) {
			ret = ret + t + "\n";
		}
		return "Lesson slots: " + "(" + (new SimpleDateFormat("HH:mm:ss").format(new Date())) + ")" 
				+ "\n--------------------------------\n" + (!ret.equals("")? ret.substring(0, ret.length()-1):"") + 
				"\n--------------------------------\n"
				+ "Number of open: " + open.size() + "\nNumber of reserved: " + reserve.size() + "\n";
	}

	public String toEmail() {
		HashMap<String, Termin> termini = new HashMap<String, Termin>();
		for (Termin t : open.values()) {
			termini.put(t.getTime(), t);
		}
		for (Termin t : reserve.values()) {
			termini.put(t.getTime(), t);
		}

		ArrayList<Termin> terminiArray = new ArrayList<Termin>(termini.values());
		Collections.sort(terminiArray, new Termin());
		String ret = "";
		for (Termin t : terminiArray) {
			ret = ret + t + "\n <br> ";
		}
		return "Lesson slots: \n <br> --------------------------------\n <br> " + ret
				+ "\n <br> --------------------------------\n <br> " + "Number of open: " + open.size()
				+ "\n <br> Number of reserved: " + reserve.size();
	}

}
