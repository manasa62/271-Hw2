/** @author Manasa Chandrashekar
 * 			DivyaShree Hassan Ravindrakumar
 */
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
import java.util.List;
import java.util.Random;

/**
 * 
 * This class implements the client which can send and receive messages from
 * other clients
 * 
 */

public class Client implements Runnable {

	private int portNum;
	public Integer[][] dtt = new Integer[5][5];
	private int pid;
	private int clockCounter;
	public String PLfilename;
	public File PLfile;
	public String dicName;
	public File dicFile;
	public LinkedList<LogEntry> PL;
	

	public Client(int pid, int portNum) {
		this.portNum = portNum;
		new Thread(this).start();
		initDtt();
		this.pid = pid;
		this.clockCounter = 1;
		this.PLfilename = "PL" + pid;
		this.PLfile = new File(this.PLfilename);
		this.dicName = "Dic" + pid;
		this.dicFile = new File(this.dicName);
		this.PL = new LinkedList<LogEntry>();
	}

	private void initDtt() {
		for (int i = 0; i < this.dtt.length; i++) {
			for (int j = 0; j < this.dtt.length; j++) {
				this.dtt[i][j] = 0;
			}
		}
	}

	/**
	 * Gets a message to be sent from the user and creates a DataGramPacket and
	 * sends it across the network using UDP User has to provide the destination
	 * host ID and the key,value pair that has to be sent
	 * 
	 * @throws IOException
	 */
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

		System.out
				.println("Enter the message and the destination to send in the format <Destination ID>:<KEY>-<VALUE>");

		try {
			msg = br.readLine();
		} catch (IOException e1) {
			System.out.println("Failed to read message");
			e1.printStackTrace();
		}

