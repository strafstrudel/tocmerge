package com.example.tocmerge.similarity;

public interface SimilarityMetric {
	
	/**
	 * Returns a measure of similarity between two strings.  A similarity
	 * of zero means the strings are maximally dissimilar where as a value
	 * of one means the strings are identical.
	 * 
	 * @param s1
	 * @param s2
	 * @return a double in the range [0..1]
	 */
	public Double similarity(String s1, String s2);

}
