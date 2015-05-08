package com.example.pgodzin.tictactoe;

import android.content.Context;
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

    Paint paint = new Paint();
    Path path = new Path();
    ArrayList<Path> paths = new ArrayList<>();
    long lastDrawn = 0;
    Context mContext;
    Bitmap board, oldboard;
    Canvas mCanvas;
    int player1Shape, player2Shape;
    int playerTurn = 0;
    int[] playerColors = new int[]{Color.BLUE, Color.RED};
    private Map<Path, Integer> colorsMap = new HashMap<>();

    static {
        System.loadLibrary("opencv_java");
    }

    public TicTacToeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        board = Bitmap.createBitmap(dp(360), dp(360), Bitmap.Config.ARGB_8888);
        new Thread(new Runnable() {
            @Override
            public void run() {
                saveBitmap(board, "board");
            }
        }).start();
        mCanvas = new Canvas(board);
        mCanvas.drawColor(0xFFFFFFFF);
        setupPaint();
        mCanvas.drawLine(dp(120), dp(10), dp(120), dp(350), paint);
        mCanvas.drawLine(dp(240), dp(10), dp(240), dp(350), paint);
        mCanvas.drawLine(dp(10), dp(120), dp(350), dp(120), paint);
        mCanvas.drawLine(dp(10), dp(240), dp(350), dp(240), paint);
        oldboard = board.copy(Bitmap.Config.ARGB_8888, true);
        paint.setColor(playerColors[0]);
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

        int currPlayerTurn = 0;
        if (playerTurn == 0) currPlayerTurn = 1;

        paint.setColor(playerColors[currPlayerTurn]);
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

    public boolean onTouchEvent(MotionEvent event) {
        // Get the coordinates of the touch event.
        float eventX = event.getX();
        float eventY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Set a new starting point
                path.moveTo(eventX, eventY);
                lastDrawn = System.currentTimeMillis();
                return true;
            case MotionEvent.ACTION_MOVE:
                // Connect the points
                path.lineTo(eventX, eventY);
                lastDrawn = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_UP:
                new CountDownTimer(1500, 500) {

                    @Override
                    public void onTick(long miliseconds) {
                    }

                    @Override
                    public void onFinish() {
                        if (System.currentTimeMillis() > lastDrawn + 1500) {
                            Toast.makeText(mContext, "Saving", Toast.LENGTH_SHORT).show();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    if (playerTurn == 0) playerTurn = 1;
                                    else if (playerTurn == 1) playerTurn = 0;
                                    paths.add(path);
                                    colorsMap.put(path, playerTurn);
                                    path = new Path();

                                    processMove();
                                    saveBitmap(board, "board");
                                }
                            }).start();
                        }
                    }
                }.start();

                break;
            default:
                return false;
        }

        // Makes our view repaint and call onDraw
        invalidate();
        return true;
    }

    public void processMove() {
        Mat oldBoard = new Mat();
        Bitmap oldBmp = oldboard.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(oldBmp, oldBoard);
        Utils.matToBitmap(oldBoard, oldBmp);

        saveBitmap(oldBmp, "old");

        Mat newBoard = new Mat();
        Bitmap bmp = board.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp, newBoard);
        Utils.matToBitmap(newBoard, bmp);
        saveBitmap(bmp, "new");

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

        Rect[] boxes = new Rect[]{
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
        int boxNum = -1;
        for (int i = 0; i < boxes.length; i++) {
            if (convexPoints.length > 0 && convexPoints[0].inside(boxes[i])) {
                boxNum = i;
                break;
            }
        }

        Utils.matToBitmap(contourMap, contourBmp);
        saveBitmap(contourBmp, "contours");

        MatOfPoint allContours = new MatOfPoint();
        List<Point> contourPts = new ArrayList<>();
        for (MatOfPoint c : contours) {
            List<Point> pList = c.toList();
            for (Point p : pList)
                if (!contourPts.contains(p) && p.inside(boxes[boxNum])) contourPts.add(p);
        }
        allContours.fromList(contourPts);
        if (recognizePlus(convexPoints, contourMap.clone(), allContours)) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "+ Identified", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "+ Not Identified", Toast.LENGTH_SHORT).show();
                }
            });
            if (recognizeX(convexPoints, contourMap.clone(), allContours)) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "X Identified", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "X Not Identified", Toast.LENGTH_SHORT).show();
                    }
                });
                if (recognizeTriangle(convexPoints, contourMap.clone(), allContours)) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, "Triangle Identified", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, "Triangle Not Identified", Toast.LENGTH_SHORT).show();
                        }
                    });
                    if (recognizeSquare(convexPoints, contourMap.clone(), allContours)) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, "Square Identified", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, "Square Not Identified", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                }
            }
        }

        oldboard = board.copy(Bitmap.Config.ARGB_8888, true);
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

        Rect cogRect = new Rect(new Point(cog.x - w, cog.y - h), new Point(cog.x + w / 2, cog.y + h / 2));
        Core.rectangle(m, cogRect.tl(), cogRect.br(), green);

        Bitmap bmp = Bitmap.createBitmap(dp(360), dp(360), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(m, bmp);
        saveBitmap(bmp, "bounding+");

        boolean notInCorners = true;

        int count = 0;
        for (Point p : convexPoints) {
            if (p.inside(cogRect)) {
                count++;
            }
        }

        for (Point p : convexPoints) {
            if (p.inside(ulRect) || p.inside(urRect) || p.inside(blRect) || p.inside(brRect)) {
                notInCorners = false;
                break;
            }
        }

        if (count == 4 && notInCorners) {
            return true;
        } else {
            return false;
        }
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

        boolean notInMidEdges = true;

        int count = 0;
        for (Point p : convexPoints) {
            if (p.inside(cogRect)) {
                count++;
            }
        }

        for (Point p : convexPoints) {
            if (p.inside(lMid) || p.inside(rMid) || p.inside(tMid) || p.inside(bMid)) {
                notInMidEdges = false;
                break;
            }
        }

        if (count == 4 && notInMidEdges) {
            return true;
        } else {
            return false;
        }
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

        boolean notInUpperCorners = true, notInMiddle = true;
        boolean br = false, bl = false, mid = false;

        for (Point p : convexPoints) {
            if (p.inside(ulRect) || p.inside(urRect)) notInUpperCorners = false;
            if (p.inside(cogRect)) notInMiddle = false;

            if (p.inside(tMid)) mid = true;
            else if (p.inside(blRect)) bl = true;
            else if (p.inside(brRect)) br = true;
        }

        return bl && br && mid && notInUpperCorners && notInMiddle;
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

        Point cog = centerOfGravity(contours);
        Core.circle(m, cog, 10, blue);

        Rect cogRect = new Rect(new Point(cog.x - w / 3, cog.y - h / 3), new Point(cog.x + w / 3, cog.y + h / 3));
        Core.rectangle(m, cogRect.tl(), cogRect.br(), green);

        Bitmap bmp = Bitmap.createBitmap(dp(360), dp(360), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(m, bmp);
        saveBitmap(bmp, "boundingSquare");

        boolean allPointsAroundEdges = true;

        Point[] allPoints = contours.toArray();
        for (Point p : allPoints) {
            if (p.inside(cogRect)) {
                allPointsAroundEdges = false;
                break;
            }
        }

        return squarish && allPointsAroundEdges;
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

    public static Point[] convexityDefects(Mat mat, List<MatOfPoint> contours, int biggestContourIndex) {
        if (contours.size() > 0 && biggestContourIndex > -1) {
            List<MatOfInt> convexHull = new ArrayList<>();
            convexHull.add(new MatOfInt());
            Imgproc.convexHull(contours.get(biggestContourIndex), convexHull.get(0));

            MatOfInt4 defects = new MatOfInt4();
            Imgproc.convexityDefects(contours.get(biggestContourIndex), convexHull.get(0), defects);

            //Rect boundingRect = Imgproc.boundingRect(contours.get(biggestContourIndex));

            // Loop over all contours
            List<Point[]> convexPoints = new ArrayList<Point[]>();
            List<Point> points = new ArrayList<Point>();
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