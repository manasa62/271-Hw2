/** @author Manasa Chandrashekar
 * 			DivyaShree Hassan Ravindrakumar
 */
package hw2;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * 
 * Representation of a Dictionary Entry which contains
 * key : Key 
 * value: Value 
 * pid: PId of the client that generated the key Value
 * clock : Time at which the entry was generated
 *
 */
public class LogEntry implements Serializable
{

	public String key;
	public String value;
	public int pid;
	public int clock;
	
	public LogEntry(String key, String value, int pid, int clock){
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
	
	public String getDictionaryEntry(){
		String dic = new String();
		dic = this.key+" "+this.value;
		return dic;
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
