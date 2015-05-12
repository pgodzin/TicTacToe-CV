package com.example.pgodzin.tictactoe;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Code from http://creative-punch.net/2014/03/make-basic-single-touch-drawing-app-android/
 */
public class TicTacToeView extends View {

    Board b = new Board();

    Paint paint = new Paint();
    Path path = new Path();
    ArrayList<Path> paths = new ArrayList<>();
    ArrayList<Path> undonePaths = new ArrayList<Path>();

    long lastDrawn = 0;
    Context mContext;
    Bitmap board, oldboard;
    Canvas mCanvas;

    int[] playerShape = new int[]{-1, -1};
    int playerTurn = 0;
    int[] playerColors = new int[]{Color.BLUE, Color.RED};

    private Map<Path, Integer> colorsMap = new HashMap<>();
    private Map<Integer, String> shapeMap;

    int mode;

    static {
        System.loadLibrary("opencv_java");
    }

    public TicTacToeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        board = Bitmap.createBitmap(dp(360), dp(360), Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(board);
        mCanvas.drawColor(0xFFFFFFFF);
        setupPaint();
        mCanvas.drawLine(dp(120), dp(10), dp(120), dp(350), paint);
        mCanvas.drawLine(dp(240), dp(10), dp(240), dp(350), paint);
        mCanvas.drawLine(dp(10), dp(120), dp(350), dp(120), paint);
        mCanvas.drawLine(dp(10), dp(240), dp(350), dp(240), paint);
        oldboard = board.copy(Bitmap.Config.ARGB_8888, true);
        paint.setColor(playerColors[0]);

        shapeMap = new HashMap<>();
        shapeMap.put(0, "+");
        shapeMap.put(1, "X");
        shapeMap.put(2, "Circle");
        shapeMap.put(3, "Square");
        shapeMap.put(4, "Triangle");
        shapeMap.put(5, "Arrow");
        shapeMap.put(6, "Heart");
    }

    private void setupPaint() {
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(15f);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawBitmap(board, 0, 0, paint);

        paint.setColor(playerColors[playerTurn]);
        canvas.drawPath(path, paint);
        mCanvas.drawPath(path, paint);

        for (Path p : paths) {
            paint.setColor(playerColors[colorsMap.get(p)]);
            canvas.drawPath(p, paint);
            mCanvas.drawPath(p, paint);
        }
    }

