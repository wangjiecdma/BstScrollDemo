package com.smoyan.bstscrolldemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.Arrays;

public class SDCardReceive extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        Log.d("test","receive sdcard mounted");
        if (intent.getAction().equals("android.intent.action.MEDIA_MOUNTED")){

            String sourcePath = clearStr(intent.getDataString(), "file://") + File.separator;

            String filePath =sourcePath+ File.separator + "bst.apk";

            File file = new File(filePath);
            Log.d("test","file path :"+filePath);
            if (file.exists()){
                Intent intentUpdate = new Intent(Intent.ACTION_VIEW);
                intentUpdate.setDataAndType(Uri.parse("file://"+filePath),"application/vnd.android.package-archive");
                intentUpdate.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intentUpdate);
                Log.d("test","start activity for update ");
            }else {
                Log.d("test","file not exits ");
            }
        }

    }

    private static String clearStr(String str, String clearStr) {
        byte[] bytes = str.getBytes();
        byte[] clearStrBytes = clearStr.getBytes();
        byte[] StrTitleBytes = new byte[clearStrBytes.length];
        System.arraycopy(bytes, 0, StrTitleBytes, 0, StrTitleBytes.length);
        byte[] newByteTmp = null;
        if (Arrays.equals(clearStrBytes, StrTitleBytes)) {
            newByteTmp = new byte[bytes.length - clearStrBytes.length];
            System.arraycopy(bytes, clearStrBytes.length, newByteTmp, 0, newByteTmp.length);
        }
        if (newByteTmp != null) {
            return new String(newByteTmp);
        } else {
            return str;
        }
    }
}
