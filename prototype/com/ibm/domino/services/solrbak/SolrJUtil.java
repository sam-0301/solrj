package com.ibm.domino.services.solrbak;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;


public class SolrJUtil {
	private static URLClassLoader loader = null;
	private static volatile boolean initSucc = false;
	
	private static Class cSolrJUtil = null;
	private static Class cCloudSolrClient = null;
	private static Class cContentStreamUpdateRequest = null;
	
	private static Method mGetSolrClient = null;
	private static Method mCreateUpdateRequest = null;
	private static Method mAddUpdateContent = null;
	private static Method mPostUpdateToSolrWithRetries = null;
	private static Method mQueryToSolrWithRetries = null;
	
	private static boolean isTest = false; // TODO product env always false
	
	// initializing
	static{
		initSucc = init();
		
		// TODO 
		System.out.println("initSucc=" + initSucc);
	}
	
	private static boolean init(){
		File j1 = null;
		
		if(!isTest){
			final String programDir = System.getProperty("notes.binary");
			if (programDir == null){
				// TODO log
				System.out.println("notes.binary is null");
				return false;
			}
				
			final String paths[] = { "osgi","saas","eclipse","plugins","com.ibm.domino.saas.corejars_1.0.0","lib" };
			File dir = new File(programDir);
			for (int i = 0; i < paths.length; i++) {
				dir = new File(dir, paths[i]);
			}
			j1 = new File(dir, "solrjutil.jar");
			if (!j1.exists()){
				System.out.println("solrjutil.jar not exist");
				return false;
			}
		}

		URL urls[];
		try {
			ClassLoader loader = null;
			
			if(isTest){
				loader = SolrJUtil.class.getClassLoader();
			} else{
				urls = new URL[] { j1.toURI().toURL() };
				loader = getClassLoader(urls);
			}
			
			cSolrJUtil = loader.loadClass("com.ibm.domino.services.solrpopulator.SolrJUtil");
			cCloudSolrClient = loader.loadClass("org.apache.solr.client.solrj.impl.CloudSolrClient");
			cContentStreamUpdateRequest = loader.loadClass("org.apache.solr.client.solrj.request.ContentStreamUpdateRequest");
			
			Class[] argTypes1 = {String.class};
			mGetSolrClient = cSolrJUtil.getMethod("getSolrClient", argTypes1);

			Class[] argTypes2 = {};
			mCreateUpdateRequest = cSolrJUtil.getMethod("createUpdateRequest", argTypes2);

			Class[] argTypes3 = {cContentStreamUpdateRequest, byte[].class};
			mAddUpdateContent = cSolrJUtil.getMethod("addUpdateContent", argTypes3);

			// int postUpdateToSolrWithRetries(CloudSolrClient client, ContentStreamUpdateRequest req, String collection, int retry)
			Class[] argTypes4 = {cCloudSolrClient, cContentStreamUpdateRequest, String.class, int.class};
			mPostUpdateToSolrWithRetries = cSolrJUtil.getMethod("postUpdateToSolrWithRetries", argTypes4);

			// queryToSolrWithRetries(String zkHosts, String collection, String handler, String paramStr, int[] statusCode, Object[] body, boolean needJson, int retry)
			Class[] argTypes5 = {String.class, String.class, String.class, String.class, int[].class, Object[].class, boolean.class, int.class};
			mQueryToSolrWithRetries = cSolrJUtil.getMethod("queryToSolrWithRetries", argTypes5);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		return true;
	}
	
	private static URLClassLoader getClassLoader(URL[] urls) {
		if (loader == null)
			loader = new URLClassLoader(urls);

		return loader;
	}
	
	private SolrJUtil(){}
	
	
	// update step 1
	public static Object getSolrUpdateClient(String zkHosts){
		if(!initSucc){
			//TODO 
			System.out.println("init fail");
			return null;
		}
		
		try {
			return mGetSolrClient.invoke(null, new Object[]{zkHosts});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	// update step 2
	public static Object createUpdateRequest(){
		if(!initSucc){
			//TODO 
			System.out.println("init fail");
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
	
	// update step 3 - iterate addUpdateContent() for trunk
	public static int addUpdateContent(Object req, byte[] s){
		if(!initSucc){
			//TODO
			System.out.println("init fail");
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
	
	// update step 4 
	public static int postUpdateToSolrWithRetries(Object client, Object req, int collectionID, int retry){
		if(!initSucc){
			//TODO
			System.out.println("init fail");
			return 100;
		}
		
		try {
			return ((Integer)mPostUpdateToSolrWithRetries.invoke(null, new Object[]{client, req, "collection"+collectionID, Integer.valueOf(retry)})).intValue();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 200;
		}
	}
	
	// query
	public static int queryToSolrWithRetries(String zkHosts, int collectionID, String handler,
			String paramStr, int[] statusCode, Object[] body, boolean needParseJsonObject, int retry){
		if(!initSucc){
			//TODO
			System.out.println("init fail");
			return 100;
		}
		
		try {
			return ((Integer)mQueryToSolrWithRetries.invoke(null, new Object[]{zkHosts, "collection"+collectionID, handler, paramStr, statusCode, body, Boolean.valueOf(needParseJsonObject), Integer.valueOf(retry)})).intValue();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 200;
		}
	}
}
