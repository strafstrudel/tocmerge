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
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.impl.factory.Sets;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.example.tocmerge.vocab.TEXT;

public class WikiBookIntroMoBio extends TextSourceBaseImpl implements TextSource {
	
	public static String URL = "https://en.wikibooks.org/wiki/An_Introduction_to_Molecular_Biology#";
	
	public WikiBookIntroMoBio() {
		super("wikimobio", URL);
	}
	
	private static ImmutableSet<String> topicBlackList = Sets.immutable.of( 
			"Quiz time",
			"Question time",
			"Facts to be remembered",
			"References"
			);
	
	public Model get() {
		try {
			int l1Index = 1;
			for (Element l1Link : getL1TOCLinks(URL)) {
				String l2Url = l1Link.attr("abs:href");
				String l1Ordinal = String.format("%s", l1Index);
				String l1Label = l1Link.text();
				if (topicBlackList.contains(l1Label)) break;
				Resource l1Topic = createTopic(1, l1Ordinal, l1Label);
				System.out.println(l1Ordinal + " " + l1Label);
				l1Index++;
				Element l2Content = getL2Content(l2Url);
				makeL2Toc(l1Topic, l1Ordinal, l2Content);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return model();
	}
	
	private void makeL2Toc(Resource l1Topic, String ordinal, Element content) throws IOException {
		Elements tocList = content.select("#toc > ul > li");
		makeNestedToc(2, l1Topic, ordinal, tocList, content);
	}
	
	int indent = 2;
	private void makeNestedToc(int level, Resource parent, String ordinal, Elements list, Element content) throws IOException {
		if (list == null) return;
		for (Element li : list) {
			Element link = li.select("a[href]").first();
			String linkUrl = link.attr("href");
			String linkLabel = link.select(".toctext").text();
			if (topicBlackList.contains(linkLabel)) continue;
			String linkOrdinal = ordinal + link.select(".tocnumber").text();
			String text = "";
			try {
				text = content.select(linkUrl).first().parent().nextElementSibling().text();
			} catch (Exception e) { }
			Resource topic = createTopic(level, parent, linkOrdinal, linkLabel);
			Resource section = createSection(linkOrdinal, text);
			addSectionTopic(section, topic);
			Elements subList = li.select("ul > li");
			for (int i = 0; i < indent; i++) System.out.print(' ');
			System.out.println(linkOrdinal + " " + linkLabel);
			indent += 2;
			makeNestedToc(level + 1, topic, linkOrdinal, subList, content);
			indent -= 2;
		}
	}
	
	private Elements getL1TOCLinks(String urlRef) throws IOException {
		Document tocL1Doc = getDocument(URL);
		return tocL1Doc.select("#mw-content-text > p > a[href^=\"/wiki/\"");
	}
	
	private Element getL2Content(String urlRef) throws IOException {
		Document doc = getDocument(urlRef);
		return doc.select("#mw-content-text").get(0);
	}
	
//	private Elements getL2TOCLinks(String urlRef) throws IOException {
//		Document tocL2Doc = getDocument(urlRef);
//		return tocL2Doc.select("#main > tbody > tr > td:nth-child(2) > blockquote p > a[href], ul > li > a[href]");
//	}
//	
//	private String getDocumentText(String urlRef) throws IOException {
//		Document doc = getDocument(urlRef);
//		Elements elements = doc.select("#main > tbody > tr > td:nth-child(2)");
//		Element element = elements.get(0);
//		return element.text();
//	}
	
	public static void main(String[] argz) {
		
		String queryString = "" +
				"prefix mobio: <" + URL + "> \n" +
				"prefix ttext: <" + TEXT.NAMESPACE + "> \n" +
				"\n" +
				"select ?text \n" +
				"where { ?topic ttext:topicLabel \"Enhancer\" . \n" +
				"        ?section ttext:sectionTopic ?topic . \n" +
				"        ?section ttext:sectionText ?text } \n";
		
		System.out.println(queryString);
		
		TextSource source = new WikiBookIntroMoBio();
		
		Model model = source.get();
		source.writeModel();
		
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
