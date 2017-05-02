package com.safe;

import android.app.Service;
import android.content.Context;
import android.content.res.Resources;
import android.hardware.Camera;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.Helper.RecogResult;
import com.LPR;
import com.util.EncryptionUtil;
import com.util.SpUtil;
import com.util.TagUtil;
import com.utils.CityCodeUtil;
import com.utils.Consts;
import com.utils.RecogFileUtil;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import static com.Helper.RecogHelper.isPic;

public class RecogHelperSafe {
    private static final int DEFAULT_SCOPE = 85; //合格分数
    protected static RecogHelperSafe recogHelper;
    public static Context mContext;


    public Random random;


    public RecogHelperSafe(String cityName) {
        random = new Random();
        init(cityName);
    }

    //isInitConfig 是否初始化参数   相机页面一定要设为true否则无法识别
    //cityName  默认的第一个汉字 如：粤
    public static synchronized RecogHelperSafe getDefault(Context context, boolean isInitConfig, String cityName) {
        synchronized (RecogHelperSafe.class) {
            if (isInitConfig)//初始化参数
                initConfig();
            mContext = (mContext == null ? context : mContext);
            return recogHelper == null ? recogHelper = new RecogHelperSafe(cityName) : recogHelper;
        }
    }


    //初始化参数
    public static void initConfig() {
        TagUtil.showLogDebug("initConfig");
        Consts.recogingDegger = 0;
        time = System.currentTimeMillis();
        bestScope = 0;
        tempCarnum = "";
    }

