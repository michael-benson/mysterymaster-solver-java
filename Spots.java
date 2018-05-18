package com.mysterymaster.solver;

import static com.mysterymaster.puzzle.ISolver.MAX_LAWS;
import static com.mysterymaster.puzzle.ISolver.MAX_LEVELS;
import com.mysterymaster.puzzle.Mark;

/**
 * The Spots class defines solve options (aka spots) set by the user in the Setup and Board forms.<br>
 * Note: The okPauseNext is set when then user clicks the Pause button.<br>
 * Copyright (c) 2018 mysterymaster.com. All rights reserved.
 * @version 2018-05-15
 * @author Michael Benson <michael.benson@mysterymaster.com>
 */
public class Spots {
	/** Pause on all events. Setup option for the Viewer. */
	private boolean okPauseAll = false;

	/** Pause when the level changes. Setup option for the Viewer. */
	private boolean okPauseLevel = false;

	/** Pause when a solution is found. Setup option for the Viewer. */
	private boolean okPauseSolution = true;

	/** Pause when a violation occurs. Setup option for the Viewer. */
	private boolean okPauseViolation = false;

	/** Pause when a mark is entered. Setup option for the Viewer. */
	private boolean okPauseMark = false;

	/** Pause when a mark is entered via a rule (aka trigger). Setup option for the Viewer. */
	private boolean okPauseTrigger = false;

	/** Pause when an assumption is made. Setup option for the Viewer. */
	private boolean okPauseGuess = false;

	/** Pause when the name changes for one or more nouns. Setup option for the Viewer. */
	private boolean okPausePlacers = false;

	/** Pause when the next opportunity arises. Set to true by viewer.doPause. Set to false by viewer.sayWait. */
	public boolean okPauseNext = false;

	/** Auto-run puzzle when page is loaded. Setup option for the Viewer. */
	public boolean okAutorun = false;

	/** Reorder chart when a pair is entered/removed. Setup option for the Viewer. */
	public boolean okRechart = false;
	
	/** Display Floating Action Button for Android. Setup option for the Viewer. */
	public boolean okShowFab = true;

	/** Allow rules to be examined. Setup option for the Solver. */
	public boolean okRules = true;

	/** Allow marks via rules. Setup option for the Solver. */
	public boolean okTriggers = true;

	/** Allow level flags. Allocate one extra because flag[0] is for all levels. Setup option for the Solver. */
	public final boolean[] okLevels = new boolean[MAX_LEVELS + 1];

	/** Allow law flags. Allocate one extra because flag[0] is for all laws. Setup option for the Solver. */
	public final boolean[] okLaws = new boolean[MAX_LAWS + 1];
	
	/** Locker object. */
	private final ILocker locker;
	
	/**
	 * Constructor. Initializes all values except okPauseNext from the locker.
	 * @param locker Locker.
	 */
	public Spots(ILocker locker) {
		this.locker = locker;
		
		// Pauses.
		okPauseAll       = locker.getBoolean("okPauseAll"      , okPauseAll);
		okPauseLevel     = locker.getBoolean("okPauseLevel"    , okPauseLevel);
		okPauseSolution  = locker.getBoolean("okPauseSolution" , okPauseSolution);
		okPauseViolation = locker.getBoolean("okPauseViolation", okPauseViolation);
		okPauseMark      = locker.getBoolean("okPauseMark"     , okPauseMark);
		okPauseTrigger   = locker.getBoolean("okPauseTrigger"  , okPauseTrigger);
		okPauseGuess     = locker.getBoolean("okPauseGuess"    , okPauseGuess);
		okPausePlacers   = locker.getBoolean("okPausePlacers"  , okPausePlacers);			

		// General.
		okAutorun  = locker.getBoolean("okAutorun" , okAutorun);
		okRechart  = locker.getBoolean("okRechart" , okRechart);
		okRules    = locker.getBoolean("okRules"   , okRules);
		okTriggers = locker.getBoolean("okTriggers", okTriggers);
		
		// Levels.
		boolean ok = true;
		for (int i = 0; i < MAX_LEVELS + 1; i++) {
			String key = "okLevel" + i;
			okLevels[i] = locker.getBoolean(key, ok);
		}

		// Laws.
		for (int i = 0; i < MAX_LAWS + 1; i++) {
			String key = "okLaw" + i;
			okLaws[i] = locker.getBoolean(key, ok);
		}
	}
	
