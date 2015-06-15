package com.example.pgodzin.tictactoe;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;


public class StartMenuActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_start_menu);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int width = pxFromDp((int) (displayMetrics.widthPixels / displayMetrics.density));
        int height = pxFromDp((int) (displayMetrics.heightPixels / displayMetrics.density));

        Button computer = new Button(this);
        computer.setAlpha(0);
        final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins((int) (width / 2.85), (int) (height / 4.1), (int) (width / 2.93), (int) (height / 1.85));
        params.width = (int) (width / 3.05);
        params.height = (int) (height / 4);
        computer.setLayoutParams(params);

        Button multiplayer = new Button(this);
        multiplayer.setAlpha(0);
        final RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params2.setMargins((int) (width / 2.85), (int) (height / 2.15), (int) (width / 2.93), (int) (height / 3.25));
        params2.width = (int) (width / 3.05);
        params2.height = (int) (height / 4);
        multiplayer.setLayoutParams(params2);

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.startmenu);
        layout.requestLayout();
        layout.addView(computer);
        layout.addView(multiplayer);

        final Intent i = new Intent(StartMenuActivity.this, MainActivity.class);

        computer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                i.putExtra("mode", 0);
                startActivity(i);
            }
        });

        multiplayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                i.putExtra("mode", 1);
                startActivity(i);
            }
        });

    }

    public int pxFromDp(final int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
