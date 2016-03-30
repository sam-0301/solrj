package com.ibm.domino.services.solrbak;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient.RemoteSolrException;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.NamedList;

import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.domino.services.solrjutil.SCNSolrJ;
import com.ibm.domino.services.solrsearch.resources.SearchRequest;

public class SolrCloudSearchRequest_bak extends SearchRequest {
	
	public static final String UTF8 = "UTF-8";
	private CloudSolrClient cloudSolrClient = null;
	private SolrQuery solrQuery = null;
	private QueryRequest queryRequest = null;
	private String collection = null;
	
	private static QueryResponse response;
	private JsonJavaObject jsonJavaObject;
	
/*	static{
		
		// TODO to check classloader
		System.out.println("HttpClient=" + org.apache.http.client.HttpClient.class.getProtectionDomain().getCodeSource().getLocation());
		System.out.println("AbstractHttpClient=" + org.apache.http.impl.client.AbstractHttpClient.class.getProtectionDomain().getCodeSource().getLocation());
		System.out.println("DefaultHttpClient=" + org.apache.http.impl.client.DefaultHttpClient.class.getProtectionDomain().getCodeSource().getLocation());
		System.out.println("CloseableHttpClient=" + org.apache.http.impl.client.CloseableHttpClient.class.getProtectionDomain().getCodeSource().getLocation());
		System.out.println("SystemDefaultHttpClient=" + org.apache.http.impl.client.SystemDefaultHttpClient.class.getProtectionDomain().getCodeSource().getLocation());
		
	}
*/
	
	/**
	 * create a request of SolrCloudClient to search on solrCloud 
	 * @param urls : the shards' http url
	 * @param ZKHosts : ZKs ip+port?  for example: 9.112.234.174:2181
	 * @param collection : collection's name which to be search on
	 * @param queryStr :solrj request url
	 * @param method : METHOD.GET or METHOD.POST
	 * @throws MalformedURLException
	 */
	public SolrCloudSearchRequest_bak(String zkHosts, String zkChroot, String collection, String handler, String queryStr, Map<String, String> params, METHOD method,String username,String password) {
		if(zkHosts == null || zkChroot == null || collection == null || queryStr == null || method == null) {
			// TODO tanglin: using log
			System.out.println("can not initial cause parame is null!");			
		} 
		
		cloudSolrClient = SCNSolrJ.getClient(zkHosts + zkChroot, false);
		
		// cloudSolrClient.setDefaultCollection(collection); 
		this.collection = collection;
		
		solrQuery = new SolrQuery(queryStr);
		
		if(handler != null && !handler.trim().isEmpty()){
			solrQuery.setRequestHandler(handler);
		}
	
		if(params != null){
			for(String key: params.keySet()){
				solrQuery.set(key, params.get(key));
			}
		}
		
		queryRequest = new QueryRequest(solrQuery, method);
	}

	/**
	 * 
	 * @return response httpCode
	 */
	@Override
	public int getResponseCode() {
		try {
			NamedList<Object> rsp = cloudSolrClient.request(queryRequest, collection);
			
			if(rsp == null){
				// TODO log and err code
				return 500;
			}
			return 200;
		} catch(RemoteSolrException e){
			// TODO log
			return e.code();
		} catch (SolrServerException e) {
			e.printStackTrace();
			return 500;
		} catch (IOException e) {
			e.printStackTrace();
			return 500;
		}
	}


	/**
	 * get the responses data as String contains responseBody, HttpCode and Headers
	 * @return the String Data
	 * @throws IOException 
	 */
	@Override
	public String getResponseData() throws IOException {
		
		if(response == null){
			System.out.println("no response , please query first!");
			return null;
		}
		
		NamedList<Object> DataList  = response.getResponse();
		Object data = DataList.get("JSON");
		
		return data.toString();
	}

	/**
	 * get the JsonJavaObject response
	 * @return the JsonJavaObject of responses
	 * @throws IOException 
	 */
	@Override
	public JsonJavaObject getResponseDataObject() throws IOException {
		
		if(response == null){
			System.out.println("no response , please query first!");
			return null;
		}
		NamedList<Object> JsonList = response.getResponse();
		
		jsonJavaObject = (JsonJavaObject) JsonList.get("JSON");
		
		return jsonJavaObject;
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
		
			try {
				cloudSolrClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		
	}

}
