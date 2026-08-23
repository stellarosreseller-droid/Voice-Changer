package com.stellaros.voicechanger;
import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.PlaybackParams;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.widget.*;
import android.graphics.Color;
import android.view.View;
import java.io.IOException;

public class MainActivity extends Activity {
    MediaRecorder recorder;
    MediaPlayer player;
    String filePath;
    boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        filePath = getExternalCacheDir().getAbsolutePath() + "/voice.3gp";
        
        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setBackgroundColor(Color.parseColor("#0F0F1A"));
        main.setPadding(40, 80, 40, 40);
        main.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView title = new TextView(this);
        title.setText("STELLAR OS\nVOICE CHANGER");
        title.setTextColor(Color.parseColor("#00FFC6"));
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0,0,0,50);
        main.addView(title);

        Button btnRecord = createBtn("● REC", "#FF3B30");
        Button btnRobot = createBtn("🤖 ROBOT", "#1E1E2E");
        Button btnBambino = createBtn("👶 BAMBINO", "#1E1E2E");
        Button btnMostro = createBtn("👹 MOSTRO", "#1E1E2E");
        Button btnAlieno = createBtn("👽 ALIENO", "#1E1E2E");
        Button btnPlay = createBtn("▶ PLAY NORMALE", "#00FFC6");

        main.addView(btnRecord);
        main.addView(btnPlay);
        main.addView(btnRobot);
        main.addView(btnBambino);
        main.addView(btnMostro);
        main.addView(btnAlieno);

        setContentView(main);

        btnRecord.setOnClickListener(v -> {
            if (!isRecording) {
                startRec();
                btnRecord.setText("■ STOP");
                isRecording = true;
            } else {
                stopRec();
                btnRecord.setText("● REC");
                isRecording = false;
                Toast.makeText(this, "Registrato!", Toast.LENGTH_SHORT).show();
            }
        });

        btnPlay.setOnClickListener(v -> playVoice(1.0f, 1.0f));
        btnRobot.setOnClickListener(v -> playVoice(0.7f, 0.7f));
        btnBambino.setOnClickListener(v -> playVoice(1.8f, 1.2f));
        btnMostro.setOnClickListener(v -> playVoice(0.5f, 0.7f));
        btnAlieno.setOnClickListener(v -> playVoice(1.5f, 0.7f));
    }

    Button createBtn(String txt, String color) {
        Button b = new Button(this);
        b.setText(txt);
        b.setBackgroundColor(Color.parseColor(color));
        b.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, 180);
        p.setMargins(0, 15, 0, 15);
        b.setLayoutParams(p);
        return b;
    }

    void startRec() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(filePath);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try { recorder.prepare(); recorder.start(); } catch (IOException e) {}
    }
    void stopRec() { try { recorder.stop(); recorder.release(); } catch(Exception e){} recorder=null; }

    void playVoice(float pitch, float speed) {
        try {
            if(player!=null) player.release();
            player = new MediaPlayer();
            player.setDataSource(filePath);
            player.prepare();
            PlaybackParams params = new PlaybackParams();
            params.setPitch(pitch);
            params.setSpeed(speed);
            player.setPlaybackParams(params);
            player.start();
        } catch(Exception e){ Toast.makeText(this, "Prima registra!", Toast.LENGTH_SHORT).show(); }
    }
}
