package recognition.ecarrecog;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.Helper.ComRecogHelper;
import com.ecaray.wintonlib.WintonRecogManager;
import com.ecaray.wintonlib.helper.AuthHelper;
import com.mine.recog.BuildConfig;
import com.mine.recog.R;
import com.mylhyl.acp.Acp;
import com.mylhyl.acp.AcpListener;
import com.mylhyl.acp.AcpOptions;
import com.utils.Consts;
import com.utils.RecogFileUtil;

import java.util.List;

import recognition.Helper.PicSaveHelper;

public class TestActivity extends Activity {


    public ImageView iv_clip;
    public TextView tv_plate;
    public TextView tv_versionNum;


    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPermition();

    }

    //权限管理
    private void initPermition() {
        Acp.getInstance(this).request(new AcpOptions.Builder().
                        setPermissions(Manifest.permission.CAMERA)
                        .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .build(),
                new AcpListener() {
                    @Override
                    public void onGranted() {//开启成功
                        init();
                    }

                    @Override
                    public void onDenied(List<String> permissions) {
                        initPermition();
                    }
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        Log.d("tag", "init");
        if ((bitmap = RecogFileUtil.saveBitmap(true)) != null) {
            iv_clip.setImageBitmap(bitmap);
        } else {
            Consts.platenum = "";
            Consts.speed = 0;
            iv_clip.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_launcher));
        }

        tv_plate.setText(Consts.platenum + "     速度：" + Consts.speed + "s");
        Consts.orgdata = null;
    }

    private void init() {

        iv_clip = (ImageView) findViewById(R.id.iv_clip);
        tv_plate = (TextView) findViewById(R.id.tv_plate);
        tv_versionNum = (TextView) findViewById(R.id.appVersionNum);
        Button btn_pic = (Button) findViewById(R.id.btn_pic);
        Button btn_video = (Button) findViewById(R.id.btn_video);

        tv_versionNum.setText("版本号: " + BuildConfig.VERSION_NAME);

        final Button btn_cast = (Button) findViewById(R.id.btn_cast);  //转换按钮
        btn_cast.setVisibility(BuildConfig.isShowCast ? View.VISIBLE : View.GONE);
        btn_cast.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_cast.setClickable(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        PicSaveHelper.getInstance().begin(TestActivity.this, new PicSaveHelper.Result() {
                            @Override
                            public void current(final String count) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        btn_cast.setText("正在处理...(" + count + ")");
                                    }
                                });
                            }

                            @Override
                            public void none() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        btn_cast.setText("分类");
                                        btn_cast.setClickable(true);
                                        Toast.makeText(TestActivity.this, "当前没有可处理图片", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }

                            @Override
                            public void success() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        btn_cast.setText("分类");
                                        btn_cast.setClickable(true);

                                    }
                                });

                            }
                        });
                        WintonRecogManager.getInstance().unBind(TestActivity.this, true);
                    }
                }).start();

            }
        });
        tv_plate.setBackgroundColor(Color.WHITE);
        tv_plate.setTextColor(Color.RED);
        btn_pic.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                iv_clip.setImageBitmap(null);
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                    bitmap = null;
                }
                tv_plate.setText("");
                Intent i = new Intent(TestActivity.this, MemoryCameraActivity.class);
                i.putExtra("pic", true);
                ComRecogHelper.isPic = true;
                startActivityForResult(i, 0);
            }
        });
        btn_video.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                iv_clip.setImageBitmap(null);
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                    bitmap = null;
                }
                tv_plate.setText("");
                Intent i = new Intent(TestActivity.this, MemoryCameraActivity.class);
                ComRecogHelper.isPic = false;
                startActivityForResult(i, 0);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.exit(0);
    }
}
