package com.stellaros.voicechanger;
import android.app.Activity;
import android.media.*;
import android.os.Bundle;
import android.widget.*;
import android.graphics.Color;
import android.view.Gravity;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

public class MainActivity extends Activity {
    boolean isLive = false;
    boolean isRecording = false;
    AudioRecord recorder;
    AudioTrack track;
    Thread liveThread;
    float currentPitch = 1.0f;
    String filePath;
    MediaRecorder mediaRecorder;
    MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }
        filePath = getExternalCacheDir().getAbsolutePath() + "/voice.3gp";
        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        main.setBackgroundColor(Color.parseColor("#0F0F1A"));
        main.setPadding(40, 60, 40, 40);
        main.setGravity(Gravity.CENTER_HORIZONTAL);
        TextView title = new TextView(this);
        title.setText("Voice Changer");
        title.setTextColor(Color.parseColor("#00FFC6"));
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);
        main.addView(title);
        TextView sub = new TextView(this);
        sub.setText("Non blocca il microfono - 100% Android");
        sub.setTextColor(Color.GRAY);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0,20,0,30);
        main.addView(sub);
        Button btnLive = createBtn("● AVVIA LIVE - OFF", "#FF3B30");
        Button btnRec = createBtn("● REGISTRA", "#2A2A3E");
        Button btnRobot = createBtn("🤖 ROBOT", "#1E1E2E");
        Button btnBambino = createBtn("👶 BAMBINO", "#1E1E2E");
        Button btnPlay = createBtn("▶ ASCOLTA", "#00FFC6");
        main.addView(btnLive); main.addView(btnRec); main.addView(btnPlay); main.addView(btnRobot); main.addView(btnBambino);
        setContentView(main);

        btnLive.setOnClickListener(v -> {
            if (!isLive) {
                startLive();
                btnLive.setText("■ STOP LIVE - Sbloccherà mic alla chiusura");
                btnLive.setBackgroundColor(Color.parseColor("#00FFC6"));
                isLive = true;
            } else {
                stopLive();
                btnLive.setText("● AVVIA LIVE - OFF");
                btnLive.setBackgroundColor(Color.parseColor("#FF3B30"));
                isLive = false;
            }
        });
        btnRobot.setOnClickListener(v -> { currentPitch = 0.6f; Toast.makeText(this,"ROBOT",0).show(); });
        btnBambino.setOnClickListener(v -> { currentPitch = 1.8f; Toast.makeText(this,"BAMBINO",0).show(); });
        btnRec.setOnClickListener(v -> {
            if(!isRecording){ startRec(); btnRec.setText("■ STOP"); isRecording=true; }
            else { stopRec(); btnRec.setText("● REGISTRA"); isRecording=false; }
        });
        btnPlay.setOnClickListener(v -> playVoice(currentPitch));
    }

    Button createBtn(String t, String c){ Button b=new Button(this); b.setText(t); b.setBackgroundColor(Color.parseColor(c)); b.setTextColor(Color.WHITE); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,150); p.setMargins(0,10,0,10); b.setLayoutParams(p); return b; }

    void startLive(){
        int sr=44100;
        int bs=AudioRecord.getMinBufferSize(sr, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        // VOICE_COMMUNICATION non blocca le altre app
        recorder=new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, sr, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bs);
        track=new AudioTrack(AudioManager.STREAM_MUSIC, sr, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bs, AudioTrack.MODE_STREAM);
        track.play(); recorder.startRecording();
        liveThread=new Thread(() -> {
            short[] buf=new short[bs/2];
            while(isLive){
                int r=recorder.read(buf,0,buf.length);
                if(currentPitch!=1.0f){
                    short[] out=changePitch(buf, r, currentPitch);
                    track.write(out,0,out.length);
                } else track.write(buf,0,r);
            }
        }); liveThread.start();
    }

    void stopLive(){
        isLive=false;
        if(liveThread!=null){ try{liveThread.join(500);}catch(Exception e){} }
        try{
            if(recorder!=null){ recorder.stop(); recorder.release(); recorder=null; }
            if(track!=null){ track.stop(); track.release(); track=null; }
            AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
            am.setMode(AudioManager.MODE_NORMAL);
            am.setSpeakerphoneOn(false);
        }catch(Exception e){}
        Toast.makeText(this,"Microfono liberato!",0).show();
    }

    short[] changePitch(short[] in, int len, float pitch){
        int newLen=(int)(len/pitch); if(newLen<=0) return in;
        short[] out=new short[newLen];
        for(int i=0;i<newLen;i++){ int idx=(int)(i*pitch); if(idx<len) out[i]=in[idx]; }
        return out;
    }
    void startRec(){ mediaRecorder=new MediaRecorder(); mediaRecorder.setAudioSource(1); mediaRecorder.setOutputFormat(2); mediaRecorder.setOutputFile(filePath); mediaRecorder.setAudioEncoder(3); try{mediaRecorder.prepare();mediaRecorder.start();}catch(Exception e){} }
    void stopRec(){ try{mediaRecorder.stop();mediaRecorder.release();}catch(Exception e){} mediaRecorder=null; }
    void playVoice(float pitch){ try{ if(player!=null) player.release(); player=new MediaPlayer(); player.setDataSource(filePath); player.prepare(); PlaybackParams p=new PlaybackParams(); p.setPitch(pitch); player.setPlaybackParams(p); player.start(); }catch(Exception e){ Toast.makeText(this,"Registra prima!",0).show(); } }

    @Override
    protected void onPause(){ super.onPause(); if(isLive) stopLive(); }
    @Override
    protected void onDestroy(){ super.onDestroy(); if(isLive) stopLive(); }
}
