package com.example.pgodzin.tictactoe;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Code from http://creative-punch.net/2014/03/make-basic-single-touch-drawing-app-android/
 */
public class TicTacToeView extends View {

    private Paint paint = new Paint();
    private Path path = new Path();
    long lastDrawn = 0;
    Context mContext;
    Bitmap board;
    Canvas mCanvas;

    public TicTacToeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        board = Bitmap.createBitmap(dp(360), dp(360), Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(board);
        mCanvas.drawColor(0xFFFFFFFF);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(25f);
        mCanvas.drawLine(dp(120), dp(10), dp(120), dp(350), paint);
        mCanvas.drawLine(dp(240), dp(10), dp(240), dp(350), paint);
        mCanvas.drawLine(dp(10), dp(120), dp(350), dp(120), paint);
        mCanvas.drawLine(dp(10), dp(240), dp(350), dp(240), paint);
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(15f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setColor(Color.BLACK);
        canvas.drawLine(dp(120), dp(10), dp(120), dp(350), paint);
        canvas.drawLine(dp(240), dp(10), dp(240), dp(350), paint);
        canvas.drawLine(dp(10), dp(120), dp(350), dp(120), paint);
        canvas.drawLine(dp(10), dp(240), dp(350), dp(240), paint);
        paint.setColor(Color.BLUE);

        canvas.drawPath(path, paint);
        mCanvas.drawPath(path, paint);
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
                            saveBoard();
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

    public void saveBoard() {
        OutputStream out = null;
        try {
            String fileName = Environment.getExternalStorageDirectory() + "/board.png";
            out = new FileOutputStream(fileName);
            board.compress(Bitmap.CompressFormat.PNG, 100, out);
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