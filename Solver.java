package com.mysterymaster.solver;

import com.mysterymaster.puzzle.*;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * The Solver class solves a logic puzzle via the Finder and Lawyer classes. This class implements the<br>
 * ISolver interface in the Puzzle package, and is the only class that references the Finder and Lawyer.<br>
 * Copyright (c) 2018 mysterymaster.com. All rights reserved.
 * @version 2018-05-16
 * @author Michael Benson <michael.benson@mysterymaster.com>
 */
public final class Solver extends Base implements ISolver, Runnable {
	/** Viewer object. This is passed to the constructor. */
	private final IViewer viewer;
	
	/** Setup Options object. Argument in the constructor. */
	final Spots spots;

	/** Stats object. Instantiated in the constructor. Must be accessible to the Viewer. */
	public final Stats stats;

	/** Finder object. Instantiated in the constructor. */
	private final Finder finder;

	/** Lawyer object. Instantiated in the constructor. */
	private final Lawyer lawyer;

	@Override
	public String toString() {
		return "Solver";
	}

	@Override
	public String asString() {
		return this.toString();
	}

	@Override
	public void jot(String msg) {
		viewer.jot(msg);
	}

	/**
	 * Constructor. Called by Viewer.constructor.
	 * @param viewer Viewer.
	 * @param spots Spots.
	 */
	public Solver(final IViewer viewer, Spots spots) {
		this.viewer = viewer;
		this.spots = spots;

		stats = new Stats();
		finder = new Finder(this);
		lawyer = new Lawyer(this);
	}

	/**
	 * Sets the puzzle. Called by viewer.setPuzzle.
	 * @param puzzle Puzzle, which may be null.
	 */
	public void setPuzzle(final Puzzle puzzle) {
		this.puzzle = puzzle;

		// Calculate number of grids, then pairs, then marks.
		if (puzzle == null) {
			maxGrids = 0;
			maxPairs = 0;
			maxMarks = 0;
		}
		else {
			maxGrids = puzzle.maxNounTypes * (puzzle.maxNounTypes - 1) / 2;
			maxPairs = maxGrids * puzzle.maxNouns;
			maxMarks = maxPairs * puzzle.maxNouns;
		}

		// Initalize marks array.
		marks = new Mark[maxMarks];
		for (int i = 0; i < maxMarks; i++) { marks[i] = new Mark(i); }

		// Initialize grids array.
		if (puzzle == null) {
			grids = null;
		}
		else {
			grids = new Mark[maxGrids][puzzle.maxNouns][puzzle.maxNouns];
			for (int g = 0; g < maxGrids; g++) {
				for (int n1 = 0; n1 < puzzle.maxNouns; n1++) {
					for (int n2 = 0; n2 < puzzle.maxNouns; n2++) {
						grids[g][n1][n2] = null;
					}
				}
			}
		}

		finder.setPuzzle(puzzle);
		lawyer.setPuzzle(puzzle);
	}

	/** Resets the Solver. Called by doFinder, viewer.reset. */
	public void reset() {
		if (puzzle != null) puzzle.reset();
		
		numGuesses = 0;
		numPairs = 0;
		numMarks = 0;
		numFacts = 0; numFactHits = 0;
		numRules = 0; numRuleHits = 0;
		numValidMarks = 0;

		quitFlag = false;

		time1 = null;
		numSolutions = 0;
		
		// Reset marks.
		for (Mark mark : marks) { mark.reset(); }
		
		// Reset grids.
		if (puzzle != null) {
			if (grids != null) {
				for (int g = 0; g < maxGrids; g++) {
					for (int n1 = 0; n1 < puzzle.maxNouns; n1++) {
						for (int n2 = 0; n2 < puzzle.maxNouns; n2++) {
							grids[g][n1][n2] = null;
						}
					}
				}
			}
		}
		
		stats.reset();
	}

	// <editor-fold defaultstate="collapsed" desc="Thread">

	/** Work flag. True while a thread is running. */
	private boolean workFlag = false;

	/** Quit flag. True when the Quit button is clicked, or app wants to stop the running thread. */
	boolean quitFlag = false;

	/** Requests the solver to stop solving. Called by viewer.doQuit. */
	public void doQuit() {
		quitFlag = true;
	}

