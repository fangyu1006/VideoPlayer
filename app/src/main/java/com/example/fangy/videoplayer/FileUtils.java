package com.example.fangy.videoplayer;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by fangy on 2017/11/14.
 */

public class FileUtils {
    private String SDCardRoot;

    public FileUtils(){
        SDCardRoot =  Environment.getExternalStorageDirectory()+ File.separator;
    }

    public File createFileInSDCard(String fileName,String dir) throws IOException {
        Log.e("fileUrils", "" + fileName);
        File file = new File(SDCardRoot + dir + File.separator + fileName);
        file.createNewFile();
        return file;
    }

    public File createSDDir(String dir)throws IOException {
        File dirFile = new File(SDCardRoot+dir);
        try {
            if (!dirFile.exists()) {
                dirFile.mkdir();
            }
        } catch (Exception e) {
            Log.e("file", "!!dir create failed");
            }
        return dirFile;
    }

    public boolean isFileExist(String fileName,String dir){
        File file = new File(SDCardRoot+dir+File.separator+fileName);
        return file.exists();
    }

    public File write2SDFromInput(String fileName,String dir,InputStream input){
        File file = null;
        OutputStream output = null;
        try {
            createSDDir(dir);
            Log.e("fileUrils", "" + dir);
            file = createFileInSDCard(fileName,dir);
            Log.e("fileUrils", "" + fileName);
            output = new FileOutputStream(file);
            byte buffer[] = new byte[4*1024];
            int temp;
            while((temp = input.read(buffer)) != -1){
                output.write(buffer,0,temp);
            }
            output.flush();
        } catch (Exception e) {
            System.out.println("writing data error："+e);
        }
        finally{
            try {
                output.close();
            } catch (Exception e2) {
                System.out.println(e2);
            }
        }
        return file;
    }
}
