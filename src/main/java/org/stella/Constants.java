package org.stella;

/**
 * This class contains all unchanging configuration values
 * 
 * @author wrongu
 *
 */
public class Constants {
	// App Metadata
	public static final String APP_NAME = "Stella";
	public static final String APP_VERSION = "0.1.0";
	
	// Configuration files
	public static final String SYSTEM_SETTINGS = "stella.default.config";
	public static final String USER_SETTINGS = "stella.user.config";
	
	// Configuration keys
	// Note that each of these must be present in resources/stella.default.config
	public static final String CONF_SERVER_PATH = "SERVER_PATH";
	public static final String CONF_SERVER_PORT = "SERVER_PORT";
	public static final String CONF_MONGO_PATH = "MONGODB_PATH";
	public static final String CONF_MONGO_PORT = "MONGODB_PORT";
	public static final String CONF_USE_GUI = "USE_GUI";
	public static final String CONF_ROOT_JSON = "ROOT_JSON_ASSETS";
	public static final String CONF_ROOT_GAME = "ROOT_GAME_ASSETS";
	
	// Package Names
	public static final String REST_PACKAGE = "org.stella.api";
	
	// Commands
	public static final String COMMAND_QUIT = "quit";
	public static final String COMMAND_GUI = "gui";
	public static final String COMMAND_CONSOLE = "console";
	public static final String COMMAND_RESTART = "restart";
	
	// Messages
	public static final String MSG_WELCOME = "Welcome to Stella! type /help for available commands.";
}
