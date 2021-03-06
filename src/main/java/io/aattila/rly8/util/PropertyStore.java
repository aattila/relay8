package io.aattila.rly8.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.DefaultPropertiesPersister;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

@Component
public class PropertyStore {

    private static final Logger log = LoggerFactory.getLogger(PropertyStore.class);

    public static final String FILE_NAME = System.getProperty("user.dir")+"/relay.properties";

    private Properties properties = new Properties();
    private DefaultPropertiesPersister persister = new DefaultPropertiesPersister();
    private File file;


    @PostConstruct
    public void init() {
        log.info("Relay properties file is: {}", FILE_NAME);
        file = new File(FILE_NAME);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                log.error("Properties file {} could not be created!", FILE_NAME, e);
            }
        } else {
            try (FileInputStream fis = new FileInputStream(file)) {
                persister.load(properties, fis);
            } catch (IOException e) {
                log.error("Properties file {} could not be read!", FILE_NAME, e);
            }

        }
    }

    public void setTimestamp(int relay, long timestamp) {

        log.info("Persisting timestamp {} for the relay {}", new Date(timestamp), relay);

        properties.setProperty(String.valueOf(relay), String.valueOf(timestamp));
        try (FileOutputStream fos = new FileOutputStream(file)) {
            persister.store(properties, fos, "LastSet");
        } catch (IOException e) {
            log.error("Properties file could not be written!", e);
        }
    }

    public long getTimestamp(int relay) {
        if (properties.containsKey(String.valueOf(relay))) {
            String stringValue = properties.getProperty(String.valueOf(relay));
            return Long.parseLong(stringValue);
        }
        return 0;
    }

}
