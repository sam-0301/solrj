package com.jni.ibm;

public class CatchThrow {
	private native void doit() throws IllegalArgumentException;

	     private void callback() throws NullPointerException {

	         throw new NullPointerException("CatchThrow.callback");
	     }
}
