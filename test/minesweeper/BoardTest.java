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
    public void testDig() {
        Board board = new Board(1, 1);
        board.dig(0, 0);
        Assert.assertFalse(board.hasBomb(0,0));
    }
}
