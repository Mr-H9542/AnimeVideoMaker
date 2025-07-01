package com.example.animevideomaker;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class CharacterCreatorActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("🧍 Character Creator Screen");
        setContentView(tv);
    }
}
