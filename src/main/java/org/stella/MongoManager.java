package org.stella;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

import static org.stella.Constants.*;

public class MongoManager {
	
	private static MongoClient client;
	private static DB db;
	private static DBCollection cDocuments, cUndo, cRedo;
	
	public static void connect(String address, int port){
		try {
			client = new MongoClient(address, port);
			db = client.getDB(DB_NAME);
			cDocuments = db.getCollection(COLL_DOCUMENTS);
			cUndo = db.getCollection(COLL_UNDO);
			cRedo = db.getCollection(COLL_REDO);
			Main.log("Connected to Mongo DataBase at "+address+":"+port);
		} catch (UnknownHostException e) {
			Main.log("Could not connect to Mongo database at "+address+":"+port, true);
			Main.log(e.getMessage(), true);
		}
	}
	
	public static void close(){
		client.close();
	}
	
}
