package mvcModel;

import java.io.*;
import java.util.ArrayList;


import org.apache.commons.io.FileUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.NodeFactory;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.tdb.TDBFactory;

public class Output 
{
	public String output;
	public String infoOutput;
	public String prefix = "http://www.decideOutput/namedGraph/Graph/";
	public Model thisModel;
	public static DatasetGraph ds;
	public Property identiConTo;
	public Property moreSpecificThan;
	public OutputStream outputStream;

	public Output(String output)
	{
		this.output = output+"Dataset";
		this.infoOutput = output+"stat.txt";
		new File(this.output).mkdirs();
		deleteDataset();
		identiConTo = ResourceFactory.createProperty("http://www.decideOutput/identiConTo");
		moreSpecificThan = ResourceFactory.createProperty("http://www.decideOutput/moreSpecificThan");
		ds = TDBFactory.createDatasetGraph(this.output) ;	
		try {
			outputStream = new FileOutputStream(output+"Identity_Contexts.nq");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}


	public static String explicitGraph = "<http://www.ontotext.com/explicit>";
	public static String prefixes = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
			+ "PREFIX owl:<http://www.w3.org/2002/07/owl#> " + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
			+ "PREFIX core: <http://opendata.inra.fr/resources/core#> "
			+ "PREFIX po2: <http://opendata.inra.fr/PO2/> " + "PREFIX IAO: <http://purl.obolibrary.org/obo/> "
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
			+ "PREFIX sesame: <http://www.openrdf.org/schema/sesame#> "
			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " ;


	public void deleteDataset()
	{
		try {
			FileUtils.deleteDirectory(new File(this.output));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public ArrayList<RDFNode> listObjectsOf(String Resource, String Property)
	{
		try {
			String queryString = prefixes + "SELECT DISTINCT ?o FROM " + explicitGraph + " WHERE " + "{" + "<"
					+ Resource + "> <" + Property + "> ?o" + "}";
			ArrayList<RDFNode> nodesList = new ArrayList<>();
			Query query = QueryFactory.create(queryString);
			QueryExecution qe = QueryExecutionFactory.sparqlService(output, query);
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


	/*
	ResourceFactory.createResource("asdasd");
	UnionClass uc = namedGraph.createUnionClass(null, new RDFList);*/

	public void writeGlobalContext(GlobalContext GC)
	{
		try {
			ds.begin(ReadWrite.WRITE);
			Node n = NodeFactory.createURI(prefix+GC.id);
			Graph namedGraph;
			if(!ds.containsGraph(n))
			{
				namedGraph = GraphFactory.createPlainGraph();
				for(String axID : GC.axioms.keySet())
				{
					Pattern ax = GC.axioms.get(axID);
					Statement st = ResourceFactory.createStatement(ResourceFactory.createResource(ax.getSubject()),ResourceFactory.createProperty(ax.getPredicate()), ResourceFactory.createResource(ax.getObject()));		
					namedGraph.add(st.asTriple());								
				}
				ds.addGraph(n, namedGraph);
				//RDFDataMgr.write(outputStream, newDS, RDFFormat.TRIG) ;
			}
			else
			{
				namedGraph = ds.getGraph(n);
				for(String axID : GC.axioms.keySet())
				{
					Pattern ax = GC.axioms.get(axID);
					Statement st = ResourceFactory.createStatement(ResourceFactory.createResource(ax.getSubject()),ResourceFactory.createProperty(ax.getPredicate()), ResourceFactory.createResource(ax.getObject()));
					namedGraph.add(st.asTriple());								
				}
				//RDFDataMgr.write(outputStream, ds, RDFFormat.TRIG) ;
			}	
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ds.commit();
			ds.end();
		}
	}


	public void addIdentityStatement(Resource resource1, Resource resource2, GlobalContext GC)
	{		
		try {
			ds.begin(ReadWrite.WRITE);
			Node n = NodeFactory.createURI(prefix+GC.id);
			Graph namedGraph;
			if(!ds.containsGraph(n))
			{
				namedGraph = GraphFactory.createPlainGraph();
				Statement st = ResourceFactory.createStatement(resource1,identiConTo,resource2);
				namedGraph.add(st.asTriple());	
				ds.addGraph(n, namedGraph);
				//RDFDataMgr.write(outputStream, namedGraph, RDFFormat.TRIG) ;
			}
			else
			{
				namedGraph = ds.getGraph(n);
				Statement st = ResourceFactory.createStatement(resource1,identiConTo,resource2);
				namedGraph.add(st.asTriple());								
				//RDFDataMgr.write(outputStream, ds.getGraph(n), RDFFormat.TRIG) ;
			}	
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			//RDFDataMgr.writeTriples(out, iterator);
			ds.commit();
			ds.end();
		}		
	}

	//GC1 more specific than GC2
	public void addHierarchyStatement(String GC1_ID, String GC2_ID)
	{		
		try {
			ds.begin(ReadWrite.WRITE);
			Node n1 = NodeFactory.createURI(prefix+GC1_ID);
			Node n2 = NodeFactory.createURI(prefix+GC2_ID);
			if(ds.containsGraph(n1) && ds.containsGraph(n2))
			{			
				Statement st = ResourceFactory.createStatement(ResourceFactory.createResource(n1.getURI()),moreSpecificThan,ResourceFactory.createResource(n2.getURI()));
				ds.getDefaultGraph().add(st.asTriple());	
				//RDFDataMgr.write(outputStream, namedGraph, RDFFormat.TRIG) ;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			//RDFDataMgr.writeTriples(out, iterator);
			ds.commit();
			ds.end();
		}		
	}

	//GC1 more specific than GC2
	public void addHierarchyStatement(GlobalContext GC1, GlobalContext GC2)
	{		
		try {
			ds.begin(ReadWrite.WRITE);
			Node n1 = NodeFactory.createURI(prefix+GC1.id);
			Node n2 = NodeFactory.createURI(prefix+GC2.id);
			Statement st = ResourceFactory.createStatement(ResourceFactory.createResource(n1.getURI()),moreSpecificThan,ResourceFactory.createResource(n2.getURI()));
			ds.getDefaultGraph().add(st.asTriple());	
			//RDFDataMgr.write(outputStream, namedGraph, RDFFormat.TRIG) ;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			//RDFDataMgr.writeTriples(out, iterator);
			ds.commit();
			ds.end();
		}		
	}

	public void writeDataSet(String statInfo)
	{
		ds.begin(ReadWrite.WRITE);
		RDFDataMgr.write(outputStream, ds, RDFFormat.TRIG) ;
		ds.clear();
		ds.commit();
		ds.end();

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(infoOutput);
			writer.println(statInfo);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			writer.close();
		}

	}
	
	
	public void writeDataSet()
	{
		ds.begin(ReadWrite.WRITE);
		RDFDataMgr.write(outputStream, ds, RDFFormat.TRIG) ;
		ds.clear();
		ds.commit();
		ds.end();
	}

}
