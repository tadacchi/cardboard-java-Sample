package com.google.vrtoolkit.cardboard.samples.treasurehunt;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.vrtoolkit.cardboard.CardboardActivity;




/**
 * Created by 2015295 on 2015/10/20.
 */
public class FirstActivity extends CardboardActivity {
    private CardboardOverlayView overlayView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        Button Actionbtn1 = (Button)findViewById(R.id.button1);
        Actionbtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(FirstActivity.this, MainActivity.class);
                intent.putExtra("Number", 1);
                startActivity(intent);
            }
        });

        Button Actionbtn2 = (Button)findViewById(R.id.button2);
        Actionbtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(FirstActivity.this, MainActivity.class);
                intent.putExtra("Number", 2);
                startActivity(intent);
            }
        });
    }


}
