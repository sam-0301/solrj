package lotus.notes.solrpopulator;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class SolrJUtilProxy {
	private static URLClassLoader loader = null;
	private static volatile boolean initSucc = false;
	
	// have to not use <?> because java compiler level is too low in Domino sandbox
	private static Class cSolrJUtilProxy = null; 
	private static Class cSolrJUpdate = null;

	private static Method mCreateUpdateRequest = null;
	private static Method mAddUpdateContent = null;
	private static Method mPostUpdateToSolrWithRetries = null;
	private static Method mQueryToSolrWithRetries = null;
	
	private static boolean isTest = false; // product env always false
	
	// initializing
	static{
		initSucc = init();
		System.out.println("SolrJ proxy class initializing success = " + initSucc);
	}
	
	private static boolean init(){
		File j1 = null;
		
		if(!isTest){
			final String programDir = System.getProperty("notes.binary");
			if (programDir == null){
				System.out.println("notes.binary is null");
				return false;
			}
				
			final String paths[] = { "osgi","saas","eclipse","plugins","com.ibm.domino.saas.corejars_1.0.0","lib" };
			File dir = new File(programDir);
			for (int i = 0; i < paths.length; i++) {
				dir = new File(dir, paths[i]);
			}
			j1 = new File(dir, "solrjutil.jar");
			if (!j1.exists()){
				System.out.println("solrjutil.jar not exist");
				return false;
			}
		}

		URL urls[];
		try {
			ClassLoader loader = null;
			
			if(isTest){
				loader = SolrJUtilProxy.class.getClassLoader();
			} else{
				urls = new URL[] { j1.toURI().toURL() };
				loader = getClassLoader(urls);
			}
			
			cSolrJUtilProxy = loader.loadClass("com.ibm.domino.services.solrpopulator.SolrJUtilProxy");
			cSolrJUpdate = loader.loadClass("com.ibm.domino.services.solrjutil.SolrJUpdate");

			Class[] argTypes2 = {String.class, String.class};
			mCreateUpdateRequest = cSolrJUtilProxy.getMethod("createUpdateRequest", argTypes2);

			Class[] argTypes3 = {cSolrJUpdate, byte[].class};
			mAddUpdateContent = cSolrJUtilProxy.getMethod("addUpdateContent", argTypes3);

			Class[] argTypes4 = {cSolrJUpdate, int.class};
			mPostUpdateToSolrWithRetries = cSolrJUtilProxy.getMethod("postUpdateToSolrWithRetries", argTypes4);

			Class[] argTypes5 = {String.class, String.class, String.class, String.class, int[].class, Object[].class, boolean.class, int.class};
			mQueryToSolrWithRetries = cSolrJUtilProxy.getMethod("queryToSolrWithRetries", argTypes5);
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
	
	private static URLClassLoader getClassLoader(URL[] urls) {
		if (loader == null)
			loader = new URLClassLoader(urls);

		return loader;
	}
	
	private SolrJUtilProxy(){}
	
	// update step 1
	public static Object createUpdateRequest(String zkHosts, int collectionID){
		if(!initSucc){
			System.out.println("SolrJ proxy class intializing fail.");
			return null;
		}
		
		try {
			return mCreateUpdateRequest.invoke(null, new Object[]{zkHosts, "collection" + collectionID});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	// update step 2 - iterate addUpdateContent() for trunk
	public static int addUpdateContent(Object req, byte[] s){
		if(!initSucc){
			System.out.println("SolrJ proxy class intializing fail.");
			return ERR700;
		}
		
		try {
			return ((Integer)mAddUpdateContent.invoke(null, new Object[]{req, s})).intValue();
		} catch (Exception e) {
			e.printStackTrace();
			return ERR701;
		}
	}
	
	// update step 3
	public static int postUpdateToSolrWithRetries(Object req, int retry){
		if(!initSucc){
			System.out.println("SolrJ proxy class intializing fail.");
			return ERR702;
		}
		
		try {
			return ((Integer)mPostUpdateToSolrWithRetries.invoke(null, new Object[]{req, Integer.valueOf(retry)})).intValue();
		} catch (Exception e) {
			e.printStackTrace();
			return ERR703;
		}
	}
	
	// query
	public static int queryToSolrWithRetries(String zkHosts, int collectionID,
			String handler, String paramStr, int[] statusCode, Object[] body,
			boolean needParseJsonObj, int retry) {
		if (!initSucc) {
			System.out.println("SolrJ proxy class intializing fail.");
			return ERR704;
		}

		try {
			return ((Integer)mQueryToSolrWithRetries.invoke(null, new Object[]{zkHosts, "collection"+collectionID, handler, paramStr, statusCode, body, Boolean.valueOf(needParseJsonObj), Integer.valueOf(retry)})).intValue();
		} catch (Exception e) {
			e.printStackTrace();
			return ERR705;
		}
	}

	
	// static final err code
	private static final int ERR700 = 700;
	private static final int ERR701 = 701;
	private static final int ERR702 = 702;
	private static final int ERR703 = 703;
	private static final int ERR704 = 704;
	private static final int ERR705 = 705;
}
