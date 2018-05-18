package com.mysterymaster.solver;

import com.mysterymaster.puzzle.Fact;
import com.mysterymaster.puzzle.Mark;
import com.mysterymaster.puzzle.Rule;

/**
 * The IViewer interface is implemented by the Viewer class and called by the Solver class.<br>
 * Copyright (c) 2018 mysterymaster.com. All rights reserved.
 * @version 2018-05-16
 * @author Michael Benson <michael.benson@mysterymaster.com>
 */
public interface IViewer {
	/**
	 * Displays a debug message, which may output to the IDE.
	 * @param msg Message.
	 */
	void jot(String msg);

	/**
	 * 1. Updates UI when a thread starts running. All threads.
	 * @param msg Message, or null.
	 */
	void sayStarted(String msg);

	/**
	 * 2. Updates UI when a thread stops running. All threads.
	 * @param msg Message, or null.
	 */
	void sayStopped(String msg);

	/**
	 * 3. Updates UI when the level changes. Finder thread.
	 * @param msg Message.
	 */
	void sayLevel(String msg);

	/**
	 * 4. Updates UI when a solution is found. Finder or Lawyer threads.
	 * @param msg Message.
	 */
	void saySolution(String msg);

	/**
	 * 5. Updates UI when a mark is entered. Finder or Lawyer threads.
	 * @param msg Message.
	 * @param mark Mark.
	 */
	void sayAddMark(String msg, Mark mark);

	/**
	 * 6. Updates UI when a mark is removed. Finder or UndoMark threads.
	 * @param msg Message.
	 * @param mark Mark.
	 */
	void sayRemoveMark(String msg, Mark mark);

	/**
	 * 7. Updates UI when a mark is validated. Finder or Lawyer threads.
	 * @param msg Message.
	 * @param mark Mark.
	 */
	void sayValidMark(String msg, Mark mark);

	/**
	 * 8. Updates UI when a potential mark contradicts existing mark. Finder or Lawyer threads.
	 * @param msg Message.
	 */
	void sayContradiction(String msg);

	/**
	 * 9. Updates UI when a mark violates a fact. Finder or Lawyer threads.
	 * @param msg Message.
	 * @param mark Mark.
	 * @param fact Fact.
	 */
	void sayFactViolation(String msg, Mark mark, Fact fact);

	/**
	 * 10. Updates UI when a mark violates a rule. Finder or Lawyer threads.
	 * @param msg Message.
	 * @param mark Mark.
	 * @param rule Rule.
	 */
	void sayRuleViolation(String msg, Mark mark, Rule rule);

	/**
	 * 11. Updates UI when a mark violates a law. Finder or Lawyer threads.
	 * @param msg Message.
	 * @param mark Mark.
	 */
	void sayLawViolation(String msg, Mark mark);

	/**
	 * 12. Updates UI when a rule updates one or more nouns. Lawyer thread.
	 * @param msg Message.
	 * @param mark Mark.
	 * @param rule Rule.
	 */
	void sayPlacers(String msg, Mark mark, Rule rule);
}
