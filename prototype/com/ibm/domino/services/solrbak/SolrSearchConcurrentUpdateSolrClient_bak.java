package com.ibm.domino.services.solrbak;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.bind.DatatypeConverter;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.solr.client.solrj.ResponseParser;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.BinaryResponseParser;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrClient;
import org.apache.solr.client.solrj.impl.HttpClientUtil;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.params.UpdateParams;
import org.apache.solr.common.util.ExecutorUtil;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SolrjNamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.ibm.domino.services.solrjutil.SolrSearchHttpSolrClient;

public class SolrSearchConcurrentUpdateSolrClient_bak extends ConcurrentUpdateSolrClient {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ResponseParser parser = new BinaryResponseParser();
	private HttpSolrClient client;
	private BlockingQueue<UpdateRequest> queue;
	private ExecutorService scheduler;
    private Logger log = LoggerFactory
    	      .getLogger(ConcurrentUpdateSolrClient.class);
    private Queue<Runner> runners;
	volatile CountDownLatch lock = null; // used to block everything
	int threadCount;
	boolean shutdownExecutor = false;
	int pollQueueTime = 250;
	private boolean streamDeletes;
	private String username;
	private String password;

	public SolrSearchConcurrentUpdateSolrClient_bak(String solrServerUrl, HttpClient client, int queueSize,
			int threadCount,ExecutorService es, boolean streamDeletes,String zKeeper,String u,String p) {
		super(solrServerUrl, client, queueSize, threadCount,es,streamDeletes);
		this.client = new SolrSearchHttpSolrClient(zKeeper, client, parser ,u,p);
		this.client.setFollowRedirects(false);
		this.scheduler= ExecutorUtil.newMDCAwareCachedThreadPool(
		        new SolrjNamedThreadFactory("concurrentUpdateScheduler"));
		this.queue = new LinkedBlockingQueue(queueSize);
		this.threadCount = threadCount;
	    this.runners = new LinkedList();
	    this.streamDeletes = streamDeletes;
	}
	
	/**
	 * 
	 * @param solrServerUrl
	  * @param queueSize
	  *          The buffer size before the documents are sent to the server
	  * @param threadCount
	  *          The number of background threads used to empty the queue
	 * @param u username
	 * @param p password
	 */
	public SolrSearchConcurrentUpdateSolrClient_bak(String solrServerUrl, int queueSize,
            int threadCount,String u,String p) {
		super(solrServerUrl,queueSize,threadCount);
		shutdownExecutor = true;
		scheduler= ExecutorUtil.newMDCAwareCachedThreadPool(
		        new SolrjNamedThreadFactory("concurrentUpdateScheduler"));
//		this.client = new HttpSolrClient(solrServerUrl);
		ModifiableSolrParams params = new ModifiableSolrParams();
	    HttpClient httpClient = HttpClientUtil.createClient(params);
		this.client = new SolrSearchHttpSolrClient(solrServerUrl, httpClient, parser, u, p);
	    this.client.setFollowRedirects(false);
		queue = new LinkedBlockingQueue(queueSize);
	    runners = new LinkedList();
	    this.threadCount = threadCount;
	    this.streamDeletes = false;
		this.username = u;
		this.password = p;
	}
	
	/**
	 * add the Basic authentication
	 * @param http
	 * @param username 
	 * @param password
	 * @throws UnsupportedEncodingException
	 */
	private static void addAuthHeader(HttpRequestBase http, String username, String password) throws UnsupportedEncodingException {
        String encoded = DatatypeConverter.printBase64Binary((username + ":" + password).getBytes("UTF-8"));
        http.addHeader("AUTHORIZATION", "Basic " + encoded);
    }
	
	class Runner implements Runnable {
	    final Lock runnerLock = new ReentrantLock();

