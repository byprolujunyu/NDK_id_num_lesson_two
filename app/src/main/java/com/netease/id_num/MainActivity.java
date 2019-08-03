package com.netease.id_num;

import android.Manifest;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("OpenCV");
    }

    private TessBaseAPI tessBaseApi;
    private String language = "ck";
    private AsyncTask<Void, Void, Boolean> asyncTask;
    private ProgressDialog progressDialog;////////////////
    private ImageView idCard;
    private TextView tesstext;
    private int index = 0;
    private int[] ids = {
            R.drawable.id_card0,
            R.drawable.id_card1,
            R.drawable.id_card2,
            R.drawable.id_card3,
            R.drawable.id_card4
    };


    @Override
    protected void onResume() {
        super.onResume();
        ActivityCompat.
                requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
                                , Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
    }

    Button previous;
    Button next;
    Button rt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        idCard = (ImageView) findViewById(R.id.idcard);
        tesstext = (TextView) findViewById(R.id.tesstext);
        idCard.setImageResource(R.drawable.id_card0);
        //15
        previous = findViewById(R.id.previous);
        previous.setOnClickListener(this);
        next = findViewById(R.id.next);
        next.setOnClickListener(this);
        rt = findViewById(R.id.rt);
        rt.setOnClickListener(this);
        tessBaseApi = new TessBaseAPI();/////////////
        initTess();
    }

    private void initTess() {
        //让它在后台去初始化 记得加读写权限
        asyncTask = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                //目录+文件名 目录下需要tessdata目录
                InputStream is = null;
                FileOutputStream fos = null;
                try {
                    is = getAssets().open(language + ".traineddata");
                    File file = new File("/sdcard/tess/tessdata/" + language + ".traineddata");
                    if (!file.exists()) {
                        file.getParentFile().mkdirs();
                        fos = new FileOutputStream(file);
                        byte[] buffer = new byte[2048];
                        int len;
                        while ((len = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, len);
                        }
                        fos.close();
                    }
                    is.close();
                    return tessBaseApi.init("/sdcard/tess", language);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (null != is)
                            is.close();
                        if (null != fos)
                            fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }

            @Override
            protected void onPreExecute() {
                showProgress();
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                dismissProgress();
                if (aBoolean) {
                    Toast.makeText(MainActivity.this, "初始化OCR成功", Toast.LENGTH_SHORT).show();
                } else {
                    finish();
                }
            }
        };
        asyncTask.execute();
    }

    private void showProgress() {
        if (null != progressDialog) {
            progressDialog.show();
        } else {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("请稍候...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
    }

    private void dismissProgress() {
        if (null != progressDialog) {
            progressDialog.dismiss();
        }
    }


    private native Bitmap findIdNumber(Bitmap bitmap, Bitmap.Config argb8888);

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rt:
                //rt.setClickable(false);
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), ids[index]);
                Bitmap idNumber = findIdNumber(bitmap, Bitmap.Config.ARGB_8888);
                bitmap.recycle();
                if (idNumber != null) {

                }
                //idCard.setImageBitmap(idNumber);
                else {
                    idNumber.recycle();
                    return;
                }
                //OCR文字识别
                //14 用之前得先初始化
                //15 文字识别
                tessBaseApi.setImage(idNumber);
                tesstext.setText(tessBaseApi.getUTF8Text());
             //   rt.setClickable(true);
                break;

            case R.id.previous:
                //revious.setClickable(false);
                tesstext.setText(null);
                index--;
                if (index < 0) {
                    index = ids.length - 1;
                }
                idCard.setImageResource(ids[index]);
             //   previous.setClickable(true);
                break;

            case R.id.next:

                tesstext.setText(null);
                index++;
                if (index >= ids.length) {
                    index = 0;
                }
                idCard.setImageResource(ids[index]);

                break;
        }

    }
}
