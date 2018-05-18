package com.mysterymaster.solver;

import com.mysterymaster.puzzle.Base;
import com.mysterymaster.puzzle.Mark;
import static com.mysterymaster.puzzle.Mark.Type.Law;
import static com.mysterymaster.puzzle.Mark.Type.Level;
import static com.mysterymaster.puzzle.Mark.Type.User;

/**
 * The Stats class defines statistics while a logic puzzle is being solved. Usage:<ol>
 * <li>Instantiate in solver.constructor.</li>
 * <li>Call stats.reset in solver.reset.</li>
 * <li>Call stats.update in solver.sayAddMark and solver.sayRemoveMark.</li></ol>
 * Copyright (c) 2018 mysterymaster.com. All rights reserved.
 * @version 2018-05-15
 * @author Michael Benson <michael.benson@mysterymaster.com>
 */
public class Stats extends Base {
	/** Array of level counters for the pairs. */
	public final LevelCounter[] levelPairs;

	/** Array of level counters for the marks. */
	public final LevelCounter[] levelMarks;

	/** Totals for marks. This is the last row in the levelMarks. */
	private final LevelCounter totalMark;

	/** Totals for pairs. This is the last row in the levelPairs. */
	private final LevelCounter totalPair;

	@Override
	public String toString() {
		return "Stats";
	}

	@Override
	public String asString() {
		return this.toString();
	}

	/** Constructor. Called by Viewer.constructor. */
	public Stats() {
		levelPairs = LevelCounter.getLevelCounters();
		levelMarks = LevelCounter.getLevelCounters();
		
		int nrows = levelMarks.length;
		totalMark = levelMarks[nrows - 1];
		totalPair = levelPairs[nrows - 1];
	}

	/** Resets the counters for the marks and pairs. Called by viewer.reset. */
	public void reset() {
		reset(levelPairs);
		reset(levelMarks);
	}

	/**
	 * Resets the array of level counters. Called by reset.
	 * @param levelCounters Array of level counters.
	 */
	private void reset(LevelCounter[] levelCounters) {
		for (LevelCounter levelCounter : levelCounters) levelCounter.reset();
	}

	/**
	 * Updates the stats when a mark is appended (d = 1) or removed (d = -1).<br>
	 * Called by viewer.sayMarkAddition, viewer.sayMarkRemoval.
	 * @param mark Mark.
	 * @param d Either 1 when mark is appended, or -1 when mark is removed.
	 */
	public void update(Mark mark, int d) {
		//print("stats.update " + mark.num + " d=" + d + " levelNum=" + mark.levelNum + " type=" + Q + mark.type.name + Q);
		int levelNum = mark.levelNum;
		if (levelNum < 1) return;

		LevelCounter levelMark = levelMarks[levelNum - 1];
		LevelCounter levelPair = levelPairs[levelNum - 1];

		// Checks if the verb is positive using its number.
		boolean isPositive = mark.verb.num > 0;

		// Update sums for the specific level and in total.
		levelMark.sum += d;
		totalMark.sum += d;
		if (isPositive) {
			levelPair.sum += d;
			totalPair.sum += d;
		}

		// Update counters for the type of mark: Level or User, Rule, or Law.
		switch (mark.type) {
			case Level:
			case User:
				levelMark.marksByLevel += d;
				totalMark.marksByLevel += d;
				if (isPositive) {
					levelPair.marksByLevel += d;
					totalPair.marksByLevel += d;
				}
				break;
			case Rule:
				levelMark.marksByRule += d;
				totalMark.marksByRule += d;
				if (isPositive) {
					levelPair.marksByRule += d;
					totalPair.marksByRule += d;
				}
				break;
			case Law:
				int j = mark.refNum - 1;
				levelMark.marksByLaw[j] += d;
				totalMark.marksByLaw[j] += d;
				if (isPositive) {
					levelPair.marksByLaw[j] += d;
					totalPair.marksByLaw[j] += d;
				}
				break;
			default:
				print("stats.updateMark bad mark.type!");
		}
	}	
}
