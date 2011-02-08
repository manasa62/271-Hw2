package hw2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;

public class RouterThread implements Runnable {

	private DatagramSocket thisConnection;
	private Router router;
	private int portNum;

	public RouterThread(int portNum, Router router) {
		this.thisConnection = null;
		this.router = router;
		this.portNum = portNum;
	}


	public void run() {
		while(true){
		try {
			this.thisConnection = new DatagramSocket(portNum);

		} catch (IOException e) {
			System.out.println("Connection failed on the port: " + portNum);
			e.printStackTrace();
		}

		/*
		 * try { out = new ObjectOutputStream(thisConnection.getOutputStream());
		 * in = new ObjectInputStream(thisConnection.getInputStream()); } catch
		 * (IOException e) {
		 * 
		 * e.printStackTrace(); }
		 */

		try {
			writeToQueue();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		try {
			readFromQueue();
		} catch (IOException e2) {

			e2.printStackTrace();
		}
		}

	}

	private void writeToQueue() throws UnknownHostException {
		Message msg = null;
		byte[] buf = new byte[10000];
		String[] msgparts = null;

		DatagramPacket recvdPkt = new DatagramPacket(buf, buf.length);

		try {

			thisConnection.receive(recvdPkt);

		} catch (IOException e1) {
			e1.printStackTrace();
		}

		msg = toObject(recvdPkt.getData());

	/*	msgparts = new String[2];
		msgparts = msg.split(":", 2);*/

		System.out.println("Packet destined to " + msg.destID
				+ "recieved from " + recvdPkt.getAddress().getHostName());

		
		//	String newmsg = msg + ":" + recvdPkt.getAddress().getHostName();
			msg.srcID = recvdPkt.getAddress().getHostName();
		
			router.getMsgQueue().add(msg);
			try {
				buf = new byte[10000];
				recvdPkt = new DatagramPacket(buf, buf.length);
				thisConnection.receive(recvdPkt);

				//printRouterClientTable();
			}

			catch (IOException e1) {
				e1.printStackTrace();
			}

			msg = null;
			msg = toObject(recvdPkt.getData());

		

	}

	private void printRouterClientTable() {
		Iterator iterator = router.getClientTable().keySet().iterator();
		System.out.println("Routing table contents-->");
		while (iterator.hasNext()) {
			String key = iterator.next().toString();
			String value = router.getClientTable().get(key).toString();

			System.out.println(key + " " + value);
		}

	}

	public void readFromQueue() throws IOException {

		System.out.println("Contents of the message queue");

		System.out.println(router.getMsgQueue().toString());

	}
	
	public Message toObject (byte[] bytes)
	{
	  Message obj = null;
	  try {
	    ByteArrayInputStream bis = new ByteArrayInputStream (bytes);
	    ObjectInputStream ois = new ObjectInputStream (bis);
	    obj = (Message)ois.readObject();
	  }
	  catch (IOException ex) {
	   ex.printStackTrace();
	  }
	  catch (ClassNotFoundException ex) {
	   ex.printStackTrace();
	  }
	  return obj;
	}

	public void start() {
		this.start();

	}

}
