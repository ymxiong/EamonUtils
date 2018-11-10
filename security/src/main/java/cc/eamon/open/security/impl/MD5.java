package cc.eamon.open.security.impl;

import cc.eamon.open.security.CodeMethod;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.UUID;


/**
 * Created by Eamon on 2017/7/28.
 */
public class MD5 implements CodeMethod {

	private static MD5 md5;
	
	private MD5() {}
	
	public static MD5 getInstance(){
		if(md5==null)md5 = new MD5();
		return md5;
	}
	
	@Override
	public String encode(byte[] s) {
        char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};       

        try {
            byte[] btInput = s;
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
	}

	@Override
	public byte[] decode(String content) {
		return null;
	}

	@Override
	public String encode(String s) {
		try {
			byte[] btInput = s.getBytes("UTF-8");
			return encode(btInput);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) {
		String pwd = MD5.getInstance().encode("test123456");
		System.out.println(pwd);
		String uuid = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 8).toUpperCase();
		System.out.println(uuid);
		System.out.println(MD5.getInstance().encode(pwd+uuid));
		
	}

}
