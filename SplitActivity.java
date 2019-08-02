package com.example.talker.blackjack;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;


public class SplitActivity extends AppCompatActivity {

    private ImageView[] splitView = new ImageView[5];
    private ImageView[] dealerView = new ImageView[5];
    private ImageView[] playerView = new ImageView[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_split);

        splitView[0] = findViewById(R.id.splitIV1);
        splitView[1] = findViewById(R.id.splitIV2);
        splitView[2] = findViewById(R.id.splitIV3);
        splitView[3] = findViewById(R.id.splitIV4);
        splitView[4] = findViewById(R.id.splitIV5);
        playerView[0] = findViewById(R.id.playerIV1);
        playerView[1] = findViewById(R.id.playerIV2);
        playerView[2] = findViewById(R.id.playerIV3);
        playerView[3] = findViewById(R.id.playerIV4);
        playerView[4] = findViewById(R.id.playerIV5);
        dealerView[0] = findViewById(R.id.dealerIV1);
        dealerView[1] = findViewById(R.id.dealerIV2);
        dealerView[2] = findViewById(R.id.dealerIV3);
        dealerView[3] = findViewById(R.id.dealerIV4);
        dealerView[4] = findViewById(R.id.dealerIV5);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_alert, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
