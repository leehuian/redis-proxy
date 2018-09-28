package com.liha;

import com.liha.Configs.ProxySystemConfigLoad;
import com.liha.netty.NettyServerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MyCommandLineRunner implements CommandLineRunner {
    private static Logger LOGGER= LoggerFactory.getLogger(MyCommandLineRunner.class);

    @Autowired
    private ProxySystemConfigLoad configLoad;

    @Autowired
    private NettyServerListener nettyServerListener;
    @Override
    public void run(String... args) throws Exception {
        configLoad.initialize();
        nettyServerListener.start();
    }
}
