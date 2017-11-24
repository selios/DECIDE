package mvcModel;

import java.util.ArrayList;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.resultset.ResultSetMem;


public class SPARQLqueries 
{

	public String endPoint;
	public static String explicitGraph = "<http://www.ontotext.com/explicit>";
	public static String prefixes = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
			+ "PREFIX owl:<http://www.w3.org/2002/07/owl#> " + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
			+ "PREFIX core: <http://opendata.inra.fr/resources/core#> "
			+ "PREFIX po2: <http://opendata.inra.fr/PO2/> " + "PREFIX IAO: <http://purl.obolibrary.org/obo/> "
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
			+ "PREFIX sesame: <http://www.openrdf.org/schema/sesame#> "
			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " ;

	public static ParameterizedSparqlString pss = new ParameterizedSparqlString(prefixes
			+ "SELECT DISTINCT ?o"
			+ " WHERE "
			+ "{ "
			+ "?resources rdf:type ?o. "
			+ "}");

	public SPARQLqueries(String endPoint)
	{
		this.endPoint = endPoint;
	}

	public ArrayList<RDFNode> listObjectsOf(String Resource, String Property)
	{
		try {
			String queryString = prefixes + "SELECT DISTINCT ?o FROM " + explicitGraph + " WHERE " + "{" + "<"
					+ Resource + "> <" + Property + "> ?o" + "}";
			ArrayList<RDFNode> nodesList = new ArrayList<>();
			Query query = QueryFactory.create(queryString);
			QueryExecution qe = QueryExecutionFactory.sparqlService(endPoint, query);
			ResultSet results = qe.execSelect();
			while (results.hasNext()) {
				QuerySolution thisRow = results.next();
				nodesList.add(thisRow.get("o"));
			}
			qe.close();
			return nodesList;
		} catch (Exception e) {
			System.out.println("Error Function: listObjectsOf" );
			e.printStackTrace();
			return null;
		}
	}
	
	public ArrayList<RDFNode> listSubjectsOf(String Property, String Resource)
	{
		try {
			String queryString = prefixes
					+ "SELECT DISTINCT ?s FROM " + explicitGraph 
					+ " WHERE "
					+ "{"
					+ "?s <"+ Property +"> <" + Resource + "> "
					+ "}";

			ArrayList<RDFNode> nodesList = new ArrayList<>();
			Query query = QueryFactory.create(queryString);
			
			QueryExecution qe = QueryExecutionFactory.sparqlService(endPoint, query);		
			ResultSet results = qe.execSelect();
			while (results.hasNext()) 
			{
				QuerySolution thisRow = results.next();
				nodesList.add(thisRow.get("s"));
			}
			qe.close();
			return nodesList;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Error Function: listSubjectsOf" );
			e.printStackTrace();
			return null;
		}
	}


	public ArrayList<String> listInstantiatedClasses()
	{
		try {
			String queryString = prefixes
					+ "SELECT DISTINCT ?o FROM " + explicitGraph
					+ " WHERE "
					+ "{"
					+ "?s rdf:type ?o "
					+ "FILTER NOT EXISTS {"
					+ "FILTER(STRSTARTS(STR(?o), 'http://www.w3.org/2002/07/owl#')) }" 
					+ "}"; 

			ArrayList<String> nodesList = new ArrayList<>();
			Query query = QueryFactory.create(queryString);
			
			QueryExecution qe = QueryExecutionFactory.sparqlService(endPoint, query);		
			ResultSet results = qe.execSelect();
			while (results.hasNext()) 
			{
				QuerySolution thisRow = results.next();
				nodesList.add(thisRow.get("o").toString());
			}
			qe.close();
			return nodesList;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Error Function: listInstantiatedClasses" );
			e.printStackTrace();
			return null;
		}
	}
	
