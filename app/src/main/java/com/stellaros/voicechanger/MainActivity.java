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
        title.setText("STELLAR OS\nLIVE VOICE");
        title.setTextColor(Color.parseColor("#00FFC6"));
        title.setTextSize(26);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0,0,0,20);
        main.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Senza Root - Funziona in vivavoce per tutte le app");
        subtitle.setTextColor(Color.GRAY);
        subtitle.setTextSize(12);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0,0,0,30);
        main.addView(subtitle);

        Button btnLive = createBtn("● AVVIA LIVE - OFF", "#FF3B30");
        Button btnRec = createBtn("● REGISTRA VOCALE", "#2A2A3E");
        Button btnRobot = createBtn("🤖 ROBOT (0.6x)", "#1E1E2E");
        Button btnBambino = createBtn("👶 BAMBINO (1.8x)", "#1E1E2E");
        Button btnMostro = createBtn("👹 MOSTRO (0.5x)", "#1E1E2E");
        Button btnAlieno = createBtn("👽 ALIENO (1.5x)", "#1E1E2E");
        Button btnPlay = createBtn("▶ ASCOLTA REGISTRAZIONE", "#00FFC6");

        main.addView(btnLive);
        main.addView(btnRec);
        main.addView(btnPlay);
        main.addView(btnRobot);
        main.addView(btnBambino);
        main.addView(btnMostro);
        main.addView(btnAlieno);
        setContentView(main);

        btnLive.setOnClickListener(v -> {
            if (!isLive) {
                startLive();
                btnLive.setText("■ STOP LIVE - ON (" + currentPitch + "x)");
                btnLive.setBackgroundColor(Color.parseColor("#00FFC6"));
                isLive = true;
                Toast.makeText(this, "LIVE ON! Ora apri WhatsApp/Discord in vivavoce", Toast.LENGTH_LONG).show();
            } else {
                stopLive();
                btnLive.setText("● AVVIA LIVE - OFF");
                btnLive.setBackgroundColor(Color.parseColor("#FF3B30"));
                isLive = false;
            }
        });

        btnRobot.setOnClickListener(v -> { currentPitch = 0.6f; if(isLive) Toast.makeText(this,"Voce ROBOT attiva",0).show(); });
        btnBambino.setOnClickListener(v -> { currentPitch = 1.8f; if(isLive) Toast.makeText(this,"Voce BAMBINO attiva",0).show(); });
        btnMostro.setOnClickListener(v -> { currentPitch = 0.5f; if(isLive) Toast.makeText(this,"Voce MOSTRO attiva",0).show(); });
        btnAlieno.setOnClickListener(v -> { currentPitch = 1.5f; if(isLive) Toast.makeText(this,"Voce ALIENO attiva",0).show(); });

        btnRec.setOnClickListener(v -> {
            if(!isRecording){ startRec(); btnRec.setText("■ STOP REGISTRAZIONE"); isRecording=true; }
            else { stopRec(); btnRec.setText("● REGISTRA VOCALE"); isRecording=false; Toast.makeText(this,"Salvato! Ora premi PLAY con effetto",0).show(); }
        });
        btnPlay.setOnClickListener(v -> playVoice(currentPitch));
    }

    Button createBtn(String t, String c){ Button b=new Button(this); b.setText(t); b.setBackgroundColor(Color.parseColor(c)); b.setTextColor(Color.WHITE); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,160); p.setMargins(0,12,0,12); b.setLayoutParams(p); return b; }

    void startLive(){
        int sampleRate=44100;
        int bufferSize=AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        recorder=new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        track=new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);
        track.play(); recorder.startRecording();
        liveThread=new Thread(() -> {
            short[] buffer=new short[bufferSize/2];
            while(isLive){
                int read=recorder.read(buffer,0,buffer.length);
                if(currentPitch!=1.0f){
                    short[] out=changePitch(buffer, read, currentPitch);
                    track.write(out,0,out.length);
                } else track.write(buffer,0,read);
            }
        }); liveThread.start();
    }
    void stopLive(){ isLive=false; try{ if(recorder!=null){recorder.stop();recorder.release();} if(track!=null){track.stop();track.release();} }catch(Exception e){} }

    short[] changePitch(short[] input, int len, float pitch){
        int newLen=(int)(len/pitch);
        if(newLen<=0) return input;
        short[] output=new short[newLen];
        for(int i=0;i<newLen;i++){ int idx=(int)(i*pitch); if(idx<len) output[i]=input[idx]; }
        return output;
    }
    void startRec(){ mediaRecorder=new MediaRecorder(); mediaRecorder.setAudioSource(1); mediaRecorder.setOutputFormat(2); mediaRecorder.setOutputFile(filePath); mediaRecorder.setAudioEncoder(3); try{mediaRecorder.prepare();mediaRecorder.start();}catch(Exception e){} }
    void stopRec(){ try{mediaRecorder.stop();mediaRecorder.release();}catch(Exception e){} mediaRecorder=null; }
    void playVoice(float pitch){ try{ if(player!=null) player.release(); player=new MediaPlayer(); player.setDataSource(filePath); player.prepare(); PlaybackParams p=new PlaybackParams(); p.setPitch(pitch); player.setPlaybackParams(p); player.start(); }catch(Exception e){ Toast.makeText(this,"Registra prima un vocale!",0).show(); } }
}
