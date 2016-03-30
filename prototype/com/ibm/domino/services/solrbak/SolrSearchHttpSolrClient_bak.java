package com.ibm.domino.services.solrbak;


import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.logging.Level;

import javax.xml.bind.DatatypeConverter;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.solr.client.solrj.ResponseParser;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.InputStreamResponseParser;
import org.apache.solr.common.util.NamedList;
import static com.ibm.domino.services.solrjutil.SCNSolrJ.log;

public class SolrSearchHttpSolrClient_bak extends HttpSolrClient {
	

	private static final long serialVersionUID = -2152673377539374409L;

	private static final String UTF_8 = "UTF-8";
	private boolean followRedirects = false;

	private String username;
	private String password;
	
	public SolrSearchHttpSolrClient_bak(String baseURL, HttpClient client, ResponseParser parser ,String user,String pass) {
	    super(baseURL, client, parser);
		this.username = user;
	    this.password = pass;
	}


	@Override
	protected NamedList<Object> executeMethod(HttpRequestBase method,
			ResponseParser processor) throws SolrServerException {
		if(method == null){
			log.severe("Null method = " + method);
			return null;
		}
		
		try {
			String encoded = DatatypeConverter.printBase64Binary((username + ":" + password).getBytes("UTF-8"));
	        method.addHeader("AUTHORIZATION", "Basic " + encoded);
		} catch (UnsupportedEncodingException e1) {
			log.log(Level.SEVERE, e1.getLocalizedMessage(), e1);
		}
		
		// delete below
		InputStream respBody = null;
		boolean shouldClose = true;
		boolean success = false;

		System.out.println(method.getRequestLine());
		try {
			// Execute the method.
			final HttpResponse response = this.getHttpClient().execute(method);
			int httpStatus = response.getStatusLine().getStatusCode();
			// Read the contents
			respBody = response.getEntity().getContent();
			Header ctHeader = response.getLastHeader("content-type");

			String contentType;
			if (ctHeader != null) {
				contentType = ctHeader.getValue();
			} else {
				contentType = "";
			}

			// handle some http level checks before trying to parse the response
			switch (httpStatus) {
			case HttpStatus.SC_OK:
			case HttpStatus.SC_BAD_REQUEST:
			case HttpStatus.SC_CONFLICT: // 409
				break;
			case HttpStatus.SC_MOVED_PERMANENTLY:
			case HttpStatus.SC_MOVED_TEMPORARILY:
				if (!this.followRedirects) {
					throw new SolrServerException("Server at " + getBaseURL()
							+ " sent back a redirect (" + httpStatus + ").");
				}
				break;
			default:
				if (processor == null || "".equals(contentType)) {
					throw new RemoteSolrException(baseUrl, httpStatus,
							"non ok status: "
									+ httpStatus
									+ ", message:"
									+ response.getStatusLine()
											.getReasonPhrase(), null);
				}
			}

			if (processor == null
					|| processor instanceof InputStreamResponseParser) {
				// no processor specified, return raw stream
				NamedList<Object> rsp = new NamedList<Object>();
				rsp.add("stream", respBody);
				// Only case where stream should not be closed
				shouldClose = false;
				success = true;
				return rsp;
			}

			NamedList<Object> rsp = null;

			try {
				rsp = processor.processResponse(respBody, UTF_8);

			} catch (Exception e) {
				throw new RemoteSolrException(baseUrl, httpStatus,
						e.getMessage(), e);
			}

			rsp.add("HttpCode", httpStatus);
			rsp.add("Headers", response.getAllHeaders());

			success = true;
			return rsp;
		} catch (ConnectException e) {
			throw new SolrServerException("Server refused connection at: "
					+ getBaseURL(), e);
		} catch (SocketTimeoutException e) {
			throw new SolrServerException(
					"Timeout occured while waiting response from server at: "
							+ getBaseURL(), e);
		} catch (IOException e) {
			throw new SolrServerException(
					"IOException occured when talking to server at: "
							+ getBaseURL(), e);
		} finally {
			if (respBody != null && shouldClose) {
				try {
					respBody.close();
				} catch (IOException e) {
					log.log(Level.SEVERE, e.getLocalizedMessage(), e);
				} finally {
					if (!success) {
						method.abort();
					}
				}
			}
		}
	}
}
