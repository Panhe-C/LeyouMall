package com.leyou.auth.test;


import com.leyou.auth.LeyouAuthApplication;
import com.leyou.auth.service.AuthService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LeyouAuthApplication.class)
public class getToken {

    @Autowired
    private AuthService authService;


    @Test
    public void getTokenCsv(){
        File csv = new File("F:\\学业资料\\java\\Java项目\\乐优商城\\temp\\csv\\Token.csv");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(csv,true));
            for (int i = 1; i < 5000; i++) {
                //新增一行数据
                String token = this.authService.accredit("user_no." + i,"1234" + i);
                writer.write("user_no." + i + ":" + token);
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
