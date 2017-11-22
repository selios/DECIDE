package mvcModel;

import java.util.HashMap;

public class IdentityGraph 
{
	public String id;
	public Node nodeRoot;
	public HashMap<String, Node> nodes;
	public HashMap<String, Edge> edges;
	public HashMap<String, Edge> edgesCl;
	public Edge causeEdge;
	
	public IdentityGraph(Node nodeRoot, String id)
	{
		this.id = id;
		this.nodeRoot = nodeRoot;
		nodes = new HashMap<String, Node>();
		edges = new HashMap<String, Edge>();
		edgesCl = new HashMap<String, Edge>();
		addNode(nodeRoot);
		causeEdge = null;
	}
	
	
	public IdentityGraph(IdentityGraph IG)
	{
		this.nodeRoot = IG.nodeRoot;
		nodes = new HashMap<String, Node>();
		nodes.putAll(IG.nodes);
		edges = new HashMap<String, Edge>();
		edges.putAll(IG.edges);
		edgesCl = new HashMap<String, Edge>();
		edgesCl.putAll(IG.edgesCl);
		causeEdge = null;
	}
	
	public void setCauseEdge(Edge e)
	{
		causeEdge = e;
	}
		
	public void addNode(Node n)
	{
		if(!nodes.containsKey(n.nodeID))
		{
			nodes.put(n.nodeID, n);
		}
	}
	
	public void addEdge(Edge e)
	{
		if(!edges.containsKey(e.edgeID))
		{
			edges.put(e.edgeID, e);
			e.one.addEdge(e);
			e.two.addEdge(e);
			edgesCl.put(e.nodeToClassID, e);
			//System.out.println("### IG " + this.id + " ### " + e.one.cl + "------(" + e.label + ")-----" + e.two.cl);
		}
	}
	
	public Boolean containsEdge(Edge e)
	{
		if(edges.containsKey(e.edgeID))
		{
			return true;
		}
		return false;
	}
	
	public Boolean checkInEdges(Node n1, Node n2, String Property)
	{
		Edge e = new Edge(n1, n2, Property);
		if(edges.containsKey(e.edgeID))
		{
			return true;
		}
		return false;
	}
	
	public Node checkInEdgesCl(Node n1, String cl, String Property)
	{
		Node n2 = new Node();
		n2.cl = cl;
		Edge e = new Edge(n1, n2, Property);
		String x = e.nodeToClassID();
		if(edgesCl.containsKey(x))
		{
			if(edgesCl.get(x).one.nodeID .equals(n1.nodeID))
			{
				return edgesCl.get(x).two;
			}
			else
			{
				return edgesCl.get(x).one;
			}
			
		}
		return null;
	}
	
	public void chechGraphPath(Node n)
	{
		//for()
	}
	
}
