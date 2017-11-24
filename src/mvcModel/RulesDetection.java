package mvcModel;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import com.google.common.math.BigIntegerMath;

public class RulesDetection 
{
	public String ontologyEndPoint;
	public String identityLinksEndPoint;
	public String outputPath;
	public String targetClass;
	public String identiConTo;
	public String moreSpecificThan;
	public SPARQLqueries QUERYontology;
	public SPARQLqueries QUERYcontexts;
	public MySQLqueries QUERYdb;
	public HashMap<String, Integer> allGlobalContexts;
	public List<Resource> AllTCinstances;

	public RulesDetection(String ontologyEndPoint, String contextsEndPoint, String rulesOutputPath, String targetClass, String identiConToProperty, String moreSpecificThan) throws ClassNotFoundException, SQLException
	{
		this.targetClass = targetClass;
		this.identiConTo = identiConToProperty;
		this.moreSpecificThan = moreSpecificThan;
		this.ontologyEndPoint = ontologyEndPoint;
		this.identityLinksEndPoint = contextsEndPoint;
		this.outputPath = rulesOutputPath;
		this.QUERYontology = new SPARQLqueries(ontologyEndPoint);
		this.QUERYcontexts = new SPARQLqueries(contextsEndPoint);
		this.allGlobalContexts = new HashMap<>();
		this.AllTCinstances = new ArrayList<>();
		QUERYdb = new MySQLqueries(false); 
	}

	public RulesDetection(String contextsEndPoint, String globalContextsOutput, String targetClass, String identiConToProperty, String moreSpecificThan)
	{
		this.QUERYcontexts = new SPARQLqueries(contextsEndPoint);
		this.outputPath = globalContextsOutput;
		this.targetClass = targetClass;
		this.identiConTo = identiConToProperty;
		this.moreSpecificThan = moreSpecificThan;
	}


	public void detectRules()
	{			
		addAllAttributesToDB();
		addAllTCinstancesToDB();
		addTCObservationsToDB();
		addMinMaxForEachAttribute();
		addAllGlobalContextsToDB();
		addGlobalContextsRelations();
		compareIdentiConPairs();
		generatePredictionRules();
	}
	
	
	public void checkRuleFromExpert()
	{
		String prefixes = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX owl:<http://www.w3.org/2002/07/owl#> " + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
				+ "PREFIX core: <http://opendata.inra.fr/resources/core#> "
				+ "PREFIX po2: <http://opendata.inra.fr/PO2/> " + "PREFIX IAO: <http://purl.obolibrary.org/obo/> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "PREFIX sesame: <http://www.openrdf.org/schema/sesame#> "
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " ;
		/*String queryString = prefixes
				+ "SELECT DISTINCT ?G" 
				+ " WHERE "
				+ "{ "
				+ "Graph ?G {"
				+ " <http://opendata.inra.fr/PO2/isComposedOf> rdfs:domain <http://opendata.inra.fr/PO2/mixture>."
				+ " <http://opendata.inra.fr/PO2/isComposedOf> rdfs:range <http://aims.fao.org/aos/agrovoc/c_8309>."
				+ " <http://opendata.inra.fr/PO2/minSupport> rdfs:domain <http://opendata.inra.fr/PO2/quality_teneur>."
				+ " <http://opendata.inra.fr/PO2/minSupport> rdfs:range <http://www.w3.org/2000/01/rdf-schema#Literal>"
						+ "}. "
				+ "}"; */
		String queryString = prefixes
				+ "SELECT DISTINCT ?G" 
				+ " WHERE { "
				+ "Graph ?G {"
				+ " <http://opendata.inra.fr/PO2/isComposedOf> rdfs:domain <http://opendata.inra.fr/PO2/mixture>."
				+ " <http://opendata.inra.fr/PO2/isComposedOf> rdfs:range <http://opendata.inra.fr/PO2/eau_deionisee>."
				+ " <http://opendata.inra.fr/PO2/minSupport> rdfs:domain <http://opendata.inra.fr/PO2/quality_teneur>."
				+ " <http://opendata.inra.fr/PO2/minSupport> rdfs:range <http://www.w3.org/2000/01/rdf-schema#Literal>"
						+ "}  filter not exists {"
						+ "Graph ?G2 {"
						+ " <http://opendata.inra.fr/PO2/isComposedOf> rdfs:domain <http://opendata.inra.fr/PO2/mixture>."
						+ " <http://opendata.inra.fr/PO2/isComposedOf> rdfs:range <http://opendata.inra.fr/PO2/eau_deionisee>."
						+ " <http://opendata.inra.fr/PO2/minSupport> rdfs:domain <http://opendata.inra.fr/PO2/quality_teneur>."
						+ " <http://opendata.inra.fr/PO2/minSupport> rdfs:range <http://www.w3.org/2000/01/rdf-schema#Literal>"
								+ "} "	
						+ "  ?G <http://www.decideOutput/moreSpecificThan> ?G2 "
						+ "filter (?G != ?G2) } }"; 
		//Collection<String> allContexts = QUERYcontexts.writeQuery(queryString);
		// allContexts.removeAll(allContexts2);
		Collection<String> allContexts = QUERYcontexts.writeQuery(queryString);
	
		String query = "SELECT AVG(AVG_ERROR_RATE), AVG(SUPPORT), sum(TOTAL_NB) "
				+ "FROM rules "
				+ "WHERE ID_ATT =20 AND (";
		for(String GC : allContexts)
		{
			query = query + " (SELECT ID_GC FROM global_context "
					+ "WHERE URI_GC= '" + GC + "') OR";
			System.out.println(GC);
		}
		query =  query.substring(0, query.length() - 2);
		query = query + ")";
		System.out.println();
		
	}


