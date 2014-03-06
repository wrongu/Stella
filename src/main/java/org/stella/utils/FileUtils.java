package org.stella.utils;

import java.io.File;

public class FileUtils {
	
	public static String getExtension(String path){
		int dot_index = path.lastIndexOf('.');
		if(dot_index != -1){
			return path.substring(dot_index+1);
		} else{
			return "";
		}
	}
	
	public static String join(String ... parts){
		if(parts.length > 0){
			StringBuilder builder = new StringBuilder();
			builder.append(parts[0]);
			for(int i=1; i<parts.length; i++){
				builder.append(File.pathSeparator);
				builder.append(parts[i]);
			}
			return builder.toString();
		} else{
			return "";
		}
	}
	
	public static boolean fileExists(String path){
		File f = new File(path);
		return f.exists() && f.isFile();
	}
	
	public static boolean dirExists(String path){
		File f = new File(path);
		return f.exists() && f.isDirectory();
	}
}
