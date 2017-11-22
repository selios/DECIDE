package mvcController;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import mvcModel.*;
import org.apache.jena.rdf.model.Resource;


public class Controller
{
	private Functions theModel;
	String onto = "http://www.semanticweb.org/drugOnto/";


	public void DECIDE(String targetClass, String endPoint, String output, String[] UP, String[] NP, String[] CP, String[] UC, Map<String, String> listNamedGraph)
	{
		////////////////TIME/////////////////
		System.out.println("---- START PROGRAM ----  ");

		
		theModel = new Functions(endPoint, output);
		
		getExpertsConstraints(UP, NP, CP, UC);
		theModel.getDepClasses();
		theModel.searchTypeOfForAllResources();
		theModel.getTcInstances(targetClass);
		theModel.getAllProperties();
		outputOntologyStats();


		for(Map.Entry<String, String> namedGraph : listNamedGraph.entrySet()) {
			SPARQLqueries blabla = new SPARQLqueries(endPoint);
			List<Resource> list2 = blabla.listInstancesOfClassFromNamedGraph(targetClass, namedGraph.getValue());
			theModel.decide(namedGraph.getKey(), list2);
		}
	
		////////////////TIME/////////////////
		System.out.print("---- END PROGRAM ----  ");
		/////////////////////////////////////
	}
	
	
	public void detectPredictionRules(String ontologyEndPoint, String contextsEndPoint, String rulesOutputPath, String targetClass, String identiConTo, String moreSpecificThan)
	{
		try {
			RulesDetection theModel = new RulesDetection(ontologyEndPoint, contextsEndPoint, rulesOutputPath, targetClass, identiConTo, moreSpecificThan);
			theModel.detectRules();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	public void addGlobalContextsRelations(String contextsEndPoint, String output, String targetClass, String identiConTo, String moreSpecificThan)
	{
		RulesDetection theModel = new RulesDetection(contextsEndPoint, output, targetClass, identiConTo, moreSpecificThan);
		theModel.checkGlobalContextsRelations();
	}

	// load the raw model in the mvcModel
	public void loadModel(String modelURI)
	{
		theModel.ReadModel(modelURI);
	}

	public void outputOntologyStats()
	{
		System.out.println("");
		System.out.println("-----------------------------------");
		System.out.println("Number of Statements: " + theModel.countStatements());
		System.out.println("Number of Instantiated Classes: " + theModel.countClasses());
		System.out.println("Number of Dep Classes: " + Functions.depClasses.size());
		System.out.println("Number of Individuals: " + theModel.countIndividuals());
		System.out.println("Number of Individuals of the Target Class: " + Functions.tcInstances.size());
		int nbProperties = theModel.dataProperties.size() +  theModel.objectProperties.size();
		System.out.println("Number of Properties: " + nbProperties);
		System.out.println("Number of Object properties: " + theModel.objectProperties.size());
		System.out.println("Number of Data properties: " + theModel.dataProperties.size());
		System.out.println("-----------------------------------");
		System.out.println("");
	}
	
	public void getExpertsConstraints(String[] UP, String[] NP, String[] CP, String[] UC)
	{
		theModel.getUnwantedProperties(UP);
		theModel.getNecessaryProperties(NP);
		theModel.getCoProperties(CP);
		theModel.getUnwantedClasses(UC);
	}

}
