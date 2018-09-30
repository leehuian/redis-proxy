package com.liha;

import com.liha.netty.NettyServerListener;
import com.liha.netty.redisclient.Initialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MyCommandLineRunner implements CommandLineRunner {
    private static Logger LOGGER= LoggerFactory.getLogger(MyCommandLineRunner.class);

    @Autowired
    private NettyServerListener nettyServerListener;

    @Autowired
    private Initialization initialization;

    @Override
    public void run(String... args) throws Exception {
        initialization.initAll();
        nettyServerListener.start();
        LOGGER.info("run success");
    }
}