	public void checkGlobalContextsRelations()
	{
		System.out.println("Checking Global Contexts Relations...");
		compareAllGlobalContexts();
		System.out.println("");
		System.out.println("Checking Global Contexts Relations... Done");
	}


	public void addAllAttributesToDB()
	{
		System.out.print("1) Adding all attributes to DB... ");
		Boolean empty = true;
		ArrayList<String> attributes1 = QUERYontology.listAllObservationAttributesNOisAbout();
		QUERYdb.addAllAttributesToDB(attributes1);
		ResultSet results = QUERYontology.listAllObservationAttributesYESisAbout();
		String query = "INSERT INTO attributes"
				+ "(URI_ATT, IS_ABOUT, MIN_VALUE, MAX_VALUE) "
				+ "VALUES ";
		while(results.hasNext())
		{
			empty = false;
			QuerySolution thisRow = results.next();
			query = query + " ('" + thisRow.get("Att") +"',"
					+ " '"+ thisRow.get("IsAbout") +"', '0', '0'),";
		}
		if(empty == false)
		{
			query =  query.substring(0, query.length() - 1);
			QUERYdb.executeUpdate(query);
		}		
		System.out.println("Done");
	}

	public void addAllTCinstancesToDB()
	{
		System.out.print("2) Adding all target class instances to DB... ");
		AllTCinstances = QUERYontology.listInstancesOfClass(this.targetClass);
		String query = "INSERT INTO target_class"
				+ "(URI_TC) VALUES ";
		for(Resource TCinstance : AllTCinstances)
		{
			query = query + " ('" + TCinstance.getURI() + "'),";	
		}
		query =  query.substring(0, query.length() - 1);
		QUERYdb.executeUpdate(query);
		System.out.println("Done");
	}


	public void addTCObservationsToDB()
	{
		System.out.print("3) Getting all observations on the target class instances to DB... ");
		for(Resource TCinstance : AllTCinstances)
		{
			try
			{							
				getAllTCSimpleObservationValues(TCinstance.getURI(), "http://opendata.inra.fr/PO2/computedResult");
				getAllTCSimpleObservationValues(TCinstance.getURI(), "http://opendata.inra.fr/PO2/observationResult");
			} catch (Exception ex)
			{
				ex.getMessage();
				System.out.println("Error Function: addTCObservationsToDB" );
			}
		}
		System.out.println("Done");
	}

