package booked.main.notifier.parse;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class Tray {
	
	Image image; 
	Image imagePaused;
	
	public Tray() throws IOException {
		InputStream stream = Tray.class.getResourceAsStream("bibo.png");
		this.image = ImageIO.read(stream);;
		InputStream streamPaused = Tray.class.getResourceAsStream("bibo_pause.png");
		this.imagePaused = ImageIO.read(streamPaused);
	}
		
	TrayIcon trayIcon;
	public void loadTray() throws AWTException, IOException{
	    //checking for support
	    if(!SystemTray.isSupported()){
	        System.out.println("System tray is not supported !!! ");
	        return ;
	    }

	    //popupmenu
	    PopupMenu trayPopupMenu = new PopupMenu();

	    //1t menuitem for popupmenu
	    MenuItem action = new MenuItem("Check 'Schedule Today'");
	    action.addActionListener(new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent e) {
	        	try {
					swapIconResume();
				} catch (AWTException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	        	Parser.showCurrent();
	        	try {
					swapIconPause();
				} catch (AWTException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	        }
	    });     
	    trayPopupMenu.add(action);

	    //2nd menu item of popupmenu
	    MenuItem close = new MenuItem("Close");
	    close.addActionListener(new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent e) {
	            System.exit(0);             
	        }
	    });
	    trayPopupMenu.add(close);
	    trayIcon = new TrayIcon(image, "BiboLessonNotifier", trayPopupMenu);
	    //setting tray icon
	   
	    trayIcon.setImageAutoSize(true);
	    SystemTray.getSystemTray().add(trayIcon);

	}
	
	public void swapIconPause() throws AWTException {
		
		trayIcon.setImage(imagePaused);
		trayIcon.setToolTip("BiboLessonNotifier(paused)");
	}
	
	public void swapIconResume() throws AWTException {
		trayIcon.setImage(image);
		trayIcon.setToolTip("BiboLessonNotifier");
		
	}

}