	public ArrayList<String> listInstantiatedClasses2()
	{
		try {
			String queryString = prefixes
					+ "SELECT DISTINCT ?o FROM " + explicitGraph
					+ " WHERE "
					+ "{"
					+ "?s rdf:type ?o "
					+ "FILTER NOT EXISTS {"
					+ "FILTER(STRSTARTS(STR(?o), 'http://www.w3.org/2002/07/owl#')) }" 
					+ "}"; 

			ArrayList<String> nodesList = new ArrayList<>();
			Query query = QueryFactory.create(queryString);
			
			QueryExecution qe = QueryExecutionFactory.sparqlService(endPoint, query);		
			ResultSet results = qe.execSelect();
			while (results.hasNext()) 
			{
				QuerySolution thisRow = results.next();
				nodesList.add(thisRow.get("o").toString());
			}
			qe.close();
			return nodesList;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Error Function: listInstantiatedClasses" );
			e.printStackTrace();
			return null;
		}
	}
	
	
	public ArrayList<RDFNode> listDepClasses()
	{
		try {
			String queryString = prefixes
					+ "SELECT DISTINCT ?O "
					+ " WHERE "
					+ "{ "
					+ "?i sesame:directType ?O. "
					+ "FILTER NOT EXISTS {"
					+ " ?v sesame:directType ?B. "
					+ " ?O rdfs:subClassOf ?B. }" 
					+ "}"; 

			ArrayList<RDFNode> nodesList = new ArrayList<>();
			Query query = QueryFactory.create(queryString);
			
			QueryExecution qe = QueryExecutionFactory.sparqlService(endPoint, query);		
			ResultSet results = qe.execSelect();
			while (results.hasNext()) 
			{
				QuerySolution thisRow = results.next();
				nodesList.add(thisRow.get("O"));
			}
			qe.close();
			return nodesList;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Error Function: listDepClasses" );
			e.printStackTrace();
			return null;
		}
	}

	public Boolean containsTriple(String s, String p, String o)
	{
		try {
			String queryString = prefixes
					+ "SELECT *"
					+ " WHERE "
					+ "{"
					+ "<"+ s +"> <" + p + "> <" + o +">"
					+ "}";

			Query query = QueryFactory.create(queryString);
			
			QueryExecution qe = QueryExecutionFactory.sparqlService(endPoint, query);		
			ResultSet results = qe.execSelect();
			while (results.hasNext()) 
			{
				return true;
			}
			qe.close();
			return false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Error Function: containsTriple" );
			e.printStackTrace();
			return false;
		}
	}

	public ArrayList<Resource> listInstancesOfClass(String cl)
	{
		try {
			String queryString = prefixes
					//+ "SELECT DISTINCT ?s FROM <http://opendata.inra.fr/data/caredas-these-boisard> "
					+ "SELECT DISTINCT ?s "
					+ " WHERE "
					+ "{"
					+ "?s rdf:type <" + cl + ">"
					+ "}";

			ArrayList<Resource> nodesList = new ArrayList<>();
			Query query = QueryFactory.create(queryString);
			
			QueryExecution qe = QueryExecutionFactory.sparqlService(endPoint, query);		
			ResultSet results = qe.execSelect();
			while (results.hasNext()) 
			{
				QuerySolution thisRow = results.next();
				try
				{
					nodesList.add(thisRow.get("s").asResource());
				} catch(Exception ex)
				{

				}
			}
			qe.close();
			return nodesList;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Error Function: listInstancesOfClass" );
			e.printStackTrace();
			return null;
		}
	}

	public ResultSetMem listInstancesOfDepClass(String cl)
	{
		QueryExecution qe = null;
		try {
			String queryString = prefixes
					//+ "SELECT DISTINCT ?s FROM <http://opendata.inra.fr/data/caredas-these-boisard> "
					+ "SELECT DISTINCT ?s "
					+ " WHERE "
					+ "{ "
					+ "?s rdf:type <" + cl + ">"
					+ "}";

			Query query = QueryFactory.create(queryString);

			qe = QueryExecutionFactory.sparqlService(endPoint, query);
			ResultSetMem results = new ResultSetMem(qe.execSelect());
			qe.close();
			return results;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Error Function: listInstancesOfClass" );
			e.printStackTrace();
			return null;
		} finally {
			if(qe != null) {
				qe.close();
			}
		}
	}