	public void addMinMaxForEachAttribute()
	{
		System.out.print("4) Adding the MIN and MAX values of each attribute in the DB... ");
		try
		{
			int numAttributes = QUERYdb.countNumberOfAttributes();
			String query = "INSERT INTO attributes (ID_ATT,MIN_VALUE, MAX_VALUE) VALUES";
			for(int attID = 1; attID<= numAttributes; attID++)
			{
				float[] min_max = QUERYdb.getMinMaxOfAttribute(attID);
				query = query + " ('" + attID + "', '" 
						+ min_max[0] + "', '" + min_max[1] +"'),";
			}
			query = query.substring(0, query.length() - 1);
			query = query + " ON DUPLICATE KEY UPDATE MIN_VALUE=VALUES(MIN_VALUE),MAX_VALUE=VALUES(MAX_VALUE)";
			QUERYdb.executeUpdate(query);
		}catch(Exception ex)
		{
			ex.getMessage();
		}
		System.out.println("Done");
	}

	//GOOD
	/*public void addAllGlobalContextsToDB()
	{
		System.out.print("5)a) Adding all Global Contexts to DB... ");
		ArrayList<String> gcList = new ArrayList<>();
		gcList.addAll(QUERYcontexts.listAllGlobalContexts(identiConTo));
		for(String GC : gcList)
		{	
			int NB_IDENTICAL_PAIRS_MS = QUERYcontexts.getNumberOfIdentiCalPairsMS(GC, identiConTo);
			int NB_IDENTICAL_PAIRS_INFERENCE = NB_IDENTICAL_PAIRS_MS + QUERYcontexts.getNumberOfIdentiCalPairsWithInference(GC, identiConTo, moreSpecificThan);
			String query = "INSERT INTO global_context"
					+ "(URI_GC, NB_IDENTICAL_PAIRS_INFERENCE, NB_IDENTICAL_PAIRS_MS) "
					+ "VALUES('" + GC +"', '" + NB_IDENTICAL_PAIRS_INFERENCE +"', '" + NB_IDENTICAL_PAIRS_MS + "')";
			allGlobalContexts.put(GC, QUERYdb.executeUpdateAndReturnID(query));		
		}
		System.out.println("Done");
	}*/


/*	public void addAllGlobalContextsToDB()
	{
		System.out.print("5)a) Adding all Global Contexts to DB... ");
		int counter=1;
		String query = "INSERT INTO global_context"
				+ "(ID_GC, URI_GC, NB_IDENTICAL_PAIRS_INFERENCE, NB_IDENTICAL_PAIRS_MS) "
				+ "VALUES";
		ResultSet rs = QUERYcontexts.listAllGlobalContexts(identiConTo);
		while (rs.hasNext()) 
		{
			QuerySolution thisRow = rs.next();
			try
			{
				String GC = thisRow.get("G").toString();
				int NB_IDENTICAL_PAIRS_MS = QUERYcontexts.getNumberOfIdentiCalPairsMS(GC, identiConTo);
				query = query + " ('" + counter + "', '" + GC +"', '0', '" + NB_IDENTICAL_PAIRS_MS + "'),";
				allGlobalContexts.put(GC, counter);
				counter ++;	
			} catch(Exception ex)
			{

			}
		}
		query =  query.substring(0, query.length() - 1);
		QUERYdb.executeUpdate(query);
		System.out.println("Done");
	}*/
	
	
	public void addAllGlobalContextsToDB()
	{
		System.out.print("5)a) Adding all Global Contexts to DB... ");
		int counter=1;
		String query = "INSERT INTO global_context"
				+ "(ID_GC, URI_GC, NB_IDENTICAL_PAIRS_INFERENCE, NB_IDENTICAL_PAIRS_MS) "
				+ "VALUES";
		ResultSet rs = QUERYcontexts.listAllGlobalContexts(identiConTo);
		while (rs.hasNext()) 
		{
			QuerySolution thisRow = rs.next();
			try
			{
				String GC = thisRow.get("G").toString();
				int NB_IDENTICAL_PAIRS_MS = thisRow.getLiteral("count").getInt();
				//int NB_IDENTICAL_PAIRS_MS = QUERYcontexts.getNumberOfIdentiCalPairsMS(GC, identiConTo);
				query = query + " ('" + counter + "', '" + GC +"', '0', '" + NB_IDENTICAL_PAIRS_MS + "'),";
				allGlobalContexts.put(GC, counter);
				counter ++;	
			} catch(Exception ex)
			{

			}
		}
		query =  query.substring(0, query.length() - 1);
		QUERYdb.executeUpdate(query);
		System.out.println("Done");
	}


