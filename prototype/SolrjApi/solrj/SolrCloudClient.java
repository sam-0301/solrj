package SolrjApi.solrj;

import java.net.MalformedURLException;

import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.LBHttpSolrClient;

public class SolrCloudClient {
	
	private static String ZKHosts = "9.112.234.174:2181,9.112.234.192:2181,9.112.234.180:2181,9.110.87.197:2181,9.110.87.198:2181/cdl4/sc04";
	
	private static String replica1_Url = "http://cdl4newsolr04.scn4.cn.ibm.com:8983/solr/acollection1_shard1_replica1";
	private static String replica2_Url = "http://cdl4newsolr03.scn4.cn.ibm.com:8983/solr/acollection1_shard1_replica2";

	private static CloudSolrClient cloudSolrClient;
	
	public CloudSolrClient getCloudClient(){
		cloudSolrClient = new CloudSolrClient(ZKHosts);
		System.out.println(cloudSolrClient.getZkHost());
		return cloudSolrClient;
	}
	
	public LBHttpSolrClient getLBHttpClient(){
		
		LBHttpSolrClient lbHttpSolrClient = null;
		try {
			lbHttpSolrClient = new LBHttpSolrClient(replica1_Url,replica2_Url);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return lbHttpSolrClient;
	}

}