	public ArrayList<Resource> listInstancesOfClassFromNamedGraph(String cl, String namedGraph)
	{
		try {
			String queryString = prefixes
					//+ "SELECT DISTINCT ?s FROM <http://opendata.inra.fr/data/caredas-these-boisard> "
					+ "SELECT DISTINCT ?s FROM <" + namedGraph + ">"
					+ " WHERE "
					+ "{"
					+ "{?s rdf:type <" + cl + ">} UNION {?s rdf:type ?c. ?c rdfs:subClassOf <" + cl + ">}"
					+ "}";

			ArrayList<Resource> nodesList = new ArrayList<>();
			Query query = QueryFactory.create(queryString);

			QueryExecution qe = QueryExecutionFactory.sparqlService(endPoint, query);
			ResultSet results = qe.execSelect();
			while (results.hasNext())
			{
				QuerySolution thisRow = results.next();
				try
				{
					nodesList.add(thisRow.get("s").asResource());
				} catch(Exception ex)
				{

				}
			}
			qe.close();
			return nodesList;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Error Function: listInstancesOfClass" );
			e.printStackTrace();
			return null;
		}
	}

	public ArrayList<String> getAllDataProperties()
	{
		try {
			String queryString = prefixes
					+ "SELECT DISTINCT ?p FROM " + explicitGraph 
					+ " WHERE "
					+ "{"
					+ "?s ?p ?o. "
					+ "FILTER isLiteral(?o) "
					+ "}";

			ArrayList<String> nodesList = new ArrayList<>();
			Query query = QueryFactory.create(queryString);
			
			QueryExecution qe = QueryExecutionFactory.sparqlService(endPoint, query);		
			ResultSet results = qe.execSelect();
			while (results.hasNext()) 
			{
				QuerySolution thisRow = results.next();
				try
				{
					nodesList.add(thisRow.get("p").toString());
				} catch(Exception ex)
				{

				}
			}
			qe.close();
			return nodesList;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Error Function: getAllDataProperties" );
			e.printStackTrace();
			return null;
		}
	}

	public ArrayList<String> getAllObjectProperties()
	{
		try {
			String queryString = prefixes
					+ "SELECT DISTINCT ?p FROM " + explicitGraph 
					+ " WHERE "
					+ "{"
					+ "?s ?p ?o. "
					+ "FILTER NOT EXISTS { FILTER isLiteral(?o).} "
					+ "FILTER NOT EXISTS {   FILTER(STRSTARTS(STR(?o), 'http://www.w3.org/2002/07/owl')).} "
				//	+ "FILTER NOT EXISTS {   FILTER(STRSTARTS(STR(?p), 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type')).} "
					+ "FILTER NOT EXISTS {   FILTER(STRSTARTS(STR(?p), 'http://www.w3.org/2000/01/rdf-schema#subClassOf')).} "
					+ "FILTER NOT EXISTS {   FILTER(STRSTARTS(STR(?p), 'http://www.w3.org/2000/01/rdf-schema#domain')).} "
					+ "FILTER NOT EXISTS {   FILTER(STRSTARTS(STR(?p), 'http://www.w3.org/2000/01/rdf-schema#range')).} "
					+ "}";

			ArrayList<String> nodesList = new ArrayList<>();
			Query query = QueryFactory.create(queryString);
			
			QueryExecution qe = QueryExecutionFactory.sparqlService(endPoint, query);		
			ResultSet results = qe.execSelect();
			while (results.hasNext()) 
			{
				QuerySolution thisRow = results.next();
				try
				{
					nodesList.add(thisRow.get("p").toString());
				} catch(Exception ex)
				{

				}
			}
			qe.close();
			return nodesList;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Error Function: getAllObjectProperties" );
			e.printStackTrace();
			return null;
		}
	}

	public int countStatements()
	{
		try {
			String queryString = prefixes
					+ "SELECT (COUNT(*) as ?count) FROM " + explicitGraph 
					+ " WHERE "
					+ "{"
					+ "?s ?p ?o. "
					+ "}";
			Query query = QueryFactory.create(queryString);
			
			QueryExecution qe = QueryExecutionFactory.sparqlService(endPoint, query);		
			ResultSet results = qe.execSelect();
			QuerySolution thisRow = results.next();
			qe.close();
			return thisRow.getLiteral("count").getInt();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Error Function: countStatements" );
			e.printStackTrace();
			return 0;
		}
	}


	public int countIndividuals()
	{
		try {
			String queryString = prefixes
					+ "SELECT (COUNT(DISTINCT ?s) as ?count) FROM " + explicitGraph 
					+ " WHERE "
					+ "{ "
					+ "?s rdf:type ?o. "
					+ "FILTER NOT EXISTS { "
					+ "FILTER(STRSTARTS(STR(?o), 'http://www.w3.org/2002/07/owl#')) }"
					+ " }";
			Query query = QueryFactory.create(queryString);
			
			QueryExecution qe = QueryExecutionFactory.sparqlService(endPoint, query);		
			ResultSet results = qe.execSelect();
			QuerySolution thisRow = results.next();
			qe.close();
			return thisRow.getLiteral("count").getInt();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Error Function: countIndividuals" );
			e.printStackTrace();
			return 0;
		}
	}

