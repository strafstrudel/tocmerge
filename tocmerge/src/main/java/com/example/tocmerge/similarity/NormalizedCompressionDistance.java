package com.example.tocmerge.similarity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

/**
 * Normalized Compression Distance metric
 * <p>
 * Algorithm derived from:
 * <p>
 * https://en.wikipedia.org/wiki/Normalized_compression_distance
 * 
 * @author bill
 */
public class NormalizedCompressionDistance implements SimilarityMetric {

	@Override
	public Double similarity(String s1, String s2) {
		StringBuffer sb = new StringBuffer(s1.length() + s2.length());
		sb.append(s1);
		sb.append(s2);
		try {
			double z12 = (double) compressedSize(sb.toString());
			double z1 = (double) compressedSize(s1);
			double z2 = (double) compressedSize(s2);
			return 1 - (z12 - Math.min(z1, z2)) / Math.max(z1, z2);
		} catch (IOException e) {
			throw new SimilarityException();
		}
	}
	
	private int compressedSize(String str) throws IOException {
		if (str == null || str.length() == 0) {
			return 0;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream(str.length());
		GZIPOutputStream gzip = new GZIPOutputStream(out);
		gzip.write(str.getBytes());
		gzip.close();
		return out.size();
	}
	
	public static void main(String[] argz) {
		
		String s1 = "abcdefghijklmabcdefghijklm";
		String s2 = "nopqrstuvwxyznopqrstuvwxyz";
		
		SimilarityMetric sim = new NormalizedCompressionDistance();
		System.out.println(sim.similarity(s1, s2));
		System.out.println(sim.similarity(s2, s1));
		System.out.println(sim.similarity(s1, s1));
		System.out.println(sim.similarity(s2, s2));
		
		
	}

}