	/** Thread number, where the zero=based number is either: 0=Finder, 1=Lawyer, 2=Eraser. */
	private int threadNum = -1;

	/**
	 * Returns a thread with the given name. Called by viewer.doSolve, viewer.addMarkByUser.
	 * @param num Zero-based thread number.
	 * @return Thread, or null
	 */
	public Thread getThread(final int num) {
		final String[] names = { "Finder", "Lawyer", "Eraser" };
		this.threadNum = num;
		Thread thread = new Thread(this);
		thread.setName(names[num]);
		return thread;
	}

	/**
	 * The thread runs one of the following methods:<br>
	 * 1) solver.doFinder.<br>
	 * 2) solver.doLawyer.<br>
	 * 3) solver.undoUserMark.
	 */
	@Override
	public void run() {
		int rs = 0;
		workFlag = true;
		quitFlag = false;
		print("exec solver.run mode=" + threadNum + " workFlag=" + workFlag);
		switch (threadNum) {
			case 0: rs = doFinder(); break;
			case 1: rs = doLawyer(); break;
			case 2: rs = undoUserMark(); break;
		}
		print("done solver.run mode=" + threadNum + " workFlag=" + workFlag + " quitFlag=" + quitFlag + " rs=" + rs);
		threadNum = -1;
		workFlag = false;
		quitFlag = false;
	}

	/** Solves the puzzle by invoking the Finder.<br>Called by the run method. */
	private int doFinder() {
		reset();
		time1 = new Date();
		String msg1 = "I started solving at " + formatDT(time1) + ".";
		sayStarted(msg1);

		int rs = finder.doWork();

		Date time2 = new Date();
		String msg2 = "I stopped solving at " + formatDT(time2) + " in " + getMsgElapsedTime(time1, time2);
		sayStopped(msg2);
		return rs;
	}

	/** Enforces the laws on the mark entered by the user.<br>Called by the run method. */
	private int doLawyer() {
		sayStarted(null);
		Mark mark = marks[numMarks - 1];
		int rs = lawyer.doWork(mark);
		sayStopped(null);
		return rs;
	}

	/** Undo marks back to and including last user mark.<br>Called by the run method. */
	private int undoUserMark() {
		sayStarted(null);
		int rs = 0;
		while (numMarks > 0) {
			if (quitFlag) break;
			Mark mark = removeMark();
			if (mark.type == Mark.Type.User) break;
		}
		sayStopped(null);
		return rs;
	}

	/**
	 * Pauses the current thread when the Viewer is called.<br>
	 * To resume, the Viewer must interrupt this thread.<br>
	 * Called by the IViewer "help" methods in the solver class.
	 */
	private void doPause() {
		if (!workFlag) return;

		try {
			Thread.sleep(Long.MAX_VALUE);
		}
		catch (InterruptedException ex) {
			//print(Thread.currentThread().getName() + " was interrupted and will resume.");
		}
	}

	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Solver">

	/** Puzzle object. */
	private Puzzle puzzle = null;

	/** Number of facts examined by the solver. Read-only by the viewer. */
	public int numFacts = 0;

	/** Number of times any fact has been referenced by the solver. Read-only by the viewer. */
	public int numFactHits = 0;

	/** Number of rules examined by the solver. Read-only by the viewer. */
	public int numRules = 0;

	/** Number of times any rule has been referenced by the solver. Read-only by the viewer. */
	public int numRuleHits = 0;

	/** Number of positive marks entered by the solver. Read-only by the viewer. */
	public int numPairs = 0;

	/** Number of marks entered by the solver. Read-only by the viewer. */
	public int numMarks = 0;

	/** Maximum number of pairs. Read-only by the viewer. */
	public int maxPairs = 0;

	/** Number of assumptions made by the solver. Read-only by the viewer. */
	public int numGuesses = 0;

	/** Maximum number of marks. Read-only by the viewer. */
	public int maxMarks = 0;

	/** Maximum number of grids. */
	private int maxGrids = 0;

	/**
	 * Number of marks examined by the Lawyer.<br>
	 * A solution is found ONLY when all of the marks have been validated by the Lawyer.
	 */
	int numValidMarks = 0;

