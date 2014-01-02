package org.stella.io;

import java.awt.event.ActionListener;

public interface IInputOutput {

	/**
	 * Register an ActionListener to be called when text is entered
	 * @param al the ActionListener to respond to <Enter> presses for input
	 */
	public void addListener(ActionListener al);
	
	/**
	 * Print a message to the output
	 * @param message
	 */
	public void printLn(String message);
	
	/**
	 * Print an error message to the output
	 * @param error_message
	 */
	public void printErrLn(String error_message);
	
	/**
	 * Re-get the most recent input text
	 * @return
	 */
	public String getLastInput();
	
	/**
	 * Close all open resources (basically, handle quitting)
	 */
	public void closeStreams();
	
	/**
	 * Prompt the user for input
	 */
	public String directory_prompt(String message);
	
}
