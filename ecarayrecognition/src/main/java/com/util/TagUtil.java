package com.util;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class TagUtil {
	public static boolean debug = false;
	private final static String TAG = "TagUtil";

	/**
	 * 弹出提示框
	 * 
	 * @param context
	 * @param str
	 */
	public static void showToast(String str, Context context) {
		Toast toast = Toast.makeText(context, str, Toast.LENGTH_SHORT);
		toast.show();
	}

	/**
	 * 弹出提示框
	 * 
	 * @param context
	 * @param str
	 */
	public static void showCenterToast(String str, Context context) {
		Toast toast = Toast.makeText(context, str, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	/**
	 * 显示debug 数据
	 * 
	 * @param str
	 */
	public static void showLogDebug(String str) {
		if (debug)
			Log.d(TAG, str);
	}

	/**
	 * 显示debug 数据
	 * 
	 * @param str
	 */
	public static void showLogDebug(String tag, String str) {
		if (debug)
			Log.d(tag, str);
	}

	public static void showLogError(String str) {
		if (debug)
			Log.e(TAG, str);
	}

}
