package com.ibm.domino.services.solrjutil;

import java.io.IOException;
import java.util.logging.Level;

import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient.RemoteSolrException;
import org.apache.solr.common.util.NamedList;

import com.ibm.commons.util.io.json.JsonJavaObject;

import static com.ibm.domino.services.solrjutil.SCNSolrJ.log;

public abstract class SolrJRequest {
	private static final int MAX_RETRIES = 5;
	
	protected int respCode = 200;
	protected Object respBody = null;
	
	protected SolrRequest<?> req;
	protected CloudSolrClient client;
	
	protected boolean isPopulator;
	protected String zkHosts; 
	protected String collection;
	protected boolean parseJsonObj;
	protected boolean needBody;
	
	public SolrJRequest(String zkHosts, String collection, boolean parseJsonObj, boolean isPopulator, boolean needBody){
		this.zkHosts = zkHosts;
		this.collection = collection;
		this.parseJsonObj = parseJsonObj;
		this.isPopulator = isPopulator;
		this.needBody = needBody;
	}
	
	protected abstract void createRequest();
	
	public int sendRequestToSolrWithRetries(int retry) {
		int err = 0;
		
		this.client = SCNSolrJ.getClient(zkHosts, isPopulator);
		this.createRequest();
		
		
		if (client == null || req == null || collection == null
				|| collection.trim().isEmpty()) {
			log.severe("Null or empty param: client = " + client + ", req = " + req
					+ ", collection = " + collection);
			return ERR1000;
		}

		if (retry <= 0) {
			log.info("retry = " + retry + ", use default 1");
			retry = 1;
		}

		if (retry > MAX_RETRIES) {
			log.info("retry = " + retry + ", use MAX_RETRIES "
					+ MAX_RETRIES);
			retry = MAX_RETRIES;
		}

		int rspCode = 500;
		NamedList<Object> rsp = null;
		for (int i = 0; i < retry; ++i) {
			try {
				rsp = client.request(req, collection);

				if (rsp == null) {
					log.warning("Got NULL response: rsp = " + rsp);
					err = ERR1007;
					continue;
				}

				rspCode = 200; // success
				break;
			} catch(RemoteSolrException e){
				log.log(Level.SEVERE, e.getLocalizedMessage(), e);
				rspCode = e.code();
				err = ERR1001;
				continue;
			} catch (SolrServerException e) {
				log.log(Level.SEVERE, e.getLocalizedMessage(), e);
				err = ERR1002;
				continue;
			} catch (IOException e) {
				log.log(Level.SEVERE, e.getLocalizedMessage(), e);
				err = ERR1003;
				continue;
			}

		}

		if (rsp == null) {
			log.severe("Got NULL response: rsp = " + rsp);
			err = ERR1004;
			return err;
		}

		this.respCode = rspCode;
		
		if(!needBody){
			return err;
		}
		
		if (this.parseJsonObj) {
			JsonJavaObject json = (JsonJavaObject) rsp.get("JSON");
			if (json == null) {
				log.severe("Got NULL JSON obj: json = " + json);
				err = ERR1005;
				return err;
			}
			this.respBody = json;
		} 
		else {
			String text = (String) rsp.get("response");
			if (text == null) {
				log.severe("Got NULL string: text = " + text);
				err = ERR1006;
				return err;
			}
			this.respBody = text;
		}

		return err;
	}
	
	public int getResponseCode(){
		return respCode;
	}
	
	public Object getResponseBody(){
		return respBody;
	}
	
	
	
	// static final error code
	private static final int ERR1000 = 1000;
	private static final int ERR1001 = 1001;
	private static final int ERR1002 = 1002;
	private static final int ERR1003 = 1003;
	private static final int ERR1004 = 1004;
	private static final int ERR1005 = 1005;
	private static final int ERR1006 = 1006;
	private static final int ERR1007 = 1007;
}
