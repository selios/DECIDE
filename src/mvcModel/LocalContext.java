package mvcModel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.jena.vocabulary.RDFS;

public class LocalContext implements Serializable
{	

	private static final long serialVersionUID = 1L;
	public String cl;
	public Boolean max;
	public HashMap<String, LinkedList<String>> outOP;
	public HashMap<String, LinkedList<String>> outDP;
	public HashMap<String, LinkedList<String>> inOP;
	public Boolean type; // true = local context out;   false = local context in

	public LocalContext(String className, Boolean type)
	{
		this.cl = className;
		this.max = false;
		this.type = type;
		if(type == true)
		{
			outOP = new HashMap<String, LinkedList<String>>();
			outDP = new HashMap<String, LinkedList<String>>();
		}
		else
		{
			inOP = new HashMap<String, LinkedList<String>>();

		}
	}

	public void setAsMax()
	{
		this.max = true;	
	}

	public void addProperty(String Property, String DomainOrRange)
	{
		if(this.type == true)
		{
			if(DomainOrRange.equals(RDFS.Literal.toString()))
			{
				addToHashmap(outDP, Property, DomainOrRange);
			}
			else
			{
				addToHashmap(outOP, Property, DomainOrRange);
			}
		}
		else
		{
			addToHashmap(inOP, Property, DomainOrRange);
		}
	}

	public void addToHashmap(HashMap<String, LinkedList<String>> hmp, String key, String value)
	{
		if(hmp.containsKey(key))
		{
			if(!hmp.get(key).contains(value))
			{
				hmp.get(key).add(value);
			}
		}
		else
		{
			hmp.put(key, new LinkedList<String>());
			hmp.get(key).add(value);
		}
	}

	public Boolean checkHashmap(HashMap<String, LinkedList<String>> hmp, String key, String value)
	{
		if(hmp.containsKey(key))
		{
			if(hmp.get(key).contains(value) || value.equals("ANY"))
			{
				return true;
			}
		}
		return false;
	}


	public Boolean removeKeyfromHashMap(HashMap<String, LinkedList<String>> hmp, String key)
	{
		if(hmp.containsKey(key))
		{
			hmp.remove(key);
			return true;
		}
		return false;
	}

	public Boolean removeValuefromHashMap(HashMap<String, LinkedList<String>> hmp, String key, String value)
	{
		if(checkHashmap(hmp, key, value) == true)
		{
			if(value.equals("ANY"))
			{
				hmp.remove(key);
			}
			else
			{
				hmp.get(key).remove(value);
			}
			return true;
		}
		return false;		
	}


