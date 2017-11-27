import mvcController.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainProgram
{
	public static void main(String[] args)
	{
		Controller theController = new Controller();
		//String output = "data/";
		String outputServeur = "/opt/joe/";
		String output = "data/";
		Map<String, String> listNamedGraph = new HashMap<>();
		listNamedGraph.put("caredas-these-florence-barbe", "http://opendata.inra.fr/data/caredas-these-florence-barbe");
		//listNamedGraph.put("caredas-bigaski","http://opendata.inra.fr/data/caredas-bigaski");
		//listNamedGraph.put("caredas-true-food-2","http://opendata.inra.fr/data/caredas-true-food-2");
		//listNamedGraph.put("caredas-lawrence","http://opendata.inra.fr/data/caredas-lawrence");
		//listNamedGraph.put("caredas-prasel","http://opendata.inra.fr/data/caredas-prasel");
		//listNamedGraph.put("caredas-sim","http://opendata.inra.fr/data/caredas-sim");
		//listNamedGraph.put("caredas-mosca","http://opendata.inra.fr/data/caredas-mosca");
		//listNamedGraph.put("caredas-gierczynski","http://opendata.inra.fr/data/caredas-gierczynski");
		//listNamedGraph.put("caredas-tarrega","http://opendata.inra.fr/data/caredas-tarrega");
		//listNamedGraph.put("caredas-phan","http://opendata.inra.fr/data/caredas-phan");
		//listNamedGraph.put("caredas-these-boisard","http://opendata.inra.fr/data/caredas-these-boisard");


		//theController.DECIDE("data/PO2_Carredas.rdf", "Carredas");

		//String targetClass = "http://opendata.inra.fr/PO2/mixture";
		//theController.DECIDE(targetClass, "data/PO2_CellExtraDry_full.rdf");

		String[] UP = new String[8];
		//UP[0]= "ANY***http://www.semanticweb.org/drugOnto/hasDrug***ANY";
		//UP[1]= "http://www.semanticweb.org/drugOnto/Drug***http://www.semanticweb.org/drugOnto/isComposedOf***http://www.semanticweb.org/drugOnto/Paracetamol";
		//UP[2]= "http://www.semanticweb.org/drugOnto/Drug***http://www.semanticweb.org/drugOnto/hasValue***ANY";
		UP[3] = "ANY***http://www.w3.org/2006/time#intervalBefore***ANY";

		String[] NP = new String[8];
		//NP[0]= "ANY***http://www.semanticweb.org/drugOnto/name***ANY";
		//NP[1]= "http://www.semanticweb.org/drugOnto/Drug***http://www.semanticweb.org/drugOnto/isComposedOf***http://www.semanticweb.org/drugOnto/Paracetamol";
		//NP[2]= "http://www.semanticweb.org/drugOnto/Paracetamol***http://www.semanticweb.org/drugOnto/hasWeight***http://www.semanticweb.org/drugOnto/Weight";
		//NP[3]= "ANY***http://www.semanticweb.org/drugOnto/hasDrug***ANY";
		//NP[4]= "http://www.semanticweb.org/drugOnto/Weight***http://www.semanticweb.org/drugOnto/hasValue***ANY";

		String[] CP = new String[8];
		//CP[0] = "ANY===http://www.semanticweb.org/drugOnto/name***ANY!!!http://www.semanticweb.org/drugOnto/isComposedOf***http://www.semanticweb.org/drugOnto/Paracetamol";
		//CP[1]= "http://www.semanticweb.org/drugOnto/Drug===http://www.semanticweb.org/drugOnto/isComposedOf***http://www.semanticweb.org/drugOnto/Paracetamol!!!http://www.semanticweb.org/drugOnto/name***ANY";
		//CP[2]= "ANY===http://www.semanticweb.org/drugOnto/hasValue***ANY!!!http://www.semanticweb.org/drugOnto/hasUnit***ANY";
		//CP[3]= "ANY===http://opendata.inra.fr/PO2/maxKernel***ANY!!!http://opendata.inra.fr/PO2/minKernel***ANY!!!http://opendata.inra.fr/PO2/maxSupport***ANY!!!http://opendata.inra.fr/PO2/minSupport***ANY!!!http://opendata.inra.fr/PO2/hasForUnitOfMeasure***ANY";

		String[] UC = new String[8];
		//UC[0] = "http://www.semanticweb.org/drugOnto/Paracetamol";
		UC[1] = "http://opendata.inra.fr/PO2/observation";


		//theController.DECIDE("http://www.semanticweb.org/drugOnto/Drug", "http://192.168.1.120:7200/repositories/Example_Drugs", output, UP, NP, CP, UC, listNamedGraph);

	
		
		theController.DECIDE("http://opendata.inra.fr/PO2/step", "http://193.54.111.143:7200/repositories/PO2", output, UP, NP, CP, UC, listNamedGraph);
			
		//theController.DECIDE("http://opendata.inra.fr/PO2/step", "http://127.0.0.1:7200/repositories/Carredas_Test", output, UP, NP, CP, UC, listNamedGraph);
			
		
		//-----------DECIDE pour les mixtures sur le serveur
		//theController.DECIDE("http://opendata.inra.fr/PO2/mixture", "http://127.0.0.1:7200/repositories/PO2", outputServeur, UP, NP, CP, UC, listNamedGraph);

		//-----------DECIDE pour les etapes sur le serveur
		//theController.DECIDE("http://opendata.inra.fr/PO2/step", "http://127.0.0.1:7200/repositories/PO2", outputServeur, UP, NP, CP, UC, listNamedGraph);

/*
		theController.detectPredictionRules
		("http://193.54.111.143:7200/repositories/PO2", // the ontology
		"http://193.54.111.143:7200/repositories/ID", // dataset with the contextual identity links
		output, // path to save rules
		"http://opendata.inra.fr/PO2/mixture", //target class
		"http://www.decideOutput/identiConTo", // identiConTo property
		"http://www.decideOutput/moreSpecificThan"); // more specific than property
*/		 		

		/*theController.checkRuleFromExpert
		("http://193.54.111.143:7200/repositories/PO2", // the ontology
		"http://193.54.111.143:7200/repositories/ID", // dataset with the contextual identity links
		output, // path to save rules
		"http://opendata.inra.fr/PO2/mixture", //target class
		"http://www.decideOutput/identiConTo", // identiConTo property
		"http://www.decideOutput/moreSpecificThan");*/
		
		//theController.addGlobalContextsRelations("http://193.54.111.143:7200/repositories/ID", output, "http://opendata.inra.fr/PO2/mixture", "http://www.decideOutput/identiConTo", "http://www.decideOutput/moreSpecificThan");

	}

}
