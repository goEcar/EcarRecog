package com.hd.lpr.lpr;
////调用方式从参考demo中的调用，
// demo中回调在主程序中实现 ，详见 MainActivity.java

public class liblpr {
    static {
        System.loadLibrary("lprlib");
    }


    //回调函数类型
    public  void PutResult(byte []pImg, int width, int height, int nLen){

        System.out.println("call PutResult");

        String plate = LprResult.getplatenum();
        String platecolor = LprResult.getplatecolor();
        String carband =  LprResult.getcarband();
    }


    //public native int  InitEnv( libastnet pObj);
    public RecResult LprResult = new RecResult();

    //初始化环境变量，模型路径， 类句柄
    public native static int InitEnv(Object pboj, String modelpath);

    //初始化车牌识别，输入 最大宽度 高度，密码 (密码暂时随意输入一个)
    public native static int InitLpr(int maxwidth, int maxheight, String password);

    //初始化人脸检测，暂不可用
    public native static int InitFaceDetect(int maxwidth, int maxheight, String password);

	//单帧车牌识别 ，输入 BGR图像， 图像的宽度  高度 ， 数据类型 ，区域坐标 []detectrect 排列为 left  top right bottom
    //返回识别到车牌的个数 ，如果>0 , 循环调用 下面的四个函数来获取结果
    public native static int LprFrame(byte[] pImg, int width, int height, int type, int []detectrect);

    //获取识别到的第index个车牌号码
    public native static String GetPlatenum(int index);

    //获取识别到的第index个车牌颜色
    public native static String GetPlatecolor(int index);

    //获取识别到的第index个车牌置信度
    public native static float GetPlateConf(int index);

    //获取识别到的第index个车牌坐标
    public native static int GetPlateRect(int index, int []platerect);

	//视频识别车牌 ，输入 BGR图像，宽度 高度，类型 ，  识别区域，  参数中经纬度 和地址是透传数据，不需要传入0 或者任意值，
    //识别到较为稳定结果后会触发回调函数
    //			Rectposnum: 识别个数
    public native static int LprVideo(byte[] pImg, int width, int height, int type, int []detectrect, int rectposnum, double longitude, double latitude, String place);
}

