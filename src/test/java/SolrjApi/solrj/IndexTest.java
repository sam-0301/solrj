package SolrjApi.solrj;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.common.util.ContentStreamBase.StringStream;
import org.junit.Test;

import com.ibm.domino.services.solrjutil.JSONResponseParser;
import com.ibm.domino.services.solrjutil.SolrSearchLBHttpSolrClient;

public class IndexTest {
	private static String ZKHosts = "9.112.234.174:2181,9.112.234.192:2181,9.112.234.180:2181,9.110.87.197:2181,9.110.87.198:2181/cdl4/sc04";
	private static SolrSearchLBHttpSolrClient sLBHttpSolrClient = null;
	private static CloudSolrClient cloudSolrClient = null;
	
	@Test
	public void postJsonTest() throws SolrServerException, IOException{
		
//		String str1 = "{\"add\":{\"doc\":{\"id\":\"666\",\"noteid\":\"10\",\"customerid\":\"10\",\"subject\":\"www\",\"owner\":\"as\",\"sendto\": [\"cn.ibm.com\"],\"servername\":\"wts\",\"unid\":\"10\"}},\"add\":{\"doc\":{\"id\":\"777\",\"noteid\":\"10\",\"customerid\":\"10\",\"subject\":\"www\",\"owner\":\"as\",\"sendto\": [\"cn.ibm.com\"],\"servername\":\"wts\",\"unid\":\"10\"}}}";
		String str2 = "{\"add\":{\"doc\":{\"id\":\"5\",\"noteid\":\"10\",\"customerid\":\"10\",\"subject\":\"www\",\"owner\":\"as\",\"sendto\": [\"cn.ibm.com\"],\"servername\":\"wts\",\"unid\":\"10\"}},\"add\":{\"doc\":{\"id\":\"6\",\"noteid\":\"10\",\"customerid\":\"10\",\"subject\":\"www\",\"owner\":\"as\",\"sendto\": [\"cn.ibm.com\"],\"servername\":\"wts\",\"unid\":\"10\"}}}";
		
		sLBHttpSolrClient = new SolrSearchLBHttpSolrClient("solrupdate","widget@1bm");
		cloudSolrClient = new CloudSolrClient(ZKHosts, sLBHttpSolrClient);
	
		cloudSolrClient.setDefaultCollection("c2");
		ContentStreamUpdateRequest csUpdateRequest = new ContentStreamUpdateRequest("/update");
		
		csUpdateRequest.setResponseParser(new JSONResponseParser());
		//csUpdateRequest.setParam("commit","true");
		
		ContentStream stringStream = new StringStream(str2); 
		//ContentStream stringStream2 = new StringStream(str4); 
		csUpdateRequest.addContentStream(stringStream);
		//csUpdateRequest.addContentStream(stringStream2);
		
		csUpdateRequest.process(cloudSolrClient);
		cloudSolrClient.close();
	}
	
	@Test
	public void postJsonTestMultiStream() throws SolrServerException, IOException{
		
		String str3 = "{\"add\":{\"doc\":{\"id\":\"9\",\"noteid\":\"10\",\"customerid\":\"10\",\"subject\":\"www\",\"owner\":\"as\",\"send";
		String str4 = "to\": [\"cn.ibm.com\"],\"servername\":\"wts\",\"unid\":\"10\"}},\"add\":{\"doc\":{\"id\":\"4\",\"noteid\":\"10\",\"customerid\":\"10\",\"subject\":\"www\",\"owner\":\"as\",\"sendto\": [\"cn.ibm.com\"],\"servername\":\"wts\",\"unid\":\"10\"}}}";
		
		sLBHttpSolrClient = new SolrSearchLBHttpSolrClient("solrupdate","widget@1bm");
		cloudSolrClient = new CloudSolrClient(ZKHosts, sLBHttpSolrClient);
	
		cloudSolrClient.setDefaultCollection("c2");
		ContentStreamUpdateRequest csUpdateRequest = new ContentStreamUpdateRequest("/update/json");
		
		csUpdateRequest.setResponseParser(new JSONResponseParser());
		//csUpdateRequest.setParam("commit","true");
		
		ContentStream stringStream = new StringStream(str3+str4); 
		csUpdateRequest.addContentStream(stringStream);
		
		
		csUpdateRequest.process(cloudSolrClient);
		cloudSolrClient.close();
	}
	
