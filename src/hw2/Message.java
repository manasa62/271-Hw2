package hw2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;

public class Message implements Serializable{

	public Integer[][] dtt;
	String destID;
	String srcID;
	int srcPid;
	public LinkedList<DicEntry> dicList = new LinkedList<DicEntry>();
	
	public Message(String dest, Integer[][] dtt2, LinkedList<DicEntry> list){
		this.dtt = dtt2;
		this.dicList = list;
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

	public void setPid(int pid) {
		this.srcPid = pid;
	}
	
	public int getPid() {
		return this.srcPid;
	}
	
	
}
