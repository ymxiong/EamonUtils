package cc.eamon.open.security;

import junit.framework.TestCase;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest
        extends TestCase {

    @Test
    public void testSecurityFactory(){

        String s = SecurityFactory.getCodeMethod(CodeMethod.SUPPORT.CODE_BASE64).encode("sdaifgoiaudsgfhj213545465213DVGA");

        System.out.println(s);

        System.out.println(new String(
                SecurityFactory.getCodeMethod(CodeMethod.SUPPORT.CODE_BASE64).decode(s)
        ));

        System.out.println(
            new String(
                SecurityFactory.getCodeMethod(CodeMethod.SUPPORT.CODE_BASE64).decode(
                        "AgMyyAZxAHiEXRVPgBIUwfRF0/bYFfsi+8gVXZjWvsLu6g1jR0+77JVQgo2iPLub5KL2lxmNecbT5f8QsZUL26mMCCcMi6yXozXL0XE5PMRNyB+tTyssmemPATuJL84ahom/YbuipGsnPYmrlMBAl3WWxAzc2mOd+mpoG1fVzJ52NJmaAS7S2hxOFblRKz0AiPUdDhwy/eg+hiQlZ/Zr1k6nQ3HahqjK0lYc19mvJ2glvpWoNStL9fjZJUTg2dXQJXfj/x2zXAsrR4iwccR3LVCqV3duQ4sMJT47ksx72CTbkRbm94M/d7FuC74h1+8jsgFgvVO2Ju1AVPoC5L0hUXPiW5ve70Q+/CI40/FMEjTOUYhKCuynbXtCoPb0UK/zwKVZXy95Ww6QJDdWj2vcFeFBDJyLaQE4Bdrxnkbw2Raj+vKAPoThEv5n1D6xkFVYbIe5OhXSagRbIHMK1hp5MVuzHj2GJWnzbrSwW5yTSOUtKs4G5SHmpPVlea+9MVC3MfdiNuEP3LhRoi8sAicfxMd4y4atWyPoON269LcRJwMSXMWLqDOoRGMD8NQ/9PR6uAcgsjm+Dd+fzzKIev8g/4tQHqJm/9A7GSQTFsRTFyFUKTtAnG1+Lns5ID71cKlNLnBK5368W+z2HTqmSJhOqywBKYUbpqnpQwNNqfj37ypaJpbeJcLCvEU2ibzxqugV85REYozRKhhkuIoRq4HXN2MFgqi+tZLxDA68X8gl+wO3jKqSLlYJv1PlpTKb1YndwaTqSgeHKmT9R08kvhF9gQd8zW88F3unAqlraGcP7mwLNiBlf+8RsHkxIOAvS+F2YYm/FM2bDS7MixIeDzllONjlvtjcatksku/U9U+PBAsLDiakvo9K6ZZyhrEa1EPYFdtq+5Guzw=="
                )
             )
        );
    }

}
