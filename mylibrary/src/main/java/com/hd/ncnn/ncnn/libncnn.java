package com.hd.ncnn.ncnn;

public class libncnn {
    static {
        System.loadLibrary("ncnnlib");
    }
    public native static int  InitDetect(String modeltxt ,String models);
    public native static int  InitDetectEx(String modeltxt ,String models);

    public native static int  DetectObjct (byte []pImg ,int width ,int height, int [] DetectResult );
    public native static int  DetectObjctEx (byte []pImg ,int width ,int height, int [] DetectResult );
    public native static  int  ReleaseDetect( );

    public static  int  InitNcnn(String modeltxt ,String models)
    {
        return InitDetect(modeltxt ,models);
    }

    public static  int  InitNcnnEx(String modeltxt ,String models)
    {
        return InitDetectEx(modeltxt ,models);
    }


}
