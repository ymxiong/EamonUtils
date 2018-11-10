package cc.eamon.open.security.impl;

import cc.eamon.open.security.CipherMethod;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


/**
 * Created by Eamon on 2017/7/28.
 */
public class RSA implements CipherMethod {

	private RSA() {}

	private static RSA rsa;

	public static RSA getInstance() {
		if (rsa == null)
			rsa = new RSA();
		return rsa;
	}

	/**
	 * 得到私钥
	 *
	 * @param key
	 *            密钥字符串（经过base64编码）
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws Exception
	 */
	private PrivateKey getPrivateKey(String key) throws NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] keyBytes;
		keyBytes = Base64.getInstance().decode(key);
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
		return privateKey;
	}


	/**
	 * 得到公钥
	 *
	 * @param key
	 *            密钥字符串（经过base64编码）
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 * @throws Exception
	 */
	private PublicKey getPublicKey(String key) throws NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] keyBytes;
		keyBytes = Base64.getInstance().decode(key);
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		PublicKey publicKey = keyFactory.generatePublic(keySpec);
		return publicKey;
	}

	/**
	 * 加密
	 * @param content 原文
	 * @param publicKey 商户公钥
	 * @return 加密后的字符流
	 */
	public byte[] encrypt(byte[] content, String publicKey) {
		PublicKey pubKey;
		try {
			pubKey = getPublicKey(publicKey);
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, pubKey);

			InputStream ins = new ByteArrayInputStream(content);
			ByteArrayOutputStream writer = new ByteArrayOutputStream();
			// rsa加密的字节大小最多是117，将需要加密的内容，按117位拆开加密
			byte[] buf = new byte[117];
			int bufl;
			while ((bufl = ins.read(buf)) != -1) {
				byte[] block = null;
				if (buf.length == bufl) {
					block = buf;
				} else {
					block = new byte[bufl];
					for (int i = 0; i < bufl; i++) {
						block[i] = buf[i];
					}
				}
				writer.write(cipher.doFinal(block));
			}
			return writer.toByteArray();

		} catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException
				| IllegalBlockSizeException | BadPaddingException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 解密
	 * @param content 密文
	 * @param privateKey 商户私钥
	 * @return 解密后的字符流
	 */
	public byte[] decrypt(byte[] content, String privateKey) {
		PrivateKey prikey;
		try {
			prikey = getPrivateKey(privateKey);
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.DECRYPT_MODE, prikey);
			InputStream ins = new ByteArrayInputStream(content);
			ByteArrayOutputStream writer = new ByteArrayOutputStream();
			// rsa解密的字节大小最多是128，将需要解密的内容，按128位拆开解密
			byte[] buf = new byte[128];
			int bufl;
			while ((bufl = ins.read(buf)) != -1) {
				byte[] block = null;
				if (buf.length == bufl) {
					block = buf;
				} else {
					block = new byte[bufl];
					for (int i = 0; i < bufl; i++) {
						block[i] = buf[i];
					}
				}
				writer.write(cipher.doFinal(block));
			}
			return writer.toByteArray();
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException
				| IllegalBlockSizeException | BadPaddingException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}


	public static String RSAPublicKey=
			"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDY6gvsAAspCgWMYh/KFvmUGgvl"+
					"j9+28kefXixFlLk1Gq1aa6jjFNqA/fqnkuJLqBJJOwItsdl9PK4tN4WR9P7Unpgs"+
					"4FQcKzyluDfpvPbs9kdSafkV689pj7v8eH/Ddorsc/3WwoR6fdXdKKgXInVjPtqp"+
					"6eDOSIe8XIyXR2U59wIDAQAB";

	public static String RSAPrivateKey=
			"MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBANjqC+wACykKBYxi"+
					"H8oW+ZQaC+WP37byR59eLEWUuTUarVprqOMU2oD9+qeS4kuoEkk7Ai2x2X08ri03"+
					"hZH0/tSemCzgVBwrPKW4N+m89uz2R1Jp+RXrz2mPu/x4f8N2iuxz/dbChHp91d0o"+
					"qBcidWM+2qnp4M5Ih7xcjJdHZTn3AgMBAAECgYEAu3ygzV3ER7aX0R1HKN/u3Soe"+
					"Ok+/KFwFuCQn1ASWiOYEDHGdyplNu8zLCGiXHJmrJIzSdziQKgV27zJcSyodz4Ut"+
					"fjYqDU43uo6QhouisjF9VJo9JgAcEeWd0yfZcOAkwltUJDPzipl+/+ORZ9wLCdxy"+
					"ZR5d+Kz8QW3AYFYoiwECQQD51vo5+n+kqReatNm0oQf0EBOsKF7P0Z1XCM9BCLJg"+
					"7ZAoplqitFEjrQVgind3tsqVZV1IQ/uIbJXy/zPm0q63AkEA3kM9ItitpxDBEtLO"+
					"GrbjcbMui9eQwL8bJdsPeN2bNHuuM5z5xPC3Tr7gkt0QSlEYIGC2HgPLkcdxGSC3"+
					"gYqOwQJAKWtjhpMp8DF8UVCkOxbrS6ISsNrshQWaUSCLw5tef0VDPgn+QrUkMobv"+
					"ukaaccVjJotsgJuMqtxdq7B1eVH6VwJAAMJ6EwRqk4ebIVVXHwBBBsJ2BkRWWlJM"+
					"5XQ6OU+ImEVT8xk2QVYRSlOcsOPQinB8hJ/P/4pDx9vGpy9VcTvoAQJBANfgUf9G"+
					"KBGwPZ2eQy0DAib9iZhlJ6DbURNouezW/4oimm3txUnlf5DxZHQSeiQroNNnirYp"+
					"y2uawWZpngMC0Gg=";

	public static void main(String[] args) throws UnsupportedEncodingException {
		System.out.println(
				new String(
						RSA.getInstance().decrypt(
								RSA.getInstance().encrypt("Eamon熊".getBytes("UTF-8"), RSAPublicKey),
								RSAPrivateKey),"UTF-8")
		);
	}

}
