package com.example.tocmerge;

import java.util.function.BiConsumer;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;

import com.example.tocmerge.rip.FreeMoBio;
import com.example.tocmerge.rip.WikiBookIntroMoBio;
import com.example.tocmerge.similarity.PairwiseCosineSimilarity;
import com.example.tocmerge.similarity.SimilarityMetric;
import com.example.tocmerge.vocab.TEXT;

public class TextSimilarity {
	
	static String query = 
			       "prefix txtmodel: <" + TEXT.NAMESPACE + ">\n" +
	               "\n" +
			       "select ?label ?text \n" +
	               "where { ?section a txtmodel:Section . \n" +
			       "        ?section txtmodel:sectionText ?text . \n" +
	               "        ?section txtmodel:sectionTopic ?topic . \n" +
			       "        ?topic txtmodel:topicLabel ?label } \n";
	
	private static void doSectionText(Model model, BiConsumer<String, String> fn) {
		try (QueryExecution exec = QueryExecutionFactory.create(QueryFactory.create(query), model)) {
			ResultSet results = exec.execSelect();
			while (results.hasNext()) {
				QuerySolution soln = results.nextSolution();
				String topic = soln.getLiteral("label").getString();
				String text = soln.getLiteral("text").getString();
				fn.accept(topic, text);
			}
		}
	}
	
	public static void main(String[] argz) {
		
		Model m1 = new FreeMoBio().readModel();
		Model m2 = new WikiBookIntroMoBio().readModel();
		SimilarityMetric metric = new PairwiseCosineSimilarity();
		doSectionText(m1, (topic1, text1) -> {
			doSectionText(m2, (topic2, text2) -> {
				double sim = metric.similarity(text1, text2);
				if (sim >= .6)
					System.out.println(topic1 + " <--> " + topic2 + " ==> " + sim);
			});
		});
		
	}

}
