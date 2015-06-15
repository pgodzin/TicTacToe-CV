package com.example.pgodzin.tictactoe;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Map;


public class MainActivity extends ActionBarActivity {

    private int mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent i = getIntent();
        mode = i.getIntExtra("mode", -1);
        TicTacToeView tttView = (TicTacToeView) findViewById(R.id.ttt);
        tttView.mode = mode;

        Button b = (Button) findViewById(R.id.newgame);
        if (b != null) {
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    restart();
                }
            });
        }
    }

    public void updatePlayerShapes(int playerTurn, Map<Integer, String> shapeMap, int moveShape) {
        ImageView shapes = (ImageView) findViewById(R.id.shape_list);
        shapes.setVisibility(View.GONE);
        TextView selectText = (TextView) findViewById(R.id.select_text);
        selectText.setVisibility(View.GONE);
        TextView p1Shape = (TextView) findViewById(R.id.p1_shape);
        p1Shape.setVisibility(View.VISIBLE);
        TextView p2Shape = (TextView) findViewById(R.id.p2_shape);
        p2Shape.setVisibility(View.VISIBLE);
        if (playerTurn == 0)
            p1Shape.setText(p1Shape.getText() + " " + shapeMap.get(moveShape));
        else if (playerTurn == 1)
            p2Shape.setText(p2Shape.getText() + " " + shapeMap.get(moveShape));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.restart) {
            restart();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void restart() {
        TicTacToeView tttView = (TicTacToeView) findViewById(R.id.ttt);
        int w = tttView.w;
        int dpw = pxFromDp(w);
        tttView.board = Bitmap.createBitmap(dpw, dpw, Bitmap.Config.ARGB_8888);
        tttView.mCanvas.setBitmap(tttView.board);
        tttView.mCanvas.drawColor(0xFFFFFFFF);
        tttView.paint.setColor(Color.BLACK);
        tttView.mCanvas.drawLine(pxFromDp(w / 3), 0, pxFromDp(w / 3), dpw, tttView.paint);
        tttView.mCanvas.drawLine(pxFromDp(2 * w / 3), 0, pxFromDp(2 * w / 3), dpw, tttView.paint);
        tttView.mCanvas.drawLine(0, pxFromDp(w / 3), dpw, pxFromDp(w / 3), tttView.paint);
        tttView.mCanvas.drawLine(0, pxFromDp(2 * w / 3), dpw, pxFromDp(2 * w / 3), tttView.paint);
        tttView.oldboard = tttView.board.copy(Bitmap.Config.ARGB_8888, true);

        tttView.path.reset();
        tttView.paths.clear();
        tttView.undonePaths.clear();

        ImageView shapes = (ImageView) findViewById(R.id.shape_list);
        shapes.setVisibility(View.VISIBLE);
        TextView selectText = (TextView) findViewById(R.id.select_text);
        selectText.setVisibility(View.VISIBLE);
        TextView p1Shape = (TextView) findViewById(R.id.p1_shape);
        p1Shape.setVisibility(View.GONE);
        TextView p2Shape = (TextView) findViewById(R.id.p2_shape);
        p2Shape.setVisibility(View.GONE);
        p1Shape.setText(R.string.p1_shape);
        p2Shape.setText(R.string.p2_shape);

        tttView.playerShape = new int[]{-1, -1};
        tttView.playerTurn = 0;
        tttView.canDraw = true;

        tttView.b.init();

        Toast.makeText(getApplicationContext(), "Restarting...", Toast.LENGTH_SHORT).show();
        tttView.draw(tttView.mCanvas);
        tttView.invalidate();
    }

    public int pxFromDp(final int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

}
