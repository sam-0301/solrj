package com.ibm.domino.services.solrjutil;


import java.net.MalformedURLException;

import org.apache.solr.client.solrj.impl.LBHttpSolrClient;


public class SolrSearchLBHttpSolrClient extends LBHttpSolrClient {

	private static final long serialVersionUID = 8942719406339299793L;

	private String username;
	private String password;

	public SolrSearchLBHttpSolrClient(String username, String password, String... solrServerUrls)
			throws MalformedURLException {
		super(null, solrServerUrls);
		this.username = username;
		this.password = password;

	}
	
	@Override
	protected SolrSearchHttpSolrClient makeSolrClient(String server) {
		
		SolrSearchHttpSolrClient client = new SolrSearchHttpSolrClient(server, this.getHttpClient(), this.getParser(), username, password);
		
	    if (this.getRequestWriter() != null) {
	      client.setRequestWriter(this.getRequestWriter());
	    }
	    if (this.getQueryParams() != null) {
	      client.setQueryParams(this.getQueryParams());
	    }
	    return client;
	}
	
}
