package comleyou.auth.test;

import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.JwtUtils;
import com.leyou.common.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

public class jwtTest {

    private static final String pubKeyPath = "F:\\学业资料\\java\\Java项目\\乐优商城\\temp\\rsa\\rsa.pub";

    private static final String priKeyPath = "F:\\学业资料\\java\\Java项目\\乐优商城\\temp\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        // 生成token
        String token = JwtUtils.generateToken(new UserInfo(20L, "jack"), privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiamFjayIsImV4cCI6MTU2MzE1OTI1NH0.d-Fasz4KajWEFkiH2LFNRogNPhG_JNkaQSob-xWcmwKJOWg9sAjwGyEzJ8X3jDuGPDuQQurTNx06It6OhZTIZ7OqJbIdhJYW9jn1xXqi7M7KdNvV5HQkOTWhD3p3j2-sBUAc3UTRdpUST3zF3rCkjk3WvegLUmeWbMf60VnZv6Q";

        // 解析token
        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }
}