	public void addGlobalContextsRelations()
	{
		System.out.print("5)b) Adding Global Contexts Relations... ");
		for(String GC : allGlobalContexts.keySet())
		{
			int moreSpecificID;
			//GC = "http://www.decideOutput/namedGraph/Graph/530093453";
			int NB_IDENTICAL_PAIRS_INFERENCE = 0;
			try
			{
				ArrayList<String> moreSpecificContexts = QUERYcontexts.getMoreSpecificContexts(GC, moreSpecificThan);		
				int thisGC_ID = allGlobalContexts.get(GC);					
				String queryCountInference = "SELECT SUM(NB_IDENTICAL_PAIRS_MS) "
						+ "FROM global_context WHERE "
						+ "ID_GC = " + thisGC_ID;
				if(!moreSpecificContexts.isEmpty())
				{				
					String query = "INSERT INTO global_context_relations"
							+ "(ID_GC, MORE_SPECIFIC_THAN) "
							+ "VALUES ";
					for(String moreSpecificGC: moreSpecificContexts)
					{
						if(allGlobalContexts.get(moreSpecificGC) != null)
						{
							moreSpecificID = allGlobalContexts.get(moreSpecificGC);
							query = query + "('" + moreSpecificID + "', '" + thisGC_ID +"'),";
							queryCountInference = queryCountInference + " OR ID_GC=" + moreSpecificID;
						}
						else
						{
							System.out.println("The GC <" + moreSpecificGC +"> does not contain any triples!!");
						}
					}
					query =  query.substring(0, query.length() - 1);
					QUERYdb.executeUpdate(query);								
				}
				NB_IDENTICAL_PAIRS_INFERENCE = QUERYdb.getSum(queryCountInference);
				QUERYdb.updateInferenceNumber(thisGC_ID, NB_IDENTICAL_PAIRS_INFERENCE);
			} catch (Exception ex)
			{
				ex.getMessage();
				System.out.println("Error add Global Contexts Relations " + GC);
			}
		}
		System.out.println("Done");
	}

	//GOOD
	/*	public void compareIdentiConPairs()
	{
		System.out.print("6) Comparing the observations of each pair... ");
		//int counter = 1;
		for(String GC : allGlobalContexts.keySet())
		{
			//System.out.println("GC " + counter);
			ArrayList<String> lessSpecificContexts = QUERYcontexts.getLessSpecificContexts(GC, moreSpecificThan);
			ResultSet results = QUERYcontexts.getAllIdentiConPairsOfContext(GC, identiConTo);
			while(results.hasNext())
			{
				QuerySolution thisRow = results.next();
				RDFNode res1 = thisRow.get("res1");
				RDFNode res2 = thisRow.get("res2");
				ArrayList<Integer> commonProperties = QUERYdb.getCommonAttributes(res1.toString(), res2.toString());
				for(Integer attID : commonProperties)
				{
					float errorRate = getErrorRate(QUERYdb.getValueOfAttribute(res1.toString(), attID), QUERYdb.getValueOfAttribute(res2.toString(), attID), attID);
					QUERYdb.insertErrorRateToGC(allGlobalContexts.get(GC), attID, errorRate, res1.toString(), res2.toString());
					for(String lessSpecificGC : lessSpecificContexts)
					{
						QUERYdb.insertErrorRateToGC(allGlobalContexts.get(lessSpecificGC), attID, errorRate, res1.toString(), res2.toString());
					}
				}
			}
			//counter ++;
		}
		System.out.println("Done");
	}*/


