package com.mysterymaster.solver;

/**
 * The Locker interface is implemented by the Locker class and called by the Viewer class.<br>
 * Note: This is a "suggestion", since the Locker and Viewer are both in the client.<br>
 * Copyright (c) 2018 mysterymaster.com. All rights reserved.
 * @version 2018-05-11
 * @author Michael Benson <michael.benson@mysterymaster.com>
 */
public interface ILocker {
	/**
	 * Returns the value from the locker given its key and default value.
	 * @param key Key.
	 * @param def Default value.
	 * @return Value.
	 */
	boolean getBoolean(String key, boolean def);
	
	/**
	 * Stores the value to the locker given its key.
	 * @param key Key.
	 * @param val Value.
	 * @return Value.
	 */
	boolean setBoolean(String key, boolean val);
	
	/**
	 * Returns the value from the locker given its key and default value.
	 * @param key Key.
	 * @param def Default value.
	 * @return Value.
	 */
	int getInt(String key, int def);
	
	/**
	 * Stores the value to the locker given its key.
	 * @param key Key.
	 * @param val Value.
	 * @return Value.
	 */
	int setInt(String key, int val);
}
