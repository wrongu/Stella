package org.stella.io;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.stella.Main;

public class ButtonPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	private JButton restart_btn, quit_btn, hide_btn;
	
	public ButtonPanel(int w, int h){
		super();
		
		setPreferredSize(new Dimension(w, h));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		restart_btn = new JButton("RESTART SERVER");
		restart_btn.addActionListener(new RestartListener());
		restart_btn.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		quit_btn = new JButton("QUIT");
		quit_btn.addActionListener(new QuitListener());
		quit_btn.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		hide_btn = new JButton("HIDE GUI");
		hide_btn.addActionListener(new HideListener());
		hide_btn.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		
		add(restart_btn);
		add(hide_btn);
		add(quit_btn);
	}
	
	private class RestartListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			Main.restartServer();
		}
	}
	private class QuitListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			Main.quit();
		}
	}
	private class HideListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			Main.setConsoleIO();
		}
	}
}
