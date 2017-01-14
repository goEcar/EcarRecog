package recognition.ecar.com.ecarrecog;

import android.app.Application;

import com.util.Consts;


/*
 *===============================================
 *
 * 文件名:${type_name}
 *
 * 描述: 
 *
 * 作者:
 *
 * 版权所有:深圳市亿车科技有限公司
 *
 * 创建日期: ${date} ${time}
 *
 * 修改人:   金征
 *
 * 修改时间:  ${date} ${time} 
 *
 * 修改备注: 
 *
 * 版本:      v1.0 
 *
 *===============================================
 */
public class RecogAppliction extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        if (!Consts.IS_DEBUG) {
            CrashHandler crashHandler = CrashHandler.getInstance();
            crashHandler.init(this);
        }
    }
}
