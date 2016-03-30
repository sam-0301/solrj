package com.ibm.domino.services.solrbak;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

//import org.apache.solr.client.solrj.impl.CloudSolrClient;
//import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
//import com.ibm.domino.services.solrpopulator.SolrJUpdate;

public class SolrJUpdateRequest_bak {
	private static URLClassLoader loader = null;
	private static volatile boolean initSucc = false;
	
	private static Class cSolrJUpdate = null;
	private static Class cCloudSolrClient = null;
	private static Class cContentStreamUpdateRequest = null;
	
	private static Method mGetSolrUpdateClient = null;
	private static Method mCreateUpdateRequest = null;
	private static Method mAddUpdateContent = null;
	private static Method mPostUpdateToSolrWithRetries = null;
	
	// initializing
	static{
		initSucc = init();
	}
	
	private static boolean init(){
		final String programDir = System.getProperty("notes.binary");
		if (programDir == null){
			// TODO log
			return false;
		}
			
		final String paths[] = { "osgi","saas","eclipse","plugins","com.ibm.domino.saas.corejars_1.0.0","lib" };
		File dir = new File(programDir);
		for (int i = 0; i < paths.length; i++) {
			dir = new File(dir, paths[i]);
		}
		final File j1 = new File(dir, "solrjutil.jar");
		if (!j1.exists()){
			// TODO log
			return false;
		}
		
		URL urls[];
		try {
			urls = new URL[] { j1.toURI().toURL() };
			final URLClassLoader loader = getClassLoader(urls);
			cSolrJUpdate = loader.loadClass("com.ibm.domino.services.solrpopulator.SolrJUpdate");
			cCloudSolrClient = loader.loadClass("org.apache.solr.client.solrj.impl.CloudSolrClient");
			cContentStreamUpdateRequest = loader.loadClass("org.apache.solr.client.solrj.request.ContentStreamUpdateRequest");
			
			Class[] argTypes1 = {String.class};
			mGetSolrUpdateClient = cSolrJUpdate.getMethod("getSolrUpdateClient", argTypes1);
			
			Class[] argTypes2 = {};
			mCreateUpdateRequest = cSolrJUpdate.getMethod("createUpdateRequest", argTypes2);
			
			Class[] argTypes3 = {cContentStreamUpdateRequest, byte[].class};
			mAddUpdateContent = cSolrJUpdate.getMethod("addUpdateContent", argTypes3);
			
			Class[] argTypes4 = {cCloudSolrClient, cContentStreamUpdateRequest, String.class, Integer.class};
			mPostUpdateToSolrWithRetries = cSolrJUpdate.getMethod("postUpdateToSolrWithRetries", argTypes4);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		return true;
	}
	
    private  static URLClassLoader getClassLoader(URL[] urls) {
		    if (loader == null)
			    loader = new URLClassLoader(urls);

	    return loader;
    }
	
    private String zkHosts = null;
    private String collectionName = null;
    private int retry = 3;
    
    private Object client = null;
    private Object req = null;
    
	// step 1
	public SolrJUpdateRequest_bak(String zkHosts, int collectionID, int retry){
		this.zkHosts = zkHosts;
		this.collectionName = "collection" + collectionID;
		this.retry = retry;
		this.client = getSolrUpdateClient();
		this.req = createUpdateRequest();
	}
	
	
	private Object getSolrUpdateClient(){
		if(!initSucc){
			return null;
		}
		
		try {
			return mGetSolrUpdateClient.invoke(null, new Object[]{zkHosts});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	private Object createUpdateRequest(){
		if(!initSucc){
			return null;
		}
		
		try {
			return mCreateUpdateRequest.invoke(null, new Object[]{});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	// step 2 - iterate addUpdateContent() for trunk
	public int addUpdateContent(byte[] s){
		if(!initSucc){
			return 100;
		}
		
		try {
			return ((Integer)mAddUpdateContent.invoke(null, new Object[]{req, s})).intValue();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 200;
		}
	}
	
	// step 3
	public int postUpdateToSolrWithRetries(){
		if(!initSucc){
			return 100;
		}
		
		try {
			return ((Integer)mPostUpdateToSolrWithRetries.invoke(null, new Object[]{client, req, collectionName, Integer.valueOf(retry)})).intValue();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 200;
		}
	}
}
