package com.ibm.domino.services.solrpopulator;

import static com.ibm.domino.services.solrjutil.SCNSolrJ.log;

import com.ibm.domino.services.solrjutil.SolrJQuery;
import com.ibm.domino.services.solrjutil.SolrJUpdate;


public class SolrJUtilProxy {
	private SolrJUtilProxy() {
	}

	// update step 1
	public static SolrJUpdate createUpdateRequest(String zkHosts, String collection) {
		return new SolrJUpdate(zkHosts, collection);
	}

	// update step 2 - iterate addUpdateContent() for trunk
	public static int addUpdateContent(SolrJUpdate req, byte[] s) {
		if (req == null || s == null) {
			log.warning("Null param: req = " + req + "; s = " + s);
			return ERR800;
		}

		return req.addUpdateContent(s);
	}

	// update step 3
	public static int postUpdateToSolrWithRetries(SolrJUpdate req, int retry) {
		if(req == null)  {
			log.warning("Null req = " + req);
			return ERR801;
		}

		return req.sendRequestToSolrWithRetries(retry);
	}
	
	// query
	public static int queryToSolrWithRetries(String zkHosts, String collection, String handler,
			String paramStr, int[] statusCode, Object[] body, boolean needParseJsonObj, int retry){
		int err = 0;
		
		SolrJQuery req = new SolrJQuery(zkHosts, collection, handler, paramStr, needParseJsonObj, true);
		
		err = req.sendRequestToSolrWithRetries(retry);
		
		if(statusCode != null && statusCode.length >= 1){
			statusCode[0] = req.getResponseCode();
		}
		
		if(body != null && body.length >= 1){
			body[0] = req.getResponseBody();
		}
		
		return err;
	}
	
	// static final error code
	private static final int ERR800 = 800;
	private static final int ERR801 = 801;
}
