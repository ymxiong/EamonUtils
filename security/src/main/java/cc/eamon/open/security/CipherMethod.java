package cc.eamon.open.security;

/**
 * Created by Eamon on 2017/7/28.
 */
public interface CipherMethod {

    enum SUPPORT {
        CIPHER_RSA, CIPHER_AES
    }


    byte[] encrypt(byte[] content, String key);

    byte[] decrypt(byte[] content, String key);

}
