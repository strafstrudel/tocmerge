package com.example.tocmerge.similarity;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.AttributeFactory;
import org.tartarus.snowball.ext.PorterStemmer;

/**
 * Simple English tokenizer with stemming and stopword filtering
 * <p>
 * Adapted from Lucene tokenizer
 * 
 * @author bill
 */
public class Tokenizer {
	
	private static CharArraySet stopWords = EnglishAnalyzer.getDefaultStopSet();
	private static AttributeFactory factory = AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY;
	private static PorterStemmer stemmer = new PorterStemmer();
	
	public static String[] tokenize(String text) {
		StandardTokenizer tokenizer = new StandardTokenizer(factory);
		CharTermAttribute attr = tokenizer.addAttribute(CharTermAttribute.class);
		tokenizer.setReader(new StringReader(text));
		ArrayList<String> tokens = new ArrayList<String>();
		try (StopFilter stopFilter = new StopFilter(tokenizer, stopWords)) {
			tokenizer.reset();
			while (stopFilter.incrementToken()) {
				String token = attr.toString();
				token = token.toLowerCase();
				stemmer.setCurrent(token);
				stemmer.stem();
				token = stemmer.getCurrent();
				tokens.add(token);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return tokens.toArray(new String[tokens.size()]);
	}
	
	public static void main(String[] argz) {
		System.out.println(Arrays.toString(tokenize("a small boy jumped the fence and fell down")));
	}

}