    // Convert dp to pixels
    public int dp(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    public boolean onTouchEvent(MotionEvent event) {
        // Get the coordinates of the touch event.
        float eventX = event.getX();
        float eventY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Set a new starting point
                undonePaths.clear();
                path.reset();
                path.moveTo(eventX, eventY);
                lastDrawn = System.currentTimeMillis();
                mX = eventX;
                mY = eventY;
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                // Connect the points
                float dx = Math.abs(eventX - mX);
                float dy = Math.abs(eventY - mY);
                if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                    path.lineTo(eventX, eventY);
                    lastDrawn = System.currentTimeMillis();
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                new CountDownTimer(1500, 500) {

                    @Override
                    public void onTick(long miliseconds) {
                    }

                    @Override
                    public void onFinish() {
                        if (System.currentTimeMillis() > lastDrawn + 1500) {
                            Toast.makeText(mContext, "Processing...", Toast.LENGTH_LONG).show();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    colorsMap.put(path, playerTurn);
                                    paint.setColor(playerColors[colorsMap.get(path)]);
                                    paths.add(path);
                                    for (Path p : paths) mCanvas.drawPath(p, paint);

                                    path = new Path();
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            invalidate();
                                        }
                                    });
                                    saveBitmap(board, "board");
                                    processMove();
                                    if (playerTurn == 0) playerTurn = 1;
                                    else if (playerTurn == 1) playerTurn = 0;

                                }
                            }).start();
                        }
                    }
                }.start();
                break;
            default:
                return false;
        }
        return true;
    }

    public void processMove() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        });

        Mat oldBoard = new Mat();
        Bitmap oldBmp = oldboard.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(oldBmp, oldBoard);
        Utils.matToBitmap(oldBoard, oldBmp);

        Mat newBoard = new Mat();
        Bitmap bmp = board.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp, newBoard);
        Utils.matToBitmap(newBoard, bmp);

        final Mat move = new Mat(newBoard.size(), CvType.CV_8UC3);
        Core.absdiff(oldBoard, newBoard, move);

        Mat diffMat = Mat.zeros(move.rows(), move.cols(), CvType.CV_8UC1);
        float threshold = 30f;
        double dist;

        for (int j = 0; j < move.rows(); j++) {
            for (int i = 0; i < move.cols(); i++) {
                double[] pix = move.get(j, i);
                dist = Math.sqrt(pix[0] * pix[0] + pix[1] * pix[1] + pix[2] * pix[2]);
                if (dist > threshold) {
                    diffMat.put(j, i, 255, 255, 255);
                }
            }
        }

        final Bitmap moveBmp = Bitmap.createBitmap(diffMat.cols(), diffMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(diffMat, moveBmp);
        saveBitmap(moveBmp, "move");

        Scalar white = new Scalar(255, 255, 255);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat mHierarchy = new Mat();
        Imgproc.findContours(diffMat, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        Mat contourMap = Mat.zeros(diffMat.size(), CvType.CV_8UC3);

        final Bitmap contourBmp = Bitmap.createBitmap(contourMap.cols(), contourMap.rows(), Bitmap.Config.ARGB_8888);

        int biggestContourIndex = biggestContourIndex(contours);
        for (int i = 0; i < contours.size(); i++) {
            int l = contours.get(i).toArray().length;
            if (l > 20)
                Imgproc.drawContours(contourMap, contours, i, white);
        }

        Rect[] cells = new Rect[]{
                new Rect(new Point(0, 0), new Point(move.cols() / 3, move.cols() / 3)),
                new Rect(new Point(0, move.cols() / 3), new Point(move.cols() / 3, 2 * move.cols() / 3)),
                new Rect(new Point(0, 2 * move.cols() / 3), new Point(move.cols() / 3, move.cols())),
                new Rect(new Point(move.cols() / 3, 0), new Point(2 * move.cols() / 3, move.cols() / 3)),
                new Rect(new Point(move.cols() / 3, move.cols() / 3), new Point(2 * move.cols() / 3, 2 * move.cols() / 3)),
                new Rect(new Point(move.cols() / 3, 2 * move.cols() / 3), new Point(2 * move.cols() / 3, move.cols())),
                new Rect(new Point(2 * move.cols() / 3, 0), new Point(move.cols(), move.cols() / 3)),
                new Rect(new Point(2 * move.cols() / 3, move.cols() / 3), new Point(move.cols(), 2 * move.cols() / 3)),
                new Rect(new Point(2 * move.cols() / 3, 2 * move.cols() / 3), new Point(move.cols(), move.cols()))
        };

        Point[] convexPoints = convexityDefects(contourMap, contours, biggestContourIndex);
        for (Point p : convexPoints) {
            Core.circle(contourMap, p, 10, new Scalar(255, 255, 255));
        }
        int cellNum = -1;
        for (int i = 0; i < cells.length; i++) {
            if (convexPoints.length > 0 && convexPoints[0].inside(cells[i])) {
                cellNum = i;
                break;
            }
        }

        for (Point p : convexPoints) {
            for (int i = 0; i < cells.length; i++) {
                if (p.inside(cells[i]) && i != cellNum && cellNum != -1) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, "Draw your shapes within a single cell. " +
                                            "Restarting game...",
                                    Toast.LENGTH_LONG).show();
                            ((MainActivity) mContext).restart();
                        }
                    });
                    return;
                }
            }
        }

        if (cellNum == -1) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    //path = new Path();
                    //paths.clear();
                    /*if (paths.size() > 0) {
                        undonePaths.add(paths.remove(paths.size() - 1));
                        invalidate();
                    }
                    board = oldboard.copy(Bitmap.Config.ARGB_8888, true);

                    if (playerTurn == 0) playerTurn = 1;
                    else if (playerTurn == 1) playerTurn = 0;*/

                    ((MainActivity) mContext).restart();
                    Toast.makeText(mContext, "This shape was not identified, game restarting",
                            Toast.LENGTH_LONG).show();


                }
            });
            return;
        }

        Utils.matToBitmap(contourMap, contourBmp);
        saveBitmap(contourBmp, "contours");

        MatOfPoint allContours = new MatOfPoint();
        List<Point> contourPts = new ArrayList<>();
        for (MatOfPoint c : contours) {
            List<Point> pList = c.toList();
            for (Point p : pList)
                if (!contourPts.contains(p) && p.inside(cells[cellNum])) contourPts.add(p);
        }
        allContours.fromList(contourPts);

        if (playerShape[playerTurn] == -1) {
            final int moveShape = recognizeShape(convexPoints, contourMap.clone(), allContours);
            if (moveShape != -1) {
                playerShape[playerTurn] = moveShape;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        ((MainActivity) mContext).updatePlayerShapes(playerTurn, shapeMap, moveShape);
                    }
                });
                oldboard = board.copy(Bitmap.Config.ARGB_8888, true);
                boolean validBoardPos = addMoveToBoard(cellNum);
                if (!validBoardPos) return;
                if (checkForEndGame()) return;

                if (mode == 0) {
                    if (playerShape[0] == 3) playerShape[1] = 1;
                    else playerShape[1] = 3;
                    paint.setColor(playerColors[1]);
                    AIPlayer ai = new AIPlayer(b, Cell.Content.P2_SHAPE,
                            Cell.Content.P1_SHAPE, playerShape[1], mCanvas, paint, mContext);
                    ai.move();
                    playerTurn = 1;
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            ((MainActivity) mContext).updatePlayerShapes(1, shapeMap, playerShape[1]);
                            invalidate();
                        }
                    });
                    if (checkForEndGame()) return;
                    oldboard = board.copy(Bitmap.Config.ARGB_8888, true);
                }
            } else {

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        /*if (playerTurn == 0) playerTurn = 1;
                        else if (playerTurn == 1) playerTurn = 0;

                        //path = new Path();
                        //paths.clear();
                        if (paths.size() > 0) {
                            undonePaths.add(paths.remove(paths.size() - 1));
                            invalidate();
                        }
                        board = oldboard.copy(Bitmap.Config.ARGB_8888, true);*/

                        ((MainActivity) mContext).restart();
                        Toast.makeText(mContext, "This shape was not identified. Restarting game...",
                                Toast.LENGTH_LONG).show();
                    }
                });
                return;
            }
        } else {
            boolean sameShape = checkSameShape(playerShape[playerTurn], convexPoints, contourMap.clone(), allContours);
            if (sameShape) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, shapeMap.get(playerShape[playerTurn]) + " recognized",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                oldboard = board.copy(Bitmap.Config.ARGB_8888, true);
                boolean validBoardPos = addMoveToBoard(cellNum);
                if (!validBoardPos) return;
                if (checkForEndGame()) return;

                if (mode == 0) {
                    if (playerShape[0] == 3) playerShape[1] = 1;
                    else playerShape[1] = 3;
                    paint.setColor(playerColors[1]);
                    AIPlayer ai = new AIPlayer(b, Cell.Content.P2_SHAPE,
                            Cell.Content.P1_SHAPE, playerShape[1], mCanvas, paint, mContext);
                    ai.move();
                    playerTurn = 1;
                    oldboard = board.copy(Bitmap.Config.ARGB_8888, true);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            invalidate();
                        }
                    });
                    if (checkForEndGame()) return;
                }
            } else {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        /*if (playerTurn == 0) playerTurn = 1;
                        else if (playerTurn == 1) playerTurn = 0;

                        //path = new Path();
                        //paths.clear();
                        if (paths.size() > 0) {
                            undonePaths.add(paths.remove(paths.size() - 1));
                            invalidate();
                        }
                        board = oldboard.copy(Bitmap.Config.ARGB_8888, true);*/
                        int tempTurn = playerTurn;
                        //if (playerTurn == 0) tempTurn = 1;
                        //else if (playerTurn == 1) tempTurn = 0;
                        if (mode == 0) tempTurn = 0;

                        Toast.makeText(mContext, "Move not recognized as a " +
                                        shapeMap.get(playerShape[tempTurn]) + ". Restarting game...",
                                Toast.LENGTH_LONG).show();
                        ((MainActivity) mContext).restart();
                    }
                });
                return;
            }
        }
    }

    public boolean checkForEndGame() {
        Cell.Content content = Cell.Content.P1_SHAPE;
        if (playerTurn == 1) content = Cell.Content.P2_SHAPE;

        if (b.isDraw()) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    new MaterialDialog.Builder(mContext)
                            .title(R.string.tie_game)
                            .content(R.string.tie_game_content)
                            .positiveText(R.string.play_again)
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    ((MainActivity) mContext).restart();
                                    dialog.dismiss();
                                }
                            })
                            .cancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    ((MainActivity) mContext).restart();
                                    dialog.dismiss();
                                }
                            })
                            .dismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    ((MainActivity) mContext).restart();
                                    dialog.dismiss();
                                }
                            }).show();
                }
            });
            return true;
        } else if (b.hasWon(content)) {
            final Cell.Content finalContent = content;
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    int content_string;
                    if (finalContent == Cell.Content.P2_SHAPE && mode == 0)
                        content_string = R.string.AI_won_content;
                    else if (finalContent == Cell.Content.P2_SHAPE && mode == 1)
                        content_string = R.string.p2_won_content;
                    else content_string = R.string.p1_won_content;

                    new MaterialDialog.Builder(mContext)
                            .title(R.string.game_over)
                            .content(content_string)
                            .positiveText(R.string.play_again)
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    ((MainActivity) mContext).restart();
                                }
                            })
                            .cancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    ((MainActivity) mContext).restart();
                                }
                            })
                            .dismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    ((MainActivity) mContext).restart();
                                }
                            }).show();
                }
            });
            return true;
        }
        return false;
    }

    public boolean addMoveToBoard(int cellNum) {
        Cell.Content content = Cell.Content.P1_SHAPE;
        if (playerTurn == 1) content = Cell.Content.P2_SHAPE;

        switch (cellNum) {
            case 0:
                if (b.cells[0][0].content != Cell.Content.EMPTY) return errorCellAlreadyFilled();
                b.cells[0][0].content = content;
                b.currentRow = 0;
                b.currentCol = 0;
                break;
            case 1:
                if (b.cells[1][0].content != Cell.Content.EMPTY) return errorCellAlreadyFilled();
                b.cells[1][0].content = content;
                b.currentRow = 1;
                b.currentCol = 0;
                break;
            case 2:
                if (b.cells[2][0].content != Cell.Content.EMPTY) return errorCellAlreadyFilled();
                b.cells[2][0].content = content;
                b.currentRow = 2;
                b.currentCol = 0;
                break;
            case 3:
                if (b.cells[0][1].content != Cell.Content.EMPTY) return errorCellAlreadyFilled();
                b.cells[0][1].content = content;
                b.currentRow = 0;
                b.currentCol = 1;
                break;
            case 4:
                if (b.cells[1][1].content != Cell.Content.EMPTY) return errorCellAlreadyFilled();
                b.cells[1][1].content = content;
                b.currentRow = 1;
                b.currentCol = 1;
                break;
            case 5:
                if (b.cells[2][1].content != Cell.Content.EMPTY) return errorCellAlreadyFilled();
                b.cells[2][1].content = content;
                b.currentRow = 2;
                b.currentCol = 1;
                break;
            case 6:
                if (b.cells[0][2].content != Cell.Content.EMPTY) return errorCellAlreadyFilled();
                b.cells[0][2].content = content;
                b.currentRow = 0;
                b.currentCol = 2;
                break;
            case 7:
                if (b.cells[1][2].content != Cell.Content.EMPTY) return errorCellAlreadyFilled();
                b.cells[1][2].content = content;
                b.currentRow = 1;
                b.currentCol = 2;
                break;
            case 8:
                if (b.cells[2][2].content != Cell.Content.EMPTY) return errorCellAlreadyFilled();
                b.cells[2][2].content = content;
                b.currentRow = 2;
                b.currentCol = 2;
                break;
            default:
                break;
        }
        return true;
    }

    public boolean errorCellAlreadyFilled() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, "Cell was already filled! Game restarting...",
                        Toast.LENGTH_LONG).show();
                ((MainActivity) mContext).restart();
            }
        });
        return false;
    }

    public boolean checkSameShape(int shape, Point[] convexPoints, Mat m, MatOfPoint contours) {
        switch (shape) {
            case 0:
                return recognizePlus(convexPoints, m.clone(), contours);
            case 1:
                return recognizeX(convexPoints, m.clone(), contours);
            case 2:
                return recognizeCircle(convexPoints, m.clone(), contours);
            case 3:
                return recognizeSquare(convexPoints, m.clone(), contours);
            case 4:
                return recognizeTriangle(convexPoints, m.clone(), contours);
            case 5:
                return recognizeArrow(convexPoints, m.clone(), contours);
            case 6:
                return recognizeHeart(convexPoints, m.clone(), contours);
            default:
                return false;
        }
    }

    public int recognizeShape(Point[] convexPoints, Mat m, MatOfPoint contours) {
        if (recognizePlus(convexPoints, m.clone(), contours)) {
            return 0;
        } else if (recognizeX(convexPoints, m.clone(), contours)) {
            return 1;
        } else if (recognizeCircle(convexPoints, m.clone(), contours)) {
            return 2;
        } else if (recognizeSquare(convexPoints, m.clone(), contours)) {
            return 3;
        } else if (recognizeTriangle(convexPoints, m.clone(), contours)) {
            return 4;
        } else if (recognizeArrow(convexPoints, m.clone(), contours)) {
            return 5;
        } else if (recognizeHeart(convexPoints, m.clone(), contours)) {
            return 6;
        } else {
            return -1;
        }
    }

    public boolean recognizePlus(Point[] convexPoints, Mat m, MatOfPoint contours) {

        Scalar blue = new Scalar(0, 0, 255, 255);
        Scalar green = new Scalar(0, 255, 0, 255);

        // Draw bounding rectangle around contour
        Rect boundingRect = Imgproc.boundingRect(contours);
        Core.rectangle(m, boundingRect.tl(), boundingRect.br(), blue);

        int w = boundingRect.width / 4;
        int h = boundingRect.height / 4;
        Rect ulRect = new Rect(boundingRect.tl(), new Point(boundingRect.tl().x + w,
                boundingRect.tl().y + h));
        Rect urRect = new Rect(new Point(boundingRect.br().x - w, boundingRect.tl().y),
                new Point(boundingRect.br().x, boundingRect.tl().y + h));
        Rect blRect = new Rect(new Point(boundingRect.tl().x, boundingRect.br().y - h),
                new Point(boundingRect.tl().x + w, boundingRect.br().y));
        Rect brRect = new Rect(new Point(boundingRect.br().x - w, boundingRect.br().y - h),
                boundingRect.br());

        Core.rectangle(m, ulRect.tl(), ulRect.br(), green);
        Core.rectangle(m, urRect.tl(), urRect.br(), green);
        Core.rectangle(m, blRect.tl(), blRect.br(), green);
        Core.rectangle(m, brRect.tl(), brRect.br(), green);

        Point cog = centerOfGravity(contours);
        Core.circle(m, cog, 10, blue);

        Rect cogRect = new Rect(new Point(cog.x - w, cog.y - h), new Point(cog.x + w, cog.y + h));
        Core.rectangle(m, cogRect.tl(), cogRect.br(), green);

        Bitmap bmp = Bitmap.createBitmap(dp(360), dp(360), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(m, bmp);
        saveBitmap(bmp, "bounding+");

        int count = 0;
        for (Point p : convexPoints) {
            if (p.inside(cogRect)) {
                count++;
            }
        }

        for (Point p : convexPoints) {
            if (p.inside(ulRect) || p.inside(urRect) || p.inside(blRect) || p.inside(brRect)) {
                return false;
            }
        }

        return count == 4;
    }

    public boolean recognizeX(Point[] convexPoints, Mat m, MatOfPoint contours) {

        Scalar blue = new Scalar(0, 0, 255, 255);
        Scalar green = new Scalar(0, 255, 0, 255);

        // Draw bounding rectangle around contour
        Rect boundingRect = Imgproc.boundingRect(contours);
        Core.rectangle(m, boundingRect.tl(), boundingRect.br(), blue);

        int w = boundingRect.width;
        int h = boundingRect.height;
        Rect lMid = new Rect(new Point(boundingRect.tl().x, boundingRect.tl().y + h / 2 - h / 8),
                new Point(boundingRect.tl().x + w / 4, boundingRect.tl().y + h / 2 + h / 8));
        Rect rMid = new Rect(new Point(boundingRect.br().x - w / 4, boundingRect.tl().y + h / 2 - h / 8),
                new Point(boundingRect.br().x, boundingRect.tl().y + h / 2 + h / 8));
        Rect tMid = new Rect(new Point(boundingRect.tl().x + w / 2 - w / 8, boundingRect.tl().y),
                new Point(boundingRect.tl().x + w / 2 + w / 8, boundingRect.tl().y + h / 4));
        Rect bMid = new Rect(new Point(boundingRect.tl().x + w / 2 - w / 8, boundingRect.br().y - h / 4),
                new Point(boundingRect.tl().x + w / 2 + w / 8, boundingRect.br().y));

        Core.rectangle(m, lMid.tl(), lMid.br(), green);
        Core.rectangle(m, rMid.tl(), rMid.br(), green);
        Core.rectangle(m, tMid.tl(), tMid.br(), green);
        Core.rectangle(m, bMid.tl(), bMid.br(), green);

        Point cog = centerOfGravity(contours);
        Core.circle(m, cog, 10, blue);

        Rect cogRect = new Rect(new Point(cog.x - w / 4, cog.y - h / 4), new Point(cog.x + w / 4, cog.y + h / 4));
        Core.rectangle(m, cogRect.tl(), cogRect.br(), green);

        Bitmap bmp = Bitmap.createBitmap(dp(360), dp(360), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(m, bmp);
        saveBitmap(bmp, "boundingX");

        int count = 0;
        for (Point p : convexPoints) {
            if (p.inside(cogRect)) {
                count++;
            }
        }

        for (Point p : convexPoints) {
            if (p.inside(lMid) || p.inside(rMid) || p.inside(tMid) || p.inside(bMid)) {
                return false;
            }
        }

        return count == 4;
    }

    public boolean recognizeTriangle(Point[] convexPoints, Mat m, MatOfPoint contours) {

        Scalar blue = new Scalar(0, 0, 255, 255);
        Scalar green = new Scalar(0, 255, 0, 255);

        // Draw bounding rectangle around contour
        Rect boundingRect = Imgproc.boundingRect(contours);
        Core.rectangle(m, boundingRect.tl(), boundingRect.br(), blue);

        int w = boundingRect.width / 4;
        int h = boundingRect.height / 4;
        Rect ulRect = new Rect(boundingRect.tl(), new Point(boundingRect.tl().x + w,
                boundingRect.tl().y + h));
        Rect urRect = new Rect(new Point(boundingRect.br().x - w, boundingRect.tl().y),
                new Point(boundingRect.br().x, boundingRect.tl().y + h));

        Rect blRect = new Rect(new Point(boundingRect.tl().x, boundingRect.br().y - h),
                new Point(boundingRect.tl().x + w, boundingRect.br().y));
        Rect brRect = new Rect(new Point(boundingRect.br().x - w, boundingRect.br().y - h),
                boundingRect.br());
        Rect tMid = new Rect(new Point(boundingRect.tl().x + w * 2 - w, boundingRect.tl().y),
                new Point(boundingRect.tl().x + w * 2 + w, boundingRect.tl().y + h));

        Core.rectangle(m, ulRect.tl(), ulRect.br(), green);
        Core.rectangle(m, urRect.tl(), urRect.br(), green);
        Core.rectangle(m, blRect.tl(), blRect.br(), green);
        Core.rectangle(m, brRect.tl(), brRect.br(), green);
        Core.rectangle(m, tMid.tl(), tMid.br(), green);

        Point cog = centerOfGravity(contours);
        Core.circle(m, cog, 10, blue);

        Rect cogRect = new Rect(new Point(cog.x - w / 2, cog.y - h / 2), new Point(cog.x + w / 2, cog.y + h / 2));
        Core.rectangle(m, cogRect.tl(), cogRect.br(), green);

        Bitmap bmp = Bitmap.createBitmap(dp(360), dp(360), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(m, bmp);
        saveBitmap(bmp, "boundingTri");

        boolean br = false, bl = false, mid = false;

        for (Point p : convexPoints) {
            if (p.inside(ulRect) || p.inside(urRect)) return false;
            if (p.inside(cogRect)) return false;

            if (p.inside(tMid)) mid = true;
            else if (p.inside(blRect)) bl = true;
            else if (p.inside(brRect)) br = true;
        }

        return bl && br && mid;
    }

    public boolean recognizeCircle(Point[] convexPoints, Mat m, MatOfPoint contours) {

        if (convexPoints.length < 15) return false;

        Scalar blue = new Scalar(0, 0, 255, 255);
        Scalar green = new Scalar(0, 255, 0, 255);

        // Draw bounding rectangle around contour
        Rect boundingRect = Imgproc.boundingRect(contours);
        Core.rectangle(m, boundingRect.tl(), boundingRect.br(), blue);

        int w = boundingRect.width;
        int h = boundingRect.height;

        double ratio = ((double) w) / h;
        if (ratio > 1) ratio = ((double) h) / w;

        boolean circular = ratio > .8;
        if (!circular) return false;

        Point cog = centerOfGravity(contours);
        Core.circle(m, cog, 10, blue);

        Rect cogRect = new Rect(new Point(cog.x - w / 4, cog.y - h / 4), new Point(cog.x + w / 4, cog.y + h / 4));
        Core.rectangle(m, cogRect.tl(), cogRect.br(), green);

        Bitmap bmp = Bitmap.createBitmap(dp(360), dp(360), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(m, bmp);
        saveBitmap(bmp, "boundingCircle");

        Point[] allPoints = contours.toArray();
        for (Point p : allPoints) {
            if (p.inside(cogRect)) {
                return false;
            }
        }

        return true;
    }

    public boolean recognizeSquare(Point[] convexPoints, Mat m, MatOfPoint contours) {

        Scalar blue = new Scalar(0, 0, 255, 255);
        Scalar green = new Scalar(0, 255, 0, 255);

        // Draw bounding rectangle around contour
        Rect boundingRect = Imgproc.boundingRect(contours);
        Core.rectangle(m, boundingRect.tl(), boundingRect.br(), blue);

        int w = boundingRect.width;
        int h = boundingRect.height;

        double ratio = ((double) w) / h;
        if (ratio > 1) ratio = ((double) h) / w;

        boolean squarish = ratio > .8;
        if (!squarish) return false;

        Point cog = centerOfGravity(contours);
        Core.circle(m, cog, 10, blue);

        Rect cogRect = new Rect(new Point(cog.x - w / 3, cog.y - h / 3), new Point(cog.x + w / 3, cog.y + h / 3));
        Core.rectangle(m, cogRect.tl(), cogRect.br(), green);

        Bitmap bmp = Bitmap.createBitmap(dp(360), dp(360), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(m, bmp);
        saveBitmap(bmp, "boundingSquare");

        Point[] allPoints = contours.toArray();
        for (Point p : allPoints) {
            if (p.inside(cogRect)) {
                return false;
            }
        }

        return true;
    }

    public boolean recognizeArrow(Point[] convexPoints, Mat m, MatOfPoint contours) {

        Scalar blue = new Scalar(0, 0, 255, 255);
        Scalar green = new Scalar(0, 255, 0, 255);

        // Draw bounding rectangle around contour
        Rect boundingRect = Imgproc.boundingRect(contours);
        Core.rectangle(m, boundingRect.tl(), boundingRect.br(), blue);

        Point cog = centerOfGravity(contours);
        if (cog.x < boundingRect.x + boundingRect.width / 2) return false;
        Core.circle(m, cog, 10, blue);

        int w = 3 * boundingRect.width / 8;
        int h = boundingRect.height / 8;
        Rect ulRect = new Rect(boundingRect.tl(), new Point(boundingRect.tl().x + w,
                boundingRect.tl().y + h));
        Rect blRect = new Rect(new Point(boundingRect.tl().x, boundingRect.br().y - h),
                new Point(boundingRect.tl().x + w, boundingRect.br().y));
        Rect midRight = new Rect(new Point(cog.x, cog.y - h * 2), new Point(boundingRect.br().x, cog.y + h * 2));

        Core.rectangle(m, ulRect.tl(), ulRect.br(), green);
        Core.rectangle(m, blRect.tl(), blRect.br(), green);
        Core.rectangle(m, midRight.tl(), midRight.br(), green);

        Bitmap bmp = Bitmap.createBitmap(dp(360), dp(360), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(m, bmp);
        saveBitmap(bmp, "boundingArrow");

        Point[] allPoints = contours.toArray();
        for (Point p : allPoints) {
            if (p.inside(ulRect) || p.inside(blRect)) {
                return false;
            }
        }

        int count = 0;
        for (Point p : convexPoints) {
            if (p.inside(midRight)) {
                count++;
            }
        }

        return count >= 3;
    }

    public boolean recognizeHeart(Point[] convexPoints, Mat m, MatOfPoint contours) {

        Scalar blue = new Scalar(0, 0, 255, 255);
        Scalar green = new Scalar(0, 255, 0, 255);

        // Draw bounding rectangle around contour
        Rect boundingRect = Imgproc.boundingRect(contours);
        Core.rectangle(m, boundingRect.tl(), boundingRect.br(), blue);

        int w = boundingRect.width / 4;
        int h = boundingRect.height / 4;
        Rect ulRect = new Rect(boundingRect.tl(), new Point(boundingRect.tl().x + w,
                boundingRect.tl().y + h));
        Rect urRect = new Rect(new Point(boundingRect.br().x - w, boundingRect.tl().y),
                new Point(boundingRect.br().x, boundingRect.tl().y + h));

        Rect bottomMid = new Rect(new Point(boundingRect.tl().x + w * 2 - w, boundingRect.br().y - h / 3),
                new Point(boundingRect.tl().x + w * 2 + w, boundingRect.br().y));
        Rect upperMid = new Rect(new Point(boundingRect.tl().x + w * 2 - w, boundingRect.tl().y + h / 4),
                new Point(boundingRect.tl().x + w * 2 + w, boundingRect.tl().y + h * 1.8));

        Core.rectangle(m, ulRect.tl(), ulRect.br(), green);
        Core.rectangle(m, urRect.tl(), urRect.br(), green);
        Core.rectangle(m, bottomMid.tl(), bottomMid.br(), green);
        Core.rectangle(m, upperMid.tl(), upperMid.br(), green);

        Point cog = centerOfGravity(contours);
        Core.circle(m, cog, 10, blue);
        Rect cogRect = new Rect(new Point(cog.x - w / 2, cog.y), new Point(cog.x + w / 2, cog.y + h));
        Core.rectangle(m, cogRect.tl(), cogRect.br(), green);

        Point[] allPoints = contours.toArray();
        for (Point p : allPoints) {
            if (p.inside(cogRect)) {
                return false;
            }
        }

        Bitmap bmp = Bitmap.createBitmap(dp(360), dp(360), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(m, bmp);
        saveBitmap(bmp, "boundingHeart");

        boolean ur = false, ul = false, bMid = false, uMid = false;

        for (Point p : convexPoints) {

            if (p.inside(bottomMid)) bMid = true;
            else if (p.inside(upperMid)) uMid = true;
            else if (p.inside(ulRect)) ul = true;
            else if (p.inside(urRect)) ur = true;
        }

        return ul && ur && bMid && uMid;
    }

    /**
     * Draws a circle where the center of gravity of the contour is and returns the x position of the COG.
     * Sources:
     * http://stackoverflow.com/questions/18345969/how-to-get-the-mass-center-of-a-contour-android-opencv
     * http://docs.opencv.org/modules/imgproc/doc/structural_analysis_and_shape_descriptors.html?highlight=moments#moments
     */
    public static Point centerOfGravity(Mat m) {
        Moments p = Imgproc.moments(m, false);
        int x = (int) (p.get_m10() / p.get_m00());
        int y = (int) (p.get_m01() / p.get_m00());
        return new Point(x, y);
    }

    /**
     * Finds the index of the largest contour area by comparing their areas
     */
    private static int biggestContourIndex(List<MatOfPoint> contours) {
        // Ignore small contours
        final float MIN_AREA = 100.0f;

        int biggestContourIndex = -1;
        double maxArea = MIN_AREA;
        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint contour = contours.get(i);
            double area = Imgproc.contourArea(contour);
            if (area > maxArea) {
                maxArea = area;
                biggestContourIndex = i;
            }
        }
        return biggestContourIndex;
    }

    public static Point[] convexityDefects(Mat mat, List<MatOfPoint> contours,
                                           int biggestContourIndex) {
        if (contours.size() > 0 && biggestContourIndex > -1) {
            List<MatOfInt> convexHull = new ArrayList<>();
            convexHull.add(new MatOfInt());
            Imgproc.convexHull(contours.get(biggestContourIndex), convexHull.get(0));

            MatOfInt4 defects = new MatOfInt4();
            Imgproc.convexityDefects(contours.get(biggestContourIndex), convexHull.get(0), defects);

            //Rect boundingRect = Imgproc.boundingRect(contours.get(biggestContourIndex));

            // Loop over all contours
            List<Point[]> convexPoints = new ArrayList<>();
            List<Point> points = new ArrayList<>();
            // Loop over all points that need to be hulled in current contour

            for (int j = 0; j < defects.toList().size() / 4; j++) {
                // points farthest from hull
                int index = defects.toList().get(4 * j + 2);
                Point p = new Point(contours.get(biggestContourIndex).get(index, 0)[0],
                        contours.get(biggestContourIndex).get(index, 0)[1]);
                // filter out points that are very close to existing points
                boolean add = true;
                for (int a = 0; a < points.size(); a++) {
                    if (distance(points.get(a), p) < 10) {
                        add = false;
                    }
                }
                if (add) {
                    points.add(p);
                }
            }
            convexPoints.add(points.toArray(new Point[0]));
            return convexPoints.get(0);
        } else return new Point[0];
    }

    /**
     * Finds the distance between two points
     */
    public static double distance(Point a, Point b) {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }

    public void saveBitmap(Bitmap bmp, String name) {
        OutputStream out = null;
        try {
            String fileName = Environment.getExternalStorageDirectory() + "/" + name + ".png";
            out = new FileOutputStream(fileName);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            out = null;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                    out = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}