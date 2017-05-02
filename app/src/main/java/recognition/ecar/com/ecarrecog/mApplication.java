package recognition.ecar.com.ecarrecog;

import android.app.Application;

import bugly.ecar.com.ecarbuglylib.BuildConfig;
import bugly.ecar.com.ecarbuglylib.util.BuglyUtil;

/*************************************
 功能：
 创建者： kim_tony
 创建日期：2017/4/21
 版权所有：深圳市亿车科技有限公司
 *************************************/

public class mApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        BuglyUtil.init(this, "c32b92f67e", false,BuildConfig.VERSION_NAME,false); // appid初始化
    }
}
