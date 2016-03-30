package com.ibm.domino.services.solrbak;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient.RemoteSolrException;
import org.apache.solr.client.solrj.impl.NoOpResponseParser;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.client.solrj.request.QueryRequest;

import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.domino.services.solrjutil.JSONResponseParser;
import com.ibm.domino.services.solrjutil.SCNSolrJ;

import org.apache.solr.common.util.ContentStreamBase.StringStream;
import org.apache.solr.common.util.NamedList;

import static com.ibm.domino.services.solrjutil.SCNSolrJ.log;

public class SolrJUtil_bak {

	private static final int MAX_RETRIES = 5;

	private SolrJUtil_bak() {
	}

	private static HashMap<ContentStreamUpdateRequest, LinkedList<byte[]>> reqContentMap = new HashMap<ContentStreamUpdateRequest, LinkedList<byte[]>>();
	private static HashMap<ContentStreamUpdateRequest, Integer> reqLengthMap = new HashMap<ContentStreamUpdateRequest, Integer>();

	// step 1
	public static CloudSolrClient getSolrClient(
			String zkHosts) {
		return SCNSolrJ.getClient(zkHosts, true);
	}

	// update step 2
	public static ContentStreamUpdateRequest createUpdateRequest() {
		ContentStreamUpdateRequest csUpdateRequest = new ContentStreamUpdateRequest(
				"/update");
		return csUpdateRequest;
	}

	// update step 3 - iterate addUpdateContent() for trunk
	public static int addUpdateContent(ContentStreamUpdateRequest req, byte[] s) {
		int err = 0;

		if (req == null || s == null) {
			// TODO log and define error code
			System.out.println("req = " + req + "; s = " + s);
			return 1;
		}

		LinkedList<byte[]> buf = reqContentMap.get(req);
		if (buf == null) {
			buf = new LinkedList<byte[]>();
			reqContentMap.put(req, buf);
		}
		buf.add(s);

		Integer length = reqLengthMap.get(req);
		if (length == null) {
			length = 0;
		}
		length += s.length;
		reqLengthMap.put(req, length);

		return err;
	}

	// update step 4
	public static int postUpdateToSolrWithRetries(CloudSolrClient client,
			ContentStreamUpdateRequest req, String collection, int retry) {
		int err = 0;
		if (client == null || req == null || collection == null
				|| collection.trim().isEmpty()) {
			// TODO log and err code
			System.out.println("client = " + client + ", req = " + req
					+ ", collection = " + collection);
			return 1;
		}

		if (retry <= 0) {
			// TODO log
			System.out.println("retry = " + retry + ", use default 1");
			retry = 1;
		}

		if (retry > MAX_RETRIES) {
			// TODO log
			System.out.println("retry = " + retry + ", use MAX_RETRIES "
					+ MAX_RETRIES);
			retry = MAX_RETRIES;
		}

		try {
			LinkedList<byte[]> buf = reqContentMap.get(req);
			if (buf == null || buf.isEmpty()) {
				// TODO log warning

			} else {
				Integer length = reqLengthMap.get(req);
				if (length == null) {
					// TODO warning
					length = 0;
				}

				byte[] newBuf = new byte[length];
				int copied = 0;
				for (byte[] b : buf) {
					System.arraycopy(b, 0, newBuf, copied, b.length);
					copied += b.length;
				}
				try {
					req.addContentStream(new StringStream(new String(newBuf,
							"UTF-8")));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					err = 6;
					return err;
				}
			}

			return sendRequestToSolrWithRetries(client, req, collection, retry,
					null, null, false);

		} finally {
			reqContentMap.remove(req);
		}

	}

