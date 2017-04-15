/* Copyright (c) 2007-2017 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package minesweeper;

import java.util.LinkedList;
import java.util.Queue;

/**
 * TODO: Specification
 */
public class Board {

    // Abstraction Function: A 2D array of squares represents a Minesweeper
    // board

    // Representation Invariant: The grid is never null and has at least one row
    // and one column. All rows are the same size. Each square of the grid holds
    // the value of the number of adjacent bombs.

    // Representation Exposure Argument: The array of squares is not accessible
    // to clients.
    // The state of the board is only viewed through its toString method.

    // Thread Safety Argument: All public non-constructor methods are
    // synchronized

    private enum State {
        UNTOUCHED, DUG, FLAGGED
    };

    private static final int DEFAULT_SIZE = 10;
    private static final float BOMB_PERCENTAGE = 0.25f;

    private class Square {
        private boolean hasBomb;
        private State state;
        private int neighborsWithBombs;

        public Square() {
            hasBomb = false;
            state = State.UNTOUCHED;
        }

        public boolean dig() {
            if (state == State.UNTOUCHED) {
                state = State.DUG;
                return hasBomb;
            }
            return false;
        }

        public void flag() {
            if (state == State.UNTOUCHED) {
                state = State.FLAGGED;
            }
        }

        public void deflag() {
            if (state == State.FLAGGED) {
                state = State.DUG;
            }
        }

        @Override
        public String toString() {
            String retval = null;
            switch (state) {
            case DUG:
                retval = (neighborsWithBombs == 0) ? "" : Integer.toString(neighborsWithBombs);
                break;
            case FLAGGED:
                retval = "F";
                break;
            case UNTOUCHED:
                retval = "-";
            }

            return retval;
        }
    }

    private class Pair {
        public Pair(int x, int y) {
            this.x = x;
            this.y = y;
        }

        private int x;
        private int y;
    }

    private volatile Square[][] grid;

    public Board() {
        this(DEFAULT_SIZE, DEFAULT_SIZE);
    }

    public Board(int rows, int cols) {
        grid = new Square[rows][cols];
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                grid[row][col] = new Square();
                grid[row][col].hasBomb = (Math.random() < BOMB_PERCENTAGE);
            }
        }

        checkRep();
    }

    /**
     * Attempts to dig at a board location. If the square specified by x and y
     * is valid and in an undug state, sets the state of the square to DUG and
     * removes the bomb if it contains one.
     * 
     * @param x
     *            column of grid
     * @param y
     *            row of grid
     * 
     * @return true if dug square contains a bomb, false otherwise
     */
    public synchronized boolean dig(int x, int y) {
        boolean retval = false;
        if (isValidGridLocation(x, y)) {
            retval = grid[y][x].dig();
            if (retval) {
                grid[y][x].hasBomb = false;
            }
            checkRep();
        }

        return retval;
    }

    /**
     * If the square specified is untouched, changes its state to flagged.
     * 
     * @param x
     *            column of grid
     * @param y
     *            row of grid
     */
    public synchronized void flag(int x, int y) {
        if (isValidGridLocation(x, y)) {
            grid[y][x].flag();
        }
    }

    /**
     * If the specified square is flagged, changes its state to untouched.
     * 
     * @param x
     *            column of grid
     * @param y
     *            row of grid
     */
    public synchronized void deflag(int x, int y) {
        if (isValidGridLocation(x, y)) {
            grid[y][x].deflag();
        }
    }

    @Override
    public synchronized String toString() {
        StringBuffer sb = new StringBuffer();
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length - 1; col++) {
                sb.append(grid[row][col]);
                sb.append(" ");
            }
            sb.append(grid[row][grid[row].length - 1]);
            sb.append(System.lineSeparator());
        }

        return sb.toString();
    }

    /*
     * Ensures the representation invariant. Updates all squares with the number
     * of neighboring bombs.
     */
    private void checkRep() {
        Queue<Pair> queue;
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                int numBombs = 0;
                queue = generateNeighbors(col, row);
                for (Pair coord : queue) {
                    if (grid[coord.y][coord.x].hasBomb) {
                        numBombs++;
                    }
                }
                grid[row][col].neighborsWithBombs = numBombs;
            }
        }
    }

    /*
     * Returns true if x and y specify a valid location on the grid. x
     * represents the column and y represents the row.
     */
    private boolean isValidGridLocation(int x, int y) {
        return x >= 0 && y >= 0 && x <= grid[0].length && y <= grid.length;
    }

    /*
     * Returns a queue of pairs of grid indexes
     */
    private Queue<Pair> generateNeighbors(int x, int y) {
        Queue<Pair> queue = new LinkedList<Pair>();
        if (x > 0) {
            queue.add(new Pair(x - 1, y));
            if (y > 0)
                queue.add(new Pair(x - 1, y - 1));
            if (y < grid.length - 1)
                queue.add(new Pair(x - 1, y + 1));
        }

        if (x < grid[0].length - 1) {
            queue.add(new Pair(x + 1, y));
            if (y > 0)
                queue.add(new Pair(x + 1, y - 1));
            if (y < grid.length - 1)
                queue.add(new Pair(x + 1, y + 1));
        }

        if (y > 0) {
            queue.add(new Pair(x, y - 1));
        }

        if (y < grid.length - 1) {
            queue.add(new Pair(x, y + 1));
        }

        return queue;
    }

}
