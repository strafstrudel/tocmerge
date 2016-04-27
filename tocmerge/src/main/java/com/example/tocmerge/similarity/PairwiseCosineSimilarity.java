package com.example.tocmerge.similarity;

import java.util.Arrays;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.ListIterable;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Sets;

/**
 * Cosine similarity metric
 * <p>
 * Algorithm adapted from
 * <p>
 * http://computergodzilla.blogspot.com/2013/07/how-to-calculate-tf-idf-of-document.html
 * 
 * @author bill
 */
public class PairwiseCosineSimilarity implements SimilarityMetric {

	private TFIDF tfidf = new TFIDF();
	
	@Override
	public Double similarity(String s1, String s2) {
		String[] terms1 = Tokenizer.tokenize(s1);
		String[] terms2 = Tokenizer.tokenize(s2);
		MutableSet<String> allTerms = Sets.mutable.of();
		allTerms.addAll(Arrays.asList(terms1));
		allTerms.addAll(Arrays.asList(terms2));
		ImmutableList<String> allTermsList = Lists.immutable.ofAll(allTerms).collect(String::toLowerCase);
		ImmutableMap<String, Double> idf = tfidf.idf(Lists.immutable.of(terms1, terms2));
		double[] vec1 = tfIdfVec(allTermsList, idf, tfidf.tf(terms1));
		double[] vec2 = tfIdfVec(allTermsList, idf, tfidf.tf(terms2));
		return cosineSimilarity(vec1, vec2);
	}
	
//	private String[] tokenize(String s) {
//		return s.replaceAll("[\\W&&[^\\s]]", "").split("\\W+");
//	}
	
	private double[] tfIdfVec(ListIterable<String> allTerms, ImmutableMap<String, Double> idf, ImmutableMap<String, Double> tf) {
		double[] tfIdfVec = new double[allTerms.size()];
		int i = 0;
		for (String term : allTerms) {
			double termTf = tf.getIfAbsentValue(term, 0.0);
			double termIdf = idf.getIfAbsentValue(term, 0.0);
			double termTfIdf = termTf * termIdf;
			tfIdfVec[i++] = termTfIdf;
		}
		return tfIdfVec;
	}
	
	private double cosineSimilarity(double[] vec1, double[] vec2) {
		double dot = 0.0;
		double mag1 = 0.0;
		double mag2 = 0.0;
		for (int i = 0; i < vec1.length; i++)  {
			dot += vec1[i] * vec2[i];
			mag1 += Math.pow(vec1[i], 2);
			mag2 += Math.pow(vec2[i], 2);
		}
		mag1 = Math.sqrt(mag1);
		mag2 = Math.sqrt(mag2);
		if (mag1 * mag2 > 0) {
			return dot / (mag1 * mag2);
		} else {
			return 0.0;
		}
	}
	
	public static void main(String[] argz) {
		
		String text1 = "An enhancer is a short region of DNA that can be bound with proteins (namely, the trans-acting factors, much like a set of transcription factors) to enhance transcription levels of genes (hence the name) in a gene cluster. While enhancers are usually cis-acting, an enhancer does not need to be particularly close to the genes it acts on, and need not be located on the same chromosome.";
		String text2 = "An enhancer is a short region of DNA that can be bound with proteins (namely, the trans-acting factors, much like a set of transcription factors) to enhance transcription levels of genes (hence the name) in a gene cluster. While enhancers are usually cis-acting, an enhancer does not need to be particularly close to the genes it acts on, and need not be located on the same chromosome.";
	
		SimilarityMetric m = new PairwiseCosineSimilarity();
		
		System.out.println(m.similarity(text1, text2));
	}

}