	public ArrayList<String> getCommonPropertiesOut(String res1, String res2)
	{
		try {
			String queryString = prefixes
					+ "SELECT DISTINCT ?p FROM " + explicitGraph 
					+ " WHERE "
					+ "{ "
					+ "<" + res1 + "> ?p ?o1. "
					+ "<" + res2 + "> ?p ?o2. "
				//	+ "FILTER NOT EXISTS {   FILTER(STRSTARTS(STR(?p), 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type')).} "
					+ "}";

			ArrayList<String> nodesList = new ArrayList<>();
			Query query = QueryFactory.create(queryString);
			
			QueryExecution qe = QueryExecutionFactory.sparqlService(endPoint, query);		
			ResultSet results = qe.execSelect();
			while (results.hasNext()) 
			{
				QuerySolution thisRow = results.next();
				try
				{
					nodesList.add(thisRow.get("p").toString());
				} catch(Exception ex)
				{

				}
			}
			qe.close();
			return nodesList;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Error Function: getCommonPropertiesOut" );
			e.printStackTrace();
			return null;
		}
	}
	
	public ArrayList<String> getCommonPropertiesIn(String res1, String res2)
	{
		try {
			String queryString = prefixes
					+ "SELECT DISTINCT ?p FROM " + explicitGraph 
					+ " WHERE "
					+ "{ "
					+ "?s1 ?p <" + res1 + ">. "
					+ "?s2 ?p <" + res2 + ">. "
					+ "FILTER NOT EXISTS {   FILTER(STRSTARTS(STR(?p), 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type')).} "
					+ "}";

			ArrayList<String> nodesList = new ArrayList<>();
			Query query = QueryFactory.create(queryString);
			
			QueryExecution qe = QueryExecutionFactory.sparqlService(endPoint, query);		
			ResultSet results = qe.execSelect();
			while (results.hasNext()) 
			{
				QuerySolution thisRow = results.next();
				try
				{
					nodesList.add(thisRow.get("p").toString());
				} catch(Exception ex)
				{

				}
			}
			qe.close();
			return nodesList;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Error Function: getCommonPropertiesIn" );
			e.printStackTrace();
			return null;
		}
	}

	// get the type of a resource from the dep classes
	public ArrayList<RDFNode> getListOfTypes(String res1)
	{
		try {



//			String queryString = prefixes
//					+ "SELECT DISTINCT ?o"
//					+ " WHERE "
//					+ "{ "
//					+ "<" + res1 + "> rdf:type ?o. "
//					+ "}";


			ArrayList<RDFNode> nodesList = new ArrayList<>();
			pss.setIri("resources", res1);
//			Query query = QueryFactory.create(queryString); // prend trop de temps !!!!
			
			QueryExecution qe = QueryExecutionFactory.sparqlService(endPoint, pss.asQuery());
			ResultSet results = qe.execSelect();
			while (results.hasNext()) 
			{
				QuerySolution thisRow = results.next();
				try
				{
					nodesList.add(thisRow.get("o"));
				} catch(Exception ex)
				{

				}
			}
			qe.close();
			return nodesList;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Error Function: getListOfTypes" );
			e.printStackTrace();
			return null;
		}		
	}
	
