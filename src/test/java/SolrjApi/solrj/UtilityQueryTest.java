package SolrjApi.solrj;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.junit.Test;

import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.domino.services.solrbak.SolrCloudSearchRequest_bak;

public class UtilityQueryTest {
	
	private static String ZKHosts = "9.112.234.174:2181,9.112.234.192:2181,9.112.234.180:2181,9.110.87.197:2181,9.110.87.198:2181";
	private static String chroot = "/cdl4/sc04";
	
	@Test
	public void SolrSearchQueryTest() throws SolrServerException, IOException{
		//SolrCloudSearchRequest(String zkHosts, String zkChroot, String collection, String handler, String queryStr, Map<String, String> params, METHOD method,String username,String password)
		
		SolrCloudSearchRequest_bak solrCloudSearchRequest = new SolrCloudSearchRequest_bak(ZKHosts, chroot, "collection1", "/select", "*:*", new HashMap<String, String>(), METHOD.POST,"solrreader","widget@1bm");
		System.out.println("httpCode : " + solrCloudSearchRequest.getResponseCode());
		JsonJavaObject javaObject = solrCloudSearchRequest.getResponseDataObject();
		System.out.println("jsonRep : " + javaObject);
		
		SolrCloudSearchRequest_bak solrCloudSearchRequest5 = new SolrCloudSearchRequest_bak(ZKHosts, chroot, "collection1", "/select", "id:1", new HashMap<String, String>(), METHOD.POST,"solrreader","widget@1bm");
		System.out.println("httpCode : " + solrCloudSearchRequest5.getResponseCode());
		Map<String, List<String>> headers = solrCloudSearchRequest5.getResponseHeaders();
		System.out.println(headers);
		
		SolrCloudSearchRequest_bak solrCloudSearchRequest1 = new SolrCloudSearchRequest_bak(ZKHosts, chroot, "collection10", "/select", "*:*", new HashMap<String, String>(), METHOD.POST,"solrreader","widget@1bm");
		System.out.println(solrCloudSearchRequest1.getResponseCode());
		String data = solrCloudSearchRequest1.getResponseData();
		System.out.println(data);
		
		SolrCloudSearchRequest_bak solrCloudSearchRequest2 = new SolrCloudSearchRequest_bak(ZKHosts, chroot, "collection1", "/select", "id:2", new HashMap<String, String>(), METHOD.POST,"solrreader","widget@1bm");
		System.out.println(solrCloudSearchRequest2.getResponseCode());
		String data1 = solrCloudSearchRequest2.getResponseData();
		System.out.println(data1);
		
		SolrCloudSearchRequest_bak solrCloudSearchRequest3 = new SolrCloudSearchRequest_bak(ZKHosts, chroot, "collection10", "/select", "*:*", new HashMap<String, String>(), METHOD.POST,"solrreader","widget@1bm");
		System.out.println(solrCloudSearchRequest3.getResponseCode());
		String data2 = solrCloudSearchRequest3.getResponseData();
		System.out.println(data2);
		
	}
	
	
}
