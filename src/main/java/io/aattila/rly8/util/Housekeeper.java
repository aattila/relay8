package io.aattila.rly8.util;

import io.aattila.rly8.device.RLY8Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class Housekeeper implements DynamicProperties {

    Logger log = LoggerFactory.getLogger(Housekeeper.class);

    @Autowired
    PropertyStore propertyStore;

    @Autowired
    RLY8Service rly8Service;

    private static final Yaml yaml = new Yaml();

    private Map<String, Integer> values;
    private long lastModified;


    @Scheduled(initialDelay = 10000, fixedRate = 30000)
    public void switchback() throws Exception {

        File triggersFile = new File(FILE_NAME);
        if(!triggersFile.exists()) {
            log.warn("Switchback not used! No switchback definition file found: {}", FILE_NAME);
            return;
        }

        long modifiedTime = triggersFile.lastModified();
        if (lastModified != modifiedTime) {
            if(lastModified > 0) {
                log.info("The switchback file is modified, going to reload.");
            }
            lastModified = modifiedTime;
            values = loadData(triggersFile);
        }

        Boolean[] switchbackChecks = new Boolean[8];
        values.forEach((name, time) -> {
            int relay = Integer.parseInt(name.substring(RELAY.length(), name.length()));
            switchbackChecks[relay - 1] = isSwitchbackNeeded(relay, time);
        });

        boolean switchbackDetected = Arrays.asList(switchbackChecks).stream()
                .filter(switchbackCheck -> switchbackCheck)
                .collect(Collectors.toList())
                .size() > 0;

        if (switchbackDetected) {

            log.info("Switchback detected");

            boolean isSwitchbackOk = false;

            try {
                boolean[] statuses = rly8Service.getRelayStatus();
                for (int i = 0; i < 8; i++) {
                    if (switchbackChecks[i]) {
                        log.info("Switchback detected for the relay {}", i + 1);
                        statuses[i] = false;
                    }
                }
                Thread.sleep(300); // a minimum time between two RLY-8 access
                isSwitchbackOk = rly8Service.setRelayStatus(statuses);
            } catch (Exception e) {
                log.error("Cannot do switchback", e);
            }

            if (isSwitchbackOk) {
                for (int i = 0; i < 8; i++) {
                    if (switchbackChecks[i]) {
                        propertyStore.setTimestamp(i + 1, 0);
                    }
                }
            }

        }
    }

    private boolean isSwitchbackNeeded(int relay, int switchbackTime) {

        long now = System.currentTimeMillis();

        // no switchback is set
        if (switchbackTime <= 0) {
            return false;
        }

        long t = propertyStore.getTimestamp(relay);

        // no persisted timestamp found (or already switched back)
        if (t <= 0) {
            return false;
        }

        return (now - t) >= switchbackTime * 60000;
    }

    private Map<String, Integer> loadData(File triggersFile) throws IOException {
        Map<String, Integer> switchbacks = new HashMap<>();
        try (InputStream in = new FileInputStream(triggersFile)) {
            LinkedHashMap map = (LinkedHashMap) yaml.load(in);
            LinkedHashMap values = (LinkedHashMap) map.get(SWITCHBACK);
            values.forEach((key, value) -> {
                switchbacks.put((String) key, (int) value);
            });
        }
        log.info("Switchbacks are loaded: {}", switchbacks);
        return switchbacks;
    }

}