	/** Number of solutions. */
	private int numSolutions = 0;

	@Override
	public int getMaxMarks() { return maxMarks; }

	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Timer">

	/** Time when either the Finder or the User began solving the puzzle. */
	private Date time1 = null;

	/** Date/Time format string. */
	private final static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ms", Locale.US);

	/**
	 * Returns the time as a formatted string.
	 * @param time Time.
	 * @return String.
	 */
	private static String formatDT(final Date time) {
		return SDF.format(time);
	}

	/**
	 * Returns the string stating the elapsed time between two times.
	 * @param time1 Time 1.
	 * @param time2 Time 2.
	 * @return String.
	 */
	private static String getMsgElapsedTime(final Date time1, final Date time2) {
		long elapsedTime = time2.getTime() - time1.getTime();
		return "" + elapsedTime + " ms.";
	}

	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Marks">

	/** Array of marks treated as a stack. This array may be empty, but is NEVER null! */
	public Mark[] marks = new Mark[0];

	/**
	 * Returns the last mark in the array, or null.
	 * @return Mark, or null.
	 */
	public Mark getLastMark() {
		return (numMarks > 0) ? marks[numMarks - 1] : null;
	}

	/**
	 * Returns the last mark entered by the user, or null.<br>
	 * Called by viewer.undoUserMark.
	 * @return Mark, or null.
	 */
	public Mark getLastUserMark() {
		Mark mark = null;
		if (workFlag) return mark;

		for (int i = numMarks; i > 0; i--) {
			mark = marks[i - 1];
			if (mark.type == Mark.Type.User) break;
		}
		return mark;
	}

	/**
	 * Submits the user's mark. Called by viewer.addMarkByUser.
	 * @param noun1 Noun 1.
	 * @param verb Verb.
	 * @param noun2 Noun 2.
	 * @return Status.
	 */
	public int addMarkByUser(final Noun noun1, final Verb verb, final Noun noun2) {
		int levelNum = MAX_LEVELS;
		char levelSub = ' ';
		return addMark("", levelNum, levelSub, Mark.Type.User, levelNum, levelSub, noun1, verb, noun2, null, -1, null);
	}

	@Override
	public int addMarkByRule(final Mark mark, final Rule rule, final char refSub, final Noun noun1, final Verb verb, final Noun noun2, final String msg) {
		int rs = 0;
		if (!spots.okTriggers) return rs;

		// Return success if the mark already exists.
		if (isMark(noun1, verb, noun2)) return rs;

		String reason = "Rule " + rule.num + (refSub == ' ' ? "" : refSub) + " on mark " + mark.num + ", " + msg;
		rs = addMark(reason, mark.levelNum, mark.levelSub, Mark.Type.Rule, rule.num, refSub, noun1, verb, noun2, null, -1, mark);
		return rs;
	}