	    public void run() {
	      runnerLock.lock();

	      log.debug("starting runner: {}", this);
	      HttpPost method = null;
	      HttpResponse response = null;
	      try {
	        while (!queue.isEmpty()) {
	          try {
	            final UpdateRequest updateRequest = 
	                queue.poll(pollQueueTime, TimeUnit.MILLISECONDS);
	            if (updateRequest == null)
	              break;
	                       
	            String contentType = "application/xml; charset=UTF-8";
	          
	            final boolean isXml = ClientUtils.TEXT_XML.equals(contentType);

	            final ModifiableSolrParams origParams = new ModifiableSolrParams(updateRequest.getParams());

	            EntityTemplate template = new EntityTemplate(new ContentProducer() {

	              public void writeTo(OutputStream out) throws IOException {
	                try {
	                  if (isXml) {
	                    out.write("<stream>".getBytes(StandardCharsets.UTF_8)); // can be anything
	                  }                                    
	                  UpdateRequest req = updateRequest;
	                  while (req != null) {                                        
	                    SolrParams currentParams = new ModifiableSolrParams(req.getParams());
	                    if (!origParams.toNamedList().equals(currentParams.toNamedList())) {
	                      queue.add(req); // params are different, push back to queue
	                      break;
	                    }
	                    
//	                    client.requestWriter.write(req, out);
	                    if (isXml) {
	                      // check for commit or optimize
	                      SolrParams params = req.getParams();
	                      if (params != null) {
	                        String fmt = null;
	                        if (params.getBool(UpdateParams.OPTIMIZE, false)) {
	                          fmt = "<optimize waitSearcher=\"%s\" />";
	                        } else if (params.getBool(UpdateParams.COMMIT, false)) {
	                          fmt = "<commit waitSearcher=\"%s\" />";
	                        }
	                        if (fmt != null) {
	                          byte[] content = String.format(Locale.ROOT,
	                              fmt,
	                              params.getBool(UpdateParams.WAIT_SEARCHER, false)
	                                  + "").getBytes(StandardCharsets.UTF_8);
	                          out.write(content);
	                        }
	                      }
	                    }
	                    out.flush();

	                    if (pollQueueTime > 0 && threadCount == 1 && req.isLastDocInBatch()) {
	                      // no need to wait to see another doc in the queue if we've hit the last doc in a batch
	                      req = queue.poll(0, TimeUnit.MILLISECONDS);
	                    } else {
	                      req = queue.poll(pollQueueTime, TimeUnit.MILLISECONDS);
	                    }

	                  }
	                  
	                  if (isXml) {
	                    out.write("</stream>".getBytes(StandardCharsets.UTF_8));
	                  }

	                } catch (InterruptedException e) {
	                  Thread.currentThread().interrupt();
//	                  log.warn("", e);
	                }
	              }
	            });
	            
	            // The parser 'wt=' and 'version=' params are used instead of the
	            // original params
	            ModifiableSolrParams requestParams = new ModifiableSolrParams(origParams);
	            requestParams.set(CommonParams.WT, parser.getWriterType());
	            requestParams.set(CommonParams.VERSION, parser.getVersion());

	            method = new HttpPost(client.getBaseURL() + "/update"
	                + ClientUtils.toQueryString(requestParams, false));
	            method.setEntity(template);
	            method.addHeader("User-Agent", HttpSolrClient.AGENT);
	            method.addHeader("Content-Type", contentType);
	            
	            try {
					addAuthHeader(method, username, password);
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}  
	                        
	            response = client.getHttpClient().execute(method);
	            
InputStream inputStream = response.getEntity().getContent();
System.out.println(inputStream.toString());
	            
	            int statusCode = response.getStatusLine().getStatusCode();
	            if (statusCode != HttpStatus.SC_OK) {
	              StringBuilder msg = new StringBuilder();
	              msg.append(response.getStatusLine().getReasonPhrase());
	              msg.append("\n\n\n\n");
	              msg.append("request: ").append(method.getURI());

	              SolrException solrExc = new SolrException(ErrorCode.getErrorCode(statusCode), msg.toString());
	              // parse out the metadata from the SolrException
	              try {
	                NamedList<Object> resp =
	                    parser.processResponse(response.getEntity().getContent(),
	                        response.getEntity().getContentType().getValue());
                
	                NamedList<Object> error = (NamedList<Object>) resp.get("error");
	                if (error != null)
	                  solrExc.setMetadata((NamedList<String>) error.get("metadata"));
	              } catch (Exception exc) {
	                // don't want to fail to report error if parsing the response fails
	                log.warn("Failed to parse error response from "+ client.getBaseURL()+" due to: "+exc);
	              }

	              handleError(solrExc);
	            } else {
	              onSuccess(response);
	            }
	          } finally {
	            try {
	              if (response != null) {
	                response.getEntity().getContent().close();
	              }
	            } catch (Exception ex) {
	              log.warn("", ex);
	            }
	          }
	        }
	      } catch (Throwable e) {
	        if (e instanceof OutOfMemoryError) {
	          throw (OutOfMemoryError) e;
	        }
	        handleError(e);
	      } finally {
	        synchronized (runners) {
	          if (runners.size() == 1 && !queue.isEmpty()) {
	            // keep this runner alive
	            scheduler.execute(this);
	          } else {
	            runners.remove(this);
	            if (runners.isEmpty())
	              runners.notifyAll();
	          }
	        }

	        log.debug("finished: {}", this);
	        runnerLock.unlock();
	      }
	    }
	  }
	
