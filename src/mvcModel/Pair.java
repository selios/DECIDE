package mvcModel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.jena.rdf.model.Resource;

public class Pair implements Serializable
{	
//	private static final long serialVersionUID = 1L;
	public String pairID;
	public String resource1;
	public String resource2;
	public HashMap<String, HashMap<String, LinkedList<Node>>> outOP;
	public LinkedList<String> outDP;
	public HashMap<String, HashMap<String, LinkedList<Node>>> inOP;
	

	public Pair(String e1, String e2)
	{
		outOP = new HashMap<>();
		outDP = new LinkedList<>();
		inOP = new HashMap<>();
		if(e1.toString().compareTo(e2.toString()) <= 0)
		{
			this.resource1 = e1;
			this.resource2 = e2;
		}
		else
		{
			this.resource1 = e2;
			this.resource2 = e1;
		}
		this.pairID = getID();
	}


	public Pair(Resource e1, Resource e2)
	{
		outOP = new HashMap<>();
		outDP = new LinkedList<>();
		inOP = new HashMap<>();
		if(e1.toString().compareTo(e2.toString()) <= 0)
		{
			this.resource1 = e1.toString();
			this.resource2 = e2.toString();
		}
		else
		{
			this.resource1 = e2.toString();
			this.resource2 = e1.toString();
		}
		this.pairID = getID();
	}
	
	public void addOPout(String Property, String Range, Node n)
	{
		if(!outOP.containsKey(Property))
		{
			HashMap<String, LinkedList<Node>> hmp = new HashMap<>();
			hmp.put(Range, new LinkedList<>());
			hmp.get(Range).add(n);
			outOP.put(Property, hmp);
		}
		else
		{
			if(outOP.get(Property).containsKey(Range))
			{
				outOP.get(Property).get(Range).add(n);
			}
			else
			{
				LinkedList<Node> pairs = new LinkedList<>();
				pairs.add(n);
				outOP.get(Property).put(Range, pairs);
			}
		}
	}
	
	public void addOPout(String Property, String Range, LinkedList<Node> listOfNodes)
	{
		if(!outOP.containsKey(Property))
		{
			HashMap<String, LinkedList<Node>> hmp = new HashMap<>();
			hmp.put(Range, new LinkedList<>());
			hmp.get(Range).addAll(listOfNodes);
			outOP.put(Property, hmp);
		}
		else
		{
			if(outOP.get(Property).containsKey(Range))
			{
				outOP.get(Property).get(Range).addAll(listOfNodes);
			}
			else
			{
				LinkedList<Node> pairs = new LinkedList<>();
				pairs.addAll(listOfNodes);
				outOP.get(Property).put(Range, pairs);
			}
		}
	}
	
	public void addDPout(String Property)
	{
		if(!outDP.contains(Property))
		{
			outDP.add(Property);
		}
	}
	
	public void addOPin(String Property, String Range, Node n)
	{
		if(!inOP.containsKey(Property))
		{
			HashMap<String, LinkedList<Node>> hmp = new HashMap<>();
			hmp.put(Range, new LinkedList<>());
			hmp.get(Range).add(n);
			inOP.put(Property, hmp);
		}
		else
		{
			if(inOP.get(Property).containsKey(Range))
			{
				inOP.get(Property).get(Range).add(n);
			}
			else
			{
				LinkedList<Node> pairs = new LinkedList<>();
				pairs.add(n);
				inOP.get(Property).put(Range, pairs);
			}
		}
	}
	
	public void addOPin(String Property, String Range, LinkedList<Node> listOfNodes)
	{
		if(!inOP.containsKey(Property))
		{
			HashMap<String, LinkedList<Node>> hmp = new HashMap<>();
			hmp.put(Range, new LinkedList<>());
			hmp.get(Range).addAll(listOfNodes);
			inOP.put(Property, hmp);
		}
		else
		{
			if(inOP.get(Property).containsKey(Range))
			{
				inOP.get(Property).get(Range).addAll(listOfNodes);
			}
			else
			{
				LinkedList<Node> pairs = new LinkedList<>();
				pairs.addAll(listOfNodes);
				inOP.get(Property).put(Range, pairs);
			}
		}
	}
	
	public void outputPair()
	{
		System.out.println("Pair " + this.pairID + ": " + this.resource1 + " <-> " +this.resource2);
	}
	
	
	public String getID()
	{
		return Integer.toString((this.resource1+this.resource2).hashCode());
	}
	
}
