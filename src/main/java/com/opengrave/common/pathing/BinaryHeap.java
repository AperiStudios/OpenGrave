/*
 * Copyright 2016 Nathan Howard
 * 
 * This file is part of OpenGrave
 * 
 * OpenGrave is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OpenGrave is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with OpenGrave. If not, see <http://www.gnu.org/licenses/>.
 */
package com.opengrave.common.pathing;

import java.util.ArrayList;

public class BinaryHeap<T extends Comparable<T>> {

	ArrayList<T> openList = new ArrayList<T>();

	public BinaryHeap() {
	}

	public void add(T node) {
		openList.add(node);
		int position = openList.size();
		swapWithParent(position);
	}

	public T takeBest() {
		if (openList.size() == 0) {
			throw (new RuntimeException("Taking element from empty Binary Heap"));
		}
		T ret = openList.get(0);
		T last = openList.remove(openList.size() - 1); // Take the last item
		if (openList.size() == 0) {
			return ret;
		}
		openList.set(0, last); // And place at start
		swapWithChildren(1);
		return ret;
	}

	public void integrityChanged(T value) {
		int idx = openList.indexOf(value);
		if (idx != -1) {
			idx = swapWithParent(idx + 1);
			idx = swapWithChildren(idx + 1);
		}
	}

	/**
	 * 
	 * @param position
	 *            The position which needs to be checked and possibly pushed up
	 *            the heap (to a lower index) The lowest(first) index should be
	 *            1. Not Zero
	 */
	private int swapWithParent(int position) {
		if (position < 1) {
			return position;
		}
		if (position == 1) {
			return position;
		} // Can not pass higher than first index.
		int parent = position / 2;
		T node = openList.get(position - 1);
		T parentNode = openList.get(parent - 1);
		if (parentNode.compareTo(node) > 0) {
			// Swap Locations of items;
			openList.set(parent - 1, node);
			openList.set(position - 1, parentNode);
			// Check to see if it needs to be passed up again
			return swapWithParent(parent);
		}
		return position;
	}

	/**
	 * 
	 * @param position
	 *            The position which needs to be checked and possibly pushed
	 *            down the heap (to a higher index) The lowest(first) index
	 *            should be 1. Not Zero
	 */
	private int swapWithChildren(int position) {
		if (position < 1) {
			throw (new RuntimeException("swapWithChild position must be 1 or higher"));
		}
		int child1 = position * 2;
		int child2 = position * 2 + 1;
		if (child1 > openList.size()) {
			return position;
		} // it has no children - done
		if (child2 > openList.size()) {
			// It has only child1.
			T child1Node = openList.get(child1 - 1);
			T node = openList.get(position - 1);
			if (node.compareTo(child1Node) <= 0) {
				// Lower or equal to only child. No futher action needed
				return position;
			} else {
				return swapWithChild1(position);
			}
		} else {
			T child1Node = openList.get(child1 - 1);
			T child2Node = openList.get(child2 - 1);
			T node = openList.get(position - 1);
			if (node.compareTo(child1Node) <= 0 && node.compareTo(child2Node) <= 0) {
				// Both Nodes are higher or equal to current. No further action needed
				return position;
			} else if (child1Node.compareTo(node) < 0 && child2Node.compareTo(node) < 0) {
				// Both Nodes are lower than current
				if (child1Node.compareTo(child2Node) < 0) {
					return swapWithChild1(position);
				} else {
					return swapWithChild2(position);
				}
			} else if (child1Node.compareTo(node) < 0) {
				// Only Child 1 is lower.
				return swapWithChild1(position);
			} else {
				// Only Child 2 is lower.
				return swapWithChild2(position);
			}
		}

	}

	private int swapWithChild1(int position) {
		int child = position * 2;

		T node = openList.get(position - 1);
		T childNode = openList.get(child - 1);

		openList.set(child - 1, node);
		openList.set(position - 1, childNode);
		return swapWithChildren(child);
	}

	private int swapWithChild2(int position) {
		int child = position * 2 + 1;

		T node = openList.get(position - 1);
		T childNode = openList.get(child - 1);

		openList.set(child - 1, node);
		openList.set(position - 1, childNode);
		return swapWithChildren(child);
	}

	public int indexOf(T node) {
		return openList.indexOf(node);
	}

	public int size() {
		return openList.size();
	}

	public T get(int idx) {
		return openList.get(idx);
	}
}