	@Override
	public NamedList<Object> request(SolrRequest request, String collection) throws SolrServerException, IOException {
				
		if (!(request instanceof UpdateRequest)) {
		      return client.request(request, collection);
		    }
		    UpdateRequest req = (UpdateRequest) request;

		    // this happens for commit...
		    if (streamDeletes) {
		      if ((req.getDocuments() == null || req.getDocuments().isEmpty())
		          && (req.getDeleteById() == null || req.getDeleteById().isEmpty())
		          && (req.getDeleteByIdMap() == null || req.getDeleteByIdMap().isEmpty())) {
		        if (req.getDeleteQuery() == null) {
		          blockUntilFinished();
		          return client.request(request, collection);
		        }
		      }
		    } else {
		      if ((req.getDocuments() == null || req.getDocuments().isEmpty())) {
		        blockUntilFinished();
		        return client.request(request, collection);
		      }
		    }


		    SolrParams params = req.getParams();
		    if (params != null) {
		      // check if it is waiting for the searcher
		      if (params.getBool(UpdateParams.WAIT_SEARCHER, false)) {
//		        log.info("blocking for commit/optimize");
		        blockUntilFinished(); // empty the queue
		        return client.request(request, collection);
		      }
		    }

		    try {
		      CountDownLatch tmpLock = lock;
		      if (tmpLock != null) {
		        tmpLock.await();
		      }

		      boolean success = queue.offer(req);

		      for (;;) {
		        synchronized (runners) {
		          // see if queue is half full and we can add more runners
		          // special case: if only using a threadCount of 1 and the queue
		          // is filling up, allow 1 add'l runner to help process the queue
		          if (runners.isEmpty() || (queue.remainingCapacity() < queue.size() && runners.size() < threadCount))
		          {
		            // We need more runners, so start a new one.
		            MDC.put("ConcurrentUpdateSolrClient.url", client.getBaseURL());
		            try {
		              Runner r = new Runner();
		              runners.add(r);
		              scheduler.execute(r);
		            } finally {
		              MDC.remove("ConcurrentUpdateSolrClient.url");
		            }
		          } else {
		            // break out of the retry loop if we added the element to the queue
		            // successfully, *and*
		            // while we are still holding the runners lock to prevent race
		            // conditions.
		            if (success)
		              break;
		          }
		        }

		        // Retry to add to the queue w/o the runners lock held (else we risk
		        // temporary deadlock)
		        // This retry could also fail because
		        // 1) existing runners were not able to take off any new elements in the
		        // queue
		        // 2) the queue was filled back up since our last try
		        // If we succeed, the queue may have been completely emptied, and all
		        // runners stopped.
		        // In all cases, we should loop back to the top to see if we need to
		        // start more runners.
		        //
		        if (!success) {
		          success = queue.offer(req, 100, TimeUnit.MILLISECONDS);
		        }
		      }
		    } catch (InterruptedException e) {
//		      log.error("interrupted", e);
		      throw new IOException(e.getLocalizedMessage());
		    }

		    // RETURN A DUMMY result
		    NamedList<Object> dummy = new NamedList();
		    dummy.add("NOTE", "the request is processed in a background stream");
		    return dummy;
		  }


}
