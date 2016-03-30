/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corporation 2014                              */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has           */
/* been deposited with the U.S. Copyright Office.                    */
/* ***************************************************************** */

package com.ibm.domino.services.solrsearch.resources;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
//import java.security.KeyManagementException;
//import java.security.KeyStore;
//import java.security.KeyStoreException;
//import java.security.NoSuchAlgorithmException;
//import java.security.UnrecoverableKeyException;
//import java.security.cert.CertificateException;
import java.util.List;
import java.util.Map;

import com.ibm.commons.util.io.json.JsonJavaObject;

public class SearchRequest {
	
	public static final String UTF8 = "UTF-8";
	
//	private final Session session;
	protected final URL requestURL;
	protected HttpURLConnection httpConn = null;
	protected URLConnection conn = null;
	protected boolean usePost = false;
	protected int responseCode = 200;
	protected String ZKHosts;
	protected String collection;
	protected String queryStr;
	protected String method;
	
	public SearchRequest(){
		requestURL = null;
	}
	
	public SearchRequest(final URL requestURL) {
		this.requestURL = requestURL;
	}
	
	public SearchRequest(final URL requestURL, final boolean usePost) {
		this.requestURL = requestURL;
		this.usePost = usePost;
	}
	
	public SearchRequest(String ZKHosts ,String collection,String queryStr,String method) {
		this.requestURL = null;//requestURL;
		this.ZKHosts = ZKHosts;
		this.collection = collection;
		this.queryStr = queryStr;
		this.method = method;
	}

	public int getResponseCode() {
		
		return responseCode;
	}
	
	public String getResponseData() throws IOException {
		
		return null;
	}
	
	public JsonJavaObject getResponseDataObject() throws IOException {
		
		JsonJavaObject jsonObject = null;
		return jsonObject;
	}
	
	public void disconnect(){
	}

	/**
	 * To extract the querystring part and to put into POST content
	 * 
	 * @param urlInfo
	 * @return queryString
	 */
	private String extractQueryParameter(URL urlInfo) {
		return urlInfo.getQuery();
	}
	
	/**
	 * Extract the URL Path from a complete URL with queryString.
	 * 
	 * @param urlInfo
	 * @return URL object with URL Path only (without QueryString)
	 * @throws MalformedURLException
	 */
	private URL extractURLPath(URL urlInfo) throws MalformedURLException {
		String originalURL = urlInfo.toString();
		String newURL = originalURL.replace(urlInfo.getQuery(), "");
		return new URL(newURL);
	}
	
	public Map<String, List<String>> getResponseHeaders(){

		return null;
	}
	
	public String testJniString(){
		return null;
	}
	
	public int testJniInt(){
		return (Integer) null;
	}
	
	
}
