package com.example.pgodzin.tictactoe;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.DisplayMetrics;

import java.util.*;

/**
 * AIPlayer using Minimax algorithm
 * From https://www3.ntu.edu.sg/home/ehchua/programming/java/JavaGame_TicTacToe_AI.html
 */
public class AIPlayer {

    int ROWS = 3;  // number of rows
    int COLS = 3;  // number of columns

    Cell[][] cells; // the board's ROWS-by-COLS array of Cells
    Cell.Content myShape;    // computer's Cell.Content
    Cell.Content oppShape;   // opponent's Cell.Content
    Board b;
    Canvas mCanvas;
    int playerShape;
    Paint paint;
    Context mContext;

    /**
     * Constructor with the given game board
     */
    public AIPlayer(Board board, Cell.Content myShape, Cell.Content oppShape, int playerShape,
                    Canvas mCanvas, Paint paint, Context context) {
        cells = board.cells;
        b = board;
        this.myShape = myShape;
        this.oppShape = oppShape;
        this.mCanvas = mCanvas;
        this.playerShape = playerShape;
        this.paint = paint;
        mContext = context;
    }

    /**
     * Get next best move for computer. Return int[2] of {row, col}
     */
    void move() {
        int[] result = minimax(2, myShape, -Integer.MAX_VALUE, Integer.MAX_VALUE); // depth, max turn
        b.cells[result[1]][result[2]].content = myShape;
        if (playerShape == 3) {
            mCanvas.drawCircle(dp(60 + 120 * result[2]), dp(60 + 120 * result[1]), dp(40), paint);
        } else if (playerShape == 1) {
            mCanvas.drawLine(dp(20 + 120 * result[2]), dp(20 + 120 * result[1]),
                    dp(100 + 120 * result[2]), dp(100 + +120 * result[1]), paint);
            mCanvas.drawLine(dp(20 + 120 * result[2]), dp(100 + 120 * result[1]),
                    dp(100 + 120 * result[2]), dp(20 + +120 * result[1]), paint);
        }
        b.currentRow = result[1];
        b.currentCol = result[2];
    }

