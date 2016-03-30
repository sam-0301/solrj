package com.ibm.domino.services.solrsearch.resources;

import java.io.IOException;

import org.junit.Test;

public class TestSolrCloudSearchRequest {
	@Test
	public void testQuery(){
		String zkHosts = "9.112.234.174:2181,9.112.234.192:2181,9.112.234.180:2181,9.110.87.197:2181,9.110.87.198:2181";
		String zkChroot = "/cdl4/sc04";
		String collection = "collection1";
		String handler = "/select";
		String fullQueryStr = "http://host:port/solr/collection1/select?q=*:*&rows=10&wt=json";
		
		SolrCloudSearchRequest req = new SolrCloudSearchRequest(zkHosts, zkChroot, collection, handler, fullQueryStr);
		System.out.println(req.getResponseCode());
		try {
			System.out.println(req.getResponseData());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
