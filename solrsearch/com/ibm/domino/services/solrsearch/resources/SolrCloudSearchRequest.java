package com.ibm.domino.services.solrsearch.resources;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;


import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.domino.services.solrjutil.SolrJQuery;

public class SolrCloudSearchRequest extends SearchRequest {
	private SolrJQuery req = null;
	private boolean sentReq = false;
	
	static{
		// TODO to check classloader
		System.out.println("HttpClient=" + org.apache.http.client.HttpClient.class.getProtectionDomain().getCodeSource().getLocation());
		System.out.println("AbstractHttpClient=" + org.apache.http.impl.client.AbstractHttpClient.class.getProtectionDomain().getCodeSource().getLocation());
		System.out.println("DefaultHttpClient=" + org.apache.http.impl.client.DefaultHttpClient.class.getProtectionDomain().getCodeSource().getLocation());
		System.out.println("CloseableHttpClient=" + org.apache.http.impl.client.CloseableHttpClient.class.getProtectionDomain().getCodeSource().getLocation());
		System.out.println("SystemDefaultHttpClient=" + org.apache.http.impl.client.SystemDefaultHttpClient.class.getProtectionDomain().getCodeSource().getLocation());
		
	}

	
	/**
	 * create a request of SolrCloudClient to search on solrCloud 
	 * @param ZKHosts : ZKs ip+port?  for example: 9.112.234.174:2181
	 * @param collection : collection's name which to be search on
	 * @param fullQueryStr :solrj request url
	 * @param method : METHOD.GET or METHOD.POST
	 * @throws MalformedURLException
	 */
	public SolrCloudSearchRequest(String zkHosts, String zkChroot, String collection, String handler, String fullQueryStr) {
		if(zkHosts == null || zkHosts.trim().isEmpty() || 
				zkChroot == null || zkChroot.trim().isEmpty() || 
				collection == null || collection.trim().isEmpty() || 
				handler == null || handler.trim().isEmpty() || 
				fullQueryStr == null || fullQueryStr.trim().isEmpty() ) {
			// TODO tanglin: using log
			System.out.println("can not initial cause parame is null!");			
		} 
		String[] ss = fullQueryStr.split("\\?");
		String paramStr = "";
		if(ss.length == 2){
			paramStr = ss[1];
		}
		
		req = new SolrJQuery(zkHosts.trim() + zkChroot.trim(), collection, handler, paramStr, true, false);
	}

	/**
	 * 
	 * @return response httpCode
	 */
	@Override
	public int getResponseCode() {
		if(req == null){
			// TODO log
			return 500;
		}
		
		if(sentReq){
			return req.getResponseCode();
		}
		
		int err = req.sendRequestToSolrWithRetries(1);
		if(err != 0){
			// TODO log
			return 500;
		}
		
		sentReq = true;
		return req.getResponseCode();
	}


	/**
	 * get the responses data as String contains responseBody, HttpCode and Headers
	 * @return the String Data
	 * @throws IOException 
	 */
	@Override
	public String getResponseData() throws IOException {
		if(req == null){
			// TODO log
			return null;
		}
		
		if(sentReq){
			return req.getResponseBody().toString();
		}
		
		int err = req.sendRequestToSolrWithRetries(1);
		if(err != 0){
			// TODO log
			return null;
		}
		
		sentReq = true;
		return req.getResponseBody().toString();
	}

	/**
	 * get the JsonJavaObject response
	 * @return the JsonJavaObject of responses
	 * @throws IOException 
	 */
	@Override
	public JsonJavaObject getResponseDataObject() throws IOException {
		
		if(req == null){
			// TODO log
			return null;
		}
		
		if(sentReq){
			return (JsonJavaObject)req.getResponseBody();
		}
		
		int err = req.sendRequestToSolrWithRetries(1);
		if(err != 0){
			// TODO log
			return null;
		}
		
		sentReq = true;
		return (JsonJavaObject)req.getResponseBody();
	}
	
	
	/**
	 * 
	 * @return response Header as Header[]
	 * @throws IOException 
	 */
	@Override
	public Map<String, List<String>> getResponseHeaders() {
		// for now we do not support headers
		return null; 
	}
	

	/**
	 * close the cloudSolrClient
	 * @throws IOException 
	 */
	@Override
	public void disconnect() {
	}

}
