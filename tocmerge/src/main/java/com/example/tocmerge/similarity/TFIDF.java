package com.example.tocmerge.similarity;

import java.util.Arrays;

import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.impl.factory.Sets;

/**
 * Functions for computing term frequency and inverse document frequency
 * <p>
 * Algorithms adapted from
 * <p>
 * http://computergodzilla.blogspot.com/2013/07/how-to-calculate-tf-idf-of-document.html
 * 
 * @author bill
 */
public class TFIDF {
	
	/**
	 * Calculate term frequency of a single term among docTerms
	 * 
	 * @param docTerms
	 * @param term
	 * @return tf of term wrt docTerms
	 */
	public double tf(String[] docTerms, String term) {
		int occurrenceCount = 0;
		for (String term1 : docTerms) {
			if (term.equalsIgnoreCase(term1)) {
				occurrenceCount++;
			}
		}
		return occurrenceCount / (double) docTerms.length;
	}
	
	/**
	 * Calculate term frequency for each term in docTerms
	 * 
	 * @param docTerms
	 * @return map of term to tf for all terms in docTerms	
	 */
	public ImmutableMap<String, Double> tf(String[] docTerms) {
		MutableMap<String, Double> tf = Maps.mutable.of();
		for (String term : docTerms) {
			term = term.toLowerCase();
			tf.put(term, tf.getOrDefault(term, 0.0) + 1);
		}
		for (String term : tf.keySet()) {
			tf.put(term, tf.get(term) / docTerms.length);
		}
		return tf.toImmutable();
	}
	
	/**
	 * Calculate inverse document frequency (idf) of term wrt docs
	 * 
	 * @param docs
	 * @param term
	 * @return idf of term wrt docs
	 */
	public double idf(ListIterable<String[]> docs, String term) {
		int nDocsWithTerm = 0;
		for (String[] docTerms : docs) {
			for (String term1 : docTerms) {
				if (term.equalsIgnoreCase(term1)) {
					nDocsWithTerm++;
					break;
				}
			}
		}
		return 1 + Math.log(docs.size() / (double) nDocsWithTerm);
	}
	
	/**
	 * Calculate inverse document frequency (idf) of each term in docs wrt docs
	 * 
	 * @param docs
	 * @return idf of each term in docs wrt docs
	 */
	public ImmutableMap<String, Double> idf(ListIterable<String[]> docs) {
		MutableMap<String, Double> idf = Maps.mutable.of();
		for (String[] docTerms : docs) {
			for (String term : Sets.immutable.ofAll(Arrays.asList(docTerms)).collect(String::toLowerCase)) {
				idf.put(term, idf.getOrDefault(term, 0.0) + 1);
			}
		}
		for (String term : idf.keySet()) {
			idf.put(term, 1 + Math.log(docs.size() / idf.get(term)));
		}
		return idf.toImmutable();
	}

}
