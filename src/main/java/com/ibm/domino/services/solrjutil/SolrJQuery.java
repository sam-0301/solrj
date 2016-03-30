package com.ibm.domino.services.solrjutil;

import static com.ibm.domino.services.solrjutil.SCNSolrJ.log;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.logging.Level;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.impl.NoOpResponseParser;
import org.apache.solr.client.solrj.request.QueryRequest;


public class SolrJQuery extends SolrJRequest{
	private String handler;
	private String paramStr; 

	public SolrJQuery(String zkHosts, String collection, String handler, String paramStr, boolean parseJsonObj, boolean isPopulator){
		super(zkHosts, collection, parseJsonObj, isPopulator, true);
		this.handler = handler;
		this.paramStr = paramStr;
	}
	
	@Override
	protected void createRequest() {
		SolrQuery solrQuery = new SolrQuery();
		
		String wt = "json";
		if(paramStr != null && !paramStr.trim().isEmpty()){
			String queryStr = null;
			String[] ss2 = paramStr.split("&");
			for(String s2 : ss2){
				
				log.finer("s2 : " + s2);
				
				int idx = s2.indexOf("="); 
				String[] ss3 = new String[2];
				if(idx <= 0 || idx > s2.length()){
					log.warning("Wrong param with '=': " + s2 + " ; idx = " + idx);
					continue;
				}
				ss3[0] = s2.substring(0, idx);
				ss3[1] = s2.substring(idx + 1);
						
				try {
					if("q".equals(ss3[0])){
						queryStr = URLDecoder.decode(ss3[1], "UTF-8");
						log.finer("q str : " + queryStr);
						solrQuery.setQuery(queryStr);
					} else if("wt".equals(ss3[0])){
						wt = URLDecoder.decode(ss3[1], "UTF-8");
						log.finer("wt str : " + wt);
					} else{
						solrQuery.add(ss3[0], URLDecoder.decode(ss3[1], "UTF-8"));
						log.finer(ss3[0] + "=" + ss3[1]);
					}
				} catch (UnsupportedEncodingException e) {
					log.log(Level.WARNING, e.getLocalizedMessage(), e);
					continue;
				}
			}
		}
		
		if (handler != null && !handler.trim().isEmpty()) {
			solrQuery.setRequestHandler(handler);
		}

		QueryRequest queryRequest = new QueryRequest(solrQuery, METHOD.POST);

		if (this.parseJsonObj) {
			queryRequest.setResponseParser(new JSONResponseParser());
		} else {
			queryRequest.setResponseParser(new NoOpResponseParser(wt));
		}

		req = queryRequest;
	}

}
