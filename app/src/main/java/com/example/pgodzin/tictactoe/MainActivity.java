package com.example.pgodzin.tictactoe;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
            Toast.makeText(getApplicationContext(), "Restart", Toast.LENGTH_SHORT).show();
            TicTacToeView tttView = (TicTacToeView) findViewById(R.id.ttt);
            tttView = new TicTacToeView(getApplicationContext(), null);
            Canvas c = new Canvas(Bitmap.createBitmap(tttView.dp(360), tttView.dp(360), Bitmap.Config.ARGB_8888));
            c.drawColor(0xFFFFFFFF);
            tttView.draw(c);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
