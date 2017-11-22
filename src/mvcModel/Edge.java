package mvcModel;

public class Edge 
{

	public String edgeID;
	public String nodeToClassID;
	public String label;
	public Node one, two;

	public Edge(Node n1, Node n2, String label)
	{
		if(n1.cl.compareTo(n2.cl) < 0)
		{
			this.one = n1;
			this.two = n2;
		}
		else
		{
			this.one = n2;
			this.two = n1;
		}
		this.label = label;
		this.edgeID = getID();
		this.nodeToClassID = nodeToClassID();
		//this.nodeToClassID = nodeToClassID(this.one.nodeID, label, n2.cl);
	}
	
	public String getID()
	{
		return Integer.toString((this.one.nodeID + label + this.two.nodeID).hashCode());
	}
	
	public String nodeToClassID()
	{
		return Integer.toString((this.one.cl + label + this.two.cl).hashCode());
		//return (n1 + property + n2Class).hashCode();
	}
	
	
	public Node returnOtherNode(Node n)
	{
		if(this.one.nodeID.equals(n.nodeID))
		{
			return this.two;
		}
		if(this.two.nodeID.equals(n.nodeID))
		{
			return this.one;
		}
		return null;
	}
	
	public Boolean isIdenticalTo(Edge anotherEdge)
	{
		if(anotherEdge != null)
		{
			if(anotherEdge.edgeID.equals(this.edgeID))
			{
				return true;
			}
		}
		return false;
	}

	
}
