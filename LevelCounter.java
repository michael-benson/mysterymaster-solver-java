package com.mysterymaster.solver;

import com.mysterymaster.puzzle.Base;
import com.mysterymaster.puzzle.ISolver;

/**
 * The Level Counter class tracks the number of marks entered for each level.<br>
 * A mark is entered by either a level method (fact or guess), triggered by a rule, or by a law.<br>
 * Copyright (c) 2018 mysterymaster.com. All rights reserved.
 * @version 2018-05-15
 * @author Michael Benson <michael.benson@mysterymaster.com>
 */
public class LevelCounter extends Base {
	/** Number of data columns. */
	private static final int MAX_COLS = ISolver.MAX_LAWS + 3;

	/** Number of marks entered by a level method. */
	int marksByLevel;

	/** Number of marks entered by a rule (trigger). */
	int marksByRule;

	/** Number of marks entered by each law. */
	final int[] marksByLaw;

	/** Total number of marks. */
	int sum;

	@Override
	public String toString() {
		return "LevelCounter";
	}

	@Override
	public String asString() {
		return this.toString();
	}

	/** Constructor. */
	LevelCounter() {
		marksByLaw = new int[ISolver.MAX_LAWS];
		reset();
	}

	/** Resets the counts for this level. Note: Must use final to avoid warning in constructor. */
	public final void reset() {
		marksByLevel = 0;
		marksByRule = 0;
		for (int j = 0; j < ISolver.MAX_LAWS; j++) marksByLaw[j] = 0;
		sum = 0;
	}

	/**
	 * Returns the array of column headers.
	 * @return Array of strings.
	 */
	public static String[] getColHeaders() {
		String[] colHeaders = new String[MAX_COLS + 1];
		int i = 0;
		colHeaders[i++] = "#";
		colHeaders[i++] = "Level";
		colHeaders[i++] = "Rule";
		for (int j = 0; j < ISolver.MAX_LAWS; j++) {
			colHeaders[i++] = "Law " + Integer.toString(j + 1);
		}
		colHeaders[i++] = "Sum";
		return colHeaders;
	}

	/**
	 * Returns the array of row headers.
	 * @return Array of strings.
	 */
	public static String[] getRowHeaders() {
		String rowHeaders[] = new String[ISolver.MAX_LEVELS + 2];
		int i = 0;
		rowHeaders[i++] = "#";
		for (int j = 0; j < ISolver.MAX_LEVELS; j++) { rowHeaders[i++] = Integer.toString(j + 1); }
		rowHeaders[i++] = "Sum";
		return rowHeaders;
	}

	/**
	 * Returns initialized array of level counters where the last one is for the totals.
	 * @return Array of level counters.
	 */
	static LevelCounter[] getLevelCounters() {
		LevelCounter[] levelCounters = new LevelCounter[ISolver.MAX_LEVELS + 1];
		for (int i = 0; i < levelCounters.length; i++) {
			levelCounters[i] = new LevelCounter();
		}
		return levelCounters;
	}

	/**
	 * Returns the counts as an array. This is helpful for the UI.
	 * @return Array of counts.
	 */
	public int[] getCounts() {
		int[] counts = new int[MAX_COLS];
		int i = 0;
		counts[i++] = marksByLevel;
		counts[i++] = marksByRule;
		for (int j = 0; j < ISolver.MAX_LAWS; j++) counts[i++] = marksByLaw[j];
		counts[i++] = sum;
		return counts;
	}
}
