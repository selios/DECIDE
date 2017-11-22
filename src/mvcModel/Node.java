package mvcModel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.TreeMap;

import org.apache.jena.rdf.model.Resource;

public class Node implements Serializable
{	
	private static final long serialVersionUID = 1L;
	public String nodeID;
	public TreeMap<String, Pair> pairs;
	public String cl;
	public HashMap<String, Edge> neighborhood;

	public Node(String e1, String e2)
	{
		this.pairs = new TreeMap<String, Pair>();
		this.neighborhood = new HashMap<String, Edge>();
		Pair p = new Pair(e1, e2);
		this.pairs.put(p.pairID, p);
		this.nodeID = getID(this.pairs);
	}

	public Node(Resource e1, Resource e2)
	{
		this.pairs = new TreeMap<String, Pair>();
		this.neighborhood = new HashMap<String, Edge>();
		Pair p = new Pair(e1, e2);
		this.pairs.put(p.pairID, p);
		this.nodeID = getID(this.pairs);
	}

	public Node(Pair p)
	{
		this.pairs = new TreeMap<String, Pair>();
		this.neighborhood = new HashMap<String, Edge>();
		this.pairs.put(p.pairID, p);
		this.nodeID = getID(this.pairs);
	}


	public Node(TreeMap<String, Pair> listOfPairs)
	{
		this.pairs = new TreeMap<String, Pair>();
		this.neighborhood = new HashMap<String, Edge>();
		this.pairs.putAll(listOfPairs);
		this.nodeID = getID(this.pairs);
	}

	public Node(Node n)
	{
		this.cl = n.cl;
		this.pairs = new TreeMap<String, Pair>();
		this.neighborhood = new HashMap<String, Edge>();
		this.neighborhood.putAll(n.neighborhood);
		this.pairs.putAll(n.pairs);
		this.nodeID = getID(this.pairs);
	}

	public Node()
	{

	}


	public String getID(Pair p)
	{
		return Integer.toString((p.resource1+p.resource2).hashCode());
	}

	public String getID(TreeMap<String, Pair> pairs)
	{
		//System.out.println("Keyset :" + Integer.toString(pairs.keySet().hashCode()));
		if(pairs.size()==1)
		{
			return Integer.toString(pairs.keySet().hashCode());
		}
		else
		{
			String result = "";

			for(String k : pairs.keySet())
			{
				//System.out.println(i +") " + Integer.toString(k.hashCode()));
				result = result+Integer.toString(k.hashCode());
			}
			//System.out.println("Result :" + result.hashCode());			
			return Integer.toString(result.hashCode());
		}	
	}


	public Pair getFirstPair()
	{
		return this.pairs.firstEntry().getValue();
	}

	public void outputNodePairs()
	{
		System.out.println("Node(" + this.cl+ ") " + this.nodeID + ": ");
		for(String p : this.pairs.keySet())
		{
			System.out.print("		"); this.pairs.get(p).outputPair();
		}
	}

	public Boolean addEdge(Edge e)
	{
		if(!neighborhood.containsKey(e.edgeID))
		{
			neighborhood.put(e.edgeID, e);
			return true;
		}
		return false;
	}


	public Node checkRelation(String Property, String Cl)
	{
		for(String eID : this.neighborhood.keySet())
		{
			if(this.neighborhood.get(eID).returnOtherNode(this).cl.equals(Cl))
			{
				return this.neighborhood.get(eID).returnOtherNode(this);
			}
		}
		return null;
	}


	public Boolean containsPairs(Node n)
	{
		if(pairs.keySet().containsAll(n.pairs.keySet()))
		{
			return true;
		}
		return false;
	}

}




/*package mvcModel;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.apache.jena.rdf.model.Resource;

public class Node implements Serializable
{	
	private static final long serialVersionUID = 1L;
	public int pairID;
	public String resource1;
	public String resource2;
	public String cl;
	public List<Edge> neighborhood;

	public Node(String e1, String e2)
	{
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
		this.neighborhood = new LinkedList<Edge>();
	}


	public Node(Resource e1, Resource e2)
	{
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
		this.neighborhood = new LinkedList<Edge>();
	}


	public int getID()
	{
		return (this.resource1+this.resource2).hashCode();
	}


}
 */