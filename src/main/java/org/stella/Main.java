package org.stella;

import org.glassfish.grizzly.http.server.HttpServer;
import org.stella.io.Console;
import org.stella.io.IInputOutput;
import org.stella.io.GUI;
import org.stella.io.LogPanel;

import java.io.File;
import java.io.IOException;

import javax.swing.SwingUtilities;

import org.apache.commons.configuration.*;
import static org.stella.Constants.*;

/**
 * Main entry point for Stella. Handles initialization of all components.
 * 
 * Main provides wrapped access to some functions, like log() which wraps
 * IInputOutput. Otherwise, go through the respective managers:
 * 	ConfigurationManager for changing user configuration values
 *  ServerManager for interacting with the http-server
 *  TODO create Projects (where each project has its own configuration)
 * 
 * @author wrongu
 */
public class Main {
	
	// allow public access to changing config values
	public static Configuration config;
	
	private static HttpServer server;
	private static IInputOutput io_handler;
	private static GUI gui;
	
	/**
	 * Main entry point for the Stella server.
	 */
    public static void main(String[] args) throws IOException {    	
    	// step 1 - load configuration for default/custom server location and port
    	config = ConfigurationManager.loadConfiguration();
    	if(config != null){
            
            // step 2 - create user interface
            if(config.getBoolean(CONF_USE_GUI)){
            	setGuiIO();
            } else{
            	setConsoleIO();
            }
            
            // set<Gui or Console>IO happens in a separate thread (because IO thread is separate and always listening)
            // so, we have to execute the rest of the init code on that thread:
            SwingUtilities.invokeLater(new Runnable(){
            	public void run(){
                    // step 1, part 2 - if this is the first time Stella is launched, the root folders
                    //	for json files and assets won't be set. set them now.
            		Main.getFileProperty(CONF_ROOT_JSON, "Choose root directory for saving JSON documents");
            		Main.getFileProperty(CONF_ROOT_GAME, "Choose root directory for loading game assets");
                    ConfigurationManager.saveUserConfig();
                    
                    // INITIAL MESSAGE TO USER
                    Main.printWelcomeMessage();
                    
                    // Steps 3 and 4: start server and database connections
                    server = ServerManager.startServer(
                    		config.getString(CONF_SERVER_PATH),
                    		config.getInt(CONF_SERVER_PORT));
					
                    MongoManager.connect(config.getString(CONF_MONGO_PATH), config.getInt(CONF_MONGO_PORT));
                    // TODO test database connections
            	}
            });
    	}
    }
    
    /**
     * Loads existing, or prompts for new, local directory parameter
     * @param prop name of the property
     * @param prompt text to display if requesting new value
     */
    private static void getFileProperty(String prop, String prompt){
        File the_file = new File(config.getString(prop));
        while(!(the_file.exists() && the_file.isDirectory())){
        	String path = io_handler.directory_prompt(prompt);
        	if(path == null) break;
        	the_file = new File(path);
        }
        // if it was set properly, save it
        if(the_file.exists() && the_file.isDirectory()){
        	ConfigurationManager.setProperty(prop, the_file.getAbsolutePath());
        }
    }
    
    private static void printWelcomeMessage(){
        Main.log("Welcome to the Stella JSON document editing server");
        Main.log("A README is available at www.github.com/wrongu/Stella\n");
        Main.log("Current configuration:");
        Main.log(" JSON documents' root directory: "+config.getString(CONF_ROOT_JSON));
        Main.log(" Assets root directory: "+config.getString(CONF_ROOT_GAME));
        Main.log("\n");
        Main.log("type /help to see server commands");
    }
    
    /**
     * complete registration of handler with listeners
     * @param handler
     */
    private static void initIO(IInputOutput handler){
    	handler.addListener(new CommandListener(handler));
    }
    
    /**
     * Shut down Stella and all its resources
     */
    public static void quit(){
    	io_handler.printLn("Stella shutting down...");
    	server.stop();
    	MongoManager.close();
    	io_handler.closeStreams();
    	if(gui != null) gui.dispose();
    	// save config details
    	ConfigurationManager.saveUserConfig();
    }
    
    /**
     * Switch I/O handling to the console and destroy the GUI
     */
    public static void setConsoleIO(){
    	if(!(io_handler instanceof Console)){
    		IInputOutput previous = io_handler;
        	// just in case this is invoked by a separate thread..
    		// (all Swing calls must be done from the same thread. invokeLater schedules
    		// this code to be run on that thread as soon as possible)
        	SwingUtilities.invokeLater(new Runnable(){
        		public void run(){
		    		if(gui != null && gui.isDisplayable()){
		    			gui.dispose();
		    			gui = null;
		    		}
		    		io_handler = new Console();
		    		initIO(io_handler);
        		}
        	});
    		// close previous handler
    		if(previous != null) previous.closeStreams();
    	}
    }
    
    public static void setGuiIO(){
    	if(!(io_handler instanceof LogPanel)){
    		IInputOutput previous = io_handler;
        	// just in case this function is invoked by a separate thread..
    		// (all Swing calls must be done from the same thread. invokeLater schedules
    		// this code to be run on that thread as soon as possible)
        	SwingUtilities.invokeLater(new Runnable(){
        		public void run(){
            		gui = new GUI();
            		io_handler = gui.getInputOutputComponent();
            		initIO(io_handler);
        		}
        	});
    		// close previous handler
    		if(previous != null) previous.closeStreams();
    	}
    }

	public static void restartServer() {
        server = ServerManager.startServer(config.getString(CONF_SERVER_PATH), config.getInt(CONF_SERVER_PORT));
	}
	
	public static void log(String message){
		log(message, false);
	}
	
	public static void log(String message, boolean error){
		if(error) io_handler.printErrLn(message);
		else io_handler.printLn(message);
	}
}

