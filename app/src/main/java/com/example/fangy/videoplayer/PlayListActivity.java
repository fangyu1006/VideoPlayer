package com.example.fangy.videoplayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by fangy on 2017/11/14.
 */

public class PlayListActivity extends Activity{
    private final String TAG = "PlayListActivity";
    private final String tag = "PlayListActivity";
    private String[] result = null;
    String tempResult = null;
    String modifiedString = null;

    JSONArray jArray;
    private static String resulttemp = null;
    private static String URL_PATH = "https://monterosa.d2.comp.nus.edu.sg/~team03/postlist.php";
    private ListView mListView;
    private ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        // StartRequestFromPHP();
        modifiedString = StringtoArray();

        result = tranferString(modifiedString).split(",");
        mListView = (ListView) findViewById(R.id.listView);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, result);
        mListView.setAdapter(arrayAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String filename = (String) parent.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(), filename, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(PlayListActivity.this, PlayerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("fileName", filename);
                intent.putExtras(bundle);
                Log.e(TAG, "Start PlayerActivity");
                startActivity(intent);
            }
        });

    }

    private String tranferString (String str){
        String a = null;
        String b = null;
        a = str.replace("][",",");
        b = a.replace("\"","");
        a = b.replace(":","");
        b = a.replace("[","");
        a = b.replace("{","");
        b = a.replace("}","");
        a = b.replace("]","");
        b = a.replace("name","");
        return b;
    }

    private String StringtoArray() {
        String result = "";

        ExecutorService executor = Executors.newSingleThreadExecutor();
        FutureTask<String> future =
                new FutureTask<String>(new Callable<String>() {//使用Callable接口作为构造参数
                    public String call() {

                        try {
                            //Thread.sleep(10000);
                            tempResult = getInputStream(tag,URL_PATH);
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            //exception = e;
                            e.printStackTrace();
                        }
                        return "";
                    }});
        executor.execute(future);

        try {
            result = future.get(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            future.cancel(true);
        } catch (ExecutionException e) {
            future.cancel(true);
        } catch (TimeoutException e) {
            future.cancel(true);
        } finally {
            executor.shutdown();
        }

        return tempResult;
    }

    public String getInputStream(String tag,String path) {
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;

        try {
            URL url = new URL(path);
            if (null != url) {
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setRequestMethod("GET");
                if (200 == httpURLConnection.getResponseCode()) {
                    inputStream = httpURLConnection.getInputStream();
                    StringBuffer sb = new StringBuffer();
                    int ss;
                    while ((ss = inputStream.read() )!= -1) {
                        sb.append((char) ss);
                    }
                    resulttemp = sb.toString();

                    inputStream.close();
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return resulttemp;
    }
}
