package cc.eamon.open.security.impl;

import cc.eamon.open.security.CipherMethod;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;


/**
 * Created by Eamon on 2017/7/28.
 */
public class AES implements CipherMethod {
	private static AES aes;
	
	private AES(){}
	
	public static AES getInstance(){
		if(aes==null)aes = new AES();
		return aes;
	}

	@Override
	public byte[] encrypt(byte[] sSrc, String sKey) {
		try {
			SecretKeySpec skeySpec = new SecretKeySpec(sKey.getBytes("UTF-8"), "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");// "算法/模式/补码方式"
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
			byte[] encrypted = cipher.doFinal(sSrc);
			return encrypted;

		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
				| BadPaddingException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public byte[] decrypt(byte[] sSrc, String sKey) {
		try {
			SecretKeySpec skeySpec = new SecretKeySpec(sKey.getBytes("UTF-8"), "AES");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

			cipher.init(Cipher.DECRYPT_MODE, skeySpec);
			return cipher.doFinal(sSrc);
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) throws UnsupportedEncodingException {

		System.out.println(
				new String(
						AES.getInstance().decrypt(
						AES.getInstance().encrypt("Eamon熊".getBytes("UTF-8"), "1234567890ABCDEF"), 
						"1234567890ABCDEF"),"UTF-8")
				);
		
		
	}

	
}
