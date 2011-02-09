package hw2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

public class Client implements Runnable {

	private int portNum;
	Integer[][] dtt = new Integer[5][5];
	int pid;
	int clockCounter;
	String dicName;
	File dicFile;

	public Client(int pid, int portNum) {
		this.portNum = portNum;
		new Thread(this).start();
		initDtt();
		this.pid = pid;
		this.clockCounter = 1;
		this.dicName = "Dic" + pid;
		this.dicFile = new File(this.dicName);

	}

	private void initDtt() {
		for (int i = 0; i < this.dtt.length; i++) {
			for (int j = 0; j < this.dtt.length; j++) {
				this.dtt[i][j] = 0;
			}
		}
	}

	public void request() throws IOException {

		DatagramSocket requestSocket = null;

		String message = null;
		String msg = null;

		try {
			requestSocket = new DatagramSocket();

		} catch (IOException e) {
			e.printStackTrace();
		}

		msg = "dummy";

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String[] msgparts = msg.split(":", 2);

		while (!msg.equals("bye-bye")) {
			System.out
					.println("Enter the message and the destination to send in the format <Destination ID>:<message payload>");
			System.out
					.println("Message payload should be of the format <key>-<value>");
			System.out.print(">>>");
			try {
				msg = br.readLine();
			} catch (IOException e1) {
				System.out.println("Failed to read message");
				e1.printStackTrace();
			}
			DicEntry d = parseMsg(msg);
			writeToDictionary(d);
			msgparts = msg.split(":", 2);
			sendMessage(msgparts[0], d, requestSocket);
		}
		// sendMessage(msg, requestSocket);

	}

	private DicEntry parseMsg(String msg) {
		String[] msgparts = msg.split(":", 2);
		String payloadParts[] = msgparts[1].split("-", 2);
		DicEntry d = new DicEntry(payloadParts[0], payloadParts[1], this.pid,
				this.clockCounter);
		this.dtt[this.pid][this.pid] = this.clockCounter;
		this.clockCounter++;
		return d;
	}

	private void writeToDictionary(DicEntry d) throws IOException {

		BufferedWriter file = null;
		BufferedReader file1 = null;
		boolean found = false;
		String thisLine;
		String[] lineParts = new String[4];
		
		file = new BufferedWriter(new FileWriter(this.dicFile, true));
		file1 = new BufferedReader(new FileReader(this.dicFile));
		while ((thisLine = file1.readLine()) != null) {
			lineParts = thisLine.split(" ", 4);
			if(lineParts[0].equals(d.key)){
				found = true;
			}
		}
		if(!found){
		file.append(d.toString() + "\n");
		}
		file.close();
		file1.close();
		
	}

	private void sendMessage(String dest, DicEntry d,
			DatagramSocket requestSocket) throws IOException {
		
			LinkedList<DicEntry> list = new LinkedList<DicEntry>();
			try {
				list = getDicEntries(dest);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			byte buf[] = new byte[10000];
			
			try {
				System.out.println("Sending message --> " + d.key + "-"
						+ d.value);

				DatagramPacket datapkt = new DatagramPacket(buf, buf.length,
						InetAddress.getByName(GFSConstants.routerName),
						this.portNum);
				
				list.add(d);
				Message msgObj = new Message(dest, this.dtt, list);
				msgObj.setPid(this.pid);
				datapkt.setData(msgObj.getBytes());

				requestSocket.send(datapkt);
				// System.out.println("client>" + msg);
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
			printTimeTable();
		}

	

	private void printTimeTable() {
		for(int i=0;i<this.dtt.length;i++){
			for(int j=0;j<this.dtt.length;j++){
				System.out.print(this.dtt[i][j]+" ");
			}
			System.out.println("\n");
		}
		
	}

	private LinkedList<DicEntry> getDicEntries(String dest) throws IOException {
		BufferedReader file = null;
		String thisLine;
		String[] lineParts = new String[4];
		int destId = Integer.parseInt(dest);
		LinkedList<DicEntry> list = new LinkedList<DicEntry>();

		file = new BufferedReader(new FileReader(this.dicFile));
		while ((thisLine = file.readLine()) != null) {
			lineParts = thisLine.split(" ", 4);
			if(this.dtt[destId][Integer.parseInt(lineParts[2])] < Integer.parseInt(lineParts[3])){
				list.add(new DicEntry(lineParts[0], lineParts[1], Integer.parseInt(lineParts[2]), Integer.parseInt(lineParts[3])));
			}
			
		}
		return list;
	}

	public void run() {
		DatagramSocket thisConnection = null;
		byte[] buf = new byte[100000];
		String[] msgparts = new String[2];

		DatagramPacket recvdpkt = new DatagramPacket(buf, buf.length);

		try {
			thisConnection = new DatagramSocket(GFSConstants.ClientPort);

		} catch (IOException e) {
			System.out.println("Socket creation failed on the client");
			e.printStackTrace();
		}
		while (true) {
			try {
				thisConnection.receive(recvdpkt);
				// String msg = new String(recvdpkt.getData());

				Message msg = toObject(recvdpkt.getData());

				// msgparts = msg.split(":", 2);
				updateDtt(msg);
				for (int i = 0; i < msg.dicList.size(); i++) {
					DicEntry d = msg.dicList.get(i);
					writeToDictionary(d);

					System.out.println("Key : " + d.key + "-" + "Value : "
							+ d.value);
				}
			} catch (IOException e) {

				e.printStackTrace();
			}

		}

	}

	private void updateDtt(Message msg) {
		for (int i = 0; i < this.dtt.length; i++) {
			for (int j = 0; j < this.dtt.length; j++) {

				this.dtt[i][j] = Math.max(this.dtt[i][j], msg.dtt[i][j]);
			}
			for (int k = 0; k < this.dtt.length; k++) {
				this.dtt[this.pid][k] = Math.max(this.dtt[this.pid][k],
						msg.dtt[msg.getPid()][k]);
			}
		}
	}

	private Message toObject(byte[] data) {

		Message obj = null;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			ObjectInputStream ois = new ObjectInputStream(bis);
			obj = (Message) ois.readObject();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		return obj;

	}

	public static void main(String args[]) {

		int pid = Integer.parseInt(args[0]);

		Client client = new Client(pid, GFSConstants.RouterSendPort);

		System.out.println("The available clients are: C1, C2, C3, C4, C5");
		System.out.println("The File Server is : FS");

		try {
			client.request();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

}