	private static QueryRequest createQueryRequest(String handler,
			String paramStr, boolean parseJson) {
		SolrQuery solrQuery; 
		Map<String, String> params = new HashMap<String, String>();
		
		String wt = "json";
		if(paramStr != null && !paramStr.trim().isEmpty()){
			String queryStr = null;
			String[] ss2 = paramStr.split("&");
			for(String s2 : ss2){
				
				log.fine("s2 : " + s2);
				
				int idx = s2.indexOf("="); 
				String[] ss3 = new String[2];
				if(idx <= 0 || idx > s2.length()){
					log.fine("Wrong param with '=': " + s2 + " ; idx = " + idx);
					continue;
				}
				ss3[0] = s2.substring(0, idx);
				ss3[1] = s2.substring(idx + 1);
						
				try {
					if("q".equals(ss3[0])){
						queryStr = URLDecoder.decode(ss3[1], "UTF-8");
						log.fine("q str : " + queryStr);
					} else if("wt".equals(ss3[0])){
						wt = URLDecoder.decode(ss3[1], "UTF-8");
						log.fine("wt str : " + wt);
					} else{
						params.put(ss3[0], URLDecoder.decode(ss3[1], "UTF-8"));
						log.fine(ss3[0] + "=" + ss3[1]);
					}
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if(queryStr == null){
				solrQuery = new SolrQuery();
			} else{
				solrQuery = new SolrQuery(queryStr);
			}
			
			if (params != null) {
				for (String key : params.keySet()) {
					solrQuery.set(key, params.get(key));
				}
			}
			
		} else {
			solrQuery = new SolrQuery();
		}
		
		if (handler != null && !handler.trim().isEmpty()) {
			solrQuery.setRequestHandler(handler);
		}

		QueryRequest queryRequest = new QueryRequest(solrQuery, METHOD.POST);

		if (parseJson) {
			queryRequest.setResponseParser(new JSONResponseParser());
		} else {
			queryRequest.setResponseParser(new NoOpResponseParser(wt));
		}

		return queryRequest;
	}

	private static int sendRequestToSolrWithRetries(CloudSolrClient client,
			SolrRequest<?> req, String collection, int retry, final int[] statusCode, final Object[] body, boolean needJson) {
		int err = 0;
		if (client == null || req == null || collection == null
				|| collection.trim().isEmpty()) {
			// TODO log and err code
			System.out.println("client = " + client + ", req = " + req
					+ ", collection = " + collection);
			return 1;
		}

		if (retry <= 0) {
			// TODO log
			System.out.println("retry = " + retry + ", use default 1");
			retry = 1;
		}

		if (retry > MAX_RETRIES) {
			// TODO log
			System.out.println("retry = " + retry + ", use MAX_RETRIES "
					+ MAX_RETRIES);
			retry = MAX_RETRIES;
		}

		int rspCode = -1;
		NamedList<Object> rsp = null;
		for (int i = 0; i < retry; ++i) {
			try {
				// TODO pseudo code
				rsp = client.request(req, collection);

				if (rsp == null) {
					// TODO log and err code
					err = 4;
					continue;
				}

				rspCode = 200; // success
				break;
			} catch(RemoteSolrException e){
				// TODO log
				rspCode = e.code();
				err = 5;
				continue;
			} catch (SolrServerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				err = 2;
				continue;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				err = 3;
				continue;
			}

		}

		if (rsp == null) {
			// TODO log
			err = 8;
			return err;
		}

		if(statusCode != null && statusCode.length > 0){
			statusCode[0] = rspCode;
		}
		
		if (body != null && body.length > 0) {

			if (needJson) {
				JsonJavaObject json = (JsonJavaObject) rsp.get("JSON");
				if (json == null) {
					// TODO log
					err = 6;
					return err;
				}
				body[0] = json;
			} else {
				String text = (String) rsp.get("response");
				if (text == null) {
					// TODO log
					err = 7;
					return err;
				}
				body[0] = text;
			}
		}

		return err;

	}
	
	// query
	public static int queryToSolrWithRetries(String zkHosts, String collection, String handler,
			String paramStr, int[] statusCode, Object[] body, boolean needJson, int retry){
		int err = 0;
		CloudSolrClient client = getSolrClient(zkHosts);
		if(client == null){
			// TODO log
			
			err = 1;
			return err;
		}
		
		QueryRequest req = createQueryRequest(handler, paramStr, needJson);
		if(req == null){
			// TODO log
			
			err = 2;
			return err;
		}
		
		err = sendRequestToSolrWithRetries(client, req, collection, retry, statusCode, body, needJson);
		return err;
	}
/*
	// test
	public static void main(String[] args) {
		//testUpdate();
		//testQuery();
	}

	private static void testUpdate() {
		String zkHosts = "9.112.234.174:2181,9.112.234.192:2181,9.112.234.180:2181,9.110.87.197:2181,9.110.87.198:2181/cdl4/sc04";
		String[] body = {
				"{\"add\":{\"doc\":{\"id\":\"zzz\",\"noteid\":\"11\",\"customerid\":\"11\",\"subject\":\"www\",\"owner\":\"as\",\"send",
				"to\": [\"abc@cn.ibm.com\"],\"servername\":\"wts\",\"unid\":\"10\"}},\"add\":{\"doc\":{\"id\":\"yyy\",\"noteid\":\"12\",\"c",
				"ustomerid\":\"12\",\"subject\":\"www\",\"owner\":\"as\",\"sendto\": [\"def@cn.ibm.com\"],\"servername\":\"wts\",\"unid\":\"10\"}}}" };
		String collection = "collection1";
		int retry = 3;

		// TODO to handle err
		CloudSolrClient client = getSolrClient(zkHosts);
		ContentStreamUpdateRequest req = createUpdateRequest();
		for (String str : body) {
			try {
				addUpdateContent(req, str.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.print(postUpdateToSolrWithRetries(client, req, collection,
				retry));
		System.exit(0);
	}
	
	private static void testQuery(){
		String zkHosts = "9.112.234.174:2181,9.112.234.192:2181,9.112.234.180:2181,9.110.87.197:2181,9.110.87.198:2181/cdl4/sc04";
		int collection = 1;
		int retry = 3;
		String handler = "/select";
		String paramStr = "q=*:*&facet=true&facet.field=owner&rows=10&wt=json&indent=true";
		boolean needJson = false;
		
		
		int[] statusCode = new int[1];
		String[] body = new String[1];
		
		System.out.println(queryToSolrWithRetries(zkHosts, "collection" + collection, handler, paramStr, statusCode, body, needJson, retry));
		System.out.println("status = " + statusCode[0]);
		System.out.println("body = " + body[0]);
		System.exit(0);
	}
*/
}
