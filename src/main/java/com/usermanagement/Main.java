package com.usermanagement;

import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Standalone Embedded Tomcat Launcher.
 * Enables running the complete Java Web Application with 1-click simplicity:
 * "mvn exec:java"
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) throws Exception {
        int port = DEFAULT_PORT;
        String portProp = System.getProperty("server.port");
        if (portProp == null) {
            portProp = System.getenv("PORT");
        }
        if (portProp != null && !portProp.isEmpty()) {
            try {
                port = Integer.parseInt(portProp);
            } catch (NumberFormatException ignored) {
            }
        }

        // Resolve webapp directory - supports dev (src/main/webapp), Docker (webapp/), and jar-relative paths
        String webappDirLocation = "src/main/webapp/";
        File webappDir = new File(webappDirLocation);
        if (!webappDir.exists()) {
            webappDirLocation = "webapp/";
            webappDir = new File(webappDirLocation);
        }
        if (!webappDir.exists()) {
            // Fallback: look relative to the jar location
            String jarDir = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
            webappDir = new File(jarDir, "webapp");
            webappDirLocation = webappDir.getAbsolutePath();
        }

        System.setProperty("java.net.preferIPv4Stack", "true");

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.setBaseDir(new File("target/tomcat").getAbsolutePath());

        // Bind to 0.0.0.0 so Railway (and other PaaS) can route external traffic
        org.apache.catalina.connector.Connector connector = tomcat.getConnector();
        connector.setPort(port);
        connector.setProperty("address", "0.0.0.0");
        tomcat.getHost().setAutoDeploy(false);

        // Register web application context
        StandardContext ctx = (StandardContext) tomcat.addWebapp("", webappDir.getAbsolutePath());
        ctx.setParentClassLoader(Main.class.getClassLoader());
        ctx.setReloadable(false);
        ctx.setBackgroundProcessorDelay(-1);

        // Map target/classes to WEB-INF/classes in the embedded resource set
        File additionWebInfClasses = new File("target/classes");
        if (additionWebInfClasses.exists()) {
            WebResourceRoot resources = new StandardRoot(ctx);
            resources.addPreResources(new DirResourceSet(
                    resources,
                    "/WEB-INF/classes",
                    additionWebInfClasses.getAbsolutePath(),
                    "/"
            ));
            ctx.setResources(resources);
        }

        System.out.println("=================================================================");
        System.out.println("   USER MANAGEMENT SYSTEM - EMBEDDED TOMCAT SERVER");
        System.out.println("=================================================================");
        System.out.println(" Server running at: http://localhost:" + port + "/");
        System.out.println(" Login Page:        http://localhost:" + port + "/login.jsp");
        System.out.println(" Home Dashboard:    http://localhost:" + port + "/home.jsp");
        System.out.println(" REST API Base:     http://localhost:" + port + "/api/users");
        System.out.println(" Default Admin:     admin / Admin@123");
        System.out.println(" Press Ctrl+C to stop the server.");
        System.out.println("=================================================================");

        tomcat.start();
        tomcat.getServer().await();
    }
}