	public Boolean containsPropertyOut(String Property, String Range)
	{
		if(checkHashmap(outDP, Property, Range) == true)
		{
			return true;
		}
		else
		{
			if(checkHashmap(outOP, Property, Range) == true)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
	}

	public Boolean containsPropertyIn(String Property, String Range)
	{
		if(checkHashmap(inOP, Property, Range) == true)
		{
			return true;
		}
		return false;
	}



	public Boolean removeDomainOfProperty(String Property, String Domain)
	{
		return removeValuefromHashMap(outDP, Property, Domain);
	}


	public Boolean removeRangeOfProperty(String Property, String Domain)
	{
		Boolean keyRemoved;
		keyRemoved = removeValuefromHashMap(outDP, Property, Domain);
		if(keyRemoved == false)
		{
			keyRemoved = removeValuefromHashMap(outOP, Property, Domain);
		}
		return keyRemoved;
	}

	public void clearAllRanges()
	{
		outDP.clear();
		outOP.clear();
	}

	public void clearAllDomains()
	{
		inOP.clear();
	}

	public Boolean checkIfContainsAxiom(Pattern a)
	{
		if(a.subject.equals(this.cl))
		{
			if(outOP.containsKey(a.predicate))
			{
				if(outOP.get(a.predicate).contains(a.object))
				{
					return true;
				}
			}
		}	
		return false;
	}


	public Boolean removeAxiom(Pattern a)
	{
		if(a.subject.equals(this.cl))
		{
			if(outOP.containsKey(a.predicate))
			{
				if(outOP.get(a.predicate).contains(a.object))
				{					
					if(outOP.get(a.predicate).size()>1)
					{
						outOP.remove(a.predicate).remove(a.object);
					}
					else
					{
						outOP.remove(a.predicate);
					}
					return true;
				}
			}
		}	
		return false;
	}


	public Boolean checkIfMoreSpecificThan(LocalContext lc1)
	{		
		if(this.cl.equals(lc1.cl))
		{		
			if(this.type.equals(lc1.type) && this.type == true)
			{
				if(this.max == true)
				{
					return true;
				}
				if(lc1.max ==true)
				{
					return false;
				}
				if(this.outDP.size() < lc1.outDP.size() || this.outOP.size() < lc1.outOP.size())
				{
					return false;
				}
				for(String dp: lc1.outDP.keySet())
				{
					if(!this.outDP.containsKey(dp))
					{
						return false;
					}
				}
				for(String op: lc1.outOP.keySet())
				{
					if(!this.outOP.containsKey(op))
					{
						return false;
					}
					else
					{
						if(!this.outOP.get(op).containsAll(lc1.outOP.get(op)))
						{
							return false;
						}
					}
				}
				return true;
			}
			else
			{
				if(this.type.equals(lc1.type) && this.type == false)
				{
					if(this.max == true)
					{
						return true;
					}
					if(lc1.max == true)
					{
						return false;
					}
					if(this.inOP.size() < lc1.inOP.size())
					{
						return false;
					}
					for(String op: lc1.inOP.keySet())
					{
						if(!this.inOP.containsKey(op))
						{
							return false;
						}
						else
						{
							if(!this.inOP.get(op).containsAll(lc1.inOP.get(op)))
							{
								return false;
							}
						}
					}
					return true;
				}
			}
		}
		return false;
	}


	public Boolean checkIfEqualTo(LocalContext lc1)
	{	
		if(lc1!=null)
		{
			if(this.cl.equals(lc1.cl))
			{		
				if(this.type.equals(lc1.type) && this.type == true)
				{
					if(!this.max == lc1.max)
					{
						return false;
					}
					else
					{
						if(!this.max == true)
						{
							if(this.outDP.size() != lc1.outDP.size() || this.outOP.size() != lc1.outOP.size())
							{
								return false;
							}
							for(String dp: lc1.outDP.keySet())
							{
								if(!this.outDP.containsKey(dp))
								{
									return false;
								}
							}
							for(String op: lc1.outOP.keySet())
							{
								if(!this.outOP.containsKey(op))
								{
									return false;
								}
								else
								{
									if(this.outOP.get(op).size() != lc1.outOP.get(op).size())
									{
										return false;
									}
									else
									{
										if(!this.outOP.get(op).containsAll(lc1.outOP.get(op)))
										{
											return false;
										}
									}							
								}
							}		
						}
						return true;
					}			
				}
				else
				{
					if(this.type.equals(lc1.type) && this.type == false)
					{
						if(this.max == lc1.max)
						{
							return false;
						}
						else
						{
							if(this.max == true)
							{
								if(this.inOP.size() != lc1.inOP.size())
								{
									return false;
								}
								for(String op: lc1.inOP.keySet())
								{
									if(!this.inOP.containsKey(op))
									{
										return false;
									}
									else
									{
										if(this.inOP.get(op).size() != lc1.inOP.get(op).size())
										{
											return false;
										}
										else
										{
											if(!this.inOP.get(op).containsAll(lc1.inOP.get(op)))
											{
												return false;
											}
										}							
									}
								}		
							}
							return true;
						}			
					}
				}
			}
		}
		return false;
	}



	public void outputLocalContextAsPatterns()
	{
		int counter= 1;
		Boolean empty = true;
		if(this.max == true)
		{
			System.out.println("Max) " + this.cl + " | http://www.w3.org/2002/07/owl#sameAs | " + this.cl);
		}
		else
		{
			if(this.type == true)
			{
				System.out.println("LC_out(" + cl + "):");
				for(String key : outDP.keySet())
				{
					empty = false;
					for(String value : outDP.get(key))
					{
						System.out.println(counter+") " + this.cl + " | " + key + " | " + value);
						counter++;
					}
				}
				for(String key : outOP.keySet())
				{
					empty = false;
					for(String value : outOP.get(key))
					{
						System.out.println(counter+") " + this.cl + " | " + key + " | " + value);
						counter++;
					}
				}	
				if(empty == false)
					System.out.println("");
			}
			else
			{
				System.out.println("LC_in(" + cl + "):");
				for(String key : inOP.keySet())
				{
					empty = false;
					for(String value : inOP.get(key))
					{
						System.out.println(counter+") " + value + " | " + key + " | " + this.cl);
						counter++;
					}
				}
				if(empty == false)
					System.out.println("");
			}	
		}	
	}


	public void outputLocalContextAsAxioms()
	{
		int counter= 1;
		Boolean empty = true;
		if(this.max == true)
		{
			System.out.println("Max) " + this.cl + " | http://www.w3.org/2002/07/owl#sameAs | " + this.cl);
		}
		else
		{
			if(this.type == true)
			{
				System.out.println("LC_out(" + cl + "):");
				for(String key : outDP.keySet())
				{
					empty = false;
					for(String value : outDP.get(key))
					{
						System.out.println(counter+") <" + key +"> <" + RDFS.domain +"> <" + this.cl +">" );
						counter++;
						System.out.println(counter+") <" + key +"> <" + RDFS.range +"> <" + value +">" );
						counter++;
					}
				}
				for(String key : outOP.keySet())
				{
					empty = false;
					for(String value : outOP.get(key))
					{
						System.out.println(counter+") <" + key +"> <" + RDFS.domain +"> <" + this.cl +">" );
						counter++;
						System.out.println(counter+") <" + key +"> <" + RDFS.range +"> <" + value +">" );
						counter++;
					}
				}
				if(empty == false)
					System.out.println("");
			}
			else
			{
				System.out.println("LC_in(" + cl + "):");
				for(String key : inOP.keySet())
				{		
					empty = false;
					for(String value : inOP.get(key))
					{
						System.out.println(counter+") <" + key +"> <" + RDFS.domain +"> <" + value +">" );
						counter++;
						System.out.println(counter+") <" + key +"> <" + RDFS.range +"> <" + this.cl +">" );
						counter++;
					}
				}
				if(empty == false)
					System.out.println("");
			}
		}
	}
	
/*	public String generateID()
	{
		if(max == true)
		{
			return Integer.toString((this.cl + "MAX").hashCode());
		}
		else
		{
			if(type == true)
			{
				for(String prop : this.outDP.keySet())
				{
					
				}
			}
			else
			{
				
			}
		}
		return null;
	}


	public String getIDofProperty(String property, LinkedList<String> ranges, Boolean InorOut)
	{
		return property+InorOut+Integer.toString(ranges.hashCode()).hashCode();
	}
*/
	
	
	
	/*	public void outputLocalContext()
	{
		if(!this.axioms.isEmpty())
		{
			int counter = 1;
			String t = "Out";
			if(this.type == false)
			{
				t = "In";
			}
			System.out.println("LC_" + t + "(" + cl + "):");
			for(String thisKey: axioms.keySet())
			{		
				Axiom thisAxiom = axioms.get(thisKey); 
				System.out.println("   Axiom " + counter);
				System.out.println("   " + thisAxiom.getSubject());
				System.out.println("   " + thisAxiom.getPredicate());				
				System.out.println("   " + thisAxiom.getObject());
				//System.out.println("  " + thisAxiom.getPredicate() + "(" + thisAxiom.getSubject() + ", " + thisAxiom.getObject() + ")");
				counter++;
			}
		}	
		System.out.println("");
	}*/






	/*private static final long serialVersionUID = 1L;
	String cl;
	List<Axiom> axioms;
	Boolean type; // true = local context out;   false = local context in

	public LocalContext(String className, Boolean type)
	{
		this.cl = className;
		axioms = new LinkedList<Axiom>();
		this.type = type;
	}

	public void addAxiom(String subject, String predicate, String object)
	{
		if(this.containsAxiom(subject, predicate, object) == false)
		{
			axioms.add(new Axiom(subject, predicate, object));
		}
	}


	public Boolean containsAxiom(String subject, String predicate, String object)
	{
		for(Axiom thisAxiom: axioms)
		{
			if(thisAxiom.getSubject() == subject)
			{
				if(thisAxiom.getObject() == predicate)
				{
					if(thisAxiom.getPredicate() == object)
					{
						return true;
					}
				}
			}		
		}
		return false;
	}


	public void outputLocalContext()
	{
		if(!this.axioms.isEmpty())
		{
			int counter = 1;
			String t = "Out";
			if(this.type == false)
			{
				t = "In";
			}
			System.out.println("LC_" + t + "(" + cl + "):");
			for(Axiom thisAxiom: axioms)
			{		
				System.out.println("   Axiom " + counter);
				System.out.println("   " + thisAxiom.getPredicate());
				System.out.println("   " + thisAxiom.getSubject());
				System.out.println("   " + thisAxiom.getObject());
				//System.out.println("  " + thisAxiom.getPredicate() + "(" + thisAxiom.getSubject() + ", " + thisAxiom.getObject() + ")");
				counter++;
			}
		}	
		System.out.println("");
	}
	 */

}