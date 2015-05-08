package com.example.pgodzin.tictactoe;

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
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

/*    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getSupportActionBar().hide();
        } else {
            getSupportActionBar().show();
        }
    }*/

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
            Toast.makeText(getApplicationContext(), "Restart", Toast.LENGTH_SHORT).show();
            TicTacToeView tttView = (TicTacToeView) findViewById(R.id.ttt);
            tttView.board = Bitmap.createBitmap(dp(360), dp(360), Bitmap.Config.ARGB_8888);
            tttView.mCanvas.setBitmap(tttView.board);
            tttView.mCanvas.drawColor(0xFFFFFFFF);
            tttView.paint.setColor(Color.BLACK);
            tttView.mCanvas.drawLine(dp(120), dp(10), dp(120), dp(350), tttView.paint);
            tttView.mCanvas.drawLine(dp(240), dp(10), dp(240), dp(350), tttView.paint);
            tttView.mCanvas.drawLine(dp(10), dp(120), dp(350), dp(120), tttView.paint);
            tttView.mCanvas.drawLine(dp(10), dp(240), dp(350), dp(240), tttView.paint);
            tttView.path.reset();
            for (Path p : tttView.paths) {
                p.reset();
            }
            tttView.draw(tttView.mCanvas);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Convert dp to pixels
    public int dp(int dp) {
        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

}