	/**
	 * Adds the mark to the queue. Called by the Finder and the Lawyer.
	 * Note: This method may internally swap the nouns.
	 * @param reason Reason.
	 * @param levelNum Level number.
	 * @param levelSub Level character.
	 * @param markType Mark type.
	 * @param refNum Reference number.
	 * @param refSub Reference character.
	 * @param nounA Noun A.
	 * @param verb Verb.
	 * @param nounB Noun B.
	 * @param facts Array of facts, or empty.
	 * @param lonerNum Loner number, or -1.
	 * @param refMark Mark that triggered a new mark, or null.
	 * @return Status.
	 */
	int addMark(final String reason, final int levelNum, final char levelSub, final Mark.Type markType, final int refNum, final char refSub, final Noun nounA, final Verb verb, final Noun nounB, final List<Fact> facts, final int lonerNum, final Mark refMark) {
		int rs = 0;
		//print("solver.addMark" + " msg=" + msg + " type=" + markType + " noun1=" + noun1 + " verb=" + verb + " noun2=" + noun2 + " quitFlag=" + quitFlag);
		if (quitFlag) return rs;

		// Set the start time if it is null. This is the case if the user is manually solving the puzzle.
		if (time1 == null) time1 = new Date();

		// Throw exception if both nouns have the same type.
		if (nounA.type == nounB.type) {
			throw new Error("solver.addMark Error: Both nouns have the same type!");
		}

		// If necessary, swap the nouns so noun1.type.num < noun2.type.num.
		Noun noun1 = nounA.type.num < nounB.type.num ? nounA : nounB;
		Noun noun2 = nounA.type.num < nounB.type.num ? nounB : nounA;

		// Return success if mark already exists, or failure if potential mark contradicts an existing mark.
		// Note: Allow rule to override contradiction.
		Mark oldMark = getGridMark(noun1, noun2);
		if (oldMark != null) {
			if (oldMark.verb != verb) {
				if (markType != Mark.Type.Rule) {
					String refTo = "" + refNum + (refSub == ' ' ? "" : refSub);
					String msg = (markType == Mark.Type.User ? "The " + markType.name : markType.name + " " + refTo) +
					 " requests a mark that would contradict mark " + oldMark.num + "!" + NL + reason;
					sayContradiction(msg);
				}
				rs = -1;
			}
			return rs;
		}

		//print("solver.addMark numMarks=" + numMarks + " maxMarks=" + maxMarks);
		if (numMarks >= maxMarks) {
			// This should never happen!
			throw new Error("solver.addMark Error: Too many marks!");
		}

		// Determine the who and what for the potential mark.
		String whom = (markType == Mark.Type.User) ? "You" : "I";
		String what = (markType == Mark.Type.Level && levelNum == MAX_LEVELS) ? " assumed " : " entered ";
		String name = whom + what + "'" + verb.code + "' for " + noun1.name + " and " + noun2.name + ".";
		if (reason.length() > 0) name += NL + reason;

		// Update the number of marks along with updating the mark.
		Mark mark = marks[numMarks++];
		mark.update(name, levelNum, levelSub, markType, refNum, refSub, noun1, verb, noun2, facts, lonerNum, refMark);

		// Update the number of guesses.
		if (mark.guess) ++numGuesses;

		// Update the number of pairs if the verb is positive.
		if (mark.verb == Puzzle.Is) {
			++numPairs;
			mark.noun1.pairs[mark.noun2.type.num - 1] = mark;
			mark.noun2.pairs[mark.noun1.type.num - 1] = mark;
		}

		// Update the grids.
		setGridMark(mark);

		// Each fact in the facts array updates the counters.
		for (Fact fact : mark.facts) {
			++fact.hits;
			if (fact.hits == 1) ++numFacts;
			++numFactHits;
		}

		// Update work variables and UI when a rule triggers a mark.
		// See sayRuleViolation when a mark violates a rule.
		if (mark.type == Mark.Type.Rule) {
			int ruleNum = mark.refNum;
			Rule rule = puzzle.rules.get(ruleNum - 1);

			++rule.hits;
			if (rule.hits == 1) ++numRules;
			++numRuleHits;
		}

		sayAddMark(name, mark);

		// Note: When the user enters a mark, the Lawyer is invoked in its own thread.
		if (mark.type != Mark.Type.User) {
			rs = lawyer.doWork(mark);
			if (rs != 0) return rs;
		}

		// See if a solution was found AFTER the Lawyer has validated ALL marks.
		if (numValidMarks == maxMarks) {
			saySolution();
			if (mark.levelNum < Solver.MAX_LEVELS) quitFlag = true;
		}

		//print("solver.addMark rs=" + rs);
		return rs;
	}

	/** Removes marks back to and including the last mark entered by the levels. */
	void undoAssumption() {
		while (numMarks > 0) {
			if (quitFlag) break;
			Mark mark = removeMark();
			if (mark.type == Mark.Type.Level) break;
		}

		if (numValidMarks > numMarks) numValidMarks = numMarks;
	}

	/**
	 * Removes the last mark that was entered. Called by undoUserMark, undoAssumption.
	 * @return Mark that was removed.
	 */
	private Mark removeMark() {
		Mark mark = marks[numMarks - 1];

		// Undo grids.
		removeGridMark(mark);

		// Undo pairs.
		if (mark.verb == Puzzle.Is) {
			--numPairs;
			mark.noun1.pairs[mark.noun2.type.num - 1] = null;
			mark.noun2.pairs[mark.noun1.type.num - 1] = null;
		}

		// Undo facts disabled by this mark.
		mark.undoDisabledFacts();

		// This mark is no longer valid.
		mark.valid = false;

		--numMarks;

		sayRemoveMark(mark);
		// Clear the rulePlacers after showing them being reset in the Viewer!
		mark.clearPlacers();
		return mark;
	}

	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Grids">

