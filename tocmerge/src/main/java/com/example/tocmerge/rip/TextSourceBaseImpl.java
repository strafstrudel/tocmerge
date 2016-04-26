package com.example.tocmerge.rip;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.output.WriterOutputStream;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.RDF;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.example.tocmerge.vocab.TEXT;

public class TextSourceBaseImpl {
	
	private final String prefix;
	private final String namespace;
	private Model model;
	
	protected TextSourceBaseImpl(String prefix, String namespace) {
		this.prefix = prefix;
		this.namespace = namespace;
		initModel();
	}
	
	private void initModel() {
		this.model = ModelFactory.createDefaultModel();
		this.model.setNsPrefix(prefix, namespace);
		this.model.setNsPrefix("txtmod", TEXT.NAMESPACE);
	}
	
	public String prefix() {
		return prefix;
	}
	
	public String namespace() {
		return namespace;
	}
	
	public Model model() {
		return model;
	}
	
	Document getDocument(String urlRef) throws IOException {
		Document document = Jsoup.connect(urlRef).get();
		return document;
	}
	
	Resource createTopic(int level, String ordinal, String label) {
		String iriRef = namespace + "Topic" + ordinal;
		Resource topic = model.createResource(iriRef);
		model.add(topic, RDF.type, TEXT.Topic);
		model.add(topic, TEXT.topicOrdinal, ordinal);
		model.add(topic, TEXT.topicLabel, label);
		model.addLiteral(topic, TEXT.topicLevel, level);
		return topic;
	}
	
	Resource createTopic(int level, Resource parent, String ordinal, String label) {
		Resource topic = createTopic(level, ordinal, label);
		model.add(topic, TEXT.subTopicOf, parent);
		return topic;
	}
	
	Resource createSection(String ordinal, String text) {
		String iriRef = namespace + "Section" + ordinal;
		Resource section = model.createResource(iriRef);
		model.add(section, RDF.type, TEXT.Section);
		model.add(section, TEXT.sectionOrdinal, ordinal);
		model.add(section, TEXT.sectionText, text);
		return section;
	}
	
	Resource createSection(Resource parent, String ordinal, String text) {
		Resource section = createSection(ordinal, text);
		model.add(section, TEXT.subSectionOf, parent);
		return section;
	}
	
	void addSectionTopic(Resource section, Resource topic) {
		model.add(section, TEXT.sectionTopic, topic);
	}
	
	public void writeModel() {
		String path = "src/main/resources/rdf/" + prefix + ".ttl";
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(path, "UTF-8");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		PrintStream out = new PrintStream(new WriterOutputStream(writer));
		RDFDataMgr.write(out, model, RDFFormat.TTL);
	}
	
	public Model readModel() {
		String path = "src/main/resources/rdf/" + prefix + ".ttl";
		InputStream in = null;
		try {
			in = new FileInputStream(path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		initModel();
		RDFDataMgr.read(model, in, Lang.TTL);
		return model;
	}

}
