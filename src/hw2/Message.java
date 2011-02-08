package hw2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;

public class Message implements Serializable{

	public double[][] dtt;
	String destID;
	String srcID;
	public LinkedList<DicEntry> dicList = new LinkedList<DicEntry>();
	
	public Message(String dest, double[][] timetable, LinkedList<DicEntry> list){
		this.dtt = timetable;
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
	
}