	public ArrayList<String> getAllSuperClasses(String res1)
	{
		try {
			String queryString = prefixes
					+ "SELECT DISTINCT ?O" 
					+ " WHERE "
					+ "{ "
					+ "<" + res1 + "> rdfs:subClassOf ?O. "
					//+ "FILTER NOT EXISTS {"
					//+ " ?O rdfs:subClassOf ?B. }" 
					+ "}"; 

			ArrayList<String> nodesList = new ArrayList<>();
			Query query = QueryFactory.create(queryString);
			
			QueryExecution qe = QueryExecutionFactory.sparqlService(endPoint, query);		
			ResultSet results = qe.execSelect();
			while (results.hasNext()) 
			{
				QuerySolution thisRow = results.next();
				try
				{
					nodesList.add(thisRow.get("O").toString());
				} catch(Exception ex)
				{

				}
			}
			qe.close();
			return nodesList;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Error Function: getAllSuperClasses" );
			e.printStackTrace();			
			return null;
		}		
	}
	
		
	public ResultSetMem getGlobalContextAxioms(String GC_URI)
	{
		try {
			String queryString = prefixes
					+ "SELECT ?s ?p ?o "
					+ " WHERE "
					+ "{ "
					+ "GRAPH <" + GC_URI +"> "
					+ "{ "
					+ "?p <http://www.w3.org/2000/01/rdf-schema#domain> ?s. "
					+ " OPTIONAL{ ?p <http://www.w3.org/2000/01/rdf-schema#range> ?o}"
					+ " } "
					+ "FILTER NOT EXISTS {?p <http://www.decideOutput/identiConTo> ?s}"
					+ "}"; 	
			Query query = QueryFactory.create(queryString);			
			QueryExecution qe = QueryExecutionFactory.sparqlService(endPoint, query);		
			ResultSetMem x = new ResultSetMem(qe.execSelect());
			qe.close();
			return x;
		} catch (Exception e) {
			System.out.println("Error Function: getGlobalContextAxioms" );
			e.printStackTrace();			
			return null;
		}	
	}

	public ArrayList<String> listAllObservationAttributesNOisAbout()
	{
		try {
			String queryString = prefixes
					+ "SELECT DISTINCT ?Att FROM " + explicitGraph 
					+ " WHERE "
					+ "{ "
					+ "{?obs <http://opendata.inra.fr/PO2/computedResult> ?attInstance.} UNION {?obs <http://opendata.inra.fr/PO2/observationResult> ?attInstance.}"
					+ "?attInstance rdf:type ?Att."
					+ "FILTER NOT EXISTS{?attInstance <http://purl.obolibrary.org/obo/IAO_0000136> ?IsAbout}"
					+ "}"; 

			ArrayList<String> nodesList = new ArrayList<>();
			Query query = QueryFactory.create(queryString);			
			QueryExecution qe = QueryExecutionFactory.sparqlService(endPoint, query);		
			ResultSet results = qe.execSelect();
			while (results.hasNext()) 
			{
				QuerySolution thisRow = results.next();
				try
				{
					nodesList.add(thisRow.get("Att").toString());
				} catch(Exception ex)
				{

				}
			}
			qe.close();
			return nodesList;
		} catch (Exception e) {
			System.out.println("Error Function: listAllObservationAttributesNOisAbout" );
			e.printStackTrace();			
			return null;
		}		
	}
	
	public ResultSetMem listAllObservationAttributesYESisAbout()
	{
		try {
			String queryString = prefixes
					+ "SELECT DISTINCT ?Att ?IsAbout FROM " + explicitGraph 
					+ " WHERE "
					+ "{ "
					+ "{?obs <http://opendata.inra.fr/PO2/computedResult> ?attInstance.} UNION {?obs <http://opendata.inra.fr/PO2/observationResult> ?attInstance.}"
					+ "?attInstance rdf:type ?Att. "
					+ "?attInstance <http://purl.obolibrary.org/obo/IAO_0000136> ?IsAbout"
					+ "}"; 

			Query query = QueryFactory.create(queryString);			
			QueryExecution qe = QueryExecutionFactory.sparqlService(endPoint, query);		
			ResultSetMem x = new ResultSetMem(qe.execSelect());
			qe.close();
			return x;
		} catch (Exception e) {
			System.out.println("Error Function: listAllObservationAttributesYESisAbout" );
			e.printStackTrace();			
			return null;
		}		
	}
	
	public ArrayList<String> getIsAboutOfAttribute(String attribute)
	{
		try {
			String queryString = prefixes
					+ "SELECT DISTINCT ?IsAbout FROM " + explicitGraph 
					+ " WHERE "
					+ "{ "
					+ "?obs rdf:type <" + attribute + ">. "
					+ "?obs <http://purl.obolibrary.org/obo/IAO_0000136> ?IsAbout. "
					+ "}"; 

			ArrayList<String> nodesList = new ArrayList<>();
			Query query = QueryFactory.create(queryString);			
			QueryExecution qe = QueryExecutionFactory.sparqlService(endPoint, query);		
			ResultSet results = qe.execSelect();
			while (results.hasNext()) 
			{
				QuerySolution thisRow = results.next();
				try
				{
					nodesList.add(thisRow.get("IsAbout").toString());
				} catch(Exception ex)
				{

				}
			}
			qe.close();
			return nodesList;
		} catch (Exception e) {
			System.out.println("Error Function: getIsAboutOfAttribute" );
			e.printStackTrace();			
			return null;
		}		
	}
	
