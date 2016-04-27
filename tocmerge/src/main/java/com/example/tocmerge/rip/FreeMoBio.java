package com.example.tocmerge.rip;

import java.io.IOException;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.example.tocmerge.vocab.TEXT;

/**
 * RDF text model extractor for http://www.web-books.com/MoBio
 * 
 * @author bill
 */
public class FreeMoBio extends TextSourceBaseImpl implements TextSource {
	
	public static String URL = "http://www.web-books.com/MoBio#";
	
	public FreeMoBio() {
		super("mobio", URL);
	}
	
	public Model get() {
		Resource rootTopic = createTopic(0, ordinal(0), "Molecular Biology Web Book");
		model().add(rootTopic, RDF.type, TEXT.Text);
		try {
			int l1Index = 1;
			for (Element l1Link : getL1TOCLinks(URL)) {
				String l2TOCUrl = l1Link.attr("abs:href");
				String l1Ordinal = ordinal(0, l1Index);
				String l1Label = l1Link.text();
				Resource l1Topic = createTopic(1, rootTopic, l1Ordinal, l1Label);
				System.out.println(l1Ordinal + " " + l1Label);
				int l2Index = 0;
				int l3Index = 0;
				String l2Ordinal = null;
				String l2Label   = null;
				Resource l2Topic = null;
				Resource l2Section = null;
				for (Element l2Link : getL2TOCLinks(l2TOCUrl)) {
					if (l2Link.parent().tagName().equals("p")) {
						l2Index++;
						l2Ordinal = ordinal(0, l1Index, l2Index);
						l2Label = l2Link.text();
						l2Topic = createTopic(2, l1Topic, l2Ordinal, l2Label);
						String l2Url = l2Link.attr("abs:href");
						String l2Text = getDocumentText(l2Url);
						l2Section = createSection(l2Ordinal, l2Text);
						addSectionTopic(l2Section, l2Topic);
						System.out.println("  " + l2Ordinal + " " + l2Label);
						l3Index = 0;
					} else {
						l3Index++;
						String l3Ordinal = ordinal(0, l1Index, l2Index, l3Index);
						String l3Label = l2Link.text();
						Resource l3Topic = createTopic(3, l2Topic, l3Ordinal, l3Label);
						String l3Url = l2Link.attr("abs:href");
						String l3Text = getDocumentText(l3Url);
						Resource l3Section = createSection(l2Section, l3Ordinal, l3Text);
						addSectionTopic(l3Section, l3Topic);
						System.out.println("    " + l3Ordinal + " " + l3Label);
					}
				}
				l1Index++;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return model();
	}
	
	private Elements getL1TOCLinks(String urlRef) throws IOException {
		Document tocL1Doc = getDocument(URL);
		return tocL1Doc.select("#main > tbody > tr > td:nth-child(2) > blockquote > ol > li > p > a[href]");
	}
	
	private Elements getL2TOCLinks(String urlRef) throws IOException {
		Document tocL2Doc = getDocument(urlRef);
		return tocL2Doc.select("#main > tbody > tr > td:nth-child(2) > blockquote p > a[href], ul > li > a[href]");
	}
	
	private String getDocumentText(String urlRef) throws IOException {
		Document doc = getDocument(urlRef);
		Elements elements = doc.select("#main > tbody > tr > td:nth-child(2)");
		Element element = elements.get(0);
		return element.text();
	}
	
	public static void main(String[] argz) {
		
		String queryString = "" +
				"prefix mobio: <" + URL + "> \n" +
				"prefix ttext: <" + TEXT.NAMESPACE + "> \n" +
				"\n" +
				"select ?text \n" +
				"where { ?topic ttext:topicLabel \"Cancer\" . \n" +
				"        ?section ttext:sectionTopic ?topic . \n" +
				"        ?section ttext:sectionText ?text } \n";
		
		System.out.println(queryString);
		
		TextSource source = new FreeMoBio();
		Model model = source.get();
		source.writeModel();
		
		// RDFDataMgr.write(System.out, model, RDFFormat.TTL);
		
		Query query = QueryFactory.create(queryString);
		
		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			ResultSet results = qexec.execSelect();
			while (results.hasNext()) {
				QuerySolution soln = results.nextSolution();
				Literal text = soln.getLiteral("text");
				System.out.println(text);
			}
		}
	}
	
}