	/** Array of grids. */
	private Mark[][][] grids = null;

	/**
	 * Returns the one-based grid number given the one-based numbers of two noun types.
	 * @param t1 One-based number of noun type 1.
	 * @param t2 One-based number of noun type 2.
	 * @return The one-based number of the grid.
	 */
	private int getGridNum(final int t1, final int t2) {
		return (t1 < t2) ? (t1 - 1) * puzzle.maxNounTypes + t2 - t1 * (t1 + 1) / 2 : (t2 - 1) * puzzle.maxNounTypes + t1 - t2 * (t2 + 1) / 2;
	}

	/**
	 * Returns the mark in the grid given by two nouns.
	 * @param noun1 Noun 1.
	 * @param noun2 Noun 2.
	 * @return The Mark object, or null.
	 */
	Mark getGridMark(final Noun noun1, final Noun noun2) {
		return getGridMark2(noun1.type.num, noun1.num, noun2.type.num, noun2.num);
	}

	private Mark getGridMark2(final int t1, final int n1, final int t2, final int n2) {
		if (t1 == t2) return null;
		if (t1 < t2) {
			int g = getGridNum(t1, t2);
			return grids[g - 1][n1 - 1][n2 - 1];
		}
		else {
			int g = getGridNum(t2, t1);
			return grids[g - 1][n2 - 1][n1 - 1];
		}
	}

	/**
	 * Enters the given mark into the grids.
	 * @param mark Mark.
	 */
	private void setGridMark(final Mark mark) {
		setGridMark2(mark.noun1.type.num, mark.noun1.num, mark.noun2.type.num, mark.noun2.num, mark);
	}

	/**
	 * Enters the mark/null into the grids using the one-based numbers of two nouns.
	 * @param t1 One-based number of noun 1's type.
	 * @param n1 One-based number of noun 1.
	 * @param t2 One-based number of noun 2's type.
	 * @param n2 One-based number of noun 2.
	 * @param mark Mark, or null.
	 */
	private void setGridMark2(final int t1, final int n1, final int t2, final int n2, final Mark mark) {
		if (t1 == t2) return;
		if (t1 < t2) {
			int g = getGridNum(t1, t2);
			grids[g - 1][n1 - 1][n2 - 1] = mark;
		}
		else {
			int g = getGridNum(t2, t1);
			grids[g - 1][n2 - 1][n1 - 1] = mark;
		}
	}

	/**
	 * Removes the given mark from the grids. Called by removeMark.
	 * @param mark Mark.
	 */
	private void removeGridMark(final Mark mark) {
		setGridMark2(mark.noun1.type.num, mark.noun1.num, mark.noun2.type.num, mark.noun2.num, null);
	}

	@Override
	public Verb getGridVerb(final Noun noun1, final Noun noun2) {
		if (noun1.type == noun2.type) return Puzzle.IsNot;
		Mark mark = getGridMark(noun1, noun2);
		return (mark == null) ? Puzzle.Maybe : mark.verb;
	}

	/**
	 * Determines if the mark already exists. Called by solver.addMarkByRule, finder, lawyer.
	 * @param noun1 Noun 1.
	 * @param verb Verb.
	 * @param noun2 Noun 2.
	 * @return True if the mark already exists, otherwise false.
	 */
	boolean isMark(final Noun noun1, final Verb verb, final Noun noun2) {
		Mark mark = getGridMark(noun1, noun2);
		boolean b = mark != null && mark.verb == verb;
		//print("puzzle.isMark(" + noun1 + "," + verb + "," + noun2 + ")?" + b);
		return b;
	}

