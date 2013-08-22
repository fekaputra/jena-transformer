package at.ac.tuwien.jenatransformer;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;


public class TransformerEntry {
	private OntModel model;
	private String name;
	private String dataStorage;
	private String emptyModel;
	private String checkInQuery;
	private String checkOutQuery;
	
	public TransformerEntry(String name, String dataStorage, String emptyModel, String checkInQuery, String checkOutQuery) {
		this.name = name;
		this.dataStorage = dataStorage;
		this.emptyModel = emptyModel;
		this.checkInQuery = TransformerHelper.readQuery(checkInQuery);
		this.checkOutQuery = TransformerHelper.readQuery(checkOutQuery);
		this.model = ModelFactory.createOntologyModel();
		
		TransformerHelper.readOwl(this.model, this.dataStorage);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public OntModel getModel() {
		return model;
	}

	public void setModel(OntModel model) {
		this.model = model;
	}

	public String getDataStorage() {
		return dataStorage;
	}

	public void setDataStorage(String dataStorage) {
		this.dataStorage = dataStorage;
	}

	public String getEmptyModel() {
		return emptyModel;
	}

	public void setEmptyModel(String emptyModel) {
		this.emptyModel = emptyModel;
	}

	public String getCheckInQuery() {
		return checkInQuery;
	}

	public void setCheckInQuery(String checkInQuery) {
		this.checkInQuery = checkInQuery;
	}

	public String getCheckOutQuery() {
		return checkOutQuery;
	}

	public void setCheckOutQuery(String checkOutQuery) {
		this.checkOutQuery = checkOutQuery;
	}

}
