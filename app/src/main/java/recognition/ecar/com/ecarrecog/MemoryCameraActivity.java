package recognition.ecar.com.ecarrecog;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.Helper.RecogResult;
import com.ecaray.wintonlib.RecogniteHelper4WT;
import com.utils.RecogFileUtil;
import com.wintone.plateid.PlateRecognitionParameter;
import com.wintone.plateid.RecogService;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.Helper.RecogHelper;
import com.utils.Consts;
import com.utils.MemoryUtil;
import com.utils.ViewfinderView;
import recognition.ecar.com.ecarrecog.anotation.DebugLog;
import tfcard.wintone.ecar.R;



/**
 * ===============================================
 * <p>
 * 项目名称: parkBees-pda
 * <p>
 * 包: com.chmtech.parkbees.activity
 * <p>
 * 类名称: MemoryCameraActivity
 * <p>
 * 类描述:拍照识别界面
 * <p>
 * 创建人:
 * <p>
 * 创建时间: 2015-9-7 上午11:45:42
 * <p>
 * 修改人:
 * <p>
 * 修改时间: 2015-9-7 上午11:45:42
 * <p>
 * 修改备注:
 * <p>
 * 版本:
 * <p>
 * ===============================================
 */

@DebugLog
public class MemoryCameraActivity extends Activity implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private Camera mCamera;
    private SurfaceView surfaceView;
    private ViewfinderView myview;
    private RelativeLayout re;
    private int width, height;
    private TimerTask timer;
    private int preWidth = 0;
    private int preHeight = 0;
    private boolean isFatty = false;
    private SurfaceHolder holder;
    private int rotation = 90;

    private boolean isSuccess; //是否获取成功
    private Camera.Parameters parameters;
    private RecogHelper recogHelper;
    private RecogniteHelper4WT mRecogHelper;
    private long time;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recogHelper = RecogHelper.getDefault(this, true,"川");

        int uiRot = getWindowManager().getDefaultDisplay().getRotation();

        requestWindowFeature(Window.FEATURE_NO_TITLE);//
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_carmera);
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        width = metric.widthPixels; //
        height = metric.heightPixels; //

        switch (uiRot) {
            case 0:
                rotation = 90;

                break;
            case 1:
                rotation = 0;

                break;
            case 2:
                rotation = 270;

                break;
            case 3:
                rotation = 180;

                break;
        }
        findView();
        if (width * 3 == height * 4) {
            isFatty = true;
        }
        if (rotation == 90 || rotation == 270) {
            myview = new ViewfinderView(MemoryCameraActivity.this, width, height, false);
        } else {
            myview = new ViewfinderView(MemoryCameraActivity.this, width, height, true);
        }


    }

    /**
     * 文通识别
     *
     * @param data   字节数组(图片)
     * @param camera 照相机
     */
    //识别帮助类
    private int nRet = -1;

    private void useWTRecognition(byte[] data, Camera camera) {
        if (mRecogHelper.isServiceIsConnected() && data != null) {
            Consts.orgdata = data;
            nRet = mRecogHelper.getRecogBinder() != null ? mRecogHelper.getRecogBinder().getnRet() : nRet;

            int initPlateIDSDK = mRecogHelper.getInitPlateIDSDK();
            if (initPlateIDSDK == 0) {

                //识别参数设置
                PlateRecognitionParameter mPlateRecParam = new PlateRecognitionParameter();
                mPlateRecParam.picByte = data;
                mPlateRecParam.devCode = "";

                //设置识别区域
                mPlateRecParam.height = preHeight;
                mPlateRecParam.width = preWidth;
                //初始化参数
                mPlateRecParam.plateIDCfg.bRotate = 1;
                mPlateRecParam.plateIDCfg.left = 0;
                mPlateRecParam.plateIDCfg.right = 0;
                mPlateRecParam.plateIDCfg.top = 0;
                mPlateRecParam.plateIDCfg.bottom = 0;

                Log.d("code", mPlateRecParam.devCode);

                //识别开始
                RecogService.MyBinder lBinder = mRecogHelper.getRecogBinder();
                String[] mFieldValue = lBinder.doRecogDetail(mPlateRecParam);
                if (nRet != 0) {
                    String[] str = {"" + nRet};
                    mRecogHelper.getResult(str, camera, data, new Geted());
                } else {
                    mRecogHelper.getResult(mFieldValue, camera, data, new Geted());
                }
            }
        }
    }

    //初始化时间
    private void initTime() {
//        if (time == 0)
//            time = System.currentTimeMillis();
    }


    /**
     * 获取车牌号后的回调类
     */
    class Geted implements RecogniteHelper4WT.OnResult {

        @Override
        public void onGeted(String fileName, String carPlate) {
//            Consts.speed = (System.currentTimeMillis() - time) / 1000.0f;
            time = System.currentTimeMillis();
            Consts.platenum = carPlate;

            Intent intent = new Intent(MemoryCameraActivity.this, TestActivity.class);
//			            	Toast.makeText(this, "识别 " + i + "次,用时：" + (System.nanoTime() - start) / Math.pow(10, 6) + "ms", Toast.LENGTH_SHORT).show();
//				            Toast.makeText(this, "focus :" + f, Toast.LENGTH_SHORT).show();
            startActivity(intent);
            MemoryCameraActivity.this.finish();

        }

        @Override
        public String saveImage(byte[] data) {
            RecogFileUtil.saveBitmap();
            return "";
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        isSuccess = true;
        if (mCamera != null) {
//			mCamera.setPreviewCallback(MemoryCameraActivity.this);
            mCamera.setParameters(mCamera.getParameters());
            mCamera.startPreview();
            mCamera.setOneShotPreviewCallback(MemoryCameraActivity.this);
        }

    }


    @Override
    protected void onPause() {
        super.onPause();


        releaseCamera();

        recogHelper.offFlash(parameters);
        isSuccess = true;

        Intent intent = new Intent(MemoryCameraActivity.this, TestActivity.class);
        startActivity(intent);
        MemoryCameraActivity.this.finish();
    }

    private void findView() {
        findViewById(R.id.btn_save).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Consts.orgdata == null || mCamera == null) {
                    Toast.makeText(MemoryCameraActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                Consts.orgw = mCamera.getParameters().getPreviewSize().width;
                Consts.orgh = mCamera.getParameters().getPreviewSize().height;
                Toast.makeText(MemoryCameraActivity.this, "正在保存~", Toast.LENGTH_SHORT).show();
                RecogFileUtil.saveBitmap();
                Toast.makeText(MemoryCameraActivity.this, "保存成功！", Toast.LENGTH_SHORT).show();
            }
        });

        surfaceView = (SurfaceView) findViewById(R.id.surfaceViwe_video);
        re = (RelativeLayout) findViewById(R.id.memory);
        // setButton();
        holder = surfaceView.getHolder();
        holder.addCallback(MemoryCameraActivity.this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        if (RecogHelper.isPic) {
            findViewById(R.id.memory).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Consts.orgdata == null || mCamera == null) {
                        Toast.makeText(MemoryCameraActivity.this, "识别失败，请再按一次", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    surfaceView.setClickable(false);
                    recogHelper.getCarnum(Consts.orgdata, mCamera, new RecogResult() {
                        @Override
                        public void recogSuccess(String carPlate, byte[] picData) {
                            Intent intent = new Intent(MemoryCameraActivity.this, TestActivity.class);
//			            	Toast.makeText(this, "识别 " + i + "次,用时：" + (System.nanoTime() - start) / Math.pow(10, 6) + "ms", Toast.LENGTH_SHORT).show();
//				            Toast.makeText(this, "focus :" + f, Toast.LENGTH_SHORT).show();
                            startActivity(intent);
                            MemoryCameraActivity.this.finish();
                        }

                        @Override
                        public void recogFail() {
//                            Toast.makeText(MemoryCameraActivity.this, "识别失败 -1", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void permitionSuccess() {

                        }

                        @Override
                        public void permitionFail() {
                            Toast.makeText(MemoryCameraActivity.this, "获取权限失败！", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }
            });
        }

    }


    @Override
    protected void onDestroy() {

        super.onDestroy();
        if (Consts.IS_WENTONG)
            mRecogHelper.unbindService();
    }

    /**
     * 方法描述：相机滞空
     * <p>
     *
     * @param
     * @return
     */
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        if (timer != null) {
            timer.cancel();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        if (mCamera == null) {
            try {
                mCamera = Camera.open();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        if (Consts.IS_WENTONG) {
            mRecogHelper = RecogniteHelper4WT.getInstance(MemoryCameraActivity.this, mCamera);
            mRecogHelper.bindRecogService();
        }
        try {
            mCamera.setPreviewDisplay(holder);
            initCamera(holder, rotation);
            re.removeView(myview);
            re.addView(myview);
            Timer time = new Timer();
            if (timer != null) {
                timer.cancel();
            }
            timer = new TimerTask() {
                public void run() {
                    if (mCamera != null) {
                        focus();
                    }
                }
            };
            time.schedule(timer, 10, 800);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void focus() {
        if (mCamera != null && isSuccess) {
            isSuccess = false;
            try {
                mCamera.autoFocus(new AutoFocusCallback() {
                    public void onAutoFocus(boolean success, Camera camera) {
                        isSuccess = true;
                        if (success) {
                            camera.setOneShotPreviewCallback(MemoryCameraActivity.this);
                        }
                    }
                });
            } catch (Exception e) {
                isSuccess = true;
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onPreviewFrame(byte[] data, final Camera camera) {
        initTime();
        Consts.orgdata = data;

        if (RecogHelper.isPic) {
            return;
        }
        if (Consts.IS_WENTONG) {
            useWTRecognition(data, camera);

        } else {
//        camera.stopPreview();
            recogHelper.getCarnum(data, camera, new RecogResult() {
                @Override
                public void recogSuccess(String carPlate, byte[] picData) {

                    //识别一次
                    Intent intent = new Intent(MemoryCameraActivity.this, TestActivity.class);
//			         	Toast.makeText(this, "识别 " + i + "次,用时：" + (System.nanoTime() - start) / Math.pow(10, 6) + "ms", Toast.LENGTH_SHORT).show();
//				        Toast.makeText(this, "focus :" + f, Toast.LENGTH_SHORT).show();
                    startActivity(intent);
                    MemoryCameraActivity.this.finish();

                    //无限次识别
//                recogHelper=RecogHelperSafe.getDefault(MemoryCameraActivity.this,true);
//                Toast.makeText(MemoryCameraActivity.this, "车牌号 ="+carPlate, Toast.LENGTH_SHORT).show();

                }

                @Override
                public void recogFail() {
//                Toast.makeText(MemoryCameraActivity.this, "识别失败 -1", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void permitionSuccess() {

                }

                @Override
                public void permitionFail() {
                    Toast.makeText(MemoryCameraActivity.this, "获取权限失败！", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
            Log.i("recog=", "可用内存=" + MemoryUtil.getmem_UNUSED(this) + "\n总内存=" + MemoryUtil.getmem_TOLAL());
        }
    }


    @Override
    public void surfaceChanged(final SurfaceHolder holder, int format, int width, int height) {

        if (mCamera != null) {
            mCamera.autoFocus(new AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if (success) {
                        synchronized (camera) {
                            new Thread() {
                                public void run() {
                                    initCamera(holder, rotation);
                                    super.run();
                                }
                            }.start();
                        }
                    }
                }
            });
        }

    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            if (mCamera != null) {
                mCamera.setPreviewCallback(null);
                mCamera.cancelAutoFocus();
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;

            }
        } catch (Exception e) {
        }

    }

    /**
     * @throws
     * @Title: initCamera
     */
    @TargetApi(14)
    private void initCamera(SurfaceHolder holder, int r) {
        parameters = mCamera.getParameters();
        List<Camera.Size> list = parameters.getSupportedPreviewSizes();
        Camera.Size size;
        int length = list.size();
        int previewWidth = 480;
        int previewheight = 640;
        int second_previewWidth;
        int second_previewheight;
        if (length == 1) {
            size = list.get(0);
            previewWidth = size.width;
            previewheight = size.height;
        } else {
            for (int i = 0; i < length; i++) {
                size = list.get(i);
                if (isFatty) {
                    if (size.height <= 960 || size.width <= 1280) {
                        second_previewWidth = size.width;
                        second_previewheight = size.height;
                        previewWidth = second_previewWidth;
                        previewheight = second_previewheight;
                    }
                } else {
                    if (size.height <= 960 || size.width <= 1280) {
                        second_previewWidth = size.width;
                        second_previewheight = size.height;
                        if (previewWidth <= second_previewWidth) {
                            previewWidth = second_previewWidth;
                            previewheight = second_previewheight;
                        }
                    }
                }
            }
        }
        preWidth = previewWidth;
        preHeight = previewheight;
        parameters.setPictureFormat(PixelFormat.JPEG);
        parameters.setPreviewSize(preWidth, preHeight);
        recogHelper.openFlash(parameters);
        parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT);
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        mCamera.setParameters(parameters);
        if (rotation == 90 || rotation == 270) {
            if (width < 1080) {
                mCamera.stopPreview();
            }
        } else {
            if (height < 1080) {
                mCamera.stopPreview();
            }
        }

        mCamera.setDisplayOrientation(r);

        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
        if (rotation == 90 || rotation == 270) {
            if (width < 1080) {
            }
        } else {
            if (height < 1080) {
            }
        }
    }


}
