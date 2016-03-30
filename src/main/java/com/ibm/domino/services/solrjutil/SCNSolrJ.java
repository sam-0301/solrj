package com.ibm.domino.services.solrjutil;

import java.net.MalformedURLException;
import java.util.HashMap;

import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.NotesThread;
import lotus.domino.Session;

import org.apache.solr.client.solrj.impl.CloudSolrClient;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SCNSolrJ {

	public static Logger log = Logger.getLogger(SCNSolrJ.class.getSimpleName());
	
	private static HashMap<String, CloudSolrClient> clients = new HashMap<String, CloudSolrClient>();
	
	private static String updateUser = "";
	private static String updatePassword = "";
	
	private static String readUser = "";
	private static String readPassword = "";
	
	private static String zkUsername = "";
	private static String zkPassword = "";

	private static int zkClientTimeout = 60000;
	private static int zkConnectionTimeout = 10000;
	
	private static int httpConnectionTimeout = 60000;
	private static int httpSocketTimeout = 60000;
	
	private static boolean isTest = false; // in product env, it's always false
	
	static{
		init();
	}
	
	private static void init(){
		if(!isTest){
			NotesThread.sinitThread();
			Session sess = null;
			try {
				sess = NotesFactory.createSessionWithFullAccess();
				
				updateUser = getNotesINI(sess, SCNSolrJConstants.NOTES_INI_SOLRJ_SOLR_UPDATE_USER, updateUser);
				updatePassword = getNotesINI(sess, SCNSolrJConstants.NOTES_INI_SOLRJ_SOLR_UPDATE_PASSWORD, updatePassword);
				readUser = getNotesINI(sess, SCNSolrJConstants.NOTES_INI_SOLRJ_SOLR_READER_USER, readUser);
				readPassword = getNotesINI(sess, SCNSolrJConstants.NOTES_INI_SOLRJ_SOLR_READER_PASSWORD, readPassword);
				zkUsername = getNotesINI(sess, SCNSolrJConstants.NOTES_INI_SOLRJ_ZOOKEEPER_USER, zkUsername);
				zkPassword = getNotesINI(sess, SCNSolrJConstants.NOTES_INI_SOLRJ_ZOOKEEPER_PASSWORD, zkPassword);
				
				zkClientTimeout = str2Int(getNotesINI(sess, SCNSolrJConstants.NOTES_INI_SOLRJ_ZOOKEEPER_CLIENT_TIMEOUT_MS, "" + zkClientTimeout), zkClientTimeout);
				zkConnectionTimeout = str2Int(getNotesINI(sess, SCNSolrJConstants.NOTES_INI_SOLRJ_ZOOKEEPER_CONNECTION_TIMEOUT_MS, "" + zkConnectionTimeout), zkConnectionTimeout);
				httpConnectionTimeout = str2Int(getNotesINI(sess, SCNSolrJConstants.NOTES_INI_SOLRJ_HTTP_CONNECTION_TIMEOUT_MS, "" + httpConnectionTimeout), httpConnectionTimeout);
				httpSocketTimeout = str2Int(getNotesINI(sess, SCNSolrJConstants.NOTES_INI_SOLRJ_HTTP_SOCKET_TIMEOUT_MS, "" + httpSocketTimeout), httpSocketTimeout);
			
			}catch(NotesException e){
				log.log(Level.SEVERE, e.getLocalizedMessage(), e);

			}finally{
				if(sess != null){
					try {
						sess.recycle();
					} catch (NotesException e) {
						log.log(Level.WARNING, "GetNotesINI got exception: " + e.getLocalizedMessage(), e);
					}	
				}
				NotesThread.stermThread();
			}

		} else {
			updateUser = "solrupdate";
			updatePassword = "widget@1bm";
			readUser = "solrreader";
			readPassword = "widget@1bm";
			zkUsername = "zkadmin";
			zkPassword = "widget@1bm";
		}

		if(zkUsername != null && !zkUsername.trim().isEmpty()){
			System.setProperty(SCNSolrJConstants.ZKDigestUsername, zkUsername.trim());
			System.setProperty(SCNSolrJConstants.ZKDigestPassword, zkPassword);
			System.setProperty(SCNSolrJConstants.ZKCredentialsProvider, SCNSolrJConstants.ZKDigestCredentialsProvider);
		}
		
		log.info("SCNSolrJ class initialized.");
	}
	
	private SCNSolrJ() {}
	
	public static synchronized CloudSolrClient getClient(String zkHosts, boolean isPopulator){
		if(zkHosts == null || zkHosts.trim().isEmpty()){
			log.severe("Wrong parameters: zkHosts = " + zkHosts);
			return null;
		}

		zkHosts = zkHosts.trim();		
		log.finer("Getting SolrJ client for cloud: " + zkHosts);
		String key = zkHosts + ":" + isPopulator;
		
		CloudSolrClient client = clients.get(key);
		if(client != null){
			return client;
		}
		
		String user = readUser;
		String password = readPassword;
		if(isPopulator){
			user = updateUser;
			password = updatePassword;
		}

		try {
			SolrSearchLBHttpSolrClient sLBHttpSolrClient = new SolrSearchLBHttpSolrClient(user, password);
			log.finer("Created SolrJ client with username = " + user + "; password = " + password);
			sLBHttpSolrClient.setConnectionTimeout(httpConnectionTimeout);
			if(!isPopulator){
				sLBHttpSolrClient.setSoTimeout(httpSocketTimeout);
			}
			client = new CloudSolrClient(zkHosts, sLBHttpSolrClient);
			client.setZkClientTimeout(zkClientTimeout);
			client.setZkConnectTimeout(zkConnectionTimeout);
			clients.put(key, client);
			return client;
		} catch (MalformedURLException e) {
			log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			return null;
		}
	}
	
	
	public static String getNotesINI(Session sess, String varName, String defaultValue){
		if(sess == null){
			log.severe("Null sess = " + sess);
			return defaultValue;
		}
		
		try {
			sess = NotesFactory.createSessionWithFullAccess();
			String var = sess.getEnvironmentString(varName, true);
			if(null == var || var.trim().isEmpty()){
				return defaultValue;
			}
			return var;
		}catch(NotesException e){
			log.log(Level.SEVERE, "GetNotesINI failed: " + e.getLocalizedMessage() + 
					" : varName = " + varName+ " , use default value = " + defaultValue, e);
			return defaultValue;
		}
	}
	
	public static int str2Int(String s, int defaultValue){
		if(null == s || s.isEmpty()){
			log.severe("Empty s = " + s);
			return defaultValue;
		}
		
		int v = defaultValue;
		try {
			v = Integer.parseInt(s);
			return v;
		} catch (Exception e) {
			log.log(Level.SEVERE, "Str2Int failed: s = " + s, e);
		}
		return v;
	}
}
