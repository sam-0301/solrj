package com.jni.ibm;

public class AddString {
	
	public AddString() {
		// TODO Auto-generated constructor stub
	}
	
	public int printInt(){
		return 33;
	}
	
	public String out(){
		return "hello";
	}
	
	public String apendString(String a,String b){
		return a+b;
	}
	
	public static void main(String[] args)
	{
		System.out.println(new AddString().printInt());
		System.out.println(new AddString().out());
		java.util.Scanner input = new java.util.Scanner(System.in);
		System.out.print("输入字符串：");
		String s = input.next();
		System.out.println("您输入的字符串为:"+s);
	
	}

}