    // Convert dp to pixels
    public int dp(int dp) {
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    /**
     * Minimax (recursive) at level of depth for maximizing or minimizing player
     * with alpha-beta cut-off. Return int[3] of {score, row, col}
     */
    private int[] minimax(int depth, Cell.Content player, int alpha, int beta) {
        // Generate possible next moves in a list of int[2] of {row, col}.
        List<int[]> nextMoves = generateMoves();

        // myShape is maximizing; while oppSeed is minimizing
        int score;
        int bestRow = -1;
        int bestCol = -1;

        if (nextMoves.isEmpty() || depth == 0) {
            // Gameover or depth reached, evaluate score
            score = evaluate();
            return new int[]{score, bestRow, bestCol};
        } else {
            for (int[] move : nextMoves) {
                // try this move for the current "player"
                cells[move[0]][move[1]].content = player;
                if (player == myShape) {  // myShape (computer) is maximizing player
                    score = minimax(depth - 1, oppShape, alpha, beta)[0];
                    if (score > alpha) {
                        alpha = score;
                        bestRow = move[0];
                        bestCol = move[1];
                    }
                } else {  // oppSeed is minimizing player
                    score = minimax(depth - 1, myShape, alpha, beta)[0];
                    if (score < beta) {
                        beta = score;
                        bestRow = move[0];
                        bestCol = move[1];
                    }
                }
                // undo move
                cells[move[0]][move[1]].content = Cell.Content.EMPTY;
                // cut-off
                if (alpha >= beta) break;
            }
            return new int[]{(player == myShape) ? alpha : beta, bestRow, bestCol};
        }
    }

    /**
     * Find all valid next moves.
     * Return List of moves in int[2] of {row, col} or empty list if gameover
     */
    private List<int[]> generateMoves() {
        List<int[]> nextMoves = new ArrayList<>(); // allocate List

        // If gameover, i.e., no next move
        if (hasWon(myShape) || hasWon(oppShape)) {
            return nextMoves;   // return empty list
        }

        // Search for empty cells and add to the List
        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLS; ++col) {
                if (cells[row][col].content == Cell.Content.EMPTY) {
                    nextMoves.add(new int[]{row, col});
                }
            }
        }
        return nextMoves;
    }

    /**
     * The heuristic evaluation function for the current board
     *
     * @Return +100, +10, +1 for EACH 3-, 2-, 1-in-a-line for computer.
     * -100, -10, -1 for EACH 3-, 2-, 1-in-a-line for opponent.
     * 0 otherwise
     */

    private int evaluate() {
        int score = 0;
        // Evaluate score for each of the 8 lines (3 rows, 3 columns, 2 diagonals)
        score += evaluateLine(0, 0, 0, 1, 0, 2);  // row 0
        score += evaluateLine(1, 0, 1, 1, 1, 2);  // row 1
        score += evaluateLine(2, 0, 2, 1, 2, 2);  // row 2
        score += evaluateLine(0, 0, 1, 0, 2, 0);  // col 0
        score += evaluateLine(0, 1, 1, 1, 2, 1);  // col 1
        score += evaluateLine(0, 2, 1, 2, 2, 2);  // col 2
        score += evaluateLine(0, 0, 1, 1, 2, 2);  // diagonal
        score += evaluateLine(0, 2, 1, 1, 2, 0);  // alternate diagonal
        return score;
    }

    /**
     * The heuristic evaluation function for the given line of 3 cells
     *
     * @Return +100, +10, +1 for 3-, 2-, 1-in-a-line for computer.
     * -100, -10, -1 for 3-, 2-, 1-in-a-line for opponent.
     * 0 otherwise
     */
    private int evaluateLine(int row1, int col1, int row2, int col2, int row3, int col3) {
        int score = 0;

        // First cell
        if (cells[row1][col1].content == myShape) {
            score = 1;
        } else if (cells[row1][col1].content == oppShape) {
            score = -1;
        }

        // Second cell
        if (cells[row2][col2].content == myShape) {
            if (score == 1) {   // cell1 is myShape
                score = 10;
            } else if (score == -1) {  // cell1 is oppShape
                return 0;
            } else {  // cell1 is empty
                score = 1;
            }
        } else if (cells[row2][col2].content == oppShape) {
            if (score == -1) { // cell1 is oppShape
                score = -10;
            } else if (score == 1) { // cell1 is myShape
                return 0;
            } else {  // cell1 is empty
                score = -1;
            }
        }

        // Third cell
        if (cells[row3][col3].content == myShape) {
            if (score > 0) {  // cell1 and/or cell2 is myShape
                score *= 10;
            } else if (score < 0) {  // cell1 and/or cell2 is oppShape
                return 0;
            } else {  // cell1 and cell2 are empty
                score = 1;
            }
        } else if (cells[row3][col3].content == oppShape) {
            if (score < 0) {  // cell1 and/or cell2 is oppShape
                score *= 10;
            } else if (score > 1) {  // cell1 and/or cell2 is myShape
                return 0;
            } else {  // cell1 and cell2 are empty
                score = -1;
            }
        }
        return score;
    }

    private int[] winningPatterns = {
            0b111000000, 0b000111000, 0b000000111, // rows
            0b100100100, 0b010010010, 0b001001001, // cols
            0b100010001, 0b001010100               // diagonals
    };

    /**
     * Returns true if thePlayer wins
     */
    private boolean hasWon(Cell.Content thePlayer) {
        int pattern = 0b000000000;  // 9-bit pattern for the 9 cells
        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLS; ++col) {
                if (cells[row][col].content == thePlayer) {
                    pattern |= (1 << (row * COLS + col));
                }
            }
        }
        for (int winningPattern : winningPatterns) {
            if ((pattern & winningPattern) == winningPattern)
                return true;
        }
        return false;
    }
}