package com.example.animevideomaker;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class SceneComposerActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("🎬 Scene Composer Screen");
        setContentView(tv);
    }
}
