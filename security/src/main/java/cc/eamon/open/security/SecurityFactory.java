package cc.eamon.open.security;

import cc.eamon.open.security.impl.*;

/**
 * Created by Eamon on 2017/7/28.
 */
public class SecurityFactory {


    public static CipherMethod getCipherMethod(CipherMethod.SUPPORT method) {

        if (method.equals(CipherMethod.SUPPORT.CIPHER_RSA)) {
            return RSA.getInstance();
        } else if (method.equals(CipherMethod.SUPPORT.CIPHER_AES)) {
            return AES.getInstance();
        } else if (method.equals("")) {

        }
        return AES.getInstance();
    }

    public static CodeMethod getCodeMethod(CodeMethod.SUPPORT method) {
        if (method.equals(CodeMethod.SUPPORT.CODE_BASE64)) {
            return Base64.getInstance();
        } else if (method.equals(CodeMethod.SUPPORT.CODE_MD5)) {
            return MD5.getInstance();
        }
        return MD5.getInstance();
    }


}
