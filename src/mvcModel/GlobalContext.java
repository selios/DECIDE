package mvcModel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.vocabulary.RDFS;

public class GlobalContext implements Serializable
{	
	private static final long serialVersionUID = 1L;
	String id;
	String cl;
	HashMap<String, LocalContext> localContexts;
	HashMap<String, LinkedList<String>> PropertyToDomains;
	HashMap<String, LinkedList<String>> PropertyToRanges;
	TreeMap<String, Pattern> axioms;


	public GlobalContext(String className)
	{
		this.id = null;
		this.cl = className;
		this.localContexts = new HashMap<String, LocalContext>();
		this.PropertyToDomains = new HashMap<>();
		this.PropertyToRanges = new HashMap<>();
		this.axioms = new TreeMap<>();
	}


	public Boolean checkVisitedNodes(HashMap<String, LinkedList<Node>> N, Node n)
	{
		if(!N.containsKey(n.cl))
		{
			return false;
		}
		else
		{
			if(N.get(n.cl).contains(n.nodeID))
			{
				return true;
			}
			return false;
		}
	}

	public void addToVisitedNodes(HashMap<String, LinkedList<Node>> N, Node n)
	{
		if(!N.containsKey(n.cl))
		{			
			N.put(n.cl, new LinkedList<>());			
		}
		N.get(n.cl).add(n);
	}

	public void addLocalContext(LocalContext LC)
	{
		if(!this.localContexts.containsKey(LC.cl))
		{
			this.localContexts.put(LC.cl, LC);
		}
	}

	public void replaceLocalContext(LocalContext LC)
	{
		if(!this.localContexts.containsKey(LC.cl))
		{
			this.localContexts.replace(LC.cl, LC);
		}
	}

	public LocalContext getExistingLocalContext(String cl)
	{
		if(!this.localContexts.containsKey(cl))
		{
			return null;
		}
		else
		{
			return this.localContexts.get(cl);
		}
	}


	public Boolean checkIfMoreSpecificThan(GlobalContext GC)
	{		
		if(this.cl.equals(GC.cl))
		{		
			if(this.localContexts.size() < GC.localContexts.size())
			{
				return false;
			}
			for(String c : GC.localContexts.keySet())
			{
				if(!this.localContexts.containsKey(c))
				{
					return false;
				}
				else
				{
					if(!this.localContexts.get(c).checkIfMoreSpecificThan(GC.localContexts.get(c)))
					{
						if(!this.localContexts.get(c).checkIfEqualTo(GC.localContexts.get(c)))
						{
							return false;
						}
					}
				}
			}
			return true;
		}
		return false;
	}

	public Boolean checkIfEqualTo(GlobalContext GC)
	{	
		if(this.id.equals(GC.id))
		{
			return true;
		}
		else
		{
			return false;
		}

		/*if(this.cl.equals(GC.cl))
		{		
			if(this.localContexts.size() != GC.localContexts.size())
			{
				return false;
			}
			for(String c : GC.localContexts.keySet())
			{
				if(!this.localContexts.containsKey(c))
				{
					return false;
				}
				else
				{
					if(!this.localContexts.get(c).checkIfEqualTo(GC.localContexts.get(c)))
					{
						return false;
					}
				}
			}
			return true;
		}
		return false;*/
	}


	public void outputGlobalContextAsPatterns()
	{
		for(String c : localContexts.keySet())
		{
			localContexts.get(c).outputLocalContextAsPatterns();
		}
	}


	public void outputGlobalContextAsAxioms()
	{
		for(String c : localContexts.keySet())
		{
			localContexts.get(c).outputLocalContextAsAxioms();
		}
	}


	public void addPropertyToDomain(String Property, String Domain)
	{
		if(PropertyToDomains.containsKey(Property))
		{
			if(!PropertyToDomains.get(Property).contains(Domain))
			{
				PropertyToDomains.get(Property).add(Domain);
			}
		}
		else
		{
			PropertyToDomains.put(Property, new LinkedList<>());
			PropertyToDomains.get(Property).add(Domain);
		}
	}


	public void addPropertyToRange(String Property, String Range)
	{
		if(PropertyToRanges.containsKey(Property))
		{
			if(!PropertyToRanges.get(Property).contains(Range))
			{
				PropertyToRanges.get(Property).add(Range);
			}
		}
		else
		{
			PropertyToRanges.put(Property, new LinkedList<>());
			PropertyToRanges.get(Property).add(Range);
		}
	}

	public void addLocalContextAxioms(LocalContext LC)
	{
		if(LC.type == true)
		{
			for(String prop : LC.outDP.keySet())
			{
				addPropertyToDomain(prop, LC.cl);
				for(String range : LC.outDP.get(prop))
				{
					addPropertyToRange(prop, range);
				}
			}
			for(String prop : LC.outOP.keySet())
			{
				addPropertyToDomain(prop, LC.cl);
				for(String range : LC.outOP.get(prop))
				{
					addPropertyToRange(prop, range);
				}
			}
		}
	}

	public void addAllLocalContextsAxioms()
	{
		for(String c : localContexts.keySet())
		{
			addLocalContextAxioms(localContexts.get(c));
		}
		for(String prop: PropertyToDomains.keySet())
		{
			for(String domain : PropertyToDomains.get(prop))
			{
				Pattern p = new Pattern(prop, RDFS.domain.getURI(), domain);
				axioms.put(p.id, p);					
			}			
		}
		for(String prop: PropertyToRanges.keySet())
		{
			for(String range : PropertyToRanges.get(prop))
			{
				if(!range.equals(""))
				{
					Pattern p = new Pattern(prop, RDFS.range.getURI(), range);
					axioms.put(p.id, p);	
				}								
			}			
		}
		this.id = Integer.toString(this.axioms.keySet().hashCode());
	}

/*	public void getAxiomsFromFile(ResultSet rs)
	{
		while (rs.hasNext()) 
		{
			QuerySolution thisRow = rs.next();
			try
			{
				String cl = thisRow.get("s").toString();
				LocalContext localC;
				if(this.localContexts.containsKey(cl))
				{
					localC = this.localContexts.get(cl);
					//this.localContexts.get(cl).addProperty(thisRow.get("p").toString(), thisRow.get("o").toString());
				}
				else
				{
					localC = new LocalContext(cl, true);
					this.localContexts.put(cl, localC);
				}
				localC.addProperty(thisRow.get("p").toString(), thisRow.get("o").toString());
			} catch(Exception ex)
			{
				ex.getMessage();
				System.out.println("Error Function: getAxiomsFromFile");
			}
		}
	}*/

	public void getAxiomsFromFile(ResultSet rs)
	{
		while (rs.hasNext()) 
		{
			QuerySolution thisRow = rs.next();
			try
			{
				String cl = thisRow.get("s").toString();
				LocalContext localC;
				if(this.localContexts.containsKey(cl))
				{
					localC = this.localContexts.get(cl);				
				}
				else
				{
					localC = new LocalContext(cl, true);
					this.localContexts.put(cl, localC);
				}
				if(thisRow.get("o") == null)
				{
					localC.addProperty(thisRow.get("p").toString(), "");
				}
				else
				{
					localC.addProperty(thisRow.get("p").toString(), thisRow.get("o").toString());
				}
			} catch(Exception ex)
			{
				ex.getMessage();
				System.out.println("Error Function: getAxiomsFromFile");
			}
		}
	}




}
