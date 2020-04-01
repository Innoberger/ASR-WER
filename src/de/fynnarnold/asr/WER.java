package de.fynnarnold.asr;

import java.util.Scanner;

public class WER {

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);

		System.out.println("Hypothese:");
		String hyp = scanner.nextLine();

		while (getWordCount(hyp) == 0) {
			System.out.println("Bitte mindestens ein Wort eingeben.");
			System.out.println("Hypothese:");

			hyp = scanner.nextLine();
		}

		System.out.println("Referenz:");
		String ref = scanner.nextLine();

		while (getWordCount(ref) == 0) {
			System.out.println("Bitte mindestens ein Wort eingeben.");
			System.out.println("Referenz:");

			ref = scanner.nextLine();
		}

		int refWords = getWordCount(ref);
		int hypWords = getWordCount(hyp);
		int[][] D = getLevenshteinMatrix(ref, hyp);
		int[] backtrace = backtrace(D, refWords, hypWords);
		double wer = getWordErrorRate(refWords, backtrace[0], backtrace[1], backtrace[2]);

		System.out.println(String.format("Wortfehlerrate (WER): %s%%", String.format("%.2f", wer * 100.0D)));
		System.out.println(String.format("Einfügungen: %s, Auslöschungen: %s, Ersetzungen: %s", backtrace[0],
				backtrace[1], backtrace[2]));
	}

	/**
	 * Code inspired by Stefan Kiesel
	 * (https://www.java-blog-buch.de/c-levenshtein-distanz/). Modified and extended
	 * by myself.
	 *
	 * The original code compares chars whereas the modified version compares words
	 * separated by spaces.
	 *
	 * @param ref The reference word sequence.
	 * @param hyp The hypothesis word sequence.
	 * @return
	 */
	private static int[][] getLevenshteinMatrix(String ref, String hyp) {
		int refWords = getWordCount(ref);
		int hypWords = getWordCount(hyp);
		int D[][] = new int[refWords + 1][hypWords + 1];

		// matrix initialization
		for (int k = 0; k < refWords + 1; k++) {
			D[k][0] = k;
		}

		// matrix initialization
		for (int k = 0; k < hypWords + 1; k++) {
			D[0][k] = k;
		}

		// looping through every word
		for (int i = 1; i < refWords + 1; i++) {
			for (int j = 1; j < hypWords + 1; j++) {
				int value = 0;

				// if words are equal, the value is 1 otherwise it remains 0
				if (!getWordAt(i - 1, ref).equals(getWordAt(j - 1, hyp))) {
					value = 1;
				}

				int min_i = D[i - 1][j] + 1;

				if (D[i][j - 1] + 1 < min_i) {
					min_i = D[i][j - 1] + 1;
				}

				if (D[i - 1][j - 1] + value < min_i) {
					min_i = D[i - 1][j - 1] + value;
				}

				D[i][j] = min_i;
			}
		}

		return D;
	}

	/**
	 * Code inspired by https://de.wikipedia.org/wiki/Levenshtein-Distanz Modified
	 * and extended by myself.
	 *
	 * @param D The matrix
	 * @param i The row value to start with
	 * @param j The column value to start with
	 * @return [insertions, deletions, substitutions]
	 */
	private static int[] backtrace(int[][] D, int i, int j) {
		int[] result = new int[] { 0, 0, 0 };

		// deletion
		if (i > 0 && D[i - 1][j] + 1 == D[i][j]) {
			result = backtrace(D, i - 1, j);
			result[1]++;
			return result;
		}

		// insertion
		if (j > 0 && D[i][j - 1] + 1 == D[i][j]) {
			result = backtrace(D, i, j - 1);
			result[0]++;
			return result;
		}

		// substitution
		if (i > 0 && j > 0 && D[i - 1][j - 1] + 1 == D[i][j]) {
			result = backtrace(D, i - 1, j - 1);
			result[2]++;
			return result;
		}

		// equality
		if (i > 0 && j > 0 && D[i - 1][j - 1] == D[i][j]) {
			return backtrace(D, i - 1, j - 1);
		}

		return result;
	}

	private static int getWordCount(String text) {
		return text.replaceAll(" ", "").equals("") ? 0 : text.split(" ").length;
	}

	private static String getWordAt(int index, String text) {
		return text.split(" ")[index];
	}

	private static double getWordErrorRate(int ref, int ins, int del, int sub) {
		return (ins + del + sub) / (double) ref;
	}

}
