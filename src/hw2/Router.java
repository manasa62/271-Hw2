/** @author Manasa Chandrashekar
 * 			DivyaShree Hassan Ravindrakumar
 */
package hw2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 
 * Implements the router functionality
 * 
 */
public class Router implements Runnable {

	public enum CLIENTSTATE {
		UP, BLOCKED, DOWN
	};

	public LinkedBlockingQueue<Message> msgQueue;
	public static HashMap<String, Router.CLIENTSTATE> clientStatus;
	public static HashMap<String, String> clientTable;

	private int portNum;

	static {
		clientStatus = new HashMap<String, Router.CLIENTSTATE>();
		clientTable = new HashMap<String, String>();
	}

	public Router(int portNum) {
		this.msgQueue = new LinkedBlockingQueue<Message>();
		this.portNum = portNum;
		new Thread(this).start();
	}

	public LinkedBlockingQueue<Message> getMsgQueue() {
		return this.msgQueue;
	}

	public void setMsgQueue(LinkedBlockingQueue<Message> msgQueue) {
		this.msgQueue = msgQueue;
	}

	public HashMap<String, Router.CLIENTSTATE> getClientStatus() {
		return this.clientStatus;
	}

	public void setClientStatus(HashMap<String, Router.CLIENTSTATE> clientStatus) {
		this.clientStatus = clientStatus;
	}

	public HashMap<String, String> getClientTable() {
		return this.clientTable;
	}

	public void setClientTable(HashMap<String, String> clientTable) {
		this.clientTable = clientTable;
	}

	/**
	 * Spawn thread which gets messages from clients
	 */
	public void listen() {

		RouterThread w;
		w = new RouterThread(this.portNum, this);
		Thread t = new Thread(w);
		t.start();

	}

	/**
	 * Separate thread which sends out messages received from clients to their
	 * destination clients
	 */
	public void run() {
		while (true) {
			if (msgQueue.size() >= 1) {

				DatagramSocket requestSocket = null;
				String message = null;
				Message msg = null;

				try {
					requestSocket = new DatagramSocket();

				} catch (IOException e) {
					e.printStackTrace();
				}

				while (!this.msgQueue.isEmpty()) {
					try {
						msg = this.msgQueue.remove();
						// System.out.println("Trying to send message :" + msg);
						try {
							sendMessage(msg, requestSocket);
						} catch (IOException e) {

							e.printStackTrace();
						}

					} catch (NoSuchElementException e) {

						e.printStackTrace();

					}

				}

				// sendTerminateMessage("END_OF_CONNECTION", requestSocket);

			}
		}

	}

	private void sendMessage(Message msg, DatagramSocket requestSocket)
			throws IOException {

		byte buf[] = new byte[100000];
		String[] msgparts = new String[5];
		DatagramPacket newpkt = null;
		InetAddress ip = null;
		try {
			// msgparts = msg.split(":", 3);

			if (Router.clientStatus.get(msg.destID).equals(CLIENTSTATE.UP)) {
				String hostname = Router.clientTable.get(msg.destID);
				try {
					ip = InetAddress.getByName(hostname);
				} catch (UnknownHostException e) {
					System.out
							.println("The host name you are trying to resolve is incorrect!");
				}
				if (msg.destID.equals("FS")) {
					newpkt = new DatagramPacket(buf, buf.length, ip,
							Constants.FileServerPort);
				} else {
					newpkt = new DatagramPacket(buf, buf.length, ip,
							Constants.ClientPort);
				}
				// String newmsg = msgparts[1] + ":" + msgparts[2];

				newpkt.setData(msg.getBytes());
				requestSocket.send(newpkt);

			} else if (Router.clientStatus.get(msg.destID).equals(
					CLIENTSTATE.BLOCKED)) {
				this.msgQueue.add(msg);
			}

		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	public static void main(String args[]) throws IOException {

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String input = new String();
		String[] addr = new String[2];

		System.out
				.println("Enter the details of the client hostnames and IP address");
		System.out.println("Format: <Label>,<Hostname>");
		System.out.println("Type \"done\" in the end");

		input = br.readLine();

		while (!input.equals("done")) {

			addr = input.split(",", 2);
			Router.clientTable.put(addr[0], addr[1]);
			Router.clientStatus.put(addr[0], CLIENTSTATE.UP);

			input = br.readLine();

		}
		
		Constants.NO_OF_NODES = Router.clientTable.size();
		System.out.println("Client table size "+ Router.clientTable.size()+" constant "+Constants.NO_OF_NODES);

		Iterator iterator = Router.clientTable.keySet().iterator();
		System.out.println("Routing table:");
		System.out.println("--------------");
		while (iterator.hasNext()) {
			String key = iterator.next().toString();
			String value = Router.clientTable.get(key).toString();

			System.out.println(key + " : " + value);
		}

		startStatusMaintainThread();

		new Router(Constants.RouterSendPort).listen();

	}

	private static void startStatusMaintainThread() {
		MaintainLinkStates w;
		w = new MaintainLinkStates();
		Thread t = new Thread(w);
		t.start();

	}

}
