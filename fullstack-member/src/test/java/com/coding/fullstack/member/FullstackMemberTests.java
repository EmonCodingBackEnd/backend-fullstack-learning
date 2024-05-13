package com.coding.fullstack.member;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.Md5Crypt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.charset.StandardCharsets;

public class FullstackMemberTests {

    @Test
    void testMd5() {
        String md5Hex = DigestUtils.md5Hex("123456");
        System.out.println(md5Hex);
    }

    @Test
    void testMd5Crypt() {
        // 默认盐：$1$+8位字符
        String md5CryptHex = Md5Crypt.md5Crypt("123456".getBytes(StandardCharsets.UTF_8), "$1$8tqwxlYF");
        System.out.println(md5CryptHex);
        String md5CryptHex2 = Md5Crypt.md5Crypt("123456".getBytes(StandardCharsets.UTF_8), "$1$8tqwxlYF");
        System.out.println(md5CryptHex2);
        Assertions.assertEquals(md5CryptHex, md5CryptHex2);
    }

    @Test
    void testMd5Cryp2t() {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String result = passwordEncoder.encode("123456");
        System.out.println(result);
        Assertions.assertTrue(passwordEncoder.matches("123456", result));
    }
}
