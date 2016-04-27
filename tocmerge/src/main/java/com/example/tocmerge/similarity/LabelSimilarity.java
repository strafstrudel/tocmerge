package com.example.tocmerge.similarity;

import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.impl.factory.Sets;

/**
 * @author bill
 *
 */
public class LabelSimilarity {
	
	public static double equivalent(String topic1, String topic2) {
		ImmutableSet<String> tokens1 = Sets.immutable.of(Tokenizer.tokenize(topic1));
		ImmutableSet<String> tokens2 = Sets.immutable.of(Tokenizer.tokenize(topic2));
		int intersectionSize = tokens1.intersect(tokens2).size();
		double averageSize = (tokens1.size() + tokens2.size()) / 2.0;
		return intersectionSize / averageSize;
	}
	
	public static double contains(String topic1, String topic2) {
		ImmutableSet<String> tokens1 = Sets.immutable.of(Tokenizer.tokenize(topic1));
		ImmutableSet<String> tokens2 = Sets.immutable.of(Tokenizer.tokenize(topic2));
		int intersectionSize = tokens1.intersect(tokens2).size();
		return intersectionSize / (double) tokens2.size();
	}
	
}
