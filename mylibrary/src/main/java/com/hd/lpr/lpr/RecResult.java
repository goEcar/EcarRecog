package com.hd.lpr.lpr;


//车牌识别结果结构体

public class RecResult {




    String platenum ;

    public String getplatenum()
    {
        return platenum ;
    }
    public void setplatenum(String szplatenum){
        this.platenum = szplatenum;
    }

    String platecolor ;
    public String getplatecolor()
    {
        return platecolor ;
    }
    public void setplatecolor(String szplatecolor){
        this.platecolor = szplatecolor;

    }

    String  carbands;



    public String getcarband()
    {
        return carbands ;
    }
    public void setcarband(String carband){
        this.carbands = carband;
    }

    int    plateleft ;
    int    plateright ;
    int    platetop ;
    int    platebottom ;


    public int getplateleft ()
    {
        return plateleft ;
    }

    public int getplatetop ()
    {
        return platetop ;
    }
    public int getplateright ()
    {
        return plateright ;
    }
    public int getplatebottom ()
    {
        return platebottom ;
    }

    float  platereal ;
    float  carbandreal ;

    public float  getplatereal ()
    {
        return platereal ;
    }

    public float  getcarbandreal ()
    {
        return carbandreal ;
    }

}

