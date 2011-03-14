/** @author Manasa Chandrashekar
 * 			DivyaShree Hassan Ravindrakumar
 */

package hw2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;
/**
 * 
 * Representation of a Message
 * dtt - 2 dimensional time table of the client that is sending the message
 * destID - destination ID where the message is destined to
 * srcHostName - HostName of the message sender
 * srcID - ID of the message sender
 * dicList - The list of dictionary entries that are being sent across
 *
 */
public class Message implements Serializable{

	public Integer[][] dtt;
	String destID;
	String srcHostName;
	int srcID;
	public LinkedList<LogEntry> logList = new LinkedList<LogEntry>();
	
	public Message(String dest, Integer[][] dtt2, LinkedList<LogEntry> list){
		this.dtt = dtt2;
		this.logList = list;
		this.destID = dest;
	}
	
	public byte[] getBytes() throws java.io.IOException{
	      ByteArrayOutputStream bos = new ByteArrayOutputStream();
	      ObjectOutputStream oos = new ObjectOutputStream(bos);
	      oos.writeObject(this);
	      oos.flush();
	      oos.close();
	      bos.close();
	      byte [] data = bos.toByteArray();
	      return data;
	  }

	public void setSrcPid(int pid) {
		this.srcID = pid;
	}
	
	public int getPid() {
		return this.srcID;
	}
	
	
}
