package com.ibm.domino.services.solrjutil;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

import javax.xml.bind.DatatypeConverter;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.solr.client.solrj.ResponseParser;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.util.NamedList;

import static com.ibm.domino.services.solrjutil.SCNSolrJ.log;

public class SolrSearchHttpSolrClient extends HttpSolrClient {
	private static final long serialVersionUID = -2152673377539374409L;

	private String username;
	private String password;
	
	public SolrSearchHttpSolrClient(String baseURL, HttpClient client, ResponseParser parser ,String user,String pass) {
	    super(baseURL, client, parser);
		this.username = user;
	    this.password = pass;
	}

	@Override
	protected NamedList<Object> executeMethod(HttpRequestBase method,
			ResponseParser processor) throws SolrServerException {
		if(method == null){
			log.severe("Null method = " + method);
			return null;
		}
		
		if(username == null || username.trim().isEmpty()){
			username = "";
		}
		
		if(password == null || password.isEmpty()){
			password = "";
		}
		
		try {
			if(!username.isEmpty()){
				log.finer("SolrJ will authenticate with username = " + username + "; password = " + password); 
				String encoded = DatatypeConverter.printBase64Binary((username.trim() + ":" + password).getBytes("UTF-8"));
		        method.addHeader("AUTHORIZATION", "Basic " + encoded);
			}
		} catch (UnsupportedEncodingException e1) {
			log.log(Level.SEVERE, e1.getLocalizedMessage(), e1);
			return null;
		}
		
		return super.executeMethod(method, processor);
	}
}
