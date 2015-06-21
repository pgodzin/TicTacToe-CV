package com.pgodzin.tictactoe;

/**
 * The Cell class models each individual cell of the game board.
 * From https://www3.ntu.edu.sg/home/ehchua/programming/java/JavaGame_TicTacToe.html
 */
public class Cell {

    public enum Content {
        EMPTY, P1_SHAPE, P2_SHAPE
    }

    Content content; // content of this cell of type Seed.
    // take a value of Seed.EMPTY, Seed.P1_SHAPE, or Seed.P2_SHAPE
    int row, col; // row and column of this cell, not used in this program

    /**
     * Constructor to initialize this cell
     */
    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        clear();  // clear content
    }

    /**
     * Clear the cell content to EMPTY
     */
    public void clear() {
        content = Content.EMPTY;
    }

}