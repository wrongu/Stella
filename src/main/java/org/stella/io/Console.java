package org.stella.io;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class Console implements IInputOutput {

	private BufferedReader input;
	private ArrayList<ActionListener> listeners;
	private String last_input;
	private Thread input_thread;
	
	public Console(){
		input = new BufferedReader(new InputStreamReader(System.in));
		listeners = new ArrayList<ActionListener>();
		last_input = "";

		input_thread = new InputListenerThread();
		input_thread.start();
	}
	
	@Override
	public void addListener(ActionListener al) {
		listeners.add(al);
	}

	@Override
	public void printLn(String message) {
		System.out.println(message);
	}

	@Override
	public void printErrLn(String error_message) {
		System.err.println(error_message);
	}
	
	@Override
	public String getLastInput(){
		return last_input;
	}

	@Override
	public void closeStreams() {
		try {
			// note: this causes an IOException inside the blocking thread
			// the next time readLine() is called, which makes it quit
			// out of the while loop (see InputListenerThread below)
			input_thread.interrupt();
		} catch (Exception e) {}
	}
	
	@Override
	public String directory_prompt(String message){
		System.out.println(message);
		try {
			input_thread.wait();
		} catch (InterruptedException e) {
			return null;
		}
		if("".equals(last_input)) return null;
		else return last_input;
	}
	
	private void dispatchInput(String line){
		last_input = line;
		for(ActionListener al : this.listeners){
			al.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, line));
		}
		// in case anything was waiting on this input
		input_thread.notifyAll();
	}
	
	private class InputListenerThread extends Thread{
		public void run(){
			while(!Thread.interrupted()){
				try {
					// block this thread while waiting for input
					String line = input.readLine();
					Console.this.dispatchInput(line);
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
			}
		}
	}
}