		try {
			LogEntry d = parseMsg(msg);
			
			msgparts = msg.split(":", 2);
			sendMessage(msgparts[0], d, requestSocket);
			writeToPL(d);
			writeToDictionary(d);
		} catch (NumberFormatException e1) {
			System.out.println("Check the format of input!!");
			System.out
					.println("Enter in the form --- <Destination ID>:<key>-<value>");
		} catch (ArrayIndexOutOfBoundsException e2) {
			System.out.println("Check the format of input!!");
			System.out
					.println("Enter in the form --- <Destination ID>:<key>-<value>");

		} catch (NullPointerException e) {
			System.out.println("");
		}
		requestSocket.close();
		// sendMessage(msg, requestSocket);

	}

	/**
	 * Convert the message from the user taken in the form of a String to a @see
	 * {@link hw2.LogEntry} Object
	 * 
	 * @param msg
	 *            Message read from the user
	 * @return
	 * @throws NumberFormatException
	 * @throws ArrayIndexOutOfBoundsException
	 */
	private LogEntry parseMsg(String msg) throws NumberFormatException,
			ArrayIndexOutOfBoundsException {

		String[] msgparts = new String[2];
		String[] payloadParts = new String[2];

		msgparts = msg.split(":", 2);

		payloadParts = msgparts[1].split("-", 2);

		LogEntry d = new LogEntry(payloadParts[0], payloadParts[1], this.pid,
				this.clockCounter);
		this.dtt[this.pid][this.pid] = this.clockCounter;
		this.clockCounter++;
		return d;
	}

	/**
	 * 
	 * @param d
	 *            An object of type @see {@link hw2.LogEntry} The dictionary
	 *            entry to be written to File.
	 * @throws IOException
	 */

	private void writeToPLfile(LogEntry d) throws IOException {

		BufferedWriter file = null;
		BufferedReader file1 = null;
		boolean found = false;
		String thisLine;
		String[] lineParts = new String[4];

		file = new BufferedWriter(new FileWriter(this.PLfile, true));
		file1 = new BufferedReader(new FileReader(this.PLfile));
		while ((thisLine = file1.readLine()) != null) {
			lineParts = thisLine.split(" ", 4);
			if (lineParts[0].equals(d.key)) {
				found = true;
			}
		}
		if (!found) {
			file.append(d.toString() + "\n");
		}
		file.close();
		file1.close();

	}
	
	public void writeToPL(LogEntry d){
		this.PL.add(d);
	}
	
	private void writeToDictionary(LogEntry d) throws IOException {

		BufferedWriter file = null;
		BufferedReader file1 = null;
		boolean found = false;
		String thisLine;
		String[] lineParts = new String[4];

		file = new BufferedWriter(new FileWriter(this.dicFile, true));
		file1 = new BufferedReader(new FileReader(this.dicFile));
		while ((thisLine = file1.readLine()) != null) {
			lineParts = thisLine.split(" ", 4);
			if (lineParts[0].equals(d.key)) {
				found = true;
			}
		}
		if (!found) {
			file.append(d.getDictionaryEntry() + "\n");
		}
		file.close();
		file1.close();

	}

	/**
	 * Composes a @see {@link hw2.Message} and sends the message across the
	 * network
	 * 
	 * @param dest
	 *            ID of the destination where the message has to be delivered
	 * @param d
	 *            @see {@link hw2.LogEntry} object to be sent
	 * @param requestSocket
	 *            Socket from where the message should be sent
	 * @throws IOException
	 */
	private void sendMessage(String dest, LogEntry d,
			DatagramSocket requestSocket) throws IOException {

		LinkedList<LogEntry> list = new LinkedList<LogEntry>();
		try {
			list = getDicEntries(dest);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		byte buf[] = new byte[10000];

		try {
			System.out.println("Sending message --> " + d.key + "-" + d.value);

			DatagramPacket datapkt = new DatagramPacket(buf, buf.length,
					InetAddress.getByName(Constants.routerName),
					this.portNum);

			list.add(d);
			Message msgObj = new Message(dest, this.dtt, list);
			msgObj.setSrcPid(this.pid);
			datapkt.setData(msgObj.getBytes());

			requestSocket.send(datapkt);
			// System.out.println("client>" + msg);
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}

	}

	public static void printTimeTable(Integer dtt[][]) {
		System.out.println("Local Time Table");
		for (int i = 0; i < dtt.length; i++) {
			for (int j = 0; j < dtt.length; j++) {
				System.out.print(dtt[i][j] + " ");
			}
			System.out.println("\n");
		}

	}

	private LinkedList<LogEntry> getDicEntries(String dest) throws IOException {
	/*	BufferedReader file = null;
		String thisLine;
		String[] lineParts = new String[4];
		int destId = Integer.parseInt(dest);
		LinkedList<LogEntry> list = new LinkedList<LogEntry>();

		file = new BufferedReader(new FileReader(this.PLfile));
		while ((thisLine = file.readLine()) != null) {
			lineParts = thisLine.split(" ", 4);
			if (this.dtt[destId][Integer.parseInt(lineParts[2])] < Integer
					.parseInt(lineParts[3])) {
				list.add(new LogEntry(lineParts[0], lineParts[1], Integer
						.parseInt(lineParts[2]), Integer.parseInt(lineParts[3])));
			}

		}
		file.close();
		return list; */
		LinkedList<LogEntry> list = new LinkedList<LogEntry>();
		int destId = Integer.parseInt(dest);
		for (int i=0; i< this.PL.size() ; i++){
			LogEntry e = this.PL.get(i);
			if (this.dtt[destId][e.pid] < e.clock) {
			list.add(e);
			}
		}
		return list;
	}

	/**
	 * The Client also recieves messages sent from other clients by running a
	 * separate thread which constantly gets messages.
	 */
	public void run() {
		DatagramSocket thisConnection = null;
		byte[] buf = new byte[100000];
		String[] msgparts = new String[2];

		DatagramPacket recvdpkt = new DatagramPacket(buf, buf.length);

		try {
			thisConnection = new DatagramSocket(Constants.ClientPort);

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
				
				/*for (int i = 0; i < msg.logList.size(); i++) {
					
					LogEntry d = msg.logList.get(i);
					
						writeToDictionary(d);*/
					LinkedList<LogEntry> newPLlist = getCorrectmsgs(msg);	
					
					updateDic(newPLlist);
					updateDtt(msg);
					/*if(thisNodeDoesNotContain(d) || allNodesDoNotContain(d)){
						writeToPL(d);
					}*/
					updatePL(msg);

					//System.out.println("Key : " + d.key + "-" + "Value : "
						//s	+ d.value);
				
			} catch (IOException e) {

				e.printStackTrace();
			}

		}
	}

	private void updatePL(Message msg) throws IOException {
		
		/*BufferedReader file = null;
		LinkedList<LogEntry> fromPL = getFromFile();
		/*(this.PLfile).delete();
		this.PLfile = new File(this.PLfilename);*/
		
		/*while(!fromPL.isEmpty()){
			writeToPL(fromPL.remove());
		}*/
		LinkedList<LogEntry> rm = new LinkedList<LogEntry>();
		
		System.out.println("Messages in PL");
		for(int i=0; i< this.PL.size(); i++){
			LogEntry d = this.PL.get(i);
			System.out.println("msg"+ i+" "+d.toString());
		}
		
		System.out.println("PL size "+this.PL.size());
		boolean write = false;
		for(int i=0; i< this.PL.size(); i++){
			System.out.println("Checking message "+i);
			LogEntry d = this.PL.get(i);
			for(int j=0; j < Constants.NO_OF_NODES; j++){
			if(this.dtt[j][d.pid] < d.clock){
				write = true;
				break;
			}
		}
			System.out.println("write: "+write);
			if(!write){
				rm.add(d);
				System.out.println("To be Removed msg "+d.toString());
			}
			
			
	}
		while(!rm.isEmpty()){
			
			LogEntry r = rm.remove();
			System.out.println("Index to be removed "+r.toString());
			this.PL.remove(r);
			
			System.out.println("Removed msg "+r.toString());
		}
		
		boolean write2 = false;
		for (int k=0; k< msg.logList.size();k++){
			LogEntry d = msg.logList.get(k);
			for(int i=0; i < Constants.NO_OF_NODES; i++){
			System.out.println("Checking row "+i);
			if(this.dtt[i][d.pid] < d.clock){
				write2 = true;
				break;
			}
		}
			System.out.println("write 2: "+write2);
			if(write2){
				writeToPL(d);
			}
	}
		
	}

	public LinkedList<LogEntry> getFromFile()
			throws FileNotFoundException, IOException {
		boolean write = false;
		BufferedReader file;
		file = new BufferedReader(new FileReader(this.PLfile));
		
		LinkedList<LogEntry> fromPL = new LinkedList<LogEntry>();
		
		String thisLine;
		while ((thisLine = file.readLine()) != null) {
			System.out.println(thisLine);
			String[] lineParts = thisLine.split(" ", 4);
			
			for(int i=0;i < this.dtt.length;i++){
				if(this.dtt[i][Integer.parseInt(lineParts[2])] < Integer.parseInt(lineParts[3])){
				  write = true;
				  
				}
			}
				if(write){
					LogEntry l = new LogEntry(lineParts[0],lineParts[1],Integer.parseInt(lineParts[2]),Integer.parseInt(lineParts[3]));
					fromPL.add(l);
				}
			
		}
		file.close();
		return fromPL;
	}

	private void updateDic(LinkedList<LogEntry> newPLlist) throws IOException {
		while(!newPLlist.isEmpty()){
			
			LogEntry d = newPLlist.remove();
			System.out.println("recvd key: "+d.key+"  value: "+d.value);
			writeToDictionary(d);
		}
		
	}

	private LinkedList<LogEntry> getCorrectmsgs(Message msg) {
		LinkedList<LogEntry> correctList = new LinkedList<LogEntry>();
		for(int i=0; i< msg.logList.size();i++) {
			LogEntry d = msg.logList.get(i);
			if(this.dtt[this.pid][d.pid] < d.clock){
				correctList.add(d);
			}
		}
		return correctList;
	}


	
	private boolean allNodesDoNotContain(LogEntry d) {
		boolean doesNothaveRec = false;
		for(int i=0;i < this.dtt.length;i++){
			if(this.dtt[i][d.pid] < d.clock){
				doesNothaveRec = true;
				break;
			}
		}
		return doesNothaveRec;
	}

	private boolean thisNodeDoesNotContain(LogEntry d) {
		if(this.dtt[this.pid][d.pid] < d.clock)
			return true;
		else
			return false;
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

	public static void main(String args[]) throws IOException {

		int pid = Integer.parseInt(args[0]);
		int ans = 1;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		Client client = new Client(pid, Constants.RouterSendPort);

		while (true) {
			System.out.println("\n The available clients are: 0, 1, 2, 3, 4");
			// System.out.println("The File Server is : FS");
			System.out.println("Choose one of the following");
			System.out.println("1: Send a message");
			System.out.println("2: Display the local time table");
			System.out.println("3: List keys in the local Dictionary");
			System.out.println("4: Get the value for a given key");
			System.out.println("5: Print the log");
			System.out.println("6: Exit the Client");
			try {
				ans = Integer.parseInt(br.readLine());
			} catch (IOException e1) {

				e1.printStackTrace();
			}

			switch (ans) {
			case 1:
				client.request();
				break;

			case 2:
				printTimeTable(client.dtt);
				break;

			case 3:
				listKeys(client);
				break;

			case 4:
				System.out.println("Enter the key: ");
				String key = br.readLine();
				getValue(key, client);
				break;

			case 5:
				System.out.println("Print the partial Log");
				printPL(client);
				break;
			
			case 6:
				System.out.println("You are going to exit!!");
				System.exit(0);

			default:
				client.request();
				break;

			}
		}
	}

	private static void printPL(Client client) {
		for ( int i=0; i < client.PL.size(); i++){
			LogEntry e = client.PL.get(i);
			System.out.println(e.toString());
		}
		
	}

	private static void getValue(String key, Client client) throws IOException {
		BufferedReader file1;
		String thisLine;
		String[] lineParts = new String[4];

		file1 = new BufferedReader(new FileReader(client.PLfile));
		while ((thisLine = file1.readLine()) != null) {
			lineParts = thisLine.split(" ", 4);
			if (lineParts[0].equals(key)) {
				System.out.println("Key : " + key + " Value: " + lineParts[1]);
			}
		}
	}

	private static void listKeys(Client client) throws IOException {

		BufferedReader file1;
		String thisLine;
		String[] lineParts = new String[4];

		file1 = new BufferedReader(new FileReader(client.dicFile));
		System.out.println("List of Keys:");
		while ((thisLine = file1.readLine()) != null) {
			lineParts = thisLine.split(" ", 4);
			System.out.println(lineParts[0]);

		}

	}
}
