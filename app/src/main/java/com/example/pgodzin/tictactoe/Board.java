package com.example.pgodzin.tictactoe;

/**
 * The Board class models the game-board.
 * From https://www3.ntu.edu.sg/home/ehchua/programming/java/JavaGame_TicTacToe.html
 */
public class Board {
    // Named-constants for the dimensions
    public static final int ROWS = 3;
    public static final int COLS = 3;

    // package access
    Cell[][] cells;  // a board composes of ROWS-by-COLS Cell instances
    int currentRow, currentCol;  // the current seed's row and column

    /**
     * Constructor to initialize the game board
     */
    public Board() {
        cells = new Cell[ROWS][COLS];  // allocate the array
        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLS; ++col) {
                cells[row][col] = new Cell(row, col); // allocate element of the array
            }
        }
    }

    /**
     * Initialize (or re-initialize) the contents of the game board
     */
    public void init() {
        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLS; ++col) {
                cells[row][col].clear();  // clear the cell content
            }
        }
    }

    /**
     * Return true if it is a draw (i.e., no more EMPTY cell)
     */
    public boolean isDraw() {
        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLS; ++col) {
                if (cells[row][col].content == Cell.Content.EMPTY) {
                    return false; // an empty seed found, not a draw, exit
                }
            }
        }
        return true; // no empty cell, it's a draw
    }

    /**
     * Return true if the player with "theSeed" has won after placing at
     * (currentRow, currentCol)
     */
    public boolean hasWon(Cell.Content theContent) {
        return (cells[currentRow][0].content == theContent         // 3-in-the-row
                && cells[currentRow][1].content == theContent
                && cells[currentRow][2].content == theContent
                || cells[0][currentCol].content == theContent      // 3-in-the-column
                && cells[1][currentCol].content == theContent
                && cells[2][currentCol].content == theContent
                || currentRow == currentCol            // 3-in-the-diagonal
                && cells[0][0].content == theContent
                && cells[1][1].content == theContent
                && cells[2][2].content == theContent
                || currentRow + currentCol == 2    // 3-in-the-opposite-diagonal
                && cells[0][2].content == theContent
                && cells[1][1].content == theContent
                && cells[2][0].content == theContent);
    }
}