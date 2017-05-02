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

import android.content.Context;
import android.hardware.Camera;

import com.safe.RecogHelperSafe;
import com.utils.Consts;

import static com.safe.RecogHelperSafe.tempCarnum;
import static com.safe.RecogHelperSafe.time;

public class RecogHelper {
    protected static RecogHelper recogHelper;
    static RecogHelperSafe recogHelperSafe;
    public static boolean isPic; //是否是照相模式


    public RecogHelper(String cityName) {
    }

    //isInitConfig 是否初始化参数   相机页面一定要设为true否则无法识别
    //cityName  默认的第一个汉字 如：粤
    public static synchronized RecogHelper getDefault(Context context, boolean isInitConfig, String cityName) {
        synchronized (RecogHelper.class) {
            recogHelperSafe = RecogHelperSafe.getDefault(context, isInitConfig, cityName);
            return recogHelper == null ? recogHelper = new RecogHelper(cityName) : recogHelper;
        }
    }


    //初始化
    private boolean init(String cityName) {
        return recogHelperSafe.init(cityName);
    }

    /**
     * 方法描述：检查权限
     * <p/>
     *
     * @param
     * @return
     */
    private boolean permitionCheck(RecogResult recogToken) {
        return recogHelperSafe.permitionCheck(recogToken);

    }


    //是否获取过权限
    private boolean isGetedPermition() {
        return recogHelperSafe.isGetedPermition();
    }

    //保存权限状态     flag：true  已获取
    private void savePermitionInfo(boolean flag) {
        recogHelperSafe.savePermitionInfo(flag);
    }


    public synchronized void getCarnum(final byte[] data, final Camera camera, final RecogResult recogToken) {
        recogHelperSafe.getCarnum(data, camera, recogToken);
    }

    /****************************************
     方法描述：使用时间限制来获取车牌
     @param  maxTime 识别时长 单位秒
     @return
     ****************************************/

    public synchronized void getCarnumByTime(final byte[] data, final Camera camera, final RecogResult recogToken, int maxTime) {
        recogHelperSafe.getCarnumByTime(data, camera, recogToken, maxTime);
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
