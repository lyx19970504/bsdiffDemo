package com.example.bsdiffdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("native-lib");
    }

    private static final String TAG = "MainActivity";
    public static final String TYPE = "application/vnd.android.package-archive";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    @SuppressLint("StaticFieldLeak")
    public void update(View view) {
        PackageManager packageManager = getPackageManager();
        try {
            PackageInfo info = packageManager.getPackageInfo(getPackageName(), 0);

            String versionName = info.versionName;
            if(versionName.equals("1.0")) {
                //模拟网络下载差分包
                new AsyncTask<Void, Void, File>() {
                    @Override
                    protected File doInBackground(Void... voids) {
                        String oldApk = getApplicationInfo().sourceDir;   //获取当前app（旧版）的apk的路径

                        /**
                         * 这里是模拟下载patch的网络请求过程, 其实演示的时候是直接用bsdiff命令差分
                         * old.apk和new.apk，得到patch文件后，使用命令adb push patch storage/emulated/0
                         * 直接将patch包放到外存目录下
                         */
                        try {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "正在下载更新包", Toast.LENGTH_SHORT).show();
                                }
                            });
                            Thread.sleep(5000);  //模拟网络下载
                            String patch = new File(Environment.getExternalStorageDirectory(), "patch").getAbsolutePath();
                            String newApk = createApk();
                            getNewApk(oldApk, newApk, patch);
                            return new File(newApk);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(File file) {
                        if (file != null && file.exists()) {
                            //发送安装apk的intent
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            Uri fileUri;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                fileUri = FileProvider.getUriForFile(MainActivity.this,
                                        getApplicationInfo().packageName + ".fileprovider", file);
                            } else {
                                fileUri = Uri.fromFile(file);
                            }
                            intent.setDataAndType(fileUri, TYPE);
                            startActivity(intent);
                        } else {
                            Log.d(TAG, "onPostExecute: " + "文件不存在!");
                        }
                    }
                }.execute();
            }else{
                Toast.makeText(MainActivity.this, "当前已是最新版本:" + versionName,Toast.LENGTH_SHORT).show();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param oldPath:旧APK的路径
     * @param newPath:新APK的路径
     * @param patch:差分包路径
     */
    public native void getNewApk(String oldPath, String newPath, String patch);

    private String createApk(){
        File file = new File(Environment.getExternalStorageDirectory(), "new.apk");
        if(!file.exists()){
            try {
                boolean success = file.createNewFile();
                if(success){
                    Log.d(TAG, "createApk: " + "成功");
                }else{
                    Log.d(TAG, "createApk: " + "失败");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file.getAbsolutePath();
    }
}
