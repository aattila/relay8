package io.aattila.rly8;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan({"io.aattila.rly8"})
public class RLY8 {

    private static final Logger log = LoggerFactory.getLogger(RLY8.class);

    public static void main(String[] args) {
        checkEnvironment();
        SpringApplication.run(RLY8.class);
    }

    /**
     * Sanity check, Java 8 must be installed
     */
    static void checkEnvironment() {
        String jvmVersion = System.getProperty("java.home");
        if (jvmVersion == null || jvmVersion.length() == 0) {
            log.error("Cannot get the java home property");
            throw new RuntimeException("java.home not set");
        }
        if (!"1.8".equals(System.getProperty("java.specification.version"))) {
            log.error("Java runtime version must be >= 1.8");
            throw new RuntimeException("Invalid java version. Java runtime version must be >= 1.8");
        }
    }


}
