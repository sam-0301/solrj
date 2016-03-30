package lotus.notes.solrpopulator;

import java.io.UnsupportedEncodingException;

import org.junit.AfterClass;
import org.junit.Test;

import static lotus.notes.solrpopulator.SolrJUtilProxy.*;

public class TestSolrJUtilProxy {
	@Test
	public void test_1_Update(){
		String zkHosts = "9.112.234.174:2181,9.112.234.192:2181,9.112.234.180:2181,9.110.87.197:2181,9.110.87.198:2181/cdl4/sc04";
		String[] body = 
			{
				"{\"add\":{\"doc\":{\"id\":\"ttt\",\"noteid\":\"11\",\"customerid\":\"11\",\"subject\":\"www\",\"owner\":\"as\",\"send",
				"to\": [\"abc@cn.ibm.com\"],\"servername\":\"wts\",\"unid\":\"10\"}},\"add\":{\"doc\":{\"id\":\"sss\",\"noteid\":\"12\",\"c",
				"ustomerid\":\"12\",\"subject\":\"www\",\"owner\":\"as\",\"sendto\": [\"def@cn.ibm.com\"],\"servername\":\"wts\",\"unid\":\"10\"}}}"
			};
		int collection = 1;
		int retry = 3;
		
		// TODO to handle err
		Object req = createUpdateRequest(zkHosts, collection);
		for(int i = 0; i < body.length; ++i){
			String str = body[i];
			try {
				addUpdateContent(req, str.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println(postUpdateToSolrWithRetries(req, retry));
	}
	
	@Test
	public void test_2_RTG(){
		String zkHosts = "9.112.234.174:2181,9.112.234.192:2181,9.112.234.180:2181,9.110.87.197:2181,9.110.87.198:2181/cdl4/sc04";
		int collection = 1;
		int retry = 3;
		String handler = "/get";
		//String paramStr = "id=ttt&wt=xml&indent=true";
		String paramStr = "_route_=Q049Y2RsNG1haWwwNS9PPXNjbjQ=20015378!&ids=Q049Y2RsNG1haWwwNS9PPXNjbjQ=20015378!0AB41E386BFFAACC00257F34000C24CD&fl=unid,folderunid&wt=xml";
		boolean needJson = false;
		
		
		int[] statusCode = new int[1];
		String[] body = new String[1];
		
		System.out.println(queryToSolrWithRetries(zkHosts, collection, handler, paramStr, statusCode, body, needJson, retry));
		System.out.println("status = " + statusCode[0]);
		System.out.println("body = " + body[0]);
	}
	
	@Test
	public void test_3_Query(){
		try {
			Thread.sleep(35000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String zkHosts = "9.112.234.174:2181,9.112.234.192:2181,9.112.234.180:2181,9.110.87.197:2181,9.110.87.198:2181/cdl4/sc04";
		int collection = 1;
		int retry = 3;
		String handler = "/select";
		String paramStr = "q=id:ttt&facet=true&facet.field=owner&rows=10&wt=json&indent=true";
		boolean needJson = false;
		
		
		int[] statusCode = new int[1];
		String[] body = new String[1];
		
		System.out.println(queryToSolrWithRetries(zkHosts, collection, handler, paramStr, statusCode, body, needJson, retry));
		System.out.println("status = " + statusCode[0]);
		System.out.println("body = " + body[0]);
	}
	
	@Test
	public void test_4_Delete(){
		String zkHosts = "9.112.234.174:2181,9.112.234.192:2181,9.112.234.180:2181,9.110.87.197:2181,9.110.87.198:2181/cdl4/sc04";
		int collection = 1;
		int retry = 3;
		String handler = "/update";
		String paramStr = "stream.body=<delete><query>owner:as</query></delete>&commit=true&stream.contentType=text/xml;charset=utf-8&wt=json";
		boolean needJson = false;
		
		
		int[] statusCode = new int[1];
		String[] body = new String[1];
		
		System.out.println(queryToSolrWithRetries(zkHosts, collection, handler, paramStr, statusCode, body, needJson, retry));
		System.out.println("status = " + statusCode[0]);
		System.out.println("body = " + body[0]);
	}

	@AfterClass
	public static void exit(){
		System.exit(0);
	}
}
