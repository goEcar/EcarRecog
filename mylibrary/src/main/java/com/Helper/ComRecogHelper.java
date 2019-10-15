package com.Helper;
/*
 *===============================================
 *
 * 文件名:${type_name}
 *
 * 描述:
 *
 * 作者:
 *
 *
 *
 * 创建日期: ${date} ${time}
 *
 *
 *
 * 修改时间:  ${date} ${time}
 *
 * 修改备注:
 *
 * 版本:      v1.0
 *
 *===============================================
 */

import android.app.Application;
import android.content.Context;
import android.hardware.Camera;
import android.util.Log;

import com.hd.lpr.lpr.liblpr;
import com.safe.RecogHelperSafe;
import com.utils.Consts;
import com.utils.RecogFileUtil;

public class ComRecogHelper {
    protected static ComRecogHelper recogHelper;
    static RecogHelperSafe recogHelperSafe;
    public static boolean isPic; //是否是照相模式


    public ComRecogHelper() {

    }

    //isInitConfig 是否初始化参数   相机页面一定要设为true否则无法识别
    //cityName  默认的第一个汉字 如：粤
    public static synchronized ComRecogHelper getDefault(Application context, boolean isInitConfig, String cityName) {
        synchronized (ComRecogHelper.class) {
            recogHelperSafe = RecogHelperSafe.getDefault(context, isInitConfig, cityName);
            return recogHelper == null ? recogHelper = new ComRecogHelper() : recogHelper;
        }
    }
    //isInitConfig 是否初始化参数   相机页面一定要设为true否则无法识别
    //cityName  默认的第一个汉字 如：粤
    //isCheckRecog  true 检查权限
    public static synchronized ComRecogHelper getDefault(Application context, boolean isInitConfig, String cityName, boolean isCheckRecog) {
        Consts.IS_CHECK_PERMITION = isCheckRecog;
        synchronized (ComRecogHelper.class) {
            recogHelperSafe = RecogHelperSafe.getDefault(context, isInitConfig, cityName);
            return recogHelper == null ? recogHelper = new ComRecogHelper() : recogHelper;
        }
    }
    //isInitConfig 是否初始化参数   相机页面一定要设为true否则无法识别
    //cityName  默认的第一个汉字 如：粤
    //isCheckRecog  true 检查权限    int maxwidth, int maxheight 初始化车牌识别，输入 最大宽度 高度，密码 (密码暂时随意输入一个)
    public static synchronized ComRecogHelper getDefaultNANjing(Context context, boolean isInitConfig, String cityName, boolean isCheckRecog,int maxwidth, int maxheight) {
        Consts.IS_CHECK_PERMITION = isCheckRecog;
        NANJing_Init(context, maxwidth,  maxheight);
        return recogHelper == null ? recogHelper = new ComRecogHelper() : recogHelper;
    }
    //是否获取过权限
    private boolean isGetedPermition() {
        return recogHelperSafe.isCheckPermition();
    }

    //保存权限状态     flag：true  已获取
    private void savePermitionInfo(boolean flag) {
        recogHelperSafe.savePermitionInfo(flag);
    }


    public synchronized void getCarnum(final byte[] data, final Camera camera, final RecogResult recogToken) throws  Exception {
        recogHelperSafe.getCarnum(data, camera, recogToken);
    }
    public static void   NANJing_Init(Context context,int maxwidth, int maxheight ){
        String  modelpath  = RecogFileUtil.getPath("carbands.bin", context);
        liblpr.InitEnv(context ,modelpath ) ;
        int nRet = liblpr.InitLpr(maxwidth,maxheight,null);
    }

    /**
     *     //			Rectposnum: 识别个数  1   Type:像素类型 2

     */
    public synchronized void getNANjingCarnum(byte[] data,int Preview_width, int Preview_height, int type,  int rectposnum,String place, int []detectrect, final YYRecogResult recogToken) throws  Exception {

        int nplatenum =  liblpr.LprVideo(data ,Preview_width ,Preview_height ,type,detectrect,rectposnum, 0 ,0,place);
        if(nplatenum > 0)
        {
            recogToken.recogSuccess();
        }else{
            recogToken.recogFail();
        }

    }
    /****************************************
     方法描述：使用时间限制来获取车牌
     @param  maxTime 识别时长 单位秒
     @return
     ****************************************/

    public synchronized void getCarnumByTime(final byte[] data, final Camera camera, final RecogResult recogToken, int maxTime) {
      //  recogHelperSafe.getCarnumByTime(data, camera, recogToken, maxTime);
    }

    //判断是否是车牌
    private boolean isNumber(String platenum) {
        return recogHelperSafe.isNumber(platenum);
    }


    public Camera.Parameters offFlash(Camera.Parameters parameters) {

        return recogHelperSafe.offFlash(parameters);
    }

    public Camera.Parameters openFlash(Camera.Parameters parameters) {
        return recogHelperSafe.openFlash(parameters);
    }


}
