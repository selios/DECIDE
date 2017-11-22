package mvcModel;
import java.io.Serializable;


public class Pattern implements Serializable 
{
	private static final long serialVersionUID = 1L;
	public String id;
	public String subject;
	String predicate;
	String object;

	public Pattern(String subject, String predicate, String object)
	{
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
		this.id = getID();
	}
	
	
	public Pattern()
	{

	}

	public String getSubject()
	{
		return this.subject;
	}

	public String getPredicate()
	{
		return this.predicate;
	}

	public String getObject()
	{
		return this.object;
	}

	public String getID()
	{
		return Integer.toString((this.subject+this.predicate+this.object).hashCode());
	}
	
	
	
/*	// Serialize an object to a byte array
	public static byte[] serializeObject(Object obj)
	{
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bout);
			out.writeObject(obj);
			out.close();
			return bout.toByteArray();
			String result = new String(bout.toByteArray());
				//System.out.println("Data Serialized");
				return result.getBytes();
		}catch(IOException i) 
		{
			i.printStackTrace();
			return null;
		}
	}


	public String hashPattern() 
	{
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] serializedList = serializeObject(this.subject+this.predicate+this.object);
		md.update(serializedList);
		byte byteData[] = md.digest();
		//convert the byte to hex format method 
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < byteData.length; i++) 
		{
			sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
		}
		//System.out.println("Digest(in hex format):: " + sb.toString());
		return sb.toString();
	}*/

}
