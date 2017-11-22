package mvcModel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.google.common.math.BigIntegerMath;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.ontology.AnnotationProperty;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.resultset.ResultSetMem;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb.TDBLoader;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDFS;
import org.bson.Document;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import javax.print.Doc;


public class Functions 
{
	public String ontoName;
	public static Dataset dataset;
	public String targetClass;
	public Model modelTDB;
	public OntModel model, infModel;
	public static ArrayList<RDFNode> depClasses;
	public static Map<String, String> typesOf;
	public static ArrayList<Resource> tcInstances;
	public ArrayList<String> dataProperties; 
	public ArrayList<String> objectProperties; 
	public HashMap<String, Pattern> UP, NP;
	public HashMap<String, ArrayList<ArrayList<Pattern>>> CP;
	public LinkedList<String> UC;


//	private static RocksDB ROCKS_DB_NodeID_LCout = null;
//	private static RocksDB ROCKS_DB_NodeID_LCin = null;
//	private static RocksDB ROCKS_DB_PairID_LCout = null;
//	private static RocksDB ROCKS_DB_PairID_LCin = null;
//	private static RocksDB ROCKS_DB_PairID_Pout = null;
//	private static RocksDB ROCKS_DB_PairID_Pin = null;


	private static MongoCollection DB_NodeID_LCout = null;
	private static MongoCollection DB_NodeID_LCin = null;
	private static MongoCollection DB_PairID_LCout = null;
	private static MongoCollection DB_PairID_LCin = null;
	private static MongoCollection DB_PairID_Pout = null;
	private static MongoCollection DB_PairID_Pin = null;

	private static MongoClient mongo = null;

	private static Options OPTIONS = new Options().setCreateIfMissing(true);
	public TreeMap<String, GlobalContext> allGlobalContexts;
	public static int numberOfIdentityStatements;
	SPARQLqueries MySPARQLqueries;
//	Output MyOutput;
	public String outputPath;


	public Functions(String endpoint, String output)
	{
		outputPath = output;
		MySPARQLqueries = new SPARQLqueries(endpoint);
//		MyOutput = new Output(output);
		dataProperties = new ArrayList<>();
		objectProperties = new ArrayList<>();
		typesOf = new HashMap<>();
		UP = new HashMap<String, Pattern>();
		NP = new HashMap<String, Pattern>();
		CP = new HashMap<>();
		UC = new LinkedList<>();
		allGlobalContexts = new TreeMap<>();
		numberOfIdentityStatements = 0;
//		deleteRocksDB();
//		RocksDB.loadLibrary();
//		openRocksDB();
		openMongoDB();
	}

	// Get All the Classes with their Object and Data Properties
	public void getDepClasses()
	{
		/*	depClasses = new ArrayList<>();
		depClasses.addAll(MySPARQLqueries.listDepClasses());*/

		depClasses = new ArrayList<>();
		ArrayList<RDFNode> instClasses = MySPARQLqueries.listInstantiatedClasses();
		for(RDFNode o : instClasses)
		{
			if(checkIfUnwantedClass(o.toString()) == false)
			{
				if(!depClasses.contains(o))
				{
					ArrayList<RDFNode> list = MySPARQLqueries.getAllSuperClasses(o.toString());
					list.retainAll(instClasses);
					if(list.isEmpty())
					{
						if(!depClasses.contains(o))
						{
							depClasses.add(o);
						}
					}
					else
					{
						if(!depClasses.containsAll(list))
						{
							depClasses.addAll(list);
						}
					}
				}
			}
		}

		/*	
		depClasses = new ArrayList<>();
		try
		{
			for(RDFNode o : MySPARQLqueries.listInstantiatedClasses())
			{
				if(depClasses.isEmpty())
				{
					depClasses.add(o);
				}
				else
				{
					ArrayList<RDFNode> unwantedClasses = new ArrayList<>();
					for(RDFNode c : depClasses)
					{
						if(MySPARQLqueries.containsTriple(c.toString(), RDFS.subClassOf.getURI(), o.toString()))
						{
							unwantedClasses.add(c);
						}
						if(MySPARQLqueries.containsTriple(o.toString(), RDFS.subClassOf.getURI(), c.toString()))
						{
							unwantedClasses.add(o);
						}
					}
					depClasses.add(o);
					depClasses.removeAll(unwantedClasses);
				}
			}
		}
		catch(Exception ex)
		{
			System.out.println(ex.getMessage());
		}
		System.out.println("asdasd");*/
		/*return depClasses;*/
	}



	// Get all the individuals of the target class
	public void getTcInstances(String targetClass)
	{
		this.targetClass = targetClass;
		Resource tc = ResourceFactory.createResource(this.targetClass);
		if(depClasses.contains(tc))
		{
			tcInstances = new ArrayList<>();
			try
			{
				tcInstances.addAll(MySPARQLqueries.listInstancesOfClass(this.targetClass));
			}
			catch(Exception ex)
			{
				System.out.println(ex.getMessage());
			}
		}
		else
		{
			System.out.println("#### The target class does not exist in the Dep Classes");
		}
	}


