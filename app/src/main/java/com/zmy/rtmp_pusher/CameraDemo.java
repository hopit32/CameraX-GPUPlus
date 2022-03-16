package com.zmy.rtmp_pusher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.AudioFormat;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.view.PreviewView;

import com.zmy.rtmp_pusher.capture.camerax_capture.CameraXCapture;
import com.zmy.rtmp_pusher.lib.RtmpCallback;
import com.zmy.rtmp_pusher.lib.RtmpPusher;
import com.zmy.rtmp_pusher.lib.audio_capture.MicAudioCapture;
import com.zmy.rtmp_pusher.lib.pusher.PusherException;

import org.wysaid.myUtils.FileUtil;
import org.wysaid.myUtils.ImageUtil;
import org.wysaid.nativePort.CGEFrameRecorder;

import jp.co.cyberagent.android.gpuimage.GPUImageView;

public class CameraDemo extends AppCompatActivity implements RtmpCallback , View.OnClickListener , CameraXCapture.Ilistener {
    private final String TAG = "CameraDemo";


    private ImageView imageView;
    private String lastVideoPathFileName = FileUtil.getPath() + "/lastVideoPath.txt";
    private TextureView textureView;
    private GPUImageView gpuImageView;
    private Button btnRecording;
    private LinearLayout linearLayout;
    private CameraXCapture videoCapture;
    private String mCurrentConfig;

    private MicAudioCapture audioCapture;
    private RtmpPusher rtmpPusher;

    @Override
    public void sendBitmap(Bitmap bitmap) {
        if (imageView!= null) {
            imageView.post(new Runnable() {
                @Override
                public void run() {
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    }
                }
            });
        }
    }

    public static class MyButtons extends androidx.appcompat.widget.AppCompatButton {

        public String filterConfig;

        public MyButtons(Context context, String config) {
            super(context);
            filterConfig = config;
        }
    }

    private View.OnClickListener mFilterSwitchListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MyButtons btn = (MyButtons) v;
            videoCapture.setFilterWidthConfig(btn.filterConfig);
            mCurrentConfig = btn.filterConfig;
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_camera);

        initView();
        setupFilterMenu();

        /*Preview previewProvider = new Preview.Builder().build();
        previewProvider.setSurfaceProvider(preview.getSurfaceProvider());*/
        videoCapture = new CameraXCapture(this.getApplicationContext(), this , 1080 ,1920, CameraSelector.DEFAULT_FRONT_CAMERA, textureView , gpuImageView,this);
        audioCapture = new MicAudioCapture(AudioFormat.ENCODING_PCM_16BIT, 44100, AudioFormat.CHANNEL_IN_STEREO);

        rtmpPusher = new RtmpPusher.Builder()
                .url("rtmp://live-rtmp.sohatv.vn/ywdacow15xwowa0p7jpdg0w470lws2zr/e405fcf9-4824-4b11-9f87-1b74756d096a")
                .audioCapture(audioCapture)
                .videoCapture(videoCapture)
                .cacheSize(100)
                .callback(this)
                .build();
        try {
            rtmpPusher.start();
        } catch (PusherException e) {
            e.printStackTrace();
        }

    }

    private void setupFilterMenu(){
        for (int i = 0; i != Filter.EFFECT_CONFIGS.length; ++i) {
            MyButtons button = new MyButtons(this, Filter.EFFECT_CONFIGS[i]);
            button.setAllCaps(false);
            if (i == 0)
                button.setText("None");
            else
                button.setText("Filter" + i);
            button.setOnClickListener(mFilterSwitchListener);
            linearLayout.addView(button);
        }
    }

    private void initView(){
        imageView = findViewById(R.id.preview_image);
        textureView = findViewById(R.id.preview_view);
        linearLayout = findViewById(R.id.layout_menu_filter);
        btnRecording = findViewById(R.id.btn_recording);
        gpuImageView = findViewById(R.id.gpuImageView);
        btnRecording.setOnClickListener(this::onClick);

    }

    @Override
    public void onAudioCaptureError(Exception e) {

    }

    @Override
    public void onVideoCaptureError(Exception e) {

    }

    @Override
    public void onVideoEncoderError(Exception e) {

    }

    @Override
    public void onAudioEncoderError(Exception e) {

    }

    @Override
    public void onPusherError(Exception e) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_recording:{
                recording();
            }
        }
    }

    private void recording() {
        if (btnRecording.getText().equals("Recording")){
            btnRecording.setText("Stop");
            String recordFilename = ImageUtil.getPath() + "/rec_" + System.currentTimeMillis() + ".mp4";
            videoCapture.startRecording(30,recordFilename);
            FileUtil.saveTextContent(recordFilename, lastVideoPathFileName);
        } else{
            btnRecording.setText("Recording");
            videoCapture.endRecording(true);
        }
    }

}