	public ResultSetMem listAllGlobalContexts(String identiConTo)
	{
		try {
			String queryString = prefixes
					+ "SELECT ?G (count(?res1) as ?count)" 
					+ " WHERE "
					+ "{ "
					+ "Graph ?G {?res1 <" + identiConTo +"> ?res2}. "
					+ "} GROUP BY ?G"; 	
			Query query = QueryFactory.create(queryString);			
			QueryExecution qe = QueryExecutionFactory.sparqlService(endPoint, query);		
			ResultSetMem x = new ResultSetMem(qe.execSelect());
			qe.close();
			return x;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Error Function: listAllGlobalContexts" );
			e.printStackTrace();			
			return null;
		}		
	}
	
	
	public ArrayList<String> listAllGlobalContextsInArray(String identiConTo)
	{
		try {
			String queryString = prefixes
					+ "SELECT DISTINCT ?G" 
					+ " WHERE "
					+ "{ "
					+ "Graph ?G {?res1 <" + identiConTo +"> ?res2}. "
					+ "}"; 

			ArrayList<String> nodesList = new ArrayList<>();
			Query query = QueryFactory.create(queryString);
			
			QueryExecution qe = QueryExecutionFactory.sparqlService(endPoint, query);		
			ResultSet results = qe.execSelect();
			while (results.hasNext()) 
			{
				QuerySolution thisRow = results.next();
				try
				{
					nodesList.add(thisRow.get("G").toString());
				} catch(Exception ex)
				{

				}
			}
			qe.close();
			return nodesList;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Error Function: listAllGlobalContexts" );
			e.printStackTrace();			
			return null;
		}		
	}
	
	public int getNumberOfIdentiCalPairsMS(String GlobalContext, String identiConTo)
	{
		try {
			String queryString = prefixes
					+ "select (count(?m1) as ?count) "
					+ " WHERE "
					+ "{ "
					+ "GRAPH <" + GlobalContext + "> {?m1 <" + identiConTo +"> ?m2}"
					+ "}"; 	
			Query query = QueryFactory.create(queryString);			
			QueryExecution qe = QueryExecutionFactory.sparqlService(endPoint, query);		
			ResultSet results = qe.execSelect();
			QuerySolution thisRow = results.next();
			qe.close();
			return thisRow.getLiteral("count").getInt();
		} catch (Exception e) {
			System.out.println("Error Function: getNumberOfIdentiCalPairs" );
			e.printStackTrace();			
			return 0;
		}		
	}
	
	public ArrayList<String> getMoreSpecificContexts(String GlobalContext, String moreSpecificThan)
	{
		try {
			String queryString = prefixes
					+ "select ?G From " + explicitGraph 
					+ " WHERE "
					+ "{ "
					+ "?G <"+ moreSpecificThan +"> <" + GlobalContext + ">."
					+ "}"; 	
			ArrayList<String> nodesList = new ArrayList<>();
			Query query = QueryFactory.create(queryString);			
			QueryExecution qe = QueryExecutionFactory.sparqlService(endPoint, query);		
			ResultSet results = qe.execSelect();
			while (results.hasNext()) 
			{
				QuerySolution thisRow = results.next();
				try
				{
					nodesList.add(thisRow.get("G").toString());
				} catch(Exception ex)
				{

				}
			}
			qe.close();
			return nodesList;
		} catch (Exception e) {
			System.out.println("Error Function: getMoreSpecificContexts" );
			e.printStackTrace();			
			return null;
		}		
	}
	
	public ResultSetMem getAllIdentiConPairsOfContext(String GlobalContext, String identiConTo)
	{
		try {
			String queryString = prefixes
					+ "select ?res1 ?res2 From " + explicitGraph 
					+ " WHERE "
					+ "{ "
					+ "GRAPH <" + GlobalContext + "> {?res1 <" + identiConTo + "> ?res2}. "
					+ "}"; 	
			Query query = QueryFactory.create(queryString);			
			QueryExecution qe = QueryExecutionFactory.sparqlService(endPoint, query);		
			ResultSetMem x = new ResultSetMem(qe.execSelect());
			qe.close();
			return x;
		} catch (Exception e) {
			System.out.println("Error Function: getAllIdentiConPairsOfContext" );
			e.printStackTrace();			
			return null;
		}		
	}
	
