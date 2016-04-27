package com.example.tocmerge;

import java.util.function.BiConsumer;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.impl.factory.Sets;
import org.eclipse.collections.impl.tuple.Tuples;

import com.example.tocmerge.rip.FreeMoBio;
import com.example.tocmerge.rip.WikiBookIntroMoBio;
import com.example.tocmerge.similarity.PairwiseCosineSimilarity;
import com.example.tocmerge.similarity.SimilarityMetric;
import com.example.tocmerge.vocab.TEXT;

public class TreeSimilarity {
	
	static String sectionTextQuery = 
			       "prefix txtmodel: <" + TEXT.NAMESPACE + ">\n" +
	               "\n" +
			       "select ?label ?level (group_concat(?sec_text) as ?text)  \n" +
	               "where { ?topic a txtmodel:Topic . \n" +
			       "        ?topic txtmodel:topicLabel ?label . " +
	               "        ?topic txtmodel:topicLevel ?level . " +
			       "        ?subTopic txtmodel:subTopicOf* ?topic . \n" +
	               "        ?section txtmodel:sectionTopic ?subTopic . \n" +
	               "        ?section txtmodel:sectionText ?sec_text . \n" +
			       "      } \n" +
	               "group by ?label ?level \n";
	
	private static void doSectionText(Model model, BiConsumer<String, String> fn) {
		try (QueryExecution exec = QueryExecutionFactory.create(QueryFactory.create(sectionTextQuery), model)) {
			ResultSet results = exec.execSelect();
			while (results.hasNext()) {
				QuerySolution soln = results.nextSolution();
//				Resource topic = soln.getResource("topic");
				String label = soln.getLiteral("label").toString();
				int level = soln.getLiteral("level").getInt();
				String text = soln.getLiteral("text").getString();
				fn.accept(level + ": " + label, text);
			}
		}
	}
	
	static String topicTreeQuery = 
		       "prefix txtmodel: <" + TEXT.NAMESPACE + ">\n" +
               "\n" +
		       "select ?topic (group_concat(?sec_text) as ?text)  \n" +
               "where { ?topic a txtmodel:Topic . \n" +
		       "        ?topic txtmodel:topicLabel ?label . " +
               "        ?subTopic txtmodel:subTopicOf* ?topic . \n" +
               "        ?section txtmodel:sectionTopic ?subTopic . \n" +
               "        ?section txtmodel:sectionText ?sec_text . \n" +
		       "      } \n" +
               "group by ?topic \n";

	private static void doTopicTreeText(Model model, BiConsumer<Resource, String> fn) {
		try (QueryExecution exec = QueryExecutionFactory.create(QueryFactory.create(topicTreeQuery), model)) {
			ResultSet results = exec.execSelect();
			while (results.hasNext()) {
				QuerySolution soln = results.nextSolution();
				Resource topic = soln.getResource("topic");
				String text = soln.getLiteral("text").getString();
				fn.accept(topic, text);
			}
		}
	}
	
	public static ImmutableMap<Pair<Resource, Resource>, Double> topicTreeSimilarty(Model m1, Model m2, SimilarityMetric metric) {
		MutableMap<Pair<Resource, Resource>, Double> pairSimilarity = Maps.mutable.of();
		doTopicTreeText(m1, (topic1, text1) -> {
			doTopicTreeText(m2, (topic2, text2) -> {
				double similarity = metric.similarity(text1, text2);
				pairSimilarity.put(Tuples.pair(topic1, topic2), similarity);
			});
		});
		return pairSimilarity.toImmutable();
	}
	
	public static void main(String[] argz) {
		
		Model m1 = new FreeMoBio().readModel();
		Model m2 = new WikiBookIntroMoBio().readModel();
		SimilarityMetric metric = new PairwiseCosineSimilarity();
		MutableList<Pair<Double, String>> similarities = Lists.mutable.of();
		doSectionText(m1, (topic1, text1) -> {
			doSectionText(m2, (topic2, text2) -> {
				double sim = metric.similarity(text1, text2);
				String topics = String.format("%s <--> %s (%s)", topic1, topic2, 
						(text1.length() > text2.length()) ? "left" : "right");
				similarities.add(Tuples.pair(sim, topics));
//				if (sim >= .3) {
//					System.out.format("%.3f %s%n", sim, topics);
//				}
			});
		});
		System.out.println("\nTree Similarity in sorted order:");
		for (Pair<Double, String> pair : similarities.sortThisBy(Pair::getOne)) {
			System.out.format("%.3f %s%n", pair.getOne(), pair.getTwo());
		}
		
	}

}
