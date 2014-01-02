package org.stella.io;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.stella.Constants;
import org.stella.Main;

public class GUI extends JFrame {

	private static final long serialVersionUID = 1L;
	
	// gui stuff
	private LogPanel in_out;
	private ButtonPanel buttons;
	
	public GUI(){
		super();
		
		setSize(800, 600);
		setTitle(Constants.APP_NAME);
		
		initContent();
		
		// allow closing to be dealt with by the WindowHandler
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowHandler());
		
		setVisible(true);
	}
	
	public IInputOutput getInputOutputComponent(){
		return in_out;
	}
	
	/**
	 * Initialize GUI elements
	 */
	private void initContent(){
		this.setLayout(new BorderLayout());
		
		Container cp = getContentPane();
		
		in_out = new LogPanel(600, 600);
		cp.add(in_out, BorderLayout.WEST);
		
		buttons = new ButtonPanel(200,600);
		cp.add(buttons, BorderLayout.EAST);
	}
	
	private class WindowHandler implements WindowListener{

		@Override
		public void windowClosing(WindowEvent e) {
			Object[] options = {"Quit", "Hide"};
			int prompt = JOptionPane.showOptionDialog(
					GUI.this, //parent
					"Quit or just hide GUI?", //message
					"Quit?", //title
					JOptionPane.YES_NO_OPTION, //type of pane
					JOptionPane.QUESTION_MESSAGE, //type of prompt
					null, //no icon
					options, // button labels
					options[0]); // default to quit
			if(prompt == JOptionPane.YES_OPTION){
				Main.quit();
			} else{
				Main.setConsoleIO();
			}
			GUI.this.dispose();
		}

		@Override
		public void windowClosed(WindowEvent e) {}

		@Override
		public void windowActivated(WindowEvent e) {}

		@Override
		public void windowDeactivated(WindowEvent e) {}

		@Override
		public void windowDeiconified(WindowEvent e) {}

		@Override
		public void windowIconified(WindowEvent e) {}

		@Override
		public void windowOpened(WindowEvent e) {}
	}
}
