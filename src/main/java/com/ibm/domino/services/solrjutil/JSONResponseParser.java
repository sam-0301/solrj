package com.ibm.domino.services.solrjutil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.logging.Level;

import org.apache.solr.client.solrj.ResponseParser;
import org.apache.solr.common.util.NamedList;

import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonJavaFactory;
import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.commons.util.io.json.JsonParser;

import static com.ibm.domino.services.solrjutil.SCNSolrJ.log;

public class JSONResponseParser extends ResponseParser {

	public static JsonJavaFactory factory = JsonJavaFactory.instanceEx;

	@Override
	public String getWriterType() {
		return "json";
	}

	@Override
	public NamedList<Object> processResponse(InputStream body, String encoding) {
		if (body == null || encoding == null) {
			log.warning("Null parameters: body = " + body + "; encoding = "
					+ encoding);
			return null;
		}

		BufferedReader r = null;
		try {
			NamedList<Object> response = new NamedList<Object>();
			r = new BufferedReader(new InputStreamReader(body, encoding));

			JsonJavaObject jsonObject = (JsonJavaObject) JsonParser.fromJson(
					factory, r);
			response.add("JSON", jsonObject);
			return response;

		} catch (Exception e) {
			log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			return null;

		} finally {
			if (r != null) {
				try {
					r.close();
				} catch (IOException e) {
					log.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
			}

			if (null != body) {
				try {
					body.close();
				} catch (IOException e) {
					log.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
			}
		}
	}

	@Override
	public NamedList<Object> processResponse(Reader reader) {
		if (reader == null) {
			log.warning("Null parameters: reader = " + reader);
			return null;
		}

		try {
			JsonJavaObject jsonObject = (JsonJavaObject) JsonParser.fromJson(
					factory, reader);
			NamedList<Object> list = new NamedList<Object>();
			list.add("JSON", jsonObject);
			
			return list;
		} catch (JsonException e) {
			log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			return null;
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				log.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
	}
	
	@Override
	public String getContentType() {
		return "text/plain";
	}
}