	/**
	 * Returns a list of nouns of noun type 2 that may be with noun 1. Called by doLaw4NounsAndType.
	 * @param noun1 Noun 1.
	 * @param nounType2 Noun type 2.
	 * @return List of nouns.
	 */
	List<Noun> getNouns(final Noun noun1, final NounType nounType2) {
		List<Noun> nouns = new ArrayList<>();
		for (Noun noun2 : nounType2.nouns) {
			if (getGridMark(noun1, noun2) == null) nouns.add(noun2);
		}
		return nouns;
	}

	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Pairs">

	@Override
	public boolean maybeRelated(final Noun noun1, final Link link, final Noun noun2) {
		boolean ok = false;
		NounType type = link.nounType;

		Noun slot1 = Mark.getPairNoun(noun1, type);
		Noun slot2 = Mark.getPairNoun(noun2, type);

		if (slot1 != null && slot2 != null) {
			// 1. Returns true if both nouns are slotted, and the slots are related.
			if (link.getVerb(slot1, slot2) == Puzzle.Is) return true;
		}
		else if (slot1 != null && slot2 == null) {
			// 2. Returns true if slot1 is related to any possible slot for noun2.
			for (Noun slotB : type.nouns) {
				if (getGridVerb(slotB, noun2) != Puzzle.Maybe) continue;
				if (link.getVerb(slot1, slotB) == Puzzle.Is) return true;
			}
		}
		else if (slot1 == null && slot2 != null) {
			// 3. Returns true if any possible slot for noun1 is related to slot2.
			for (Noun slotA : type.nouns) {
				if (getGridVerb(slotA, noun1) != Puzzle.Maybe) continue;
				if (link.getVerb(slotA, slot2) == Puzzle.Is) return true;
			}
		}
		else {
			// 4. Returns true if any possible slot for noun1 is related to any possible slot for noun2.
			for (Noun slotA : type.nouns) {
				if (getGridVerb(slotA, noun1) != Puzzle.Maybe) continue;
				for (Noun slotB : type.nouns) {
					if (getGridVerb(slotB, noun2) != Puzzle.Maybe) continue;
					if (link.getVerb(slotA, slotB) == Puzzle.Is) return true;
				}
			}
		}

		return ok;
	}

	boolean canBeLinked(final Noun noun1, final Link link, final Noun slot2, final int i) {
		List<Noun> slots = link.nounType.nouns;
		for (Noun slot1 : slots) {
			Verb verb = (i != 1) ? link.getVerb(slot1, slot2) : link.getVerb(slot2, slot1);
			if (verb == Puzzle.Is && canBeWith(slot1, noun1)) return true;
		}
		return false;
	}

	@Override
	public Noun getCommonNoun(final Noun noun1, final Noun noun2, final NounType nounType3) {
		for (Noun noun3 : nounType3.nouns) {
			if (canBeWith(noun1, noun3) && canBeWith(noun2, noun3)) return noun3;
		}
		return null;
	}

	@Override
	public boolean canBeWith(final Noun noun1, final Noun noun2) {
		boolean rs = false;
		Noun noun;

		// Return false if there is an 'X' for noun1 and noun2.
		Mark oldMark = getGridMark(noun1, noun2);
		if (oldMark != null && oldMark.verb == Puzzle.IsNot) return rs;

		// Return false if noun1 is with another noun of noun2's type.
		noun = Mark.getPairNoun(noun1, noun2.type);
		if (noun != null && noun != noun2) return rs;

		// Return false if noun2 is with another noun of noun1's type.
		noun = Mark.getPairNoun(noun2, noun1.type);
		if (noun != null && noun != noun1) return rs;

		return true;
	}

	@Override
	public boolean cannotBeWith(final List<Noun> nouns, final Noun noun2) {
		for (Noun noun1 : nouns) {
			if (getGridVerb(noun1, noun2) != Puzzle.IsNot) return false;
		}
		return true;
	}

	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Calls to IViewer methods">

	/**
	 * Updates UI when the solver begins. Called by doFinder.
	 * @param msg Message.
	 */
	private void sayStarted(final String msg) {
		viewer.sayStarted(msg);
		doPause();
	}

	/**
	 * Updates UI when the solver stops. Called by doFinder.
	 * @param msg Message.
	 */
	private void sayStopped(final String msg) {
		viewer.sayStopped(msg);
		doPause();
	}

