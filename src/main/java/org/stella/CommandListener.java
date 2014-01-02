package org.stella;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.stella.io.IInputOutput;

public class CommandListener implements ActionListener {
	
	private IInputOutput source;
	
	public CommandListener(IInputOutput source){
		this.source = source;
	}
	
	@Override
	public void actionPerformed(ActionEvent ae) {
		String line = source.getLastInput();
		if(line != null){
			if(line.startsWith("/")){
				String cmd_name = line.substring(1, Math.max(line.indexOf(" "), line.length()));
				if(cmd_name.equals(Constants.COMMAND_QUIT)){
					Main.quit();
				} else if(cmd_name.equals(Constants.COMMAND_GUI)){
					Main.setGuiIO();
				} else if(cmd_name.equals(Constants.COMMAND_CONSOLE)){
					Main.setConsoleIO();
				} else if(cmd_name.equals(Constants.COMMAND_RESTART)){
					Main.restartServer();
				} else{
					printHelp();
				}
			}
		}
	}
	
	public void printHelp(){
		this.source.printLn("Commands:");
		this.source.printLn("\t/"+Constants.COMMAND_QUIT + "\tquit "+Constants.APP_NAME);
		this.source.printLn("\t/"+Constants.COMMAND_GUI + "\tswitch to gui mode");
		this.source.printLn("\t/"+Constants.COMMAND_CONSOLE + "\tswitch to console mode");
		this.source.printLn("\t/"+Constants.COMMAND_RESTART + "\trestart the server");
	}

}
