/**
 * 
 */
package com.example;

/**
 * @author Johnathan
 *
 */
public class MultithreadedReadWriteTest implements Runnable{

	public MultithreadedReadWriteTest(){
		Thread t = new Thread(this);
		t.start();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MultithreadedReadWriteTest test = new MultithreadedReadWriteTest();
		System.out.println("HEY, SHIT WORKED");

	}

	@Override
	public void run() {
		while(true){}
	}
}