	/**
	 * Updates UI when the level changes. Called by the Finder.
	 * @param msg Message.
	 */
	void sayLevel(final String msg) {
		viewer.sayLevel(msg);
		doPause();
	}

	/** Updates UI when a solution is found. Called by addMark. */
	private void saySolution() {
		//print("solver.saySolution isAnswer=" + puzzle.isAnswer());
		++numSolutions;
		Date time2 = new Date();
		String msg = "I have " + (numSolutions == 1 ? "a solution" : numSolutions + " solutions") + " at " + formatDT(time2) + " in " + getMsgElapsedTime(time1, time2);
		viewer.saySolution(msg);
		doPause();
	}

	/**
	 * Updates UI when a mark is entered. Called by addMark.
	 * @param msg Message.
	 * @param mark Mark.
	 */
	private void sayAddMark(final String msg, final Mark mark) {
		stats.update(mark, 1);
		viewer.sayAddMark(msg, mark);
		doPause();
	}

	/**
	 * Updates UI when the last mark is undone. Called by removeMark.<br>
	 * Note: When a mark is removed, viewer.sayRemoveMark needs to display what rulePlacers are undone.
	 * @param mark Mark.
	 */
	private void sayRemoveMark(final Mark mark) {
		stats.update(mark, -1);
		String msg = "I removed mark " + mark.num + ".";
		if (mark.hasPlacers()) msg += NL + mark.getResetPlacersMsg();
		viewer.sayRemoveMark(msg, mark);
		doPause();
	}

	/**
	 * Updates UI when a mark is validated. Called by lawyer.doWork.<br>
	 * Called after the Lawyer has validated the mark.
	 * @param mark Mark.
	 */
	void sayValidMark(final Mark mark) {
		String msg = "I validated mark " + mark.num + ".";
		viewer.sayValidMark(msg, mark);
		doPause();
	}

	/**
	 * Updates UI when a potential mark contradicts an existing mark. Called by addMark.
	 * @param msg Message.
	 */
	private void sayContradiction(final String msg) {
		viewer.sayContradiction(msg);
		doPause();
	}

	/**
	 * Updates Solver and UI when a mark violates a fact. Called by lawyer.doFacts.
	 * @param mark Mark that violated the fact.
	 * @param fact Fact that was violated by the mark.
	 */
	void sayFactViolation(final Mark mark, final Fact fact) {
		++fact.hits;
		if (fact.hits == 1) ++numFacts;
		++numFactHits;
		String msg = mark.name + NL + "Mark " + mark.num + " violates fact " + fact.num + "!" + NL + fact.name;
		viewer.sayFactViolation(msg, mark, fact);
		doPause();

		if (mark.levelNum < MAX_LEVELS) quitFlag = true;
	}

	/**
	 * Updates Solver and UI when a mark violates a rule. Called by lawyer.doRules.
	 * @param mark Mark that violated the rule.
	 * @param rule Rule that was violated by the mark.
	 */
	void sayRuleViolation(final Mark mark, final Rule rule) {
		++rule.hits;
		if (rule.hits == 1) ++numRules;
		++numRuleHits;
		String msg = "Mark " + mark.num + " violates rule " + rule.num + "!";
		viewer.sayRuleViolation(msg, mark, rule);
		doPause();
	}

	/**
	 * Updates UI when a mark violates a law. Called by the Lawyer.
	 * @param msg Message.
	 * @param mark Mark that violated the law.
	 */
	void sayLawViolation(final String msg, final Mark mark) {
		viewer.sayLawViolation(msg, mark);
		doPause();
	}

	/**
	 * Updates Solver and UI when a noun(s) is updated by a rule invoked on a mark.<br>
	 * Note: When a mark is removed, viewer.sayRemoveMark needs to display what rulePlacers are undone.<br>
	 * Called by lawyer.doRules.
	 * @param mark Mark.
	 * @param rule Rule.
	 */
	void sayPlacers(final Mark mark, final Rule rule) {
		++rule.hits;
		if (rule.hits == 1) ++numRules;
		++numRuleHits;
		String msg = mark.getRulePlacersMsg(rule);
		viewer.sayPlacers(msg, mark, rule);
		doPause();
	}

	// </editor-fold>
}
