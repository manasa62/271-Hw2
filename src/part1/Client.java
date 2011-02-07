package part1;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
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

public class Client implements Runnable {

	private int portNum;

	public Client(int portNum) {
		this.portNum = portNum;
		new Thread(this).start();
	}

	public void request() {

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

		while (!msg.equals("bye")) {
			System.out
					.println("Enter the message and the destination to send in the format <Destination Name>:<message payload>");
			System.out.print(">>>");
			try {
				msg = br.readLine();
			} catch (IOException e1) {
				System.out.println("Failed to read message");
				e1.printStackTrace();
			}

			sendMessage(msg, requestSocket);
		}
		// sendMessage(msg, requestSocket);

	}

	private void sendMessage(String msg, DatagramSocket requestSocket) {
		{

			byte buf[] = new byte[10000];
			try {
				System.out.println("Sending message --> " + msg);

				DatagramPacket datapkt = new DatagramPacket(buf, buf.length,
						InetAddress.getByName(GFSConstants.routerName),
						this.portNum);
				datapkt.setData(msg.getBytes());

				requestSocket.send(datapkt);
				System.out.println("client>" + msg);
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}

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
				String msg = new String(recvdpkt.getData());

				msgparts = msg.split(":", 2);
				System.out.println("Message content is: " + msgparts[0]);
				System.out.println("Packet received from host " + msgparts[1]);

			} catch (IOException e) {

				e.printStackTrace();
			}

		}

	}

	public static void main(String args[]) {
		Client client = new Client(GFSConstants.RouterSendPort);

		System.out.println("The available clients are: C1, C2, C3, C4, C5");
		System.out.println("The File Server is : FS");

		client.request();
	}

}