	@Test
	public void addDocumentTestByCloudSolr() throws SolrServerException, IOException{ //filed matchs with the schema.xml
		
		cloudSolrClient.setDefaultCollection("acollection1");
		
		SolrInputDocument document1 = new SolrInputDocument();
		document1.addField("id", "333");
		document1.addField("noteid", "222");
		document1.addField("customerid", "12");
		document1.addField("owner", "as");
		document1.addField("servername", "wts");
		document1.addField("unid", "3");
		document1.addField("sendto", "cn.ibm.com");
		document1.addField("subject", "www");
		
		UpdateRequest req = new UpdateRequest();
	    req.add(document1);   
	    req.setCommitWithin(-1);
	    UpdateResponse response = req.process(cloudSolrClient);
	    System.out.println(response);
	    
		System.out.println("commited...");
		cloudSolrClient.commit();
		cloudSolrClient.close();
	}
	
	@Test
	public void addDocs() throws SolrServerException, IOException{ //filed matchs with the schema.xml
		
		cloudSolrClient.setDefaultCollection("acollection1");
		
		Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
		
		for(int i =1;i<=3;i++){
			SolrInputDocument document1 = new SolrInputDocument();
			document1.addField("id", i);
			document1.addField("noteid", i*10);
			document1.addField("customerid", i*10);
			document1.addField("owner", "as");
			document1.addField("servername", "wts");
			document1.addField("unid", i*10);
			document1.addField("sendto", "cn.ibm.com");
			document1.addField("subject", "www");
			docs.add(document1);
		}
		
		UpdateResponse response = cloudSolrClient.add(docs);
		System.out.println(response);

		System.out.println("commited...");
		cloudSolrClient.commit();
		cloudSolrClient.close();
	}
	
	@Test
	public void realTimeGet() throws SolrServerException, IOException{ //filed matchs with the schema.xml
	
		cloudSolrClient.setDefaultCollection("acollection1");
			
		SolrInputDocument document = new SolrInputDocument();
		
		document.addField("id", "2222");
		document.addField("noteid", "8888");
		document.addField("customerid", "8888");
		document.addField("owner", "3");
		document.addField("servername", "wts");
		document.addField("unid", "3");
		document.addField("sendto", "cn.ibm.com");
		document.addField("subject", "www");
		
		cloudSolrClient.add(document);
			
		SolrQuery query = new SolrQuery();
		query.set("qt","/get");
		query.set("id","2222");
		
		System.out.println(query.toString());
		
		QueryResponse queryResponse = cloudSolrClient.query(query);
		
		System.out.println("===============" + queryResponse);
		
		cloudSolrClient.commit();
		cloudSolrClient.close();
	}
	
	@Test
	public void updateDoc() throws IOException, SolrServerException{

		cloudSolrClient.setDefaultCollection("acollection1");
		DocPojo docPojo = new DocPojo();
		docPojo.setId("321");
		docPojo.setNoteid("123");
		docPojo.setOwner("tonson");
		docPojo.setSendto("watson");
		docPojo.setServername("ibm.com");
		docPojo.setSubject("hello ibm!");
		docPojo.setUnid("u984732543");
		docPojo.setCustomerid("65");
		
		cloudSolrClient.addBean(docPojo);
		cloudSolrClient.commit();
		cloudSolrClient.close();
		
	}
	
	
	
	
//	@SuppressWarnings("unused")
//	@Test
//	public void concurrentUpdateDoc() throws SolrServerException, IOException{
//		
//		String url = "http://cdl4newsolr04.scn4.cn.ibm.com:8983/solr/acollection1";
//		
//		SolrSearchConcurrentUpdateSolrClient concurrentUpdateSolrClient = new SolrSearchConcurrentUpdateSolrClient(url,30,1,"solrupdate","widget@1bm"); 
//	
////		ConcurrentUpdateSolrClient concurrentUpdateSolrClient = new ConcurrentUpdateSolrClient(url, 15, 1);
//		
//		Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
//		
//		for(int i =1;i<=3;i++){
//			SolrInputDocument document1 = new SolrInputDocument();
//			document1.addField("id", i);
//			document1.addField("noteid", i*100);
//			document1.addField("customerid", i*1000);
//			document1.addField("owner", "as");
//			document1.addField("servername", "wts");
//			document1.addField("unid", i*10);
//			document1.addField("sendto", "cn.ibm.com");
//			document1.addField("subject", "www");
//			docs.add(document1);
//		}
////		
////		concurrentUpdateSolrClient.add(docs);
////		
//
////		concurrentUpdateSolrClient.optimize();
//
//		concurrentUpdateSolrClient.deleteById("333");
//		
////		UpdateRequest req = new UpdateRequest();
////	    req.add(docs);
////	    req.setCommitWithin(-1);  
////		UpdateResponse response = req.process(concurrentUpdateSolrClient);
////	    System.out.println(response);
////		System.out.println("commit...");
////		concurrentUpdateSolrClient.commit();
//System.out.println("close...");
//		concurrentUpdateSolrClient.close();
//	}
	
}
