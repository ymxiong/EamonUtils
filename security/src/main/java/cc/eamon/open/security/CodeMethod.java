package cc.eamon.open.security;

/**
 * Created by Eamon on 2017/7/28.
 */
public interface CodeMethod {

    enum SUPPORT {
        CODE_MD5, CODE_BASE64
    }

    String encode(byte[] content);

    byte[] decode(String content);

    String encode(String content);
}
