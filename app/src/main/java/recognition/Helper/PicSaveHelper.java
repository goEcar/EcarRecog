package recognition.Helper;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import com.ecaray.wintonlib.WintonRecogManager;
import com.ecaray.wintonlib.helper.RecogniteHelper4WT;
import com.utils.RecogFileUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.safe.RecogHelperSafe.mContext;


/*************************************
 功能：
 创建者： kim_tony
 创建日期：2017/5/16
 版权所有：深圳市亿车科技有限公司
 *************************************/

public class PicSaveHelper {
    static PicSaveHelper picSaveHelper = new PicSaveHelper();
    String mainPath; //原路径
    String samePath;  //相同图片路径
    String difPath;   //不同图片路径

    private PicSaveHelper() {
        initPaths();
    }

    private void initPaths() {
        String sdDir = RecogFileUtil.getSdPatch(mContext);
        mainPath = sdDir + "/testLPR/Img/";

        samePath = sdDir + "/testLPR/Img/相同/";
        difPath = sdDir + "/testLPR/Img/异常/";

        File sameFile = new File(samePath);
        File difFIle = new File(difPath);

        if (!sameFile.exists()) {
            sameFile.mkdirs();
        }
        if (!difFIle.exists()) {
            difFIle.mkdirs();
        }
    }

    public static PicSaveHelper getInstance() {

        return picSaveHelper;
    }

    //开始扫描处理图片
    public void begin(Activity activity, Result result) {
        initPaths();
        List<File> paths = getAllFiles(mainPath);
        if (paths == null || paths.isEmpty()) {
            Log.d("tagutil", "目录无图片，请检查: ");
            result.none();
            return;
        }
        int total = paths.size();
        int index = 0;
        for (File file : paths) {
            boolean isSuccess = toPompara(activity, file.getAbsolutePath());
            result.current((isSuccess ? ++index : index) + "/" + (isSuccess ? total : --total));
        }
        if (total == 0) {
            result.none();
        } else {
            result.success();
        }
        Log.d("tagutil", "图片处理完毕: ");

    }

    //图片识别结果是否相同
    public boolean toPompara(Activity activity, final String path) {
        if (!path.endsWith("jpg") && !path.endsWith("png")) {
            return false;
        }
        final Bitmap bitmap = BitmapFactory.decodeFile(path);
        getWinPlate(activity, new RecogniteHelper4WT.OnResult() {
                    @Override
                    public void onGeted(String fileName, String number) {

                        //文通识别结果
                        String comNum = getComPlate(path);       //公司识别结果
                        Log.d("tagutil", "comNum: " + comNum);
                        if (!TextUtils.isEmpty(number))
                            if (!TextUtils.isEmpty(comNum) && number.equals(comNum)) {
                                saveBitmap(bitmap, samePath + getFileName(path));
                            } else {  //不相同
                                saveBitmap(bitmap, difPath + getFileName(path));
                            }
                        else {  //不相同
                            saveBitmap(bitmap, difPath + getFileName(path));
                        }
                        deleteFile(path);//删除原有的图片
                    }

                    @Override
                    public void recogFail() {
                        //文通识别结果
                        saveBitmap(bitmap, difPath + getFileName(path));
                        deleteFile(path);//删除原有的图片


                    }

                    @Override
                    public String saveImage(byte[] bytes) {
                        return null;
                    }
                },
                path,
                bitmap.getWidth(),
                bitmap.getHeight());
        return true;

    }

    //删除图片
    private void deleteFile(String path) {
        new File(path).delete();
    }

    //保存图片
    private void saveBitmap(Bitmap bitmap, String path) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100,
                    fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //获取文通车牌号
    private void getWinPlate(Activity activity,
                             RecogniteHelper4WT.OnResult getted,
                             String path,
                             int width, int hight) {
//        WintonRecogManager.getInstance().useWTRecognitionByPic(
//                activity,
//                path,
//                getted,
//                width,
//                hight
//        );

    }

    //
    private byte[] bitmapTobyte(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] data = stream.toByteArray();
        try {
            stream.flush();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;

    }

    //获取公司车牌
    private String getComPlate(String path) {
        String fileName = getFileName(path);
        return fileName.substring(fileName.lastIndexOf("_") + 1, fileName.lastIndexOf("_") + 8);
    }

    //获取文件名
    private static String getFileName(String pathandname) {

        int start = pathandname.lastIndexOf("/");
        int end = pathandname.lastIndexOf(".");
        if (start != -1 && end != -1) {
            return pathandname.substring(start + 1, end);
        } else {
            return null;
        }

    }

    //获取所有文件
    private List<File> getAllFiles(String filePath) {
        List<File> path = Arrays.asList(new File(filePath).listFiles());
        return path;
    }

    public interface Result {

        void current(String count); //当前处理语句

        void none();    //无文件

        void success(); //处理完毕


    }
}
