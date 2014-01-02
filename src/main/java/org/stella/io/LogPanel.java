package org.stella.io;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class LogPanel extends JPanel implements IInputOutput {

	private static final long serialVersionUID = 1L;
	
	private JTextArea output;
	private JTextField input;
	// listener for <enter> presses on keyboard
	private final ActionListener input_listener; 
	// keep these separate so we can guarantee that input_listener gets first access to the event
	private ArrayList<ActionListener> external_listeners; 
	private boolean echo_enabled = true;
	private String last_input;
	
	public LogPanel(int w, int h){
		setPreferredSize(new Dimension(w-1, h));
		
		output = new JTextArea();
		output.setEditable(false);
		output.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		input = new JTextField();
		JScrollPane scroll_wrapper = new JScrollPane(output);
		scroll_wrapper.setPreferredSize(new Dimension(w, h-50));
		last_input = "";
		
		this.setLayout(new BorderLayout());
		
		this.add(scroll_wrapper, BorderLayout.NORTH);
		this.add(input, BorderLayout.SOUTH);

		external_listeners = new ArrayList<ActionListener>();
		input_listener = new InputListener();
		input.addActionListener(input_listener);
	}
	
	public void print(String str){
		output.append(str);
	}
	
	@Override
	public void printLn(String str){
		print(str + '\n');
	}

	@Override
	public void addListener(ActionListener al) {
		external_listeners.add(al);
	}

	@Override
	public void printErrLn(String error_message) {
		printLn("ERROR: " + error_message);
	}

	@Override
	public String getLastInput() {
		return last_input;
	}
	
	@Override
	public void closeStreams(){
		// nothing to do. how nice!
	}
	
	@Override
	public String directory_prompt(String message){
		Object[] options = {"Ok", "Cancel"};
		int confirmed = JOptionPane.showOptionDialog(this, message, "Choose File", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if(confirmed == JOptionPane.OK_OPTION){
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.showDialog(this, "Choose");
			File f = chooser.getSelectedFile();
			if(f != null) return f.getAbsolutePath();
		}
		return null;
	}
	
	/**
	 * Set whether or not all input should be automatically reflected
	 * in the output (default is true)
	 * 
	 * @param enable whether or not to echo input directly to output
	 */
	public void setEchoEnabled(boolean enable){
		echo_enabled = enable;
	}
	
	/**
	 * An input listener that echoes the input to the output
	 * and stores the latest input in LogPanel.last_input
	 * 
	 * @author wrongu
	 */
	private class InputListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			// record last_input
			LogPanel.this.last_input = input.getText();
			// echo to output (if enabled)
			if(LogPanel.this.echo_enabled)
				LogPanel.this.printLn(last_input);
			// clear input field
			input.setText("");
			// dispatch event to all other listeners
			for(ActionListener al : external_listeners){
				al.actionPerformed(e);
			}
		}
	}
}