    //初始化
    public boolean init(String cityName) {
        //检查sp文件是否保存过权限信息
        isGetedPermition();
        //初始化识别算法
        String sdDir = RecogFileUtil.getSdPatch(mContext);

        if (sdDir == null) {
            Toast.makeText(mContext, "找不到存储路径", Toast.LENGTH_LONG).show();
            return false;
        }

        String ImgPath = sdDir + "/testLPR/Img/";
        Consts.IMAGGE_DIR = ImgPath;
        String modelPath = sdDir + "/testLPR/models/";

        File f1 = new File(ImgPath);
        File f2 = new File(modelPath);
        if (!f1.exists()) {
            f1.mkdirs();
        }
        if (!f2.exists()) {
            f2.mkdirs();
        }

        File en = new File(modelPath + "en.model");
        File zh = new File(modelPath + "zh.model");
        File en_num = new File(modelPath + "en_num.model");

        if (!en.exists() || !en.exists() || !en_num.exists()) {
            Resources res = mContext.getResources();
            try {
                RecogFileUtil.writeModelFile(res.getAssets().open("en.model"), en);
                RecogFileUtil.writeModelFile(res.getAssets().open("en_num.model"), zh);
                RecogFileUtil.writeModelFile(res.getAssets().open("zh.model"), en_num);
//                RecogFileUtil.writeModelFile(res.openRawResource(R.raw.en), en);
//                RecogFileUtil.writeModelFile(res.openRawResource(R.raw.zh), zh);
//                RecogFileUtil.writeModelFile(res.openRawResource(R.raw.en_num), en_num);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            Log.d("tagutil", String.format("current time is in java : %d", System.currentTimeMillis()));
            LPR.init(modelPath.getBytes("GBK"), CityCodeUtil.getCityCode(cityName, mContext));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;

        }
        return true;
    }

    /**
     * 方法描述：检查权限
     * <p/>
     *
     * @param
     * @return
     */
    public boolean permitionCheck(RecogResult recogToken) {
        if (!Consts.RECOG_PERMITION) {
            //从sp文件确认
            isGetedPermition();
            if (Consts.RECOG_PERMITION) { //已经获取过权限
                recogToken.permitionSuccess();
                return Consts.RECOG_PERMITION;
            }
            //文件比对
            String sdDir = RecogFileUtil.getSdPatch(mContext);
            TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Service.TELEPHONY_SERVICE);
            try {
                String imeiSha = EncryptionUtil.getSHA(tm.getDeviceId());
                String fileSha = RecogFileUtil.getString(sdDir + Consts.SPATH);
                if (TextUtils.isEmpty(imeiSha) || TextUtils.isEmpty(fileSha) || !imeiSha.equals(fileSha)) {
                    recogToken.permitionFail();
                    Consts.RECOG_PERMITION = false;
                } else {
                    recogToken.permitionSuccess();
                    Consts.RECOG_PERMITION = true;
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                recogToken.permitionFail();
                Consts.RECOG_PERMITION = false;
            }

        }
        savePermitionInfo(Consts.RECOG_PERMITION);
        return Consts.RECOG_PERMITION;

    }


    SpUtil spUtil;

    //是否获取过权限
    public boolean isGetedPermition() {
        if (spUtil == null) {
            spUtil = new SpUtil(mContext, Consts.SP_PERMITION);
        }
        return Consts.RECOG_PERMITION =
                (boolean) spUtil.getData(Consts.IS_GETEDPERMITION, Boolean.class, false);
    }

    //保存权限状态     flag：true  已获取
    public void savePermitionInfo(boolean flag) {
        if (spUtil == null) {
            spUtil = new SpUtil(mContext, Consts.SP_PERMITION);
        }
        spUtil.save(Consts.IS_GETEDPERMITION, flag);
    }


    public static String tempCarnum = "";
    public static long time;  //开始识别时间
    public final static int MAX_DEGGER = 3;//最高前后比对次数
    public static final int MATCHING_LENG = 4; //匹配车牌长度

    //获取车牌-识别池最优匹配
//    public synchronized void getCarnum(final byte[] data, final Camera camera, final RecogResult recogToken) {
//    synchronized (recogHelper.getClass()) {

//        if (permitionCheck(recogToken)) {
//            Log.i("carnum--", "开始识别" + !isRecoging);
//            if (data != null && !isRecoging) {
//                isRecoging = true;
//                Consts.orgdata = data;
//                int width = camera.getParameters().getPreviewSize().width;
//                int height = camera.getParameters().getPreviewSize().height;
//                int location = random.nextInt(EncryptionUtil.getLocation(EncryptionUtil.MYRECOG_SCOPE));
//                if (data.length > location) {
//                    int mloc = EncryptionUtil.getLocation(EncryptionUtil.MYRECOG_LOC);
//                    int value = EncryptionUtil.getLocation(EncryptionUtil.MYRECOG_VALUE);
//                    data[mloc] = (byte) location;
//                    data[location] = (byte) value;
//                } else {
//                    recogToken.recogFail();
//                    return;
//                }
//                String platenum = "";
//                try {
//                    LPR.locate(width, height, data); //0-255
//                    platenum = new String(LPR.getplate(0), "GBK").trim();
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//
////                Log.d("number1", TextUtils.isEmpty(platenum) ? "返回空  " : platenum + "正确率="+scope);
//
//                //第一次不往下走
//                if (!isPic
//                        && TextUtils.isEmpty(tempCarnum.trim())
//                        && isNumber(platenum)) {
//                    tempCarnum = platenum.trim();
//                    recogToken.recogFail();
//                    isRecoging = false;
//                    return;
//                }
//
//                boolean getedSuccess;//车牌是否获取成功
//                if (isPic) {
//                    getedSuccess = isNumber(platenum);
//                } else {
//                    getedSuccess = isNumber(platenum) && tempCarnum.contains(platenum.substring(0, MATCHING_LENG));
//                    if (!TextUtils.isEmpty(platenum) && ++Consts.recogingDegger >= MAX_DEGGER && !getedSuccess) {
//                        //达到最高次数 则返回成功
//                        getedSuccess = true;
//                        Consts.recogingDegger = 0;
//                    }
//
//                    Log.d("number2", getedSuccess ? platenum : "返回空");
//                }
//
//                if (getedSuccess) {
//                    Consts.orgw = width;
//                    Consts.orgh = height;
//                    Consts.speed = (System.currentTimeMillis() - time) / 1000.0f;
//                    time = System.currentTimeMillis();
//                    Consts.platenum = platenum;
//                    recogToken.recogSuccess(platenum, data);
//                    tempCarnum = "";
//                } else {
//                    Consts.orgdata = null;
//                    Consts.orgw = 0;
//                    Consts.orgh = 0;
//                    recogToken.recogFail();
//
//                    tempCarnum = !TextUtils.isEmpty(platenum) ?
//                            tempCarnum.concat(platenum) :
//                            tempCarnum;
//                    Log.d("number3", "匹配" + (!TextUtils.isEmpty(platenum) ? platenum.substring(0, MATCHING_LENG) : "") + "  " + tempCarnum);
//
//                }
//                isRecoging = false;
//            }
//        }
//}
//    }


    public static int bestScope;//上一次的识别率
    byte[] bestData; //最优的data
    String currentNum;

    //获取车牌
//    public synchronized void getCarnum(final byte[] data, final Camera camera, final RecogResult recogToken) {
//    synchronized (recogHelper.getClass()) {

    //        if (permitionCheck(recogToken)) {
//            if (data != null && !isRecoging) {
//                isRecoging = true;
//                Consts.orgdata = data;
//                int width = camera.getParameters().getPreviewSize().width;
//                int height = camera.getParameters().getPreviewSize().height;
//                int location = random.nextInt(EncryptionUtil.getLocation(EncryptionUtil.MYRECOG_SCOPE));
//                if (data.length > location) {
//                    int mloc = EncryptionUtil.getLocation(EncryptionUtil.MYRECOG_LOC);
//                    int value = EncryptionUtil.getLocation(EncryptionUtil.MYRECOG_VALUE);
//                    data[mloc] = (byte) location;
//                    data[location] = (byte) value;
//                } else {
//                    recogToken.recogFail();
//                    return;
//                }
//                String platenum = "";
//                int scope = 0;
//                try {
//                    LPR.locate(width, height, data); //0-255
//                    platenum = new String(LPR.getplate(0), "GBK").trim();
//                    scope = LPR.getplatescore(0);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//
//                boolean getedSuccess;//车牌是否获取成功
//                if (isPic) {
//                    getedSuccess = isNumber(platenum);
//                } else {
//                    getedSuccess = isNumber(platenum) && ++Consts.recogingDegger >= MAX_DEGGER;
//                    if (isNumber(platenum)) {
//                        if (scope > bestScope) {  //当前可信度大于最大可信度
//                            bestScope = scope;
//                            bestData = data;
//                            currentNum = platenum;
//                        }
//                        if(Consts.IS_DEBUG)
//                        Log.d("number2", platenum + "可信度：" + scope + " 当前次数" + Consts.recogingDegger);
//                    }
//                }
//
//                if (getedSuccess) {
//                    Consts.orgw = width;
//                    Consts.orgh = height;
//                    Consts.speed = (System.currentTimeMillis() - time) / 1000.0f;
//                    time = System.currentTimeMillis();
//                    Consts.platenum = currentNum;
//
//                    if(Consts.IS_DEBUG)
//                        Log.d("number2", currentNum + "最高可信度：" + bestScope + " 当前次数" + Consts.recogingDegger);
//                    recogToken.recogSuccess(currentNum, bestData);
//                    tempCarnum = "";
//                } else {
//                    Consts.orgdata = null;
//                    Consts.orgw = 0;
//                    Consts.orgh = 0;
//                }
//                isRecoging = false;
//            }
//        }
//}
//    }
    public synchronized void getCarnum(final byte[] data, final Camera camera, final RecogResult recogToken) {
        synchronized (recogHelper.getClass()) {
            if (permitionCheck(recogToken)) {
                if (data != null) {
                    Consts.orgdata = data;
                    //加密
                    int width = camera.getParameters().getPreviewSize().width;
                    int height = camera.getParameters().getPreviewSize().height;
                    int location = random.nextInt(EncryptionUtil.getLocation(EncryptionUtil.MYRECOG_SCOPE));
                    if (data.length > location) {
                        EncryptionUtil.setPoint(data, location);
                    } else {
                        recogToken.recogFail();
                        return;
                    }

                    String platenum = "";
                    int scope = 0;
                    try {
                        LPR.locate(width, height, data); //0-255
                        platenum = new String(LPR.getplate(0), "GBK").trim();
                        scope = LPR.getplatescore(0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


//                Log.d("number1", TextUtils.isEmpty(platenum) ? "返回空  " : platenum + "正确率="+scope);

                    //第一次不往下走
                    if (!isPic
                            && TextUtils.isEmpty(tempCarnum.trim())
                            && isNumber(platenum)) {
                        tempCarnum = platenum.trim();
                        if (scope < 80) {
                            recogToken.recogFail();
                            Log.d("number2", "第一次获取 tempnum=" + tempCarnum);
                            return;
                        }

                    }

                    boolean getedSuccess;//车牌是否获取成功
                    if (isPic) {
                        getedSuccess = isNumber(platenum);
                    } else {
                        getedSuccess = isNumber(platenum) && (isScopeOk(scope) || tempCarnum.equals(platenum));
                        Log.d("number2", getedSuccess ? platenum : "返回空" + "\nplatenum=" + platenum + "\ntempnum=" + tempCarnum);
                    }

                    if (getedSuccess) {
                        Consts.orgw = width;
                        Consts.orgh = height;
                        Consts.speed = (System.currentTimeMillis() - time) / 1000.0f;
                        time = System.currentTimeMillis();
                        Consts.platenum = platenum;
                        recogToken.recogSuccess(platenum, data);
                        tempCarnum = "";
                    } else {
                        Consts.orgdata = null;
                        Consts.orgw = 0;
                        Consts.orgh = 0;
                        recogToken.recogFail();
                        tempCarnum = isNumber(platenum) ? platenum : tempCarnum;//初始化中间车牌
                    }
                }
            }
        }
    }


    /****************************************
     方法描述：使用时间限制来获取车牌
     @param  maxTime 识别时长 单位秒
     @return
     ****************************************/

    public synchronized void getCarnumByTime(final byte[] data, final Camera camera, final RecogResult recogToken, int maxTime) {
        synchronized (recogHelper.getClass()) {
            if (permitionCheck(recogToken)) {

                if (data != null) {
                    Consts.orgdata = data;
                    //加密
                    int width = camera.getParameters().getPreviewSize().width;
                    int height = camera.getParameters().getPreviewSize().height;
                    int location = random.nextInt(EncryptionUtil.getLocation(EncryptionUtil.MYRECOG_SCOPE));
                    if (data.length > location) {
                        EncryptionUtil.setPoint(data, location);
                    } else {
                        recogToken.recogFail();
                        return;
                    }

                    String platenum = "";
                    try {
                        LPR.locate(width, height, data); //0-255
                        platenum = new String(LPR.getplate(0), "GBK").trim();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }


//                Log.d("number1", TextUtils.isEmpty(platenum) ? "返回空  " : platenum + "正确率="+scope);

                    //第一次不往下走
                    if (!isPic
                            && TextUtils.isEmpty(tempCarnum.trim())
                            && isNumber(platenum)) {
                        tempCarnum = platenum.trim();
                        recogToken.recogFail();
                        Log.d("number2", "第一次获取 tempnum=" + tempCarnum);
                        return;
                    }

                    boolean getedSuccess;//车牌是否获取成功
                    if (isPic) {
                        getedSuccess = isNumber(platenum);
                    } else {
                        getedSuccess = ((isNumber(platenum) && tempCarnum.equals(platenum)))
                                || (!TextUtils.isEmpty(platenum) && (System.currentTimeMillis() - time) / 1000 >= maxTime);   //超过时间限制
                        Log.d("number2", getedSuccess ? platenum : "返回空" + "\nplatenum=" + platenum + "\ntempnum=" + tempCarnum);
                    }

                    if (getedSuccess) {
                        Consts.orgw = width;
                        Consts.orgh = height;
                        Consts.speed = (System.currentTimeMillis() - time) / 1000.0f;
                        time = System.currentTimeMillis();
                        Consts.platenum = platenum;
                        recogToken.recogSuccess(platenum, data);
                        tempCarnum = "";

                    } else {
                        Consts.orgdata = null;
                        Consts.orgw = 0;
                        Consts.orgh = 0;
                        recogToken.recogFail();
                        tempCarnum = isNumber(platenum) ? platenum : tempCarnum;//初始化中间车牌
                        Log.d("number3", "匹配" + (!TextUtils.isEmpty(platenum) ? platenum.substring(0, MATCHING_LENG) : "") + "  " + tempCarnum);
                    }
                }
            }
        }
    }

    //判断是否是车牌
    public boolean isNumber(String platenum) {
        return !TextUtils.isEmpty(platenum) && platenum.trim().length() > 3;
    }

    //成绩是否达标
    public boolean isScopeOk(int scope) {
        return scope > DEFAULT_SCOPE;
    }

    public Camera.Parameters offFlash(Camera.Parameters parameters) {

        if (parameters != null) {
            if (parameters == null) return parameters;
            String flashMode = parameters.getFlashMode();
            if (flashMode.equals(Camera.Parameters.FLASH_MODE_TORCH)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                parameters.setExposureCompensation(0);
            }
        }
        return parameters;
    }

    public Camera.Parameters openFlash(Camera.Parameters parameters) {
        if (parameters != null) {
            String flashMode = parameters.getFlashMode();
            if (!flashMode.equals(Camera.Parameters.FLASH_MODE_TORCH)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                parameters.setExposureCompensation(0);
            }
        }
        return parameters;
    }


}
