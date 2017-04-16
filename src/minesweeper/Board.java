/* Copyright (c) 2007-2017 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package minesweeper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

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

    public enum State {
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
                state = State.UNTOUCHED;
            }
        }

        @Override
        public String toString() {
            String retval = null;
            switch (state) {
            case DUG:
                retval = (neighborsWithBombs == 0) ? " " : Integer.toString(neighborsWithBombs);
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

    public Board(File file) {
        if (!file.exists()) {
            throw new RuntimeException(String.format("%s does not exist", file));
        }

        String line;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            line = reader.readLine();
            String[] tokens = line.split(" ");
            if (tokens.length != 2) {
                throw new RuntimeException(String.format("Invalid board file - line 1 %s", line));
            }

            int rows = Integer.parseInt(tokens[1]);
            int cols = Integer.parseInt(tokens[0]);

            grid = new Square[rows][cols];
            for (int row = 0; row < rows; row++) {
                line = reader.readLine();
                String[] columns = line.split(" ");
                if (columns.length != cols) {
                    throw new RuntimeException(
                            String.format("Line %d is inconsistent with number of columns specified", row + 1));
                }
                for (int col = 0; col < cols; col++) {
                    grid[row][col] = new Square();
                    if (Integer.parseInt(columns[col]) == 1) {
                        grid[row][col].hasBomb = true;
                    }
                }
            }

            line = reader.readLine();
            if (line != null && !line.equals("")) {
                throw new RuntimeException(String.format("File %s contains more lines than specified on line 1", file));
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException();
        } catch (IOException ioe) {
            throw new RuntimeException();
        }
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
            digBomblessNeighbors(x, y);
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
            if (row < grid.length - 1) {
                sb.append(System.lineSeparator());
            }
        }

        return sb.toString();
    }

    /*
     * Ensures the representation invariant. Updates all squares with the number
     * of neighboring bombs.
     */
    private void checkRep() {
        List<Pair> list;
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                int numBombs = 0;
                list = generateNeighbors(col, row);
                for (Pair coord : list) {
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
     * Returns a list of pairs of grid indexes
     */
    private List<Pair> generateNeighbors(int x, int y) {
        List<Pair> list = new LinkedList<Pair>();
        if (x > 0) {
            list.add(new Pair(x - 1, y));
            if (y > 0)
                list.add(new Pair(x - 1, y - 1));
            if (y < grid.length - 1)
                list.add(new Pair(x - 1, y + 1));
        }

        if (x < grid[0].length - 1) {
            list.add(new Pair(x + 1, y));
            if (y > 0)
                list.add(new Pair(x + 1, y - 1));
            if (y < grid.length - 1)
                list.add(new Pair(x + 1, y + 1));
        }

        if (y > 0) {
            list.add(new Pair(x, y - 1));
        }

        if (y < grid.length - 1) {
            list.add(new Pair(x, y + 1));
        }

        return list;
    }

    /*
     * If the square at grid[y][x] has no neighbors with bombs, set neighbors'
     * states to dug if untouched and recursively do the same to each of their
     * neighbors.
     */
    private void digBomblessNeighbors(int x, int y) {
        if (grid[y][x].neighborsWithBombs > 0)
            return;

        List<Pair> neighbors = generateNeighbors(x, y);
        while (!neighbors.isEmpty()) {
            Pair coord = neighbors.remove(0);
            Square square = grid[coord.y][coord.x];
            if (square.state == State.UNTOUCHED) {
                square.state = State.DUG;
                if (square.neighborsWithBombs == 0) {
                    neighbors.addAll(generateNeighbors(coord.x, coord.y));
                }
            }
        }

    }
}
