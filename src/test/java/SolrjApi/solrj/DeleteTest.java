package SolrjApi.solrj;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.junit.Test;

import com.ibm.domino.services.solrjutil.SolrSearchLBHttpSolrClient;

public class DeleteTest {
	
	private static String ZKHosts = "9.112.234.174:2181,9.112.234.192:2181,9.112.234.180:2181,9.110.87.197:2181,9.110.87.198:2181/cdl4/sc04";
	private static String collection = "acollection1";

	
	@Test
	public void deleteById() throws SolrServerException, IOException{
		SolrSearchLBHttpSolrClient sLBHttpSolrClient =new SolrSearchLBHttpSolrClient("solrupdate","widget@1bm");
		CloudSolrClient cloudSolrClient = new CloudSolrClient(ZKHosts, sLBHttpSolrClient);
		cloudSolrClient.connect();
		cloudSolrClient.setDefaultCollection(collection);
		cloudSolrClient.deleteById("321");
		
		cloudSolrClient.commit();
		cloudSolrClient.close();
		
	}
	
	@Test
	public void deleteByQuery() throws SolrServerException, IOException{
		SolrSearchLBHttpSolrClient sLBHttpSolrClient =new SolrSearchLBHttpSolrClient("solrupdate","widget@1bm");
		CloudSolrClient cloudSolrClient = new CloudSolrClient(ZKHosts, sLBHttpSolrClient);
		
		cloudSolrClient.connect();
		cloudSolrClient.setDefaultCollection(collection);
		cloudSolrClient.deleteByQuery("id:*");
		cloudSolrClient.commit();
		cloudSolrClient.close();
	}

}
