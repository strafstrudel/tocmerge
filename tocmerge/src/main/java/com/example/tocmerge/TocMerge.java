package com.example.tocmerge;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.api.set.SetIterable;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.impl.factory.Sets;

import com.example.tocmerge.rip.FreeMoBio;
import com.example.tocmerge.rip.WikiBookIntroMoBio;
import com.example.tocmerge.similarity.LabelSimilarity;
import com.example.tocmerge.similarity.PairwiseCosineSimilarity;
import com.example.tocmerge.similarity.SimilarityMetric;
import com.example.tocmerge.vocab.TEXT;

public class TocMerge {
	
	private static double equivalenceThreshold = 0.3;
	private static String MERGE_NS = "http://example.com/merged#";
	
	public void merge(Model m1, Model m2) {
		
		Resource text1 = getText(m1);
		Resource text2 = getText(m2);
		String title1 = getLabel(m1, text1);
		String title2 = getLabel(m2, text2);
		System.out.format("Comparing texts \"%s\" and \"%s\"...%n", title1, title2);
		SimilarityMetric metric = new PairwiseCosineSimilarity();
		System.out.println("  Computing pairwise topic tree similarity...");
		ImmutableMap<Pair<Resource, Resource>, Double> topicTreeSimilarity = TreeSimilarity.topicTreeSimilarty(m1, m2, metric);
		ImmutableMap<Pair<Resource, Resource>, Double> topicLabelSimilarity = labelSimilarity(m1, m2, topicTreeSimilarity);
		ImmutableMap<Pair<Resource, Resource>, Double> combinedSimilarity = combinedSimilarity(topicTreeSimilarity, topicLabelSimilarity);
		combinedSimilarity = combinedSimilarity.select((pair, sim) -> sim >= equivalenceThreshold);
		combinedSimilarity.forEachKeyValue((pair, sim) -> {
			System.out.format("%.3f : %s | %s%n", sim, getLabel(m1, pair.getOne()), getLabel(m2, pair.getTwo()));
		});
		Model merged = ModelFactory.createDefaultModel();
		ImmutableMap<Resource, Resource> equivalences = buildEquivalences(m1, m2, merged, combinedSimilarity.keysView().toSet());
		MutableSet<Resource> visited = Sets.mutable.of(text1, text2);
		while (true) {
			
		}
		
	}
	
	private ImmutableMap<Resource, Resource> buildEquivalences(Model m1, Model m2, Model merged, 
			SetIterable<Pair<Resource, Resource>> equivalences) {
		MutableMap<Resource, Resource> map = Maps.mutable.of();
		equivalences.toList().zipWithIndex().each(pair -> {
			Resource topic1 = pair.getOne().getOne();
			Resource topic2 = pair.getOne().getTwo();
			int index = pair.getTwo();
			String label1 = getLabel(m1, topic1);
			String label2 = getLabel(m2, topic2);
			String newLabel = String.format("[%s == %s]", label1, label2);
			String newIriRef = MERGE_NS + "MergedTopic_" + index;
			Resource mergedTopic = createTopic(merged, newIriRef, newLabel);
			map.put(topic1, mergedTopic);
			map.put(topic2, mergedTopic);
		});
		return map.toImmutable();
	}
	
	private Resource createTopic(Model model, String iriRef, String label) {
		Resource topic = model.createResource(iriRef);
		model.add(topic, RDF.type, TEXT.Topic);
		model.add(topic, TEXT.topicLabel, label);
		return topic;
	}
	
	private Resource getText(Model m) {
		return m.listStatements(null, RDF.type, TEXT.Text).next().getSubject();
	}
	
	private String getLabel(Model m, Resource topic) {
		return m.listStatements(topic, TEXT.topicLabel, (RDFNode) null).next().getString();
	}
	
	private String getOrdinal(Model m, Resource topic) {
		return m.listStatements(topic, TEXT.topicOrdinal, (RDFNode) null).next().getString();
	}
	
	private ImmutableList<Resource> getSubTopics(Model m, Resource topic) {
		MutableList<Resource> subTopics = Lists.mutable.of();
		StmtIterator iter = m.listStatements(null, TEXT.subTopicOf, topic);
		while (iter.hasNext()) {
			subTopics.add(iter.next().getSubject());
		}
		subTopics = subTopics.sortThisBy(t -> getOrdinal(m, t));
		return subTopics.toImmutable();
	}
	
	private ImmutableMap<Pair<Resource, Resource>, Double> labelSimilarity(Model m1, Model m2, ImmutableMap<Pair<Resource, Resource>, Double> treeSimilarity) {
		MutableMap<Pair<Resource, Resource>, Double> map = Maps.mutable.of();
		for (Pair<Resource, Resource> topicPair : treeSimilarity.keysView()) {
			String label1 = getLabel(m1, topicPair.getOne());
			String label2 = getLabel(m2, topicPair.getTwo());
			double sim = LabelSimilarity.equivalent(label1, label2);
			map.put(topicPair, sim);
		}
		return map.toImmutable();
	}
	
	private ImmutableMap<Pair<Resource, Resource>, Double> combinedSimilarity(
			ImmutableMap<Pair<Resource, Resource>, Double> treeSimilarity,
			ImmutableMap<Pair<Resource, Resource>, Double> labelSimilarity) {
		MutableMap<Pair<Resource, Resource>, Double> map = Maps.mutable.of();
		for (Pair<Resource, Resource> topicPair : treeSimilarity.keysView()) {
			double sim = treeSimilarity.get(topicPair) * labelSimilarity.get(topicPair);
			map.put(topicPair, sim);
		}
		return map.toImmutable();
	}
	public static void main(String[] args) {
		
		TocMerge tm = new TocMerge();
		Model m1 = new FreeMoBio().readModel();
		Model m2 = new WikiBookIntroMoBio().readModel();
		tm.merge(m1, m2);
		
	}

}
