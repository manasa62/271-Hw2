package hw2;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class DicEntry implements Serializable
{

	public String key;
	public String value;
	public int pid;
	public int clock;
	
	public DicEntry(String key, String value, int pid, int clock){
		this.key = key;
		this.value = value;
		this.pid = pid;
		this.clock = clock;
	}
	
	public String toString(){
		String res = new String();
		res = this.key+" "+this.value+" "+this.pid+" "+this.clock;
		return res;
		
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
	
}