	public void compareIdentiConPairs()
	{
		System.out.print("6) Comparing the observations of each pair... ");

		int ruleID, GC_ID, counter=0;
		int totalGCs = allGlobalContexts.size();
		for(String GC : allGlobalContexts.keySet())
		{
			GC_ID = allGlobalContexts.get(GC);
			System.out.println(counter++ +") GC[" + GC_ID + "] <" + GC + "> / " + totalGCs);
			ResultSet results = QUERYcontexts.getAllIdentiConPairsOfContext(GC, identiConTo);
			while(results.hasNext())
			{
				String query = "INSERT INTO global_context_observations "
						+ "(ID_GC_ATT, ID_GC, ID_ATT, TOTAL_VALUE, TOTAL_NB) VALUES";
				QuerySolution thisRow = results.next();
				HashMap<Integer, Float> commonAttributes = QUERYdb.getCommonAttributes(thisRow.get("res1").toString(), thisRow.get("res2").toString());			
				if(!commonAttributes.isEmpty())
				{
					for (HashMap.Entry<Integer, Float> entry : commonAttributes.entrySet())
					{
						ruleID = ("GC_"+GC_ID+"ATT_"+entry.getKey()).hashCode();
						query = query + " (" + ruleID + "," +GC_ID+","+entry.getKey()+","+ entry.getValue()+", 1),";
					}
					query = query.substring(0, query.length() - 1);
					query = query + " ON DUPLICATE KEY UPDATE TOTAL_VALUE = TOTAL_VALUE+VALUES(TOTAL_VALUE), TOTAL_NB = TOTAL_NB+VALUES(TOTAL_NB);";
					QUERYdb.executeUpdate(query);
				}
			}
		}
		System.out.println("Done");
	}


