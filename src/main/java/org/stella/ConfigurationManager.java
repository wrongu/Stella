package org.stella;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;

public class ConfigurationManager {
	
	private static PropertiesConfiguration user_config;
	
	/**
	 * Load (or create) a configuration file in the parent directory of the JAR file
	 * @return a Configuration object with either default or loaded settings
	 */
	public static Configuration loadConfiguration() {
		// using composite configuration so that any values not set by the user will default
		// 	to the system settings (see resources/stella.default.config)
		CompositeConfiguration config = new CompositeConfiguration();
		File user_config_file = getUserConfigFile();
		
		// create the file if it doesn't already exist
		try {
			user_config_file.getParentFile().mkdirs();
			user_config_file.createNewFile(); // does not overwrite if it already exists
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		// by adding user_config_file first, it will always be checked first
		// (thus overriding default values)
		try {
			user_config = new PropertiesConfiguration(user_config_file);
			config.addConfiguration(user_config);
		} catch (ConfigurationException e) {
			System.err.println("Stella cannot create local configuration file. Only defaults will be used.");
		}
		
		// add the system (default) properties
		File system_config_file = getSystemConfigFile();
		try {
			SystemConfiguration.setSystemProperties(system_config_file.toString());
			config.addConfiguration(new SystemConfiguration());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return config;
    }
	
	/**
	 * cross-platform method for locating user configuration information
	 */
	private static File getUserConfigFile(){
		// thanks to http://stackoverflow.com/questions/3784657/what-is-the-best-way-to-save-user-settings-in-java-application
		String path = System.getProperty("user.home") 
				+ File.separator + "." + Constants.APP_NAME.toLowerCase()
				+ File.separator + Constants.USER_SETTINGS;
		return new File(path);
	}
	
	/**
	 * Use a ClassLoader to find the default config file in the packaged JAR
	 * @return a File object pointing to the config file
	 */
	private static File getSystemConfigFile(){
		// search the classpath for system settings (resources/stella.default.config)
		URL config_file_URL = ConfigurationManager.class.getClassLoader().getResource(Constants.SYSTEM_SETTINGS);
		if(config_file_URL == null){
			System.err.println("could not find system config file '"+Constants.SYSTEM_SETTINGS+"'. aborting.");
			System.exit(1);
		}
		return new File(config_file_URL.toString());
	}

	/**
	 * Save all properties that have been modified by setProperty()
	 */
	public static void saveUserConfig() {
		if(user_config != null){
			try {
				user_config.save();
			} catch (ConfigurationException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void setProperty(String name, Object value){
		user_config.setProperty(name, value);
	}
}
