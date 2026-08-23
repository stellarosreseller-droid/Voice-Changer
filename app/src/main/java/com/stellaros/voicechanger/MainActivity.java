package com.stellaros.voicechanger;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("Voice Changer - Build OK!");
        tv.setTextSize(24);
        setContentView(tv);
    }
}
