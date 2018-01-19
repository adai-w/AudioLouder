package com.example.asus.audiolouder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.NoiseSuppressor;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    short click_cnt=0;
    boolean isRecording = false;//是否录放的标记
    static final int frequency = 44100;
    static final int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    int recBufSize;//录音缓存区大小
    int playBufSize;//播放缓存区大小
    AudioRecord audioRecord;//录音对象
    AudioTrack audioTrack;//播放对象
    AcousticEchoCanceler acousticEchoCanceler;//回声消除器 api 16+(4.1+)
    NoiseSuppressor noiseSuppressor;//噪声抑制器 api 16+(4.1+)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button power=(Button)findViewById(R.id.power);
        power.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                click_cnt%=2;
                recBufSize = AudioRecord.getMinBufferSize(frequency,
                        channelConfiguration, audioEncoding);

                playBufSize=AudioTrack.getMinBufferSize(frequency,
                        channelConfiguration, audioEncoding);
                // -----------------------------------------
                audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency,
                        channelConfiguration, audioEncoding, recBufSize);

                audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frequency,
                        channelConfiguration, audioEncoding,
                        playBufSize, AudioTrack.MODE_STREAM);
                //power.setTextColor(getColorStateList(2));
                acousticEchoCanceler = AcousticEchoCanceler.create(audioRecord.getAudioSessionId());
                noiseSuppressor = NoiseSuppressor.create(audioRecord.getAudioSessionId());
                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED){
                    if (AcousticEchoCanceler.isAvailable()) {
                        acousticEchoCanceler.setEnabled(true);
                    } else {
                        Toast.makeText(MainActivity.this, "您的手机不支持回声控制", Toast.LENGTH_SHORT).show();
                    }
                    if (NoiseSuppressor.isAvailable()) {
                        noiseSuppressor.setEnabled(true);
                    } else {
                        Toast.makeText(MainActivity.this, "您的手机不支持噪音消除", Toast.LENGTH_SHORT).show();
                    }
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.RECORD_AUDIO},1);
                }else {
                    Thread thread = new Thread(new Runnable() {
                        public void run() {
                            rec_play();
                        }
                    });
                    if(click_cnt==0){
                        power.setText("START");
                        isRecording = false;
                    }else{
                        power.setText("STOP");
                        thread.start();
                    }
                    click_cnt++;
                }
            }
        });

    }
    public void rec_play(){

        //边录边放
        try {

            byte[] buffer = new byte[recBufSize];//音频缓冲区
            audioRecord.startRecording();//开始录制
            audioTrack.play();//开始播放
            isRecording = true;

            while (isRecording) {
                //从MIC保存数据到缓冲区
                int bufferReadResult = audioRecord.read(buffer, 0,
                        recBufSize);

                byte[] tmpBuf = new byte[bufferReadResult];
                System.arraycopy(buffer, 0, tmpBuf, 0, bufferReadResult);
                //写入数据即播放
                audioTrack.write(tmpBuf, 0, tmpBuf.length);
            }
            audioTrack.stop();
            audioRecord.stop();
            acousticEchoCanceler.release();
        } catch (Throwable t) {
            Toast.makeText(MainActivity.this,"App exit in error",Toast.LENGTH_SHORT).show();
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch (requestCode){
            case 1:
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Thread thread = new Thread(new Runnable() {
                        public void run() {
                            rec_play();
                        }
                    });
                    thread.start();
                    click_cnt++;
                }else{
                    Toast.makeText(MainActivity.this,"You denied the permission",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }
}
