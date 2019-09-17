package io.aattila.rly8.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;

@Service
public class Sensors implements DynamicProperties {

    Logger log = LoggerFactory.getLogger(Sensors.class);

    private final Yaml yaml = new Yaml();

    public boolean isEnabled(String sensorKey) throws IOException {

        File triggersFile = new File(FILE_NAME);
        if (!triggersFile.exists()) {
            log.error("No sensors definition file found: {}", FILE_NAME);
            return false;
        }

        try (InputStream in = new FileInputStream(triggersFile)) {
            LinkedHashMap map = (LinkedHashMap) yaml.load(in);
            LinkedHashMap sensors = (LinkedHashMap) map.get(SENSORS);
            if (!sensors.containsKey(sensorKey)) {
                log.error("No sensor key is defined ath the sensors node", sensorKey);
                return false;
            }
            LinkedHashMap sensorData = (LinkedHashMap) sensors.get(sensorKey);
            String type = (String) sensorData.get(TYPE);
            String source = (String) sensorData.get(SOURCE);
            String key = (String) sensorData.get(KEY);

            if ("yaml".equalsIgnoreCase(type)) {
                return getSenzorDataFromYml(source, key);
            }

            log.error("Sensor source type is not implemented: {}", type);
            return false;
        }
    }


    private boolean getSenzorDataFromYml(String source, String key) throws IOException {

        Yaml sensorYaml = new Yaml();

        File sensorFile = new File(source);
        if (!sensorFile.exists()) {
            log.error("Sensor file not found: {}", sensorFile);
            return false;
        }

        try (InputStream in = new FileInputStream(sensorFile)) {
            LinkedHashMap map = (LinkedHashMap) sensorYaml.load(in);
            String[] keys = key.split("\\.");

            for (int i = 0; i < keys.length; i++) {

                if(!map.containsKey(keys[i])) {
                    log.error("The yaml file {} is not contains property", source, key);
                    return false;
                }

                Object obj = map.get(keys[i]);
                if(obj instanceof LinkedHashMap) {
                    map = (LinkedHashMap) obj;
                } else {
                    log.info("Sensor data for {} is {}", key, obj);
                    return (boolean)obj;
                }

            }
            return false;
        }
    }

}
