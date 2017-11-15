package com.example.fangy.videoplayer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by fangy on 2017/11/14.
 */

public class SegmentDownload {

    /**
     * @param urlStr Url of segment
     * @param path filePath to save the segment
     * @param fileName the file
     * @return responseTime
     */
    public long download(String urlStr, String path, String fileName) {
        long responseTime = 0;
        long beginTime = System.currentTimeMillis();
        try {
            FileUtils fileUtils = new FileUtils();
            if(fileUtils.isFileExist(fileName,path)) {
                return 0;
            } else {
                InputStream inputStream = getInputStreamFromUrl(urlStr);
                File resultFile=fileUtils.write2SDFromInput(fileName,path,inputStream);
                if(resultFile == null) {
                    return -1;
                }
            }
        } catch (Exception e) {
            System.out.println("error reading data" + e);
            return -1;
        }
        responseTime = System.currentTimeMillis() - beginTime;

        return responseTime;
    }

    public InputStream getInputStreamFromUrl(String urlStr)throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection urlConn = (HttpURLConnection)url.openConnection();
        InputStream inputStream = urlConn.getInputStream();
        return inputStream;
    }
}