	/**
	 * Updates the Setup Option, and saves the new value to the locker.
	 * @param key Key.
	 * @param val Value.
	 * @return Value.
	 */
	public boolean setValue(String key, boolean val) {
		switch (key) {
			// Pauses.
			case "okPauseAll"      : okPauseAll       = locker.setBoolean(key, val); break;
			case "okPauseLevel"    : okPauseLevel     = locker.setBoolean(key, val); break;
			case "okPauseSolution" : okPauseSolution  = locker.setBoolean(key, val); break;
			case "okPauseViolation": okPauseViolation = locker.setBoolean(key, val); break;
			case "okPauseMark"     : okPauseMark      = locker.setBoolean(key, val); break;
			case "okPauseTrigger"  : okPauseTrigger   = locker.setBoolean(key, val); break;
			case "okPauseGuess"    : okPauseGuess     = locker.setBoolean(key, val); break;
			case "okPausePlacers"  : okPausePlacers   = locker.setBoolean(key, val); break;

			// General.
			case "okAutorun" : okAutorun  = locker.setBoolean(key, val); break;
			case "okRechart" : okRechart  = locker.setBoolean(key, val); break;
			case "okShowFab" : okShowFab  = locker.setBoolean(key, val); break;
			case "okRules"   : okRules    = locker.setBoolean(key, val); break;
			case "okTriggers": okTriggers = locker.setBoolean(key, val); break;

			// Levels.
			case "okLevels0": okLevels[0] = locker.setBoolean(key, val); break;
			case "okLevels1": okLevels[1] = locker.setBoolean(key, val); break;
			case "okLevels2": okLevels[2] = locker.setBoolean(key, val); break;
			case "okLevels3": okLevels[3] = locker.setBoolean(key, val); break;
			case "okLevels4": okLevels[4] = locker.setBoolean(key, val); break;

			// Laws.
			case "okLaws0": okLaws[0] = locker.setBoolean(key, val); break;
			case "okLaws1": okLaws[1] = locker.setBoolean(key, val); break;
			case "okLaws2": okLaws[2] = locker.setBoolean(key, val); break;
			case "okLaws3": okLaws[3] = locker.setBoolean(key, val); break;
			case "okLaws4": okLaws[4] = locker.setBoolean(key, val); break;
			case "okLaws5": okLaws[5] = locker.setBoolean(key, val); break;
		}

		return val;
	}
	
	/**
	 * Returns the value of the Setup Option.
	 * @param key Key.
	 * @return Value.
	 */
	public boolean getValue(String key) {
		boolean val = false;
		switch (key) {
			// Pauses.
			case "okPauseAll"      : val = okPauseAll;       break;
			case "okPauseLevel"    : val = okPauseLevel;     break;
			case "okPauseSolution" : val = okPauseSolution;  break;
			case "okPauseViolation": val = okPauseViolation; break;
			case "okPauseMark"     : val = okPauseMark;      break;
			case "okPauseTrigger"  : val = okPauseTrigger;   break;
			case "okPauseGuess"    : val = okPauseGuess;     break;
			case "okPausePlacers"  : val = okPausePlacers;   break;

			// General.
			case "okAutorun" : val = okAutorun;  break;
			case "okRechart" : val = okRechart;  break;
			case "okShowFab" : val = okShowFab;  break;
			case "okRules"   : val = okRules;    break;
			case "okTriggers": val = okTriggers; break;

			// Levels.
			case "okLevels0": val = okLevels[0]; break;
			case "okLevels1": val = okLevels[1]; break;
			case "okLevels2": val = okLevels[2]; break;
			case "okLevels3": val = okLevels[3]; break;
			case "okLevels4": val = okLevels[4]; break;

			// Laws.
			case "okLaws0": val = okLaws[0]; break;
			case "okLaws1": val = okLaws[1]; break;
			case "okLaws2": val = okLaws[2]; break;
			case "okLaws3": val = okLaws[3]; break;
			case "okLaws4": val = okLaws[4]; break;
			case "okLaws5": val = okLaws[5]; break;
		}

		return val;		
	}

	// <editor-fold defaultstate="collapsed" desc="IViewer">

	public boolean sayStarted(final String msg) {
		return msg != null && (okPauseNext || okPauseAll);
	}

	public boolean sayStopped() {
		return false;
	}

	public boolean sayLevel() {
		return okPauseNext || okPauseAll || okPauseLevel;
	}

	public boolean saySolution() {
		return okPauseNext || okPauseAll || okPauseSolution;
	}

	public boolean sayAddMark(Mark mark) {
		return okPauseNext || okPauseAll || okPauseMark || (okPauseTrigger && mark.type == Mark.Type.Rule) || (okPauseGuess && mark.levelNum == MAX_LEVELS);
	}

	public boolean sayRemoveMark(final Mark mark) {
		return okPauseNext || okPauseAll || okPauseMark || (mark.hasPlacers() && okPausePlacers);
	}

	public boolean sayValidMark() {
		return okPauseNext || okPauseAll || okPauseMark;
	}

	public boolean sayContradiction() {
		return okPauseNext || okPauseAll || okPauseViolation;
	}

	public boolean sayFactViolation() {
		return okPauseNext || okPauseAll || okPauseViolation;
	}

	public boolean sayRuleViolation() {
		return okPauseNext || okPauseAll || okPauseViolation;
	}

	public boolean sayLawViolation() {
		return okPauseNext || okPauseAll || okPauseViolation;
	}

	public boolean sayPlacers() {
		return okPauseNext || okPauseAll || okPausePlacers;
	}

	// </editor-fold>
}