	public void decide(String nameProject, List<Resource> tcInstances2)
	{
		long startTime = System.currentTimeMillis();
		Integer nbIdentityGraph = 0;
		Integer nbIdentityGraphCounter = 0;
		Integer nbNodeIndentityGraph = 0;
		Integer nbNodeIndentityGraphCounter = 0;
		List<Resource> tcInstances3 = new ArrayList<>();
		tcInstances3.addAll(tcInstances);

		Output out = new Output(outputPath + nameProject+"/");
		Integer counter = 0;
		int s = 0;
		int numbIndivs = tcInstances3.size();
		Integer numPairs = ((numbIndivs - tcInstances2.size()) * tcInstances2.size()) + BigIntegerMath.binomial(tcInstances2.size(), 2).intValue();
//		Integer numPairs = (numbIndivs * (tcInstances2.size() -1)) / 2;
		System.out.println("Number of possible pairs: " + numPairs);
		for (Resource res1 :tcInstances2)
		{
			tcInstances3.remove(res1);
			for (Resource res2 : tcInstances3)
			{

				//Resource res1 = ResourceFactory.createResource("http://opendata.inra.fr/PO2_Caredas_SIM/caredas_lawrence_process_80_step_448_mixture_174");
				//Resource res2 = ResourceFactory.createResource("http://opendata.inra.fr/PO2_Caredas_SIM/caredas_these_florence_barbe_process_291_step_1525_mixture_464");
				counter++;				
//				System.out.println("-------------------------------------");
//				System.out.print("#Pair " + counter + " / " + numPairs + " : ");
//				Integer prog = 50;
//				System.out.print("\r[");
//				Integer nbD = Double.valueOf(counter.doubleValue()/numPairs.doubleValue() * prog).intValue();
//				for(int i = 0; i < nbD; i++) {
//					System.out.print("=");
//				}
//				for(int i = nbD; i < prog; i++) {
//					System.out.print(" ");
//				}
				if(counter % 100 == 0) {
				Long endTimeTemp = System.currentTimeMillis();
				Long totalTimeMsTemp = (endTimeTemp - startTime) ;
				Double pourcent = Double.valueOf(counter.doubleValue()/numPairs.doubleValue() * 100);

					Long rest =(long)(((numPairs.doubleValue() - counter.doubleValue()) / counter.doubleValue()) * Double.valueOf(totalTimeMsTemp));
					System.out.print("\r" + pourcent + " % " + String.format(" %d min, %d sec",
							TimeUnit.MILLISECONDS.toMinutes(rest),
							TimeUnit.MILLISECONDS.toSeconds(rest) -
									TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(rest))));
				}
//				if(res1.toString().compareTo(res2.toString()) <= 0)
//				{
//					System.out.println(res1 + " <--> " + res2);
//				}
//				else
//				{
//					System.out.println(res2 + " <--> " + res1);
//				}
//				System.out.println("-------------------------------------");

				Node mainNode = new Node(res1, res2);
				mainNode.cl = targetClass;
				LinkedList<GlobalContext> GCset = new LinkedList<>();				
				ArrayList<IdentityGraph> IGset = constructIdentityGraphs(mainNode);	
//				System.out.println("Number of Identity Graphs: " + IGset.size());
				nbIdentityGraph += IGset.size();
				nbIdentityGraphCounter++;
				int iterations = 0;
				for(IdentityGraph IG : IGset)
				{
//					System.out.println("Number of Nodes In Identity Graph: " + IG.nodes.size());
					nbNodeIndentityGraph += IG.nodes.size();
					nbNodeIndentityGraphCounter++;
					HashMap<String, LinkedList<LocalContext>> cN = new HashMap<>(); // cause Nodes
					HashMap<String, HashMap<String, Node>> N = new HashMap<>();
					GlobalContext GC = new GlobalContext(targetClass);
					Queue<LocalContext> LCset = new LinkedList<LocalContext>();	
					Pattern a = new Pattern();
					iterations++;
					GC = generateGC(IG.nodeRoot, a, GC, LCset, N, IG, cN);
					GCset = addMSGlobalContextToSet(GCset, GC);
					int i=0;
					while(!LCset.isEmpty()) 
					{				
						N = new HashMap<>();
						GC = new GlobalContext(targetClass);
						GC.addLocalContext(LCset.element());
						iterations++;
						GC = generateGC(IG.nodeRoot, a, GC, LCset, N, IG, cN);
						s = GCset.size();
						GCset = addMSGlobalContextToSet(GCset, GC);
						LCset.remove();
						if(s == GCset.size())
						{
							i++;
							if(i==10)
							{
							//	System.out.println("Loop Reached Limit");
								break;
							}
						}
						else
						{
							i=0;
						}
					}
				}
//				System.out.println("Number of Iterations to generate MS contexts: " + iterations);
//				System.out.println("Number of Global Contexts: " + GCset.size());
				numberOfIdentityStatements = numberOfIdentityStatements + GCset.size();
//				outputMSGlobalContextsAsAxioms(GCset);
				saveMSSet(res1, res2, GCset, out);
			}
		}
		System.out.println("");
		Long endTime = System.currentTimeMillis();
		Long totalTimeMs = (endTime - startTime) ;
		System.out.println();
		String statInfo = "====================================\n"
		+ nameProject + String.format(" %d min, %d sec\n",
				TimeUnit.MILLISECONDS.toMinutes(totalTimeMs),
				TimeUnit.MILLISECONDS.toSeconds(totalTimeMs) -
						TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(totalTimeMs)))
		+ "Mean nb identity graph : " + nbIdentityGraph.doubleValue()/nbIdentityGraphCounter.doubleValue()+"\n"
		+"Mean nb nodes in identity graph : " + nbNodeIndentityGraph.doubleValue()/nbNodeIndentityGraphCounter.doubleValue()+"\n"
		+"====================================\n"
		+"Total Number of Different Global Contexts: " + allGlobalContexts.size()+"\n"
		+"Total Number of IdentityStatements: " + numberOfIdentityStatements + "\n"
		+"====================================";
		System.out.print("Writing dataset...");
		out.writeDataSet(statInfo);
		System.out.println("  Done");
	}


	public ArrayList<IdentityGraph> constructIdentityGraphs(Node mainNode)
	{	
		ArrayList<IdentityGraph> finalIGsets = new ArrayList<>();
		Queue<IdentityGraph> IGset = new LinkedList<IdentityGraph>();	
		IdentityGraph IG = new IdentityGraph(mainNode, Integer.toString(IGset.size() + finalIGsets.size()));
		IGset.add(IG);					
		int IGcounter =1;
		while(!IGset.isEmpty()) 
		{
			LinkedList<String> visitedNodes = new LinkedList<>();
			//System.out.println("[ Identity Graph " + IGcounter+" ]");
			IGset.element().id = Integer.toString(IGcounter);
			expandIdentityGraph(mainNode, IGset.element(), IGset, visitedNodes);
			finalIGsets.add(IGset.remove());
			IGcounter++;
		}
		return finalIGsets;
	}


	public IdentityGraph expandIdentityGraph(Node n, IdentityGraph IG, Queue<IdentityGraph> IGset, LinkedList<String> visitedNodes)
	{    
		if(!visitedNodes.contains(n.nodeID))
		{
			//n.outputNodePairs();
			visitedNodes.add(n.nodeID);
			LocalContext LCout = addLCoutToDB(n, getLocalContextOut(n));
			LocalContext LCin = addLCinToDB(n, getLocalContextIN(n));
			if(LCout != null)
			{
				for(String OPout : LCout.outOP.keySet()) // for each outgoing object property
				{
					for(String range: LCout.outOP.get(OPout)) // for each range in this object property
					{
						IdentityGraph IG_copy = null;
						LinkedList<Node> result = new LinkedList<Node>();;
						if(IG.causeEdge!= null)
						{
							if(IG.causeEdge.label.equals(OPout))
							{
								if(IG.causeEdge.one.nodeID.equals(n.nodeID) &&  IG.causeEdge.two.cl.equals(range))
								{
									result.add(IG.causeEdge.two);
								}
								else
								{
									if(IG.causeEdge.two.nodeID.equals(n.nodeID)&&  IG.causeEdge.one.cl.equals(range))
									{
										result.add(IG.causeEdge.one);
									}
									else
									{
										result = getCombsOut(n, OPout, range);
										IG_copy = new IdentityGraph(IG);
									}
								}
							}
							else
							{
								result = getCombsOut(n, OPout, range);
								IG_copy = new IdentityGraph(IG);
							}
						}
						else
						{
							result = getCombsOut(n, OPout, range);
							IG_copy = new IdentityGraph(IG);
						}
						int i=0;						
						for(Node nextNode: result)
						{
							//nextNode.outputNodePairs();
							if(i==0)
							{
								i++;
								IG.addNode(nextNode);
								Edge thisEd = new Edge(n, nextNode, OPout);							
								if(IG.containsEdge(thisEd) == false )
								{					
									IG.addEdge(thisEd);
									IG = expandIdentityGraph(nextNode, IG, IGset, visitedNodes);
								}
								else
								{
									if(thisEd.isIdenticalTo(IG.causeEdge))
									{
										IG = expandIdentityGraph(nextNode, IG, IGset, visitedNodes);
									}
								}

							}
							else
							{								
								IdentityGraph IG1 = new IdentityGraph(IG_copy);
								IG1.addNode(nextNode);
								Edge e = new Edge(n, nextNode, OPout);
								IG1.addEdge(e);
								IG1.setCauseEdge(e);
								IGset.add(IG1);
							}			
						}																						
					}
				}
			}
			if(LCin != null)
			{
				for(String OPin : LCin.inOP.keySet()) // for each incoming object property
				{
					for(String range: LCin.inOP.get(OPin)) // for each range in this object property
					{
						IdentityGraph IG_copy = null;
						LinkedList<Node> result = new LinkedList<Node>();;
						if(IG.causeEdge!= null)
						{
							if(IG.causeEdge.label.equals(OPin))
							{
								if(IG.causeEdge.one.nodeID.equals(n.nodeID))
								{
									result.add(IG.causeEdge.two);
								}
								else
								{
									if(IG.causeEdge.two.nodeID.equals(n.nodeID))
									{
										result.add(IG.causeEdge.one);
									}
									else
									{
										result = getCombsIn(n, OPin, range);
										IG_copy = new IdentityGraph(IG);
									}
								}
							}
							else
							{
								result = getCombsIn(n, OPin, range);
								IG_copy = new IdentityGraph(IG);
							}
						}
						else
						{
							result = getCombsIn(n, OPin, range);
							IG_copy = new IdentityGraph(IG);
						}
						int i=0;
						for(Node nextNode: result)
						{
							//nextNode.outputNodePairs();
							if(i==0)
							{
								i++;
								IG.addNode(nextNode);
								Edge thisEd = new Edge(n, nextNode, OPin);							
								if(IG.containsEdge(thisEd) == false)
								{					
									IG.addEdge(thisEd);
									IG = expandIdentityGraph(nextNode, IG, IGset, visitedNodes);
								}
								else
								{
									if(thisEd.isIdenticalTo(IG.causeEdge))
									{
										IG = expandIdentityGraph(nextNode, IG, IGset, visitedNodes);
									}
								}
							}
							else
							{								
								IdentityGraph IG1 = new IdentityGraph(IG_copy);
								IG1.addNode(nextNode);
								Edge e = new Edge(n, nextNode, OPin);
								IG1.addEdge(e);
								IG1.setCauseEdge(e);
								IGset.add(IG1);
							}										
						}			
					}
				}
			}
		}
		return IG;
	}



	public GlobalContext generateGC(Node n, Pattern a_src, GlobalContext GC, Queue<LocalContext> LCset, HashMap<String, HashMap<String, Node>> N, IdentityGraph IG, HashMap<String, LinkedList<LocalContext>> cN)
	{
		try
		{
			if(checkVisitedNodes(N, n) == false)
			{
				addToVisitedNodes(N, n);
				LocalContext LC_n = getLCoutFromDB(n);
				LocalContext LC_ex = GC.getExistingLocalContext(n.cl);
				if(LC_ex == null || LC_ex.checkIfEqualTo(LC_n))
				{
					GC.addLocalContext(LC_n);
					for(String eID: n.neighborhood.keySet())
					{
						if(IG.edges.containsKey(eID))
						{
							String property = n.neighborhood.get(eID).label;
							Node node_dest = n.neighborhood.get(eID).returnOtherNode(n);
							Pattern a_dest = new Pattern(n.cl, property, node_dest.cl);
							GC = generateGC(node_dest, a_dest, GC, LCset, N, IG, cN);
						}
					}
				}			
				else
				{
					if(LC_n.checkIfMoreSpecificThan(LC_ex))
					{
						for(String eID: n.neighborhood.keySet())
						{
							if(IG.edges.containsKey(eID))
							{
								String property = n.neighborhood.get(eID).label;
								Node node_dest = n.neighborhood.get(eID).returnOtherNode(n);
								Pattern a_dest = new Pattern(n.cl, property, node_dest.cl);
								if(LC_ex.checkIfContainsAxiom(a_dest))
								{
									GC = generateGC(node_dest, a_dest, GC, LCset, N, IG, cN);
								}		
							}
						}
					}
					else
					{
						if(a_src.id != null)
						{
							LocalContext LC_src = GC.localContexts.get(a_src.subject);
							if(checkIfNecessaryProperty(a_src.subject, a_src.predicate, a_src.object) == false)
							{
								LC_src.removeAxiom(a_src);
								LC_src = checkForCoProperties(LC_src);
								GC.replaceLocalContext(LC_src);		
							}
							else
							{
								LC_src = null;
							}
						}
					}
					if(checkInCauseNodes(cN, n, LC_n) == false)
					{
						LCset.add(LC_n);
						addToCauseNodes(cN, n, LC_n);
					}
					LocalContext commonContext = getCommonLocalContext(LC_n, LC_ex);
					commonContext = checkForCoProperties(commonContext);
					if(commonContext.checkIfEqualTo(LC_ex) == false)
					{
						if(checkInCauseNodes(cN, n, commonContext) == false)
						{
							LCset.add(commonContext);
							addToCauseNodes(cN, n, commonContext);
						}
					}
				}			
			}
			return GC;
		}catch (Exception ex)
		{
			ex.printStackTrace();
			System.out.println("Exception in Generate GC");
			return GC;
		}
	}


	public void outputMSGlobalContextsAsPatterns(LinkedList<GlobalContext> GCset)
	{
		int i=1;
		for(GlobalContext GC : GCset)
		{
			System.out.println("-------- GC_" +i +"(" +GC.cl+") --------");
			GC.outputGlobalContextAsPatterns();
			i++;
		}
	}

	public void outputMSGlobalContextsAsAxioms(LinkedList<GlobalContext> GCset)
	{
		int i=1;
		for(GlobalContext GC : GCset)
		{
			System.out.println(i+") GC[" +GC.id+"]");
			//GC.outputGlobalContextAsAxioms();
			i++;		
		}
	}
	

	public void saveMSSet(Resource res1, Resource res2, LinkedList<GlobalContext> GCset, Output out)
	{
		for(GlobalContext GC : GCset)
		{
			saveGlobalContext(res1, res2, GC, out);
		}
	}

	public void saveGlobalContext(Resource res1, Resource res2, GlobalContext GC, Output out)
	{
		if(!allGlobalContexts.containsKey(GC.id))
		{
			out.writeGlobalContext(GC);
			checkIdentityRelation(GC, out);
			out.addIdentityStatement(res1, res2, GC);
		}
		else
		{
			out.addIdentityStatement(res1, res2, GC);
		}
	}
	
	public void checkIdentityRelation(GlobalContext GC, Output out)
	{
		for(String GCid : allGlobalContexts.keySet())
		{
			if(GC.checkIfMoreSpecificThan(allGlobalContexts.get(GCid)))
			{
				out.addHierarchyStatement(GC.id, GCid);
			}
			else
			{
				if(allGlobalContexts.get(GCid).checkIfMoreSpecificThan(GC))
				{
					out.addHierarchyStatement(GCid, GC.id);
				}
			}
		}
		allGlobalContexts.put(GC.id, GC);
	}


	//il y a un probleme ici
	public LinkedList<GlobalContext> addMSGlobalContextToSet(LinkedList<GlobalContext> GCset, GlobalContext GC)
	{
		if(GCset.isEmpty())
		{
			GC.addAllLocalContextsAxioms();
			GCset.add(GC);
			return GCset;
		}
		else
		{
			Boolean add = false;
			LinkedList<GlobalContext> result = new LinkedList<>();
			GC.addAllLocalContextsAxioms();
			result.addAll(GCset);
			for(GlobalContext thisGC : GCset)
			{
				if(GC.id.equals(thisGC.id))
				{
					return GCset;
				}
				else
				{
					if(thisGC.checkIfMoreSpecificThan(GC))
					{
						return GCset;
					}
					else
					{
						if(GC.checkIfMoreSpecificThan(thisGC))
						{
							result.remove(thisGC);
							add = true;
						}
						else
						{
							add = true;
						}
					}					
				}
				/*if(thisGC.checkIfMoreSpecificThan(GC))
				{
					break;
				}
				else
				{
					if(!GC.checkIfEqualTo(thisGC))
					{
						if(GC.checkIfMoreSpecificThan(thisGC))
						{
							result.remove(thisGC);
							add = true;
						}
						else
						{
							add = true;
						}
					}		
				}*/
			}
			if(add==true)
			{
				result.add(GC);
			}
			return result;
		}
	}

	public Boolean checkInCauseNodes(HashMap<String, LinkedList<LocalContext>> cN, Node n, LocalContext lc)
	{
		if(cN.containsKey(n.nodeID))
		{
			for(LocalContext localCntxt: cN.get(n.nodeID))
			{
				if(localCntxt.checkIfEqualTo(lc))
				{
					return true;
				}
			}
		}
		return false;
	}

	public void addToCauseNodes(HashMap<String, LinkedList<LocalContext>> cN, Node n, LocalContext lc)
	{
		if(cN.containsKey(n.nodeID))
		{
			cN.get(n.nodeID).add(lc);
		}
		else
		{
			cN.put(n.nodeID, new LinkedList<>());
			cN.get(n.nodeID).add(lc);
		}
	}

	public LocalContext moreSpecificThan(ArrayList<LocalContext> LClist)
	{
		LocalContext result = null;
		for(int i=0; i< LClist.size()-1; i++)
		{
			if(i==0)
			{
				result = getCommonLocalContext(LClist.get(0), LClist.get(1));
			}
			else
			{
				result = getCommonLocalContext(result, LClist.get(i+1));
			}
		}	
		return result;
	}



	public Boolean checkVisitedNodes(HashMap<String, HashMap<String, Node>> N, Node n)
	{
		if(!N.containsKey(n.cl))
		{
			return false;
		}
		else
		{
			if(N.get(n.cl).containsKey(n.nodeID))
			{
				return true;
			}
			return false;
		}
	}

	public void addToVisitedNodes(HashMap<String, HashMap<String, Node>> N, Node n)
	{
		if(!N.containsKey(n.cl))
		{	
			N.put(n.cl, new HashMap<>());			
		}
		N.get(n.cl).put(n.nodeID, n);
	}

	public LinkedList<Node> getCombsOut(Node n, String Property, String Range)
	{
		List<List<Node>> input = Lists.newArrayList();
		for(String pID : n.pairs.keySet())
		{
			try
			{
				input.add(getPairOutFromDB(n.pairs.get(pID)).outOP.get(Property).get(Range));
			}catch(Exception ex)
			{

			}
		}
		if(!input.isEmpty())
		{
			return mergeCombinations(input, n , Property, Range);
		}
		else
		{
			return new LinkedList<>();
		}
	}


	public LinkedList<Node> getCombsIn(Node n, String Property, String Range)
	{
		List<List<Node>> input = Lists.newArrayList();
		for(String pID : n.pairs.keySet())
		{
			try
			{
				input.add(getPairInFromDB(n.pairs.get(pID)).inOP.get(Property).get(Range));
			}catch(Exception ex)
			{

			}
		}
		if(!input.isEmpty())
		{
			return mergeCombinations(input, n , Property, Range);
		}
		else
		{
			return new LinkedList<>();
		}	
	}

	public LocalContext getLocalContextOut(Node n)
	{
		if(n.pairs.size()>1)
		{
			ArrayList<LocalContext> LClist = new ArrayList<>();
			for(String p : n.pairs.keySet())
			{
				LClist.add(getMaxLocalContextOUT(n.pairs.get(p).resource1, n.pairs.get(p).resource2));
			}
			return getCommonLocalContext(LClist);
		}
		else
		{
			return getMaxLocalContextOUT(n.getFirstPair().resource1, n.getFirstPair().resource2);
		}	
	}

	public LocalContext getLocalContextIN(Node n)
	{
		if(n.pairs.size()>1)
		{
			ArrayList<LocalContext> LClist = new ArrayList<>();
			for(String p : n.pairs.keySet())
			{
				LClist.add(getMaxLocalContextIN(n.pairs.get(p).resource1, n.pairs.get(p).resource2));
			}
			return getCommonLocalContext(LClist);
		}
		else
		{
			return getMaxLocalContextIN(n.getFirstPair().resource1, n.getFirstPair().resource2);
		}

	}



	public LocalContext getCommonLocalContext(ArrayList<LocalContext> LClist)
	{
		LocalContext result = null;
		for(int i=0; i< LClist.size()-1; i++)
		{
			if(i==0)
			{
				result = getCommonLocalContext(LClist.get(0), LClist.get(1));
			}
			else
			{
				result = getCommonLocalContext(result, LClist.get(i+1));
			}
		}	
		return result;
	}


	public LocalContext getCommonLocalContext(LocalContext lc1, LocalContext lc2)
	{
		if(lc1.cl.equals(lc2.cl))
		{
			if(lc1.type.equals(lc2.type) && lc1.type == true)
			{
				if(lc1.max == true)
				{
					if(lc2.max == true)
					{
						return lc1;
					}
					else
					{
						return lc2;
					}
				}
				if(lc2.max == true)
				{
					return lc1;
				}
				LocalContext result = new LocalContext(lc1.cl, true);
				for(String dp: lc1.outDP.keySet())
				{
					if(lc2.outDP.containsKey(dp))
					{
						LinkedList<String> commonRanges = intersectTwoLists(lc1.outDP.get(dp), lc2.outDP.get(dp));
						if(!commonRanges.isEmpty())
						{
							result.outDP.put(dp,commonRanges);
						}
					}
				}
				for(String op: lc1.outOP.keySet())
				{
					if(lc2.outOP.containsKey(op))
					{
						LinkedList<String> commonRanges = intersectTwoLists(lc1.outOP.get(op), lc2.outOP.get(op));
						if(!commonRanges.isEmpty())
						{
							result.outOP.put(op,commonRanges);
						}
					}
				}
				return result;
			}
			else
			{
				if(lc1.type.equals(lc2.type) && lc1.type == false)
				{
					if(lc1.max == true)
					{
						if(lc2.max == true)
						{
							return lc1;
						}
						else
						{
							return lc2;
						}
					}
					if(lc2.max == true)
					{
						return lc1;
					}
					LocalContext result = new LocalContext(lc1.cl, false);
					for(String op: lc1.inOP.keySet())
					{
						if(lc2.inOP.containsKey(op))
						{
							LinkedList<String> commonRanges = intersectTwoLists(lc1.inOP.get(op), lc2.inOP.get(op));
							if(!commonRanges.isEmpty())
							{
								result.inOP.put(op,commonRanges);
							}
						}
					}
					return result;
				}
			}
		}
		return null;
	}


	public LinkedList<String> intersectTwoLists(LinkedList<String> list1, LinkedList<String> list2)
	{
		Collection<String> collection1 = list1;
		Collection<String> collection2 = list2;
		collection1.retainAll(collection2);
		LinkedList<String> result = new LinkedList<>();
		result.addAll(collection1);
		return result;
	}


	public LocalContext getMaxLocalContextOUT(String res1, String res2)
	{
		if(res1.equals(res2))
		{
			String c = typeOf(res1);
			LocalContext max = new LocalContext(c, true);
			max.setAsMax();
			return max;
		}
		else
		{
			Pair thisPair = new Pair(res1, res2);
			LocalContext LCout = getLCoutFromDB(thisPair.resource1, thisPair.resource2, thisPair.pairID);
			if(LCout!= null)
			{
				//LCout.outputLocalContextAsPatterns();
				return LCout;
			}
			String c = typeOf(res1); //TODO
			if(c!= null)
			{			
				LCout = new LocalContext(c, true);
				HashMap<String, ArrayList<RDFNode>> hmp1 = new HashMap<>();
				HashMap<String, ArrayList<RDFNode>> hmp2 = new HashMap<>();
				try
				{		
					for(String thisPredicate : MySPARQLqueries.getCommonPropertiesOut(res1, res2))
					{
						if(checkIfUnwantedProperty("ANY", thisPredicate, "ANY")
								|| checkIfUnwantedProperty(c.toString(), thisPredicate, "ANY"))
						{
						}
						else
						{
							hmp1.put(thisPredicate, new ArrayList<>());
							hmp2.put(thisPredicate, new ArrayList<>());
							for(RDFNode o1 : MySPARQLqueries.listObjectsOf(res1, thisPredicate))
							{
								hmp1.get(thisPredicate).add(o1);		
							}
							for(RDFNode o2 : MySPARQLqueries.listObjectsOf(res2, thisPredicate))
							{
								hmp2.get(thisPredicate).add(o2);		
							}
							if(dataProperties.contains(thisPredicate))
							{
								if(compareDataLists(hmp1.get(thisPredicate), hmp2.get(thisPredicate)) == true)
								{
									LCout.addProperty(thisPredicate.toString(), RDFS.Literal.toString());
									thisPair.addDPout(thisPredicate);
								}
								else if(checkIfNecessaryProperty("ANY", thisPredicate, "ANY")
										|| checkIfNecessaryProperty(c.toString(), thisPredicate, "ANY"))
								{
									LCout.clearAllRanges();
									break;							
								}						
							}
							else
							{
								if(objectProperties.contains(thisPredicate))
								{
									HashMap<String, ArrayList<String>> list1 = new HashMap<>();
									HashMap<String, ArrayList<String>> list2 = new HashMap<>();
									for (RDFNode n1 : hmp1.get(thisPredicate))
									{
										String t1 = typeOf(n1.toString());
										if(t1!=null)
										{
											if(!list1.containsKey(t1))
											{
												list1.put(t1, new ArrayList<>());
											}
											list1.get(t1).add(n1.toString());
										}
									}
									for (RDFNode n2 : hmp2.get(thisPredicate))
									{									
										String t2 = typeOf(n2.toString());
										if(t2!=null)
										{
											if(!list2.containsKey(t2))
											{
												list2.put(t2, new ArrayList<>());
											}
											list2.get(t2).add(n2.toString());
										}
									}
									Boolean br = false;
									for(String thisC : list1.keySet())
									{
										if(list2.containsKey(thisC))
										{
											if(checkIfUnwantedProperty("ANY", thisPredicate, thisC.toString())
													|| checkIfUnwantedProperty(c.toString(), thisPredicate, thisC.toString()))
											{

											}
											else
											{
												if(compareObjectsLists(list1.get(thisC), list2.get(thisC)) == true)
												{									
													LCout.addProperty(thisPredicate.toString(), thisC.toString());	
													LinkedList<Node> listOfNodes = getCombinations(list1.get(thisC), list2.get(thisC), thisC);
													thisPair.addOPout(thisPredicate, thisC, listOfNodes);
												}
												else if(checkIfNecessaryProperty("ANY", thisPredicate, "ANY")
														|| checkIfNecessaryProperty(c.toString(), thisPredicate, "ANY")
														|| checkIfNecessaryProperty("ANY", thisPredicate, thisC.toString())
														|| checkIfNecessaryProperty(c.toString(), thisPredicate, thisC.toString()))
												{
													LCout.clearAllRanges();
													br = true;
													break;	
												}
											}
										}
									}
									if(br==true) { break;}
								}
							}
						}
					}
					LCout = checkForCoProperties(LCout);
				}
				catch(Exception ex)
				{
					System.out.println(ex.getMessage());
				}
			}		
			addPairOutToDB(thisPair);
			addLCoutToDB(thisPair.resource1, thisPair.resource2, thisPair.pairID, LCout);
			//LCout.outputLocalContextAsPatterns();
			return LCout;
		}
	}


	public LocalContext getMaxLocalContextIN(String res1, String res2)
	{	
		if(res1.equals(res2))
		{
			String c = typeOf(res1);
			LocalContext max = new LocalContext(c, false);
			max.setAsMax();
			return max;
		}
		else
		{
			Pair thisPair = new Pair(res1, res2);
			LocalContext LCin = getLCinFromDB(thisPair.resource1, thisPair.resource2, thisPair.pairID);
			if(LCin!= null)
			{
				//LCin.outputLocalContextAsPatterns();
				return LCin;
			}		
			String c = typeOf(res1);
			if(c!= null)
			{
				LCin = new LocalContext(c.toString(), false);
				HashMap<String, ArrayList<RDFNode>> hmp1 = new HashMap<>();
				HashMap<String, ArrayList<RDFNode>> hmp2 = new HashMap<>();
				try
				{		
					for(String thisPredicate : MySPARQLqueries.getCommonPropertiesIn(res1, res2))
					{
						if(checkIfUnwantedProperty("ANY", thisPredicate, "ANY")
								|| checkIfUnwantedProperty(c.toString(), thisPredicate, "ANY"))
						{
						}
						else
						{
							hmp1.put(thisPredicate, new ArrayList<>());
							hmp2.put(thisPredicate, new ArrayList<>());
							for(RDFNode o1 : MySPARQLqueries.listSubjectsOf(thisPredicate, res1))
							{
								hmp1.get(thisPredicate).add(o1);		
							}
							for(RDFNode o2 : MySPARQLqueries.listSubjectsOf(thisPredicate, res2))
							{
								hmp2.get(thisPredicate).add(o2);		
							}
							if(objectProperties.contains(thisPredicate))
							{
								HashMap<String, ArrayList<String>> list1 = new HashMap<>();
								HashMap<String, ArrayList<String>> list2 = new HashMap<>();
								for (RDFNode n1 : hmp1.get(thisPredicate))
								{
									String t1 = typeOf(n1.toString());
									if(t1!=null)
									{
										if(!list1.containsKey(t1))
										{
											list1.put(t1, new ArrayList<>());
										}
										list1.get(t1).add(n1.toString());
									}
								}
								for (RDFNode n2 : hmp2.get(thisPredicate))
								{
									String t2 = typeOf(n2.toString());
									if(t2!=null)
									{
										if(!list2.containsKey(t2))
										{
											list2.put(t2, new ArrayList<>());
										}
										list2.get(t2).add(n2.toString());
									}
								}
								Boolean br = false;
								for(String thisC : list1.keySet())
								{
									if(list2.containsKey(thisC))
									{
										if(checkIfUnwantedProperty("ANY", thisPredicate, thisC.toString())
												|| checkIfUnwantedProperty(c.toString(), thisPredicate, thisC.toString()))
										{							
										}
										else
										{
											if(compareObjectsLists(list1.get(thisC), list2.get(thisC)) == true)
											{
												LCin.addProperty(thisPredicate, thisC.toString());
												LinkedList<Node> listOfNodes = getCombinations(list1.get(thisC), list2.get(thisC), thisC);
												thisPair.addOPin(thisPredicate, thisC, listOfNodes);
											}
											else if(checkIfNecessaryProperty("ANY", thisPredicate, "ANY")
													|| checkIfNecessaryProperty(c.toString(), thisPredicate, "ANY")
													|| checkIfNecessaryProperty("ANY", thisPredicate, thisC.toString())
													|| checkIfNecessaryProperty(c.toString(), thisPredicate, thisC.toString())) 
											{
												LCin.clearAllDomains();
												br = true;
												break;	
											}
										}
									}
								}
								if(br==true) { break;}
							}
						}
					}
					LCin = checkForCoProperties(LCin);
				}
				catch(Exception ex)
				{
					System.out.println(ex.getMessage());
				}
			}
			addPairInToDB(thisPair);
			addLCinToDB(thisPair.resource1, thisPair.resource2, thisPair.pairID, LCin);
			//LCin.outputLocalContextAsPatterns();
			return LCin;
		}
	}


	public LinkedList<Node> mergeCombinations(List<List<Node>> input, Node n, String Property, String cl)
	{		
		PermutationGenerator pg = new PermutationGenerator();
		List<List<Node>> output = pg.permutate(input);
		LinkedList<Node> result = new LinkedList<>();
		Node existingNode = n.checkRelation(Property, cl);
		if(existingNode != null)
		{
			for(List<Node> ln : output)
			{	
				TreeMap<String, Pair> pairs = new TreeMap<>();
				for(Node aNode : ln)
				{
					pairs.putAll(aNode.pairs);
				}
				Node x = new Node(pairs);
				if(x.containsPairs(existingNode))
				{
					x.cl = cl;
					result.add(x);
				}				
			}	
		}
		else
		{			
			for(List<Node> ln : output)
			{	
				TreeMap<String, Pair> pairs = new TreeMap<>();
				for(Node aNode : ln)
				{
					pairs.putAll(aNode.pairs);
				}
				Node x = new Node(pairs);
				x.cl = cl;
				result.add(x);

			}	
		}
		return result;

		/*// then
        print(output);*/
	}


	public void print(List<List<Node>> output) {
		for (List<Node> list : output) {
			System.out.println(Arrays.toString(list.toArray()));
		}
		System.out.println("TOTAL: " + output.size());
	}


	public LinkedList<Node> getCombinations(ArrayList<String> list1, ArrayList<String> list2, String cl)
	{
		LinkedList<Node> listOfNodes = new LinkedList<>();
		ArrayList<String> sameTerms = new ArrayList<>();
		sameTerms.addAll(list1);
		sameTerms.retainAll(list2);
		if(sameTerms.isEmpty())
		{
			return permutations(listOfNodes, list1, list2, new Stack<String>(), list1.size(), cl);
		}
		else
		{
			for(String s : sameTerms)
			{
				list1.remove(s);
				list2.remove(s);
			}
			LinkedList<Node> result = permutations(listOfNodes, list1, list2, new Stack<String>(), list1.size(), cl);			
			for(String s : sameTerms)
			{
				Pair p = new Pair(s, s);
				for(Node n : result)
				{
					n.pairs.put(p.pairID, p);	
				}
			}	
			return result;
		}				
	}

	public LinkedList<Node> permutations(LinkedList<Node> listOfNodes, ArrayList<String> list1, ArrayList<String> list2, Stack<String> permutation, int size, String cl) {

		/* permutation stack has become equal to size that we require */
		if(permutation.size() == size) {
			/* print the permutation */
			TreeMap<String, Pair> pairs = new TreeMap<>();
			for(int i=0; i<list1.size();i++)
			{
				Pair p = new Pair(list1.get(i), permutation.get(i));
				pairs.put(p.pairID, p);
			}
			Node n = new Node(pairs);
			n.cl = cl;
			listOfNodes.add(n);
			// System.out.println(Arrays.toString(permutation.toArray(new String[0])));
		}

		/* items available for permutation */
		String[] availableItems = list2.toArray(new String[0]);
		for(String i : availableItems) {
			/* add current item */
			permutation.push(i);

			/* remove item from available item set */
			list2.remove(i);

			/* pass it on for next permutation */
			permutations(listOfNodes, list1, list2, permutation, size, cl);

			/* pop and put the removed item back */
			list2.add(permutation.pop());
		}
		return listOfNodes;
	}


	public void addPairOutToDB(Pair p)
	{
		//addToDB(p.pairID.getBytes(), serializeObject(p),  "PairID-Pout");
		addToDB(p.pairID.getBytes(), serializeObject(p), DB_PairID_Pout);


	}

	public void addPairInToDB(Pair p)
	{
		addToDB(p.pairID.getBytes(), serializeObject(p), DB_PairID_Pin);
		//addToDB(p.pairID.getBytes(), serializeObject(p), "PairID-Pin");
	}

	public Pair getPairOutFromDB(Pair p)
	{
		try
		{
			byte[] x = getValueFromDB(p.pairID.getBytes(), DB_PairID_Pout);
			//byte[] x = getValueFromDB(p.pairID.getBytes(), "PairID-Pout");
			if(x != null)
			{
				return (Pair) deSerializeObject(x);
			}
			else
			{
				return null;
			}
		}catch (Exception ex)
		{
			return null;
		}
	}

	public Pair getPairInFromDB(Pair p)
	{
		try
		{
			byte[] x = getValueFromDB(p.pairID.getBytes(), DB_PairID_Pin);
			//byte[] x = getValueFromDB(p.pairID.getBytes(), "PairID-Pin");
			if(x != null)
			{
				return (Pair) deSerializeObject(x);
			}
			else
			{
				return null;
			}
		}catch (Exception ex)
		{
			return null;
		}
	}

	public void addLCoutToDB(String res1, String res2, String id, LocalContext LCout)
	{
		addToDB(id.getBytes(), serializeObject(LCout), DB_PairID_LCout);
		//addToDB(id.getBytes(), serializeObject(LCout), "PairID-LCout");
	}

	public LocalContext getLCoutFromDB(String res1, String res2, String id)
	{
		byte[] x = getValueFromDB(id.getBytes(), DB_PairID_LCout);
		//byte[] x = getValueFromDB(id.getBytes(), "PairID-LCout");
		if(x != null)
		{
			return (LocalContext) deSerializeObject(x);
		}
		else
		{
			return null;
		}
	}

	public LocalContext addLCoutToDB(Node n, LocalContext LCout)
	{
		addToDB(n.nodeID.getBytes(), serializeObject(LCout), DB_NodeID_LCout);
		//addToDB(n.nodeID.getBytes(), serializeObject(LCout), "NodeID-LCout");
		return LCout;
	}

	public LocalContext getLCoutFromDB(Node n)
	{
		byte[] x = getValueFromDB(n.nodeID.getBytes(), DB_NodeID_LCout);
		//byte[] x = getValueFromDB(n.nodeID.getBytes(), "NodeID-LCout");
		if(x != null)
		{
			return (LocalContext) deSerializeObject(x);
		}
		else
		{
			return null;
		}
	}



	public void addLCinToDB(String res1, String res2, String id, LocalContext LCin)
	{
		addToDB(id.getBytes(), serializeObject(LCin), DB_PairID_LCin);
		//addToDB(id.getBytes(), serializeObject(LCin), "PairID-LCin");
	}

	public LocalContext getLCinFromDB(String res1, String res2, String id)
	{
		byte[] x = getValueFromDB(id.getBytes(), DB_PairID_LCin);
		//byte[] x = getValueFromDB(id.getBytes(), "PairID-LCin");
		if(x != null)
		{
			return (LocalContext) deSerializeObject(x);
		}
		else
		{
			return null;
		}
	}

	public LocalContext addLCinToDB(Node n, LocalContext LCin)
	{
		addToDB(n.nodeID.getBytes(), serializeObject(LCin), DB_NodeID_LCin);
		//addToDB(n.nodeID.getBytes(), serializeObject(LCin), "NodeID-LCin");
		return LCin;
	}

	public LocalContext getLCinFromDB(Node n)
	{
		byte[] x = getValueFromDB(n.nodeID.getBytes(), DB_NodeID_LCin);
		//byte[] x = getValueFromDB(n.nodeID.getBytes(), "NodeID-LCin");
		if(x != null)
		{
			return (LocalContext) deSerializeObject(x);
		}
		else
		{
			return null;
		}
	}



	/*	public void addNodesToDB(Map<String, Node> generalNodes)
	{		
		byte[] KEY = serializeObject(generalNodes);
		for(String pairID : generalNodes.keySet())
		{
			String thisId = Integer.toString(generalNodes.get(pairID).nodeID);
			addToDB(thisId.getBytes(), KEY, "Pair-GeneralNode");
		}
	}

	@SuppressWarnings("unchecked")
	public Map<String, Node> getGeneralNode(Node n)
	{
		String key = Integer.toString(n.nodeID);
		byte[] x = getValueFromDB(key.getBytes(), "Pair-GeneralNode");
		if(x != null)
		{
			return (Map<String, Node>) deSerializeObject(x);
		}
		return null;
	}*/


	public Boolean compareDataLists(ArrayList<RDFNode> list1, ArrayList<RDFNode> list2)
	{
		if(list1.isEmpty() && list2.isEmpty())
		{
			return true;
		}
		else
		{
			if(list1.size() != list2.size())
			{
				return false;
			}
			else
			{
				ArrayList<RDFNode> usedNodes = new ArrayList<>();
				for(RDFNode n1 : list1)
				{
					Boolean exist = false;
					for(RDFNode n2: list2)
					{
						if(!usedNodes.contains(n2))
						{
							if(n1.asLiteral().equals(n2.asLiteral()))
							{
								usedNodes.add(n2);
								exist = true;
								break;
							}
						}				
					}
					if(exist == false)
					{
						return false;
					}
				}
				return true;
			}
		}
	}


	public Boolean compareObjectsLists(ArrayList<String> list1, ArrayList<String> list2)
	{
		if(list1.isEmpty() && list2.isEmpty())
		{
			return true;
		}
		else
		{
			if(list1.size() != list2.size())
			{
				return false;
			}
			else
			{
				return true;
			}
		}
	}

	// get the type of a resource from the dep classes
	public String typeOf(String res1)
	{	
//		for(RDFNode cl : MySPARQLqueries.getListOfTypes(res1))
//		{
//			if(depClasses.contains(cl))
//			{
//				return cl.toString();
//			}
//		}
		try{
				return typesOf.get(res1);
		} catch(Exception ex)
		{
			ex.getMessage();
			System.out.println("Error Type Of: " + res1);
			return null;
		}


//		return null;

	}


	// store the Unwanted Properties
	public void getUnwantedProperties(String[] unwantedProperties)
	{
		for(int i=0; i<unwantedProperties.length; i++)
		{
			if(unwantedProperties[i]!= null && unwantedProperties[i].contains("***"))
			{
				String[] pattern = unwantedProperties[i].split("\\*\\*\\*");
				if(pattern.length == 3)
				{
					Pattern a = new Pattern(pattern[0], pattern[1], pattern[2]);
					UP.put(a.id, a);		
				}	
			}					
		}
	}

	// store the Necessary Properties
	public void getNecessaryProperties(String[] necessaryProperties)
	{
		for(int i=0; i<necessaryProperties.length; i++)
		{
			if(necessaryProperties[i]!= null && necessaryProperties[i].contains("***"))
			{
				String[] pattern = necessaryProperties[i].split("\\*\\*\\*");
				if(pattern.length == 3)
				{
					Pattern a = new Pattern(pattern[0], pattern[1], pattern[2]);
					NP.put(a.id, a);
				}	
			}					
		}
	}

	// store the Co-occuring Properties
	public void getCoProperties(String[] coProperties)
	{	
		for(int i=0; i<coProperties.length; i++)
		{
			if(coProperties[i]!= null && coProperties[i].contains("==="))
			{
				String[] syntax = coProperties[i].split("===");
				String[] props = syntax[1].split("!!!");
				ArrayList<Pattern> list = new ArrayList<>();
				for(int j=0; j<props.length; j++)
				{
					if(props[j]!= null && props[j].contains("***"))
					{
						String[] terms = props[j].split("\\*\\*\\*");
						list.add(new Pattern(syntax[0], terms[0], terms[1]));
					}
				}
				if(!CP.containsKey(syntax[0]))
				{
					ArrayList<ArrayList<Pattern>> cont = new ArrayList<>();
					cont.add(list);
					CP.put(syntax[0], cont);
				}
				else
				{
					CP.get(syntax[0]).add(list);
				}
			}
		}
	}


	// store the Unwanted Classes
	public void getUnwantedClasses(String[] unwantedClasses)
	{
		for(int i=0; i<unwantedClasses.length; i++)
		{
			if(unwantedClasses[i]!= null)
			{
				UC.add(unwantedClasses[i]);		
			}					
		}
	}


	// check if a property is listed as unwanted property: true if this property is unwanted
	public Boolean checkIfUnwantedProperty(String subject, String predicate, String object)
	{	
		Pattern a = new Pattern(subject, predicate, object);
		if(UP.containsKey(a.id))
		{
			return true;
		}		
		return false;
	}

	// check if a property is listed as necessary property: true if this property is necessary
	public Boolean checkIfNecessaryProperty(String subject, String predicate, String object)
	{
		Pattern a = new Pattern(subject, predicate, object);
		if(NP.containsKey(a.id))
		{
			return true;
		}
		return false;
	}


	// check if a property is listed as necessary property: true if this property is necessary
	public LocalContext checkForCoProperties(LocalContext localContext)
	{		
		if(localContext != null && localContext.type == true)
		{
			if(CP.containsKey(localContext.cl))
			{
				for(ArrayList<Pattern> list : CP.get(localContext.cl))
				{
					int i = 0;
					for(Pattern a: list)
					{
						if(localContext.containsPropertyOut(a.predicate, a.object) == true)
						{
							i++;
						}			
					}
					if(i>0 && i!= list.size())
					{
						for(Pattern a: list)
						{
							localContext.removeRangeOfProperty(a.predicate, a.object);
						}
					}
				}
			}
			if(CP.containsKey("ANY"))
			{
				for(ArrayList<Pattern> list : CP.get("ANY"))
				{
					int i = 0;
					for(Pattern a: list)
					{
						if(localContext.containsPropertyOut(a.predicate, a.object) == true)
						{
							i++;
						}			
					}
					if(i>0 && i!= list.size())
					{
						for(Pattern a: list)
						{
							localContext.removeRangeOfProperty(a.predicate, a.object);
						}
					}
				}
			}
		}	
		return localContext;
	}


	// check if a property is listed as necessary property: true if this property is necessary
	public Boolean checkIfUnwantedClass(String cl)
	{
		if(UC.contains(cl))
		{
			return true;
		}
		return false;
	}



	public void addToHashMap(HashMap<String, ArrayList<String>> hmp, String key, String value)
	{
		if(!hmp.containsKey(key))
		{
			hmp.put(key, new ArrayList<>());
		}
		hmp.get(key).add(value);
	}
















	// =========================================================================

	// Read the model from a given URI
	public void ReadModel(String modelName) 
	{
		try 
		{
			model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM, null);
			//model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
			FileManager.get().readModel(model, modelName);
			model.setStrictMode(false);
			model.loadImports();
			System.out.println("Done: Read Model");
		} 
		catch (Exception ex) 
		{
			System.out.println("##### Error Function: readModel #####");
			System.out.println("EXCEPTION: " + ex.getMessage());
		}
	}

	// Count Individuals
	public int countIndividuals() 
	{	
		return MySPARQLqueries.countIndividuals();
	}

	// Count Classes
	public int countClasses() 
	{
		return MySPARQLqueries.listInstantiatedClasses().size();
	}

	// Return the number of statements in the loaded model
	public int countStatements()
	{
		return MySPARQLqueries.countStatements();
	}

	// Count Properties
	public int countProperties() 
	{
		return objectProperties.size() + dataProperties.size();
		/*int size = 0;
		for(ExtendedIterator<OntProperty> prop = model.listAllOntProperties(); prop.hasNext();)
		{
			prop.next();
			size++;
		}
		return size;*/
	}

	// Count Object Properties
	public int countObjectProperties() 
	{
		return objectProperties.size();
		/*int size = 0;
		for(ExtendedIterator<ObjectProperty> objProp = model.listObjectProperties(); objProp.hasNext();)
		{
			objProp.next();
			size++;
		}
		return size;*/
	}

	// Count Data Properties
	public int countDataTypeProperties() 
	{	
		return dataProperties.size();
		/*int size = 0;
		for(ExtendedIterator<DatatypeProperty> dataProp = model.listDatatypeProperties(); dataProp.hasNext();)
		{
			dataProp.next();
			size++;
		}
		return size;*/
	}

	// Count Annotation Properties
	public int countAnnotationProperties() 
	{		
		int size = 0;
		for(ExtendedIterator<AnnotationProperty> annotProp = model.listAnnotationProperties(); annotProp.hasNext();)
		{
			annotProp.next();
			size++;
		}
		return size;
	}


	public void getAllProperties()
	{
		dataProperties.addAll(MySPARQLqueries.getAllDataProperties());
		objectProperties.addAll(MySPARQLqueries.getAllObjectProperties());
	}


	/*	public void getAllProperties()
	{	
		for(StmtIterator stmtIti = model.listStatements(null, null, (RDFNode) null); stmtIti.hasNext();)
		{
			Statement st = stmtIti.next();
			if(!st.getPredicate().equals(RDF.type))
			{
				if(st.getObject().isLiteral())
				{
					if(!dataProperties.contains(st.getPredicate()))
						dataProperties.add(st.getPredicate());
				}
				else
				{
					if(!objectProperties.contains(st.getPredicate()))
						objectProperties.add(st.getPredicate());
				}
			}
		}
	}*/


	// load ontology
	public Model loadModelTDB(String source, String ontoName)
	{
		String TDBdirectory = "data/TDB_" + ontoName;
		dataset = TDBFactory.createDataset(TDBdirectory);
		modelTDB = dataset.getDefaultModel();
		TDBLoader.loadModel(modelTDB, source);
		//FileManager.get().readModel(tdb, source);
		System.out.println("Done: Load Model");
		this.ontoName = ontoName;
		return modelTDB;	
	}

	// close ontology
	public void closeModel()
	{
		model.close();
		//dataset.close();
		System.out.println("");
		System.out.println("Done: Close Model");
	}


	// Serialize an object to a byte array
	public byte[] serializeObject(Object obj)
	{

		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bout);
			out.writeObject(obj);
			out.close();
			return bout.toByteArray();
//			String result = new String(bout.toByteArray());
//			System.out.println("Data Serialized");
//			return result.getBytes();
		}catch(IOException i) 
		{
			i.printStackTrace();
			return null;
		}
	}

	// De-serialize a byte array to object
	public Object deSerializeObject(byte[] byteValue)
	{
		try {
			Object obj = null;
			ByteArrayInputStream bin = new ByteArrayInputStream(byteValue);
			ObjectInputStream in = new ObjectInputStream(bin);
			try 
			{
				obj = in.readObject();
			} catch (ClassNotFoundException e) 
			{
				e.printStackTrace();
				return null;
			}
			in.close();
			//System.out.println("Data deSerialized");
			return obj;
		}catch(IOException i) {
			i.printStackTrace();
			return null;
		}
	}


	// get Value from RocksDB
	public static byte[] getValueFromDB(byte[] key, MongoCollection DB)
	{
		byte[] result = null;
		try {
			MongoCursor<Document> ite = DB.find(Filters.eq("key",Base64.encodeBase64String(key))).iterator();
			while(ite.hasNext()) {
				Document doc = ite.next();
				result = Base64.decodeBase64(doc.getString("value"));
			}
//			result = DB.get(key);
		} catch (MongoException e) {
			e.printStackTrace();
		}
		return result;
	}


	// add to RocksDB
	public static void addToDB(byte[] key, byte[] value, MongoCollection DB)
	{
		try {
			Document doc = new Document();
			doc.append("key", Base64.encodeBase64String(key));
			doc.append("value", Base64.encodeBase64String(value));
			DB.insertOne(doc);
//			DB.put(key, value);
		} catch (MongoException e) {
			e.printStackTrace();
		}		
	}

	public String generateKey(String resource1, String resource2)
	{
		String key = null;
		if (resource1.compareTo(resource2) < 0)
		{
			key = resource1 + ":!:" + resource2;
		}
		else
		{
			key = resource2 + ":!:" + resource1;
		}
		return key;
	}
	
	

	public void deleteRocksDB()
	{
		try {
			FileUtils.deleteDirectory(new File(outputPath+"DB"));
			/*FileUtils.deleteDirectory(new File(outputPath+"DB/ROCKS_DB_NodeID_LCout.db"));
			FileUtils.deleteDirectory(new File(outputPath+"DB/ROCKS_DB_NodeID_LCin.db"));
			FileUtils.deleteDirectory(new File(outputPath+"DB/ROCKS_DB_PairID_LCout.db"));
			FileUtils.deleteDirectory(new File(outputPath+"DB/ROCKS_DB_PairID_LCin.db"));
			FileUtils.deleteDirectory(new File(outputPath+"DB/ROCKS_DB_PairID_Pout.db"));
			FileUtils.deleteDirectory(new File(outputPath+"DB/ROCKS_DB_PairID_Pin.db"));*/
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Convert from byte[] to String
	public static String byteToString(byte[] B)
	{
		if(B != null)
		{
			try {
				return new String(B, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return null;
			}
		}
		else
		{
			return null;
		}
	}

	public void openMongoDB()
	{
		try {
			mongo = new MongoClient("localhost", 27017);
			MongoDatabase db = mongo.getDatabase("MONGO_JOE");
			db.drop();

			try {
				db.createCollection("MONGO_DB_NodeID_LCout");
			} catch (MongoException e) {

			}
			DB_NodeID_LCout = db.getCollection("MONGO_DB_NodeID_LCout");

			try {
			db.createCollection("MONGO_DB_NodeID_LCin");
			} catch (MongoException e) {

			}
			DB_NodeID_LCin = db.getCollection("MONGO_DB_NodeID_LCin");

				try {
			db.createCollection("MONGO_DB_PairID_LCout");
				} catch (MongoException e) {

				}
			DB_PairID_LCout = db.getCollection("MONGO_DB_PairID_LCout");

					try {
			db.createCollection("MONGO_DB_PairID_LCin");
					} catch (MongoException e) {

					}
			DB_PairID_LCin = db.getCollection("MONGO_DB_PairID_LCin");

						try {
			db.createCollection("MONGO_DB_PairID_Pout");
						} catch (MongoException e) {

						}
			DB_PairID_Pout = db.getCollection("MONGO_DB_PairID_Pout");

							try {
			db.createCollection("MONGO_DB_PairID_Pin");
							} catch (MongoException e) {

							}
			DB_PairID_Pin = db.getCollection("MONGO_DB_PairID_Pin");

			// do something
		} catch (MongoException e) {
			// do some error handling
			e.printStackTrace();
		}
	}

	public void searchTypeOfForAllResources() {
		int c = 0;
		for (RDFNode node : depClasses) {
			System.out.println((c++)+" / "+depClasses.size());
			String n = node.toString();
			ResultSet rs = MySPARQLqueries.listInstancesOfDepClass(n);
			while(rs.hasNext()) {
				QuerySolution thisRow = rs.next();
				try
				{
					typesOf.put(thisRow.get("s").toString(), n);
				} catch(Exception ex)
				{

				}

			}
		}
	}

//	public void openRocksDB()
//	{
//		try {
//			// a factory method that returns a RocksDB instance
//			// returns pathnames for files and directory
//			try {
//				FileUtils.forceMkdir(new File(outputPath+"DB"));
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			ROCKS_DB_NodeID_LCout = RocksDB.open(OPTIONS, outputPath+"DB/ROCKS_DB_NodeID_LCout.db");
//			ROCKS_DB_NodeID_LCin = RocksDB.open(OPTIONS, outputPath+"DB/ROCKS_DB_NodeID_LCin.db");
//			ROCKS_DB_PairID_LCout = RocksDB.open(OPTIONS, outputPath+"DB/ROCKS_DB_PairID_LCout.db");
//			ROCKS_DB_PairID_LCin = RocksDB.open(OPTIONS, outputPath+"DB/ROCKS_DB_PairID_LCin.db");
//			ROCKS_DB_PairID_Pout = RocksDB.open(OPTIONS, outputPath+"DB/ROCKS_DB_PairID_Pout.db");
//			ROCKS_DB_PairID_Pin = RocksDB.open(OPTIONS, outputPath+"DB/ROCKS_DB_PairID_Pin.db");
//			// do something
//		} catch (RocksDBException e) {
//			// do some error handling
//			e.printStackTrace();
//		}
//	}




	/*
	// load RocksDB library
	public static final RocksDB openRocksDB()
	{
		try (final ColumnFamilyOptions cfOpts = new ColumnFamilyOptions().optimizeUniversalStyleCompaction()) {

			// list of column family descriptors, first entry must always be default column family
			final List<ColumnFamilyDescriptor> cfDescriptors = Arrays.asList(
					new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, cfOpts),
					new ColumnFamilyDescriptor("NodeID-LCout".getBytes(), cfOpts),
					new ColumnFamilyDescriptor("NodeID-LCin".getBytes(), cfOpts),
					new ColumnFamilyDescriptor("PairID-LCout".getBytes(), cfOpts),
					new ColumnFamilyDescriptor("PairID-LCin".getBytes(), cfOpts),
					new ColumnFamilyDescriptor("PairID-Pout".getBytes(), cfOpts),
					new ColumnFamilyDescriptor("PairID-Pin".getBytes(), cfOpts)
					);

			// a list which will hold the handles for the column families once the db is opened
			Functions.columnFamilyHandleList =  new ArrayList<>();

			try (final DBOptions options = new DBOptions().setCreateIfMissing(true)
					.setCreateMissingColumnFamilies(true);
					final RocksDB db = RocksDB.open(options,
							dbPath, cfDescriptors,
							columnFamilyHandleList)) {
				return db;
			} catch (RocksDBException e) {
				e.printStackTrace();
			}
		} // frees the column family options
		return null;

















		// a static method that loads the RocksDB C++ library.
		//RocksDB.loadLibrary();
		// the Options class contains a set of configurable DB options
		// that determines the behaviour of the database.
		try (final ColumnFamilyOptions cfOpts = new ColumnFamilyOptions().optimizeUniversalStyleCompaction()) 
		{
			// list of column family descriptors, first entry must always be default column family
			final List<ColumnFamilyDescriptor> cfDescriptors = Arrays.asList(
					new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, cfOpts),
					new ColumnFamilyDescriptor("NodeID-LCout".getBytes(), cfOpts),
					new ColumnFamilyDescriptor("NodeID-LCin".getBytes(), cfOpts),
					new ColumnFamilyDescriptor("PairID-LCout".getBytes(), cfOpts),
					new ColumnFamilyDescriptor("PairID-LCin".getBytes(), cfOpts),
					new ColumnFamilyDescriptor("PairID-Pout".getBytes(), cfOpts),
					new ColumnFamilyDescriptor("PairID-Pin".getBytes(), cfOpts)
					);

		try (final Options options = new Options().setCreateIfMissing(true)) 
		{
			// a factory method that returns a RocksDB instance
			try (final RocksDB db = RocksDB.open(options, dbPath)) 
			{	
				return db;
			}
			catch (RocksDBException e) 
			{
				System.out.println("Error RocksDB");
				System.out.println(e.getMessage());
			}
		} 
		catch (Exception e) 
		{
			System.out.println("Error RocksDB");
			System.out.println(e.getMessage());
		}
		}
		catch (Exception e) {
		}
		return null;
	}*/


	/*public LocalContext getMaxLocalContextOUT2(Resource res1, Resource res2)
{	
	RDFNode c = typeOf(res1);
	LocalContext LCout = null;
	if(c!= null)
	{
		LCout = new LocalContext(c.toString(), true);
		HashMap<Property, ArrayList<RDFNode>> hmp1 = new HashMap<>();
		HashMap<Property, ArrayList<RDFNode>> hmp2 = new HashMap<>();
		try
		{		
			for(StmtIterator stmtIti = res1.listProperties(); stmtIti.hasNext();)
			{
				Statement thisStatement = stmtIti.next();
				Property thisPredicate = thisStatement.getPredicate();
				if(checkIfUnwantedProperty("ANY", thisPredicate.getURI(), "ANY")
						|| checkIfUnwantedProperty(c.toString(), thisPredicate.getURI(), "ANY"))
				{
				}
				else
				{
					if(res2.hasProperty(thisPredicate) && !hmp1.containsKey(thisPredicate))
					{
						hmp1.put(thisPredicate, new ArrayList<>());
						hmp2.put(thisPredicate, new ArrayList<>());
						for(StmtIterator stmtItiProp = res1.listProperties(thisPredicate); stmtItiProp.hasNext();)
						{
							Statement thisStatementProp = stmtItiProp.next();
							hmp1.get(thisPredicate).add(thisStatementProp.getObject());		
						}
						for(StmtIterator stmtItiProp = res2.listProperties(thisPredicate); stmtItiProp.hasNext();)
						{
							Statement thisStatementProp = stmtItiProp.next();
							hmp2.get(thisPredicate).add(thisStatementProp.getObject());		
						}
						if(dataProperties.contains(thisPredicate))
						{
							if(compareDataLists(hmp1.get(thisPredicate), hmp2.get(thisPredicate)) == true)
							{
								LCout.addProperty(thisPredicate.toString(), RDFS.Literal.toString());					
							}
							else if(checkIfNecessaryProperty("ANY", thisPredicate.toString(), "ANY")
									|| checkIfNecessaryProperty(c.toString(), thisPredicate.toString(), "ANY"))
							{
								LCout.clearAllRanges();
								break;							
							}						
						}
						else
						{
							if(objectProperties.contains(thisPredicate))
							{
								HashMap<RDFNode, ArrayList<RDFNode>> list1 = new HashMap<>();
								HashMap<RDFNode, ArrayList<RDFNode>> list2 = new HashMap<>();
								for (RDFNode n1 : hmp1.get(thisPredicate))
								{
									RDFNode t1 = typeOf(n1.asResource());
									if(!list1.containsKey(t1))
									{
										list1.put(t1, new ArrayList<>());
									}
									list1.get(t1).add(n1);
								}
								for (RDFNode n2 : hmp2.get(thisPredicate))
								{
									RDFNode t2 = typeOf(n2.asResource());
									if(!list2.containsKey(t2))
									{
										list2.put(t2, new ArrayList<>());
									}
									list2.get(t2).add(n2);
								}
								Boolean br = false;
								for(RDFNode thisC : list1.keySet())
								{
									if(list2.containsKey(thisC))
									{
										if(checkIfUnwantedProperty("ANY", thisPredicate.toString(), thisC.toString())
												|| checkIfUnwantedProperty(c.toString(), thisPredicate.toString(), thisC.toString()))
										{

										}
										else
										{
											if(compareObjectsLists(list1.get(thisC), list2.get(thisC)) == true)
											{									
												LCout.addProperty(thisPredicate.toString(), thisC.toString());								
											}
											else if(checkIfNecessaryProperty("ANY", thisPredicate.toString(), "ANY")
													|| checkIfNecessaryProperty(c.toString(), thisPredicate.toString(), "ANY")
													|| checkIfNecessaryProperty("ANY", thisPredicate.toString(), thisC.toString())
													|| checkIfNecessaryProperty(c.toString(), thisPredicate.toString(), thisC.toString()))
											{
												LCout.clearAllRanges();
												br = true;
												break;	
											}
										}
									}
								}
								if(br==true) { break;}
							}
						}
					}
				}
			}
			LCout = checkForCoProperties(c.toString(), LCout);
		}
		catch(Exception ex)
		{
			System.out.println(ex.getMessage());
		}
	}
	LCout.outputLocalContextAsPatterns();
	String key = generateKey(res1.toString(), res2.toString());
		addToDB(key.getBytes(), serializeObject(LCout), "LocalContexts-OUT");
		byte[] x = getValueFromDB(key.getBytes(), "LocalContexts-OUT");
		if(x != null)
		{
			LocalContext lc = (LocalContext) deSerializeObject(x);
			System.out.println("From ROCKS-DB");
			lc.outputLocalContext();
		}
	return LCout;
}*/

	/*public LocalContext getMaxLocalContextIN(Resource res1, Resource res2)
{	
	RDFNode c = typeOf(res1);
	LocalContext LCin = null;
	if(c!= null)
	{
		LCin = new LocalContext(c.toString(), false);
		HashMap<Property, ArrayList<RDFNode>> hmp1 = new HashMap<>();
		HashMap<Property, ArrayList<RDFNode>> hmp2 = new HashMap<>();
		try
		{		
			for(StmtIterator stmtIti = model.listStatements(null, null, res1); stmtIti.hasNext();)
			{
				Statement thisStatement = stmtIti.next();
				Property thisPredicate = thisStatement.getPredicate();
				if(checkIfUnwantedProperty("ANY", thisPredicate.getURI(), "ANY")
						|| checkIfUnwantedProperty(c.toString(), thisPredicate.getURI(), "ANY"))
				{
				}
				else
				{
					if(model.contains(null, thisPredicate, res2) && !hmp1.containsKey(thisPredicate))
					{
						hmp1.put(thisPredicate, new ArrayList<>());
						hmp2.put(thisPredicate, new ArrayList<>());
						for(StmtIterator stmtItiProp = model.listStatements(null, thisPredicate, res1); stmtItiProp.hasNext();)
						{
							Statement thisStatementProp = stmtItiProp.next();
							hmp1.get(thisPredicate).add(thisStatementProp.getSubject());		
						}
						for(StmtIterator stmtItiProp = model.listStatements(null, thisPredicate, res2); stmtItiProp.hasNext();)
						{
							Statement thisStatementProp = stmtItiProp.next();
							hmp2.get(thisPredicate).add(thisStatementProp.getSubject());		
						}
						if(objectProperties.contains(thisPredicate))
						{
							HashMap<RDFNode, ArrayList<RDFNode>> list1 = new HashMap<>();
							HashMap<RDFNode, ArrayList<RDFNode>> list2 = new HashMap<>();
							for (RDFNode n1 : hmp1.get(thisPredicate))
							{
								RDFNode t1 = typeOf(n1.asResource());
								if(!list1.containsKey(t1))
								{
									list1.put(t1, new ArrayList<>());
								}
								list1.get(t1).add(n1);
							}
							for (RDFNode n2 : hmp2.get(thisPredicate))
							{
								RDFNode t2 = typeOf(n2.asResource());
								if(!list2.containsKey(t2))
								{
									list2.put(t2, new ArrayList<>());
								}
								list2.get(t2).add(n2);
							}
							Boolean br = false;
							for(RDFNode thisC : list1.keySet())
							{
								if(list2.containsKey(thisC))
								{
									if(checkIfUnwantedProperty("ANY", thisPredicate.toString(), thisC.toString())
											|| checkIfUnwantedProperty(c.toString(), thisPredicate.toString(), thisC.toString()))
									{							
									}
									else
									{
										if(compareObjectsLists(list1.get(thisC), list2.get(thisC)) == true)
										{
											LCin.addProperty(thisPredicate.toString(), thisC.toString());
										}
										else if(checkIfNecessaryProperty("ANY", thisPredicate.toString(), "ANY")
												|| checkIfNecessaryProperty(c.toString(), thisPredicate.toString(), "ANY")
												|| checkIfNecessaryProperty("ANY", thisPredicate.toString(), thisC.toString())
												|| checkIfNecessaryProperty(c.toString(), thisPredicate.toString(), thisC.toString())) 
										{
											LCin.clearAllDomains();
											br = true;
											break;	
										}
									}
								}
							}
							if(br==true) { break;}
						}
					}
				}
			}
			LCin = checkForCoProperties(c.toString(), LCin);
		}
		catch(Exception ex)
		{
			System.out.println(ex.getMessage());
		}
	}
	LCin.outputLocalContextAsPatterns();
		String key = generateKey(res1.toString(), res2.toString());
		addToDB(key.getBytes(), serializeObject(LCin), "Pair-LCin");
		byte[] x = getValueFromDB(key.getBytes(), "Pair-LCin");
		if(x != null)
		{
			LocalContext lc = (LocalContext) deSerializeObject(x);
			System.out.println("From ROCKS-DB");
			lc.outputLocalContextAsPatterns();
		}
	return LCin;
}*/
}
