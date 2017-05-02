package com.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 3DES加密工具类
 */
public class EncryptionUtil {
	public final static String SIGNKEY = "110100 110011 1100110 110110 1100100 110000 110100 1100110 110010 110000 110111 110000 1100100 1100010 110110 111000 1100100 110001 110010 110101 110100 111000 110110 110011 1100110 110011 111001 110001 111000 110110 110110 111001";//

	// 密钥
	private final static String secretKey = getKey();
	// 向量
	private final static String iv = "01234567";
	// 加解密统一使用的编码方式
	private final static String encoding = "utf-8";

	public  static String MYRECOG_LOC="110001 110010 110011";  //位置
	public  static String MYRECOG_SCOPE="110010 110101 110101"; //范围
	public  static String MYRECOG_VALUE="110001 110010 111000"; //值

	// 将二进制字符串转换成Unicode字符串
	public static String binaryStrToStr(String binStr) {
		String[] tempStr = strToStrArray(binStr);
		char[] tempChar = new char[tempStr.length];
		for (int i = 0; i < tempStr.length; i++) {
			tempChar[i] = binaryStrToChar(tempStr[i]);
		}
		return String.valueOf(tempChar);
	}

	private static String getKey() {
		String secretKey = binaryStrToStr(SIGNKEY);
		while (secretKey.length() < 24) {
			secretKey = secretKey.concat(binaryStrToStr(SIGNKEY));
		}
		return secretKey.substring(0, 24);
	}



	public static String getSHA(String val) throws NoSuchAlgorithmException {
		MessageDigest md5 = MessageDigest.getInstance("SHA-1");
		md5.update((val.concat("ecaray")).getBytes());
		byte[] m = md5.digest();// 加密
		return getString(m);
	}

	private static String getString(byte[] b) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			sb.append(b[i]);
		}
		return sb.toString();
	}

	public  static int getLocation(String code){
		return Integer.valueOf(binaryStrToStr(code));
	}

	// 将字符串转换成二进制字符串，以空格相隔
	public String strToBinarystr(String str) {
		char[] strChar = str.toCharArray();
		StringBuilder result = new StringBuilder();
		for (int i = 0, len = strChar.length; i < len; i++) {
			result.append(Integer.toBinaryString(strChar[i]) + " ");
		}
		return result.toString();
	}
	// 将初始二进制字符串转换成字符串数组，以空格相隔
	private   static  String[] strToStrArray(String str) {
		return str.split(" ");
	}
	// 将二进制字符串转换为char
	private static char binaryStrToChar(String binStr) {
		int[] temp = binaryStrToIntArray(binStr);
		int sum = 0;
		for (int i = 0; i < temp.length; i++) {
			sum += temp[temp.length - 1 - i] << i;
		}
		return (char) sum;
	}
	// 将二进制字符串转换成int数组
	private static int[] binaryStrToIntArray(String binStr) {
		char[] temp = binStr.toCharArray();
		int[] result = new int[temp.length];
		for (int i = 0; i < temp.length; i++) {
			result[i] = temp[i] - 48;
		}
		return result;
	}
	public static void setPoint(byte[] data, int location) {
		int mloc = EncryptionUtil.getLocation(EncryptionUtil.MYRECOG_LOC);
		int value = EncryptionUtil.getLocation(EncryptionUtil.MYRECOG_VALUE);
		data[mloc] = (byte) location;
		data[location] = (byte) value;
	}
}
