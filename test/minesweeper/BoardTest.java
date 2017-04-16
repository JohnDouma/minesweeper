/* Copyright (c) 2007-2017 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package minesweeper;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

/**
 * TODO: Description
 */
public class BoardTest {
    
    // TODO: Testing strategy
    
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    // TODO: Tests
    
    @Test(expected=RuntimeException.class)
    public void testConstructBoardWithNonexistantFile() {
        Board board = new Board(new File("doesntexist.txt"));   
    }
    
    @Test(expected=IndexOutOfBoundsException.class)
    public void testDigInvalidSquare() {
        Board board = new Board(3, 4);
        board.hasBomb(3, 4);
    }
    
    @Test
    public void testDigWithOneSquare() {
        Board board = new Board(1, 1);
        board.dig(0, 0);
        Assert.assertFalse(board.hasBomb(0,0));
    }
    
    @Test
    public void testBoardInitialization() {
        Board board = new Board(10, 10);
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                Assert.assertEquals(Board.State.UNTOUCHED, board.getState(col, row));
            }
        }
    }
    
    @Test
    public void testFlag() {
        Board board = new Board(5, 5);
        Assert.assertEquals(Board.State.UNTOUCHED, board.getState(3, 3));
        board.flag(3, 3);
        Assert.assertEquals(Board.State.FLAGGED, board.getState(3, 3));
    }
    
    @Test
    public void testFlagDugSquare() {
        Board board = new Board(5, 5);
        Assert.assertEquals(Board.State.UNTOUCHED, board.getState(3, 3));
        board.dig(3, 3);
        board.flag(3, 3);
        Assert.assertEquals(Board.State.DUG, board.getState(3, 3));
    }
    
    @Test
    public void testDeflagFlaggedSquare() {
        Board board = new Board(5, 5);
        Assert.assertEquals(Board.State.UNTOUCHED, board.getState(3, 3));
        board.flag(3, 3);
        Assert.assertEquals(Board.State.FLAGGED, board.getState(3, 3));
        board.deflag(3, 3);
        Assert.assertEquals(Board.State.UNTOUCHED, board.getState(3, 3));
    }
    
    @Test
    public void testDeflagUnflaggedSquare() {
        Board board = new Board(5, 5);
        Assert.assertEquals(Board.State.UNTOUCHED, board.getState(3, 3));
        board.deflag(3, 3);
        Assert.assertEquals(Board.State.UNTOUCHED, board.getState(3, 3));
    }
}
