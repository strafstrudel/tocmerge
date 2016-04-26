package com.example.tocmerge.rip;

import org.apache.jena.rdf.model.Model;

public interface TextSource {
	
	/**
	 * @return an RDF model of the text
	 */
	public Model get();
	
	/**
	 * Writes file to disk in RDF TTL format
	 */
	public void writeModel();
	
	/**
	 * Reads RDF TTL from disk
	 */
	public Model readModel();

}
