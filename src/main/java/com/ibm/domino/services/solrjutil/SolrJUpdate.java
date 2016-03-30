package com.ibm.domino.services.solrjutil;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.logging.Level;

import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.common.util.ContentStreamBase.StringStream;

import static com.ibm.domino.services.solrjutil.SCNSolrJ.log;


public class SolrJUpdate extends SolrJRequest{
	private int length = 0;
	private LinkedList<byte[]> buf = new LinkedList<byte[]>();

	
	// step 1
	public SolrJUpdate(String zkHosts, String collection){
		super(zkHosts, collection, false, true, false);
	}
	
	// step 2 - iterate
	public int addUpdateContent(byte[] s) {
		int err = 0;

		if (s == null) {
			log.severe("Null s = " + s);
			return ERR900;
		}

		buf.add(s);
		length += s.length;

		return err;
	}
	
	@Override
	protected void createRequest() {
		ContentStreamUpdateRequest csUpdateRequest = new ContentStreamUpdateRequest(
				"/update");
		
		
		if (buf == null || buf.isEmpty() || length <= 0) {
			log.severe("Empty buf  = " + buf + " or length = " + length);
			return;
		}

		byte[] newBuf = new byte[length];
		int copied = 0;
		for (byte[] b : buf) {
			System.arraycopy(b, 0, newBuf, copied, b.length);
			copied += b.length;
		}
		
		try {
			csUpdateRequest.addContentStream(new StringStream(new String(
					newBuf, "UTF-8")));
		} catch (UnsupportedEncodingException e) {
			log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			return;
		}
		
		this.req = csUpdateRequest;
	}
	
	
	// static final error code
	private static final int ERR900 = 900;
}
