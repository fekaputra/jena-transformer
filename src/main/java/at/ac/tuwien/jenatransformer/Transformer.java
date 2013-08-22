package at.ac.tuwien.jenatransformer;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;


/**
 * Simple transformer for the ontology
 * 	How it work?
 * 
 * 	1. Register each of the local data model (e.g., opm, eplan) into the transformer as a TransformerEntry
 * 		You will need (a)name, (b)empty data model, (c)file storage, (d)local2common model query, (e)common2local model query
 * 	2. Provide the input data to be mapped
 * 	3. Transform and get the resulted data in other local / common data model 
 * 
 * 
 * @author Juang
 * @since 21.08.2013
 *
 */
public class Transformer {
	/**
	 * common data jena model  
	 */
	private OntModel vcdm;
	/**
	 * registry for the local data model
	 */
	private Map<String, TransformerEntry> registry;
	
	public Transformer(String vcdmURI) {
		vcdm = ModelFactory.createOntologyModel();
		registry = new HashMap<String, TransformerEntry>();
		initVCDM(vcdmURI);
	}
	
	/**
	 * instatiate the common data model
	 * register each local model & add it into the common data model if necessary
	 * 
	 * @param vcdmURI : location of the common data model
	 */
	private void initVCDM(String vcdmURI) {
		TransformerHelper.readOwl(vcdm, vcdmURI);
		for(TransformerEntry entry : registry.values()) {
			vcdm.addSubModel(entry.getModel());
		}
	}
	
	/**
	 * register a new transformerEntry into the transformer
	 * 	put the data into the datamodel as a submodel
	 * 
	 * @param name
	 * @param dataStorage
	 * @param emptyModel
	 * @param inQuery
	 * @param OutQuery
	 */
	public void registerTransformer(String name, String dataStorage, String emptyModel, String inQuery, String OutQuery) {
		TransformerEntry entry = new TransformerEntry(name, dataStorage, emptyModel, inQuery, OutQuery);
		registry.put(name, entry);
		vcdm.addSubModel(entry.getModel());
	}

	
	/**
	 * register a new transformerEntry into the transformer
	 * 	put the data into the datamodel as a submodel
	 * 
	 * @param entry
	 */
	public void registerTransformer(TransformerEntry entry) {
		registry.put(entry.getName(), entry);
		vcdm.addSubModel(entry.getModel());
	}
	
	/**
	 * Unregister transformerEntry,
	 * 	remove the data from the common data model
	 * 
	 * @param name
	 */
	public void unRegisterTransformer(String name) {
		TransformerEntry entry = registry.get(name);
		vcdm.removeSubModel(entry.getModel());
		registry.remove(name);
	}
	
	/**
	 * check in new data from a certain data model, 
	 * 	afterward propagates the changes, 
	 * 	and store the filename 
	 * 
	 * @param modelName
	 * @param fileURI
	 * @return
	 */
	public Map<String, String> checkInProses(String modelName, String fileURI) {
		Model vcdmChanges = checkInToVCDM(modelName, fileURI);
		Map<String, String> propagateResult = propagateChanges(vcdmChanges);
		
		return propagateResult;
	}

	/**
	 * load the input file, and embed it into the local data model
	 * 	Afterward, invoke rebind() to recalculate the inference (if using any)
	 * 
	 * @param modelName
	 * @param fileURI
	 * @return
	 */
	private Model checkInToVCDM(String modelName, String fileURI) {
		TransformerEntry entry = registry.get(modelName);
		
		OntModel subModel = entry.getModel();
		OntModel input = TransformerHelper.readOwl(fileURI);
		subModel.addSubModel(input);
		Model model = constructQuery(entry.getCheckInQuery());
		vcdm.rebind();
		
		return model;
	}
	
	/**
	 * execute construct query
	 * 
	 * @param query
	 * @return
	 */
	public Model constructQuery(String query) {
		Query q = QueryFactory.create(query);
		QueryExecution qe = QueryExecutionFactory.create(q, vcdm);
		Model ret = qe.execConstruct(); 
		
		return ret;
	}
	
	/**
	 * propagate the changes from common data model to each local tool
	 * 	currently the @param vcdmChanges are not used, 
	 * 	but I have a feeling that later it will be helpful
	 * 
	 * @param vcdmChanges
	 * @return
	 */
	private Map<String, String> propagateChanges(Model vcdmChanges) {
		Map<String, String> changes = new HashMap<String, String>();
		for(String entryKey: registry.keySet()) {
			TransformerEntry entry = registry.get(entryKey);
			Model retVal = constructQuery(entry.getCheckOutQuery());
			entry.getModel().add(retVal);
			String filename = entryKey+"_result.owl";
			TransformerHelper.save(entry.getModel(), filename);
			changes.put(entryKey, filename);
		}
		
		return changes;
	}

	/**
	 * Olga should provide some inconsistency checking here later
	 * 
	 * @return
	 */
	public boolean isConsistent() {
		// TODO: involved Olga's consistency checking here
		return true;
	}
	
	public static void main(String[] args) {
		String opm = "src/main/resource/opm/";
		String eplan = "src/main/resource/eplan/";
		
		TransformerEntry opmEntry = new TransformerEntry(
				"opm",
				opm+"opm.owl", 
				opm+"opm_empty.owl", 
				opm+"opm2vcdm.sparql", 
				opm+"vcdm2opm.sparql");
		TransformerEntry eplanEntry = new TransformerEntry(
				"eplan",
				eplan+"eplan.owl", 
				eplan+"eplan_empty.owl", 
				eplan+"eplan2vcdm.sparql", 
				eplan+"vcdm2eplan.sparql");
		
		Transformer transformer = new Transformer("src/main/resource/vcdm.owl");
		transformer.registerTransformer(opmEntry);
		transformer.registerTransformer(eplanEntry);
		
		Map<String,String> result = transformer.checkInProses("opm", "src/main/resource/opm-filled.owl");
		
		for(String key : result.keySet()) {
			System.out.println("resulted file for model '"+key+"': "+result.get(key));
		}
	}
}