	public ResultSetMem getMixtureObservations_Simple_UM(String mixture, String observationProperty)
	{
		try {
			String queryString = prefixes
					+ "select ?att ?isAbout ?maxK ?minK ?maxS ?minS ?UM ?scale  From " + explicitGraph 
					+ " WHERE "
					+ "{ "
					+ "?obs <http://opendata.inra.fr/PO2/observes> <" + mixture +">. "
					+ "?obs <" + observationProperty + "> ?measure. "
					+ "?measure rdf:type ?att. "
					+ "?measure <http://opendata.inra.fr/PO2/maxKernel> ?maxK."
					+ "?measure <http://opendata.inra.fr/PO2/minKernel> ?minK. "
					+ "?measure <http://opendata.inra.fr/PO2/maxSupport> ?maxS. "
					+ "?measure <http://opendata.inra.fr/PO2/minSupport> ?minS. "
					+ "?measure <http://opendata.inra.fr/PO2/hasForUnitOfMeasure> ?UM. "
					+ "OPTIONAL {?measure <http://purl.obolibrary.org/obo/IAO_0000136> ?isAbout}. "
					+ "OPTIONAL {?obs <http://opendata.inra.fr/PO2/hasForScale> ?scale} "
					+ "FILTER NOT EXISTS { ?measure <http://opendata.inra.fr/PO2/isSingularMeasureOf> ?arg}"
					+ "}"; 	
			Query query = QueryFactory.create(queryString);			
			QueryExecution qe = QueryExecutionFactory.sparqlService(endPoint, query);		
			ResultSetMem x = new ResultSetMem(qe.execSelect());
			qe.close();
			return x;
		} catch (Exception e) {
			System.out.println("Error Function: getMixtureObservations_Simple_UM" );
			e.printStackTrace();			
			return null;
		}		
	}
	
	public ResultSetMem getMixtureObservations_Simple_MesScale(String mixture, String observationProperty)
	{
		try {
			String queryString = prefixes
					+ "select ?att ?isAbout ?val ?MS ?scale From " + explicitGraph 
					+ " WHERE "
					+ "{ "
					+ "?obs <http://opendata.inra.fr/PO2/observes> <" + mixture +">. "
					+ "?obs <" + observationProperty + "> ?measure. "
					+ "?measure rdf:type ?att. "
					+ "?measure <http://opendata.inra.fr/PO2/hasForValue> ?val."
					+ "?measure <http://opendata.inra.fr/PO2/hasForMeasurementScale> ?MS. "
					+ "OPTIONAL {?measure <http://purl.obolibrary.org/obo/IAO_0000136> ?isAbout} "
					+ "OPTIONAL {?obs <http://opendata.inra.fr/PO2/hasForScale> ?scale} "
					+ "FILTER NOT EXISTS { ?measure <http://opendata.inra.fr/PO2/isSingularMeasureOf> ?arg}"
					+ "}"; 	
			Query query = QueryFactory.create(queryString);			
			QueryExecution qe = QueryExecutionFactory.sparqlService(endPoint, query);		
			ResultSetMem x = new ResultSetMem(qe.execSelect());
			qe.close();
			return x;
		} catch (Exception e) {
			System.out.println("Error Function: getMixtureObservations_Simple_MesScale" );
			e.printStackTrace();			
			return null;
		}		
	}
	
	
	public ArrayList<String> writeQuery(String queryString)
	{
		try {
			ArrayList<String> nodesList = new ArrayList<>();
			Query query = QueryFactory.create(queryString);
			
			QueryExecution qe = QueryExecutionFactory.sparqlService(endPoint, query);		
			ResultSet results = qe.execSelect();
			while (results.hasNext()) 
			{
				QuerySolution thisRow = results.next();
				try
				{
					nodesList.add(thisRow.get("G").toString());
				} catch(Exception ex)
				{

				}
			}
			qe.close();
			return nodesList;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Error Function: listAllGlobalContexts" );
			e.printStackTrace();			
			return null;
		}		
	}
	
	
	
	
	

}
