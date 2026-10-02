package com.usermanagement.config;

import com.usermanagement.util.DBInitializer;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ApplicationPath;

/**
 * Jersey ResourceConfig registering REST resources and features.
 */
public class JerseyApplication extends ResourceConfig {

    private static final Logger logger = LoggerFactory.getLogger(JerseyApplication.class);

    public JerseyApplication() {
        logger.info("Initializing Jersey REST Application Config...");
        
        // Explicitly register REST resource classes (avoids Java 22 ASM classfile scanner warnings)
        register(com.usermanagement.web.AuthResource.class);
        register(com.usermanagement.web.UserResource.class);

        // Also scan package as backup
        packages("com.usermanagement.web");

        // Enable Jackson for POJO JSON serialization
        register(JacksonFeature.class);

        // Run database initialization on startup
        try {
            DBInitializer.initialize();
        } catch (Exception e) {
            logger.error("Error during DBInitializer startup in JerseyApplication", e);
        }

        logger.info("Jersey REST Application configured successfully.");
    }
}
