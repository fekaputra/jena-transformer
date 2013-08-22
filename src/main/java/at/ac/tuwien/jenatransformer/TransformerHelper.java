package at.ac.tuwien.jenatransformer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

public class TransformerHelper {

	/**
	 * read the query from file
	 * 
	 * @param filename
	 * @return 
	 */
	public static String readQuery(String filename)
	{
	   String content = null;
	   File file = new File(filename);
	   try {
	       FileReader reader = new FileReader(file);
	       char[] chars = new char[(int) file.length()];
	       reader.read(chars);
	       content = new String(chars);
	       reader.close();
	   } catch (IOException e) {
	       e.printStackTrace();
	   }
	   return content;
	}
	
	/**
	 * changes are not automatically saved into the ontology, 
	 * 	so you have to trigger it yourself, and choose where to store your model+changes 
	 * 
	 * @param outFile
	 */
	public static void save(Model model, String outFile) {
		try {
			model.write(new FileWriter(outFile), "RDF/XML");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * linking OntModel with an owl file
	 * 
	 * @param owlFile
	 */
	public static void readOwl(OntModel model, String owlFile) {
		InputStream in = FileManager.get().open(owlFile);
		if(in==null) throw new IllegalArgumentException("File: '"+owlFile+"' not found");
		model.read(in, null);
	}
	
	/**
	 * linking OntModel with an owl file
	 * 
	 * @param owlFile
	 */
	public static OntModel readOwl(String owlFile) {
		OntModel model = ModelFactory.createOntologyModel();
		InputStream in = FileManager.get().open(owlFile);
		if(in==null) throw new IllegalArgumentException("File: '"+owlFile+"' not found");
		model.read(in, null);
		return model;
	}
}
