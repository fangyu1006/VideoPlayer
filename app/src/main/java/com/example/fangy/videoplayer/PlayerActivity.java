package com.example.fangy.videoplayer;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlayerActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    //private EditText myText;
    private VideoView videoView;
    private Spinner m_spinner;

    Thread socketThread;
    Handler uiHandler;

    final static String serverURL = "http://monterosa.d2.comp.nus.edu.sg/~team03/";
    static String nextUri;
    static String resolution = "medium";
    static String fileName;
    static Integer count;
    static List<String> lowPlayList = new ArrayList<String>();
    static List<String> mediumPlayList = new ArrayList<String>();
    static List<String> highPlayList = new ArrayList<String>();
    static List<String> menuPlayList = new ArrayList<String>();
    static String mpdUri;
    static MPD mpd;
    static Integer playSegmentNmb;
    static Integer requestSegmentNub;
    static Representation currentRepresentation;
    static Segment currentPlaySegment;
    static long downloadTime;
    private String SDCardRoot;
    private String segmentPath;
    boolean isAutoAdaptation = true;
    char currentResolution = 1;
    char lastResolution = 1;

    @Override
    public void onCreate(Bundle savedInstanceState){
        // Receiving the data from previous activity
        Intent i = getIntent();
        fileName = i.getExtras().getString("fileName");

        SDCardRoot= Environment.getExternalStorageDirectory()+ File.separator;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // myText = (EditText) this.findViewById(R.id.editText);
        videoView = (VideoView) this.findViewById(R.id.videoView);

        Spinner spinner=(Spinner) findViewById(R.id.spinner_quality);
        spinner.setSelection(3);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String[] qualities = getResources().getStringArray(R.array.quality);
                Toast.makeText(PlayerActivity.this, qualities[pos] + " Resolution!" , Toast.LENGTH_LONG).show();
                if (pos != 3) {
                    isAutoAdaptation = false;
                    if (mpd != null) {
                        currentRepresentation = mpd.representation.get(pos);
                    }
                } else {
                    isAutoAdaptation = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Button playButton = (Button) this.findViewById(R.id.button_play);
        playButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                playSegmentNmb = 0;
                requestSegmentNub = 0;
                mpdUri = serverURL + "playlists/" + fileName + ".mpd";
                parseMPD();
                if (currentRepresentation == null)
                {
                    currentRepresentation = mpd.representation.get(1);
                }
                new downloadSegmentThread().start();
                playSegment();
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
            @Override
            public void onCompletion(MediaPlayer mp){
           //     Toast.makeText(PlayerActivity.this, "" + downloadTime, Toast.LENGTH_LONG).show();

                lastResolution = currentResolution;
                // delete last segment
                File lastSegmentFile = new File(segmentPath);
                if (lastSegmentFile.exists()) {
                    lastSegmentFile.delete();
                }
                // play next file
                if (playSegmentNmb < mpd.representation.get(0).segments.size()) {
                    playSegment();
                }
            }
        });
    }

    private void playSegment() {
        currentPlaySegment = currentRepresentation.segments.get(playSegmentNmb);
        segmentPath = SDCardRoot + "VideoPlayer/" + currentPlaySegment.getMedia();
        File segmentFile = new File(segmentPath);

        while(!segmentFile.exists()) {
          // if segment is not exist, just wait
            for (int i = 0; i < 3; i ++) {
                segmentPath = SDCardRoot + "VideoPlayer/" + mpd.representation.get(i).segments.get(playSegmentNmb).getMedia();
                segmentFile = new File(segmentPath);
                if (segmentFile.exists()) {
                    currentPlaySegment = mpd.representation.get(i).segments.get(playSegmentNmb);
                    break;
                }
            }
        }
        Log.e(TAG, "Play segment " + playSegmentNmb);
        currentResolution = currentPlaySegment.getMedia().charAt(0);
        if (currentResolution != lastResolution) {
            Toast.makeText(PlayerActivity.this, "Change Quality", Toast.LENGTH_LONG).show();
        }

        videoView.setVideoPath(segmentPath);
        videoView.start();
        playSegmentNmb ++;
    }


    private void parseMPD() {
        uiHandler = new Handler();

        socketThread = new Thread(new Runnable() {
            @Override
            public void run() {
                ParseMPD parseMpd = new ParseMPD();
                try {
                    mpd = parseMpd.parse(mpdUri);
                } catch (DashParserException e) {
                    e.printStackTrace();
                }
            }
        });
        socketThread.start();
        try {
            socketThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class downloadSegmentThread extends Thread{
        public void run(){
            long lastDowloadTime;
            int bufferCount;

            while((requestSegmentNub < currentRepresentation.segments.size())  ) {
                lastDowloadTime = downloadTime;
                bufferCount = requestSegmentNub - playSegmentNmb;
             //   System.out.println(currentRepresentation.toString());
                if (bufferCount < 4) {
                    if (isAutoAdaptation == true) {

                        /**
                         * Choose the representation based on how much effective segments in buffer
                         * effective segment means the segment is going to play
                         */
                        if (bufferCount == 3) {
                            currentRepresentation = mpd.representation.get(2);
                        } else if (bufferCount == 2) {
                            currentRepresentation = mpd.representation.get(1);
                        } else {
                            currentRepresentation = mpd.representation.get(0);
                        }
                    }

                    Log.e(TAG, "Request segment " + requestSegmentNub);

                    SegmentDownload segmentDownloader = new SegmentDownload();
                    downloadTime = segmentDownloader.download(
                            currentRepresentation.basedUrl + currentRepresentation.segments.get(requestSegmentNub).getMedia(),
                            "VideoPlayer",currentRepresentation.segments.get(requestSegmentNub).getMedia());

                    // if failed to get the segment, request dowload again
                    if (downloadTime != -1) {
                        requestSegmentNub ++;
                    }
                }
            }
        }
    }
}