	public void generatePredictionRules()
	{
		System.out.print("7) Generating the prediction rules... ");
		for(String GC : allGlobalContexts.keySet())
		{			
			Boolean empty = true;
			int GC_ID = allGlobalContexts.get(GC);
			int numberOfPairsWithInf = QUERYdb.getNumberOfIdenticalPairsInference(GC_ID);
			String query = "INSERT INTO rules "
					+ "(ID_GC, ID_ATT, AVG_ERROR_RATE, SUPPORT, TOTAL_NB) VALUES";
			java.sql.ResultSet rs = QUERYdb.getRules(GC_ID);
			try {
				while(rs.next())
				{
					empty = false;
					float avgErrorRate = calculateErrorRate(rs.getFloat(2), rs.getInt(3), rs.getFloat(4), rs.getFloat(5));
					float support = (float) rs.getInt(3) / numberOfPairsWithInf;
					query = query + " (" + GC_ID + "," +rs.getInt(1)+","+avgErrorRate+","+ support+"," + rs.getInt(3) +"),";
				}
				if(empty == false)
				{
					query = query.substring(0, query.length() - 1);					
					QUERYdb.executeUpdate(query);
				}								
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Done");
	}


	/*public void generatePredictionRules()
	{
		System.out.print("7) Generating the prediction rules... ");
		for(String GC : allGlobalContexts.keySet())
		{
			int GC_ID = allGlobalContexts.get(GC);
			java.sql.ResultSet rs = QUERYdb.getAverageErrorRate(GC_ID);
			try {
				while(rs.next())
				{
					float avgErrorRate = rs.getFloat(2);
					int numberOfPairs = QUERYdb.getNumberOfPairsInGCWithID(GC_ID, rs.getInt(1));
					int numberOfIdenticalPairsInference = QUERYdb.getNumberOfIdenticalPairsInference(GC_ID);
					double support = Math.round(((double)numberOfPairs / numberOfIdenticalPairsInference)*100)/100D;
					QUERYdb.addNewRule(GC_ID, rs.getInt(1), avgErrorRate, support, numberOfPairs+" / " +numberOfIdenticalPairsInference);
				}
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
				System.out.println("Error Function: generatePredictionRules" );
			}
		}
		System.out.println("Done");
	}*/


	public void getNumberOfPairsWithInference()
	{

	}

	public void getAllTCSimpleObservationValues(String TCinstance, String obsProperty) throws SQLException
	{
		int type = 0;
		Boolean add = false;
		if(obsProperty == "http://opendata.inra.fr/PO2/computedResult")
		{
			type =1;
		}	
		String typeString = Integer.toBinaryString(type);
		ResultSet results = QUERYontology.getMixtureObservations_Simple_UM(TCinstance, obsProperty);
		String query = "INSERT INTO tc_observations"
				+ " (ID_TC, ID_ATT, VALUE, MEASURE_UNIT, OBSERVATION_TYPE, SCALE)"
				+ " VALUES";
		int TCinstanceID = QUERYdb.getTCFromDB(TCinstance);
		while(results.hasNext())
		{
			QuerySolution thisRow = results.next();
			RDFNode att = thisRow.get("att");
			RDFNode isAbout = thisRow.get("isAbout");
			float value = getFuzzyValue(thisRow.get("maxK").toString(),thisRow.get("minK").toString(),thisRow.get("maxS").toString(),thisRow.get("minS").toString());
			RDFNode unit = thisRow.get("UM");
			RDFNode scale = thisRow.get("scale");
			int attributeID = 0;
			if(att!= null)
			{	
				if(isAbout!= null)
				{
					attributeID = getAttributeFromDb(att.toString(), isAbout.toString());
				}
				else
				{
					attributeID = getAttributeFromDb(att.toString(),"");
				}
				add = true;
				query = query + " ('" + TCinstanceID +"', '" + attributeID +"', '" 
						+ value + "', '" + unit + "', '" + typeString 
						+ "', '" + scale +"'),";				
			}
			if(attributeID == 0)
			{
				System.out.println("Error Finding Attribute in DB");
			}
		}
		/*results = QUERYontology.getMixtureObservations_Simple_MesScale(TCinstance, obsProperty);
		while(results.hasNext())
		{
			QuerySolution thisRow = results.next();
			RDFNode att = thisRow.get("att");
			RDFNode isAbout = thisRow.get("isAbout");
			float value = thisRow.get("val").asLiteral().getFloat();
			RDFNode unit = thisRow.get("MS");
			RDFNode scale = thisRow.get("scale");
			int attributeID = 0;
			if(att!= null)
			{
				if(isAbout!= null)
				{
					attributeID = getAttributeFromDb(att.toString(), isAbout.toString());
				}
				else
				{
					attributeID = getAttributeFromDb(att.toString(),"");
				}
				add = true;
				query = query + " ('" + TCinstanceID +"', '" + attributeID +"', '" 
						+ value + "', '" + unit + "', '" + typeString 
						+ "', '" + scale +"'),";			
			}
			if(attributeID == 0)
			{
				System.out.println("Error Finding Attribute in DB");
			}
		}*/
		if(add == true)
		{
			query =  query.substring(0, query.length() - 1);
			QUERYdb.executeUpdate(query);
		}
	}


	public float getFuzzyValue(String maxKernel, String minKernel, String maxSupport, String minSupport)
	{
		try
		{
			Float minS = Float.valueOf(minSupport);
			Float minK = Float.valueOf(minKernel);
			Float maxK = Float.valueOf(maxKernel);
			Float maxS = Float.valueOf(maxSupport);
			return (minS + 2 * minK + 2 * maxK + maxS) / 6;
		}
		catch(Exception ex)
		{
			System.out.print("!! Fuzzy values are not numbers !! ");
			System.out.print("MaxKernel: " +maxKernel);
			System.out.print("  - MinKernel: " +minKernel);
			System.out.print("  - MaxSupport: " +maxSupport);
			System.out.println("  - MinSupport: " +minSupport);
			return (maxKernel+minKernel+maxSupport+minSupport).hashCode();
		}
	}

	public int getAttributeFromDb(String attribute, String isAbout)
	{
		try {
			return QUERYdb.getAttributeFromDB(attribute, isAbout);
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}


	public float calculateErrorRate(float total_value, int total_nb, float min_value, float max_value)
	{		
		return ((total_value/total_nb)*100)/(max_value - min_value);
	}

	public float getErrorRate(float v1, float v2, int attID)
	{		
		float[] min_max;
		try {
			min_max = QUERYdb.getMinMaxOfAttributeFromAtt(attID);
			return(Math.abs(v1 - v2)*100)/Math.abs(min_max[1] - min_max[0]);
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Error get Error Rate of attribute id: "+attID);
			return 0;
		}		
	}



	/*public TreeMap<String, GlobalContext> getAllGlobalContextsFromFile()
	{
		TreeMap<String, GlobalContext> allGlobalContexts = new TreeMap<>();		
		ResultSet rs = QUERYcontexts.listAllGlobalContexts(identiConTo);
		while (rs.hasNext()) 
		{
			try
			{
				QuerySolution thisRow = rs.next();
				String namedGraph = thisRow.get("G").toString();
				GlobalContext gc = getGlobalContext(namedGraph, targetClass);
				gc.addAllLocalContextsAxioms();
				allGlobalContexts.put(gc.id, gc);
			} catch(Exception ex)
			{

			}
		}
		return allGlobalContexts;
	}*/


	public void compareAllGlobalContexts()
	{
		long startTime = System.currentTimeMillis();
		Output MyOutput = new Output(this.outputPath);
		ArrayList<GlobalContext> GClist = getAllGlobalContextsFromFile();
		ArrayList<GlobalContext> GClist2 = new ArrayList<>();
		GClist2.addAll(GClist);
		Integer counter = 0;
		int numbIndivs = GClist.size();
		Integer numPairs = (numbIndivs * (numbIndivs -1)) / 2;	
		for (GlobalContext gc1 :GClist) 
		{
			GClist2.remove(gc1);
			for (GlobalContext gc2 : GClist2)  
			{	
				counter ++;
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
				if(gc1.checkIfMoreSpecificThan(gc2))
				{
					MyOutput.addHierarchyStatement(gc1, gc2);
				}
				else
				{
					if(gc2.checkIfMoreSpecificThan(gc1))
					{
						MyOutput.addHierarchyStatement(gc2, gc1);
					}
				}
			}
		}
		MyOutput.writeDataSet();
	}


	public ArrayList<GlobalContext> getAllGlobalContextsFromFile()
	{
		ArrayList<GlobalContext> allGlobalContexts = new ArrayList<>();
		ArrayList<String> namedGraphsList = QUERYcontexts.listAllGlobalContextsInArray(identiConTo);
		for(String namedGraph: namedGraphsList)
		{
			GlobalContext gc = getGlobalContext(namedGraph, targetClass);
			gc.addAllLocalContextsAxioms();
			allGlobalContexts.add(gc);
/*			System.out.println(namedGraph + " || " + gc.id);
			System.out.println(namedGraph.equals("http://www.decideOutput/namedGraph/Graph/"+gc.id));*/			
		}
		return allGlobalContexts;
	}


	public GlobalContext getGlobalContext(String namedGraph, String targetClass)
	{
		GlobalContext gc = new GlobalContext(targetClass);	
		ResultSet rs = QUERYcontexts.getGlobalContextAxioms(namedGraph);
		gc.getAxiomsFromFile(rs);
		return gc;
	}



}
