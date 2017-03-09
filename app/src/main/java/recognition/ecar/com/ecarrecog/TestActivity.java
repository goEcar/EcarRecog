package recognition.ecar.com.ecarrecog;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mylhyl.acp.Acp;
import com.mylhyl.acp.AcpListener;
import com.mylhyl.acp.AcpOptions;

import java.util.List;

import com.Helper.RecogHelper;
import com.util.Consts;
import com.util.RecogFileUtil;

import recognition.ecar.com.ecarrecog.anotation.DebugLog;
import tfcard.wintone.ecar.R;

@DebugLog
public class TestActivity extends Activity {


    public ImageView iv_clip;
    public TextView tv_plate;


    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().hide();
        setContentView(R.layout.activity_main);
        initPermition();


    }


    //权限管理
    private void initPermition() {
        Acp.getInstance(this).request(new AcpOptions.Builder().
                setPermissions(Manifest.permission.CAMERA)
                        .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                .setDeniedMessage()
//                .setDeniedCloseBtn()
//                .setDeniedSettingBtn()
//                .setRationalMessage()
//                .setRationalBtn()
                        .build(),
                new AcpListener() {
                    @Override
                    public void onGranted() {//开启成功
                        initData();
                    }

                    @Override
                    public void onDenied(List<String> permissions) {
                        initPermition();
                    }
                });
    }

    private void initData() {
        iv_clip = (ImageView) findViewById(R.id.iv_clip);
        tv_plate = (TextView) findViewById(R.id.tv_plate);
        Button btn_pic = (Button) findViewById(R.id.btn_pic);
        Button btn_video = (Button) findViewById(R.id.btn_video);
        Log.d("tag","initData");



        if ((bitmap= RecogFileUtil.saveBitmap())==null) {
//            return ;  //保存图片
        } else{
            iv_clip.setImageBitmap(bitmap);
        }

        tv_plate.setBackgroundColor(Color.WHITE);
        tv_plate.setTextColor(Color.RED);
        tv_plate.setText(Consts.platenum+"     速度："+Consts.speep+"s");
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
                RecogHelper.isPic = true;
                startActivity(i);
                finish();
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
                RecogHelper.isPic = false;
                startActivity(i);
                finish();
            }
        });
    }


}
