package com.yoshio3;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import javax.ws.rs.ApplicationPath;
import java.io.File;

/**
 * Created by yoterada on 2017/04/05.
 */
@Component
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig(){
        super(MultiPartFeature.class);
        register(FileUpOperation.class);
    }
}
