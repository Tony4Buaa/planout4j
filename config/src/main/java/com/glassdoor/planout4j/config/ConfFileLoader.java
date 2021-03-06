package com.glassdoor.planout4j.config;

import java.io.File;

import org.apache.log4j.chainsaw.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import com.glassdoor.planout4j.util.VersionLogger;

/**
 * Responsible for loading PlanOut4J config file.
 * Checks system property <code>planout4jConfigFile</code> and defaults to the embedded <code>planout4j.conf</code>
 * (which, of course, still allows for individual properties to be overridden via system props).
 */
public class ConfFileLoader {

    static {
        VersionLogger.log("config");
    }

    public static final String P4J_CONF_FILE = "planout4jConfigFile";

    /**
     * Returns config object which will attempt to resolve properties in the following order:<ol>
     * <li>custom config file pointed to by <code>planout4jConfigFile</code> system property</li>
     * <li>default embedded config</li>
     * </ol>
     *
     * @return Planout4j configuration (backends, etc.)
     */
    public static Config loadConfig() {
        final Config internalConfig = ConfigFactory.load("planout4j").resolve();
        final String customConfigPath = System.getProperty(P4J_CONF_FILE);
        final Logger log = LoggerFactory.getLogger(ConfFileLoader.class);
        if (customConfigPath != null) {
            final File configFile = new File(customConfigPath);
            if (configFile.isFile() && configFile.canRead()) {
                log.info("Using custom config: {}", configFile.getAbsolutePath());
                return ConfigFactory.defaultOverrides().withFallback(
                        ConfigFactory.parseFile(configFile).withFallback(internalConfig)).resolve();
            } else {
                log.warn("Invalid custom config path: {} (resolves to {})", customConfigPath, configFile.getAbsolutePath());
            }
        }
        log.info("Using embedded default config");
        return internalConfig;
    }

    public static void main(String[] args) {

        final String customConfigPath = "/Users/zhuifengbuaa/IdeaProject/planout4j/demos/conf/demo_planout4j.conf";
        final File configFile = new File(customConfigPath);
        boolean file = configFile.isFile();
        System.out.println("file is: " + file);
//                && configFile.canRead()
    }
}

