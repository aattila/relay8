package io.aattila.rly8.util;

import io.aattila.rly8.device.RLY8Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;

@Component
public class Scheduler implements DynamicProperties {

    Logger log = LoggerFactory.getLogger(Scheduler.class);

    @Autowired
    PropertyStore propertyStore;

    @Autowired
    Sensors sensors;

    @Autowired
    RLY8Service rly8Service;


    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

    private final Yaml yaml = new Yaml();
    private long lastModified;

    @PostConstruct
    public void setupTriggers() throws IOException {

        File triggersFile = new File(FILE_NAME);

        if (!triggersFile.exists()) {
            log.warn("Schedulers not used! No trigger definition file found: {}", FILE_NAME);
            return;
        }

        lastModified = triggersFile.lastModified();

        scheduler.initialize();
        loadFile(triggersFile);
    }

    @Scheduled(initialDelay = 10000, fixedRate = 10000)
    public void checkTriggerFile() throws IOException {
        File triggersFile = new File(FILE_NAME);
        if (!triggersFile.exists()) {
            log.warn("Schedulers not used! No trigger definition file found: {}", FILE_NAME);
            return;
        }

        long modifiedTime = triggersFile.lastModified();
        if (lastModified == modifiedTime) {
            return;
        }

        lastModified = modifiedTime;
        log.info("The triggers file is modified, going to reschedule.");
        scheduler.shutdown();
        scheduler.initialize();

        loadFile(triggersFile);
    }

    private void loadFile(File triggersFile) throws IOException {
        try (InputStream in = new FileInputStream(triggersFile)) {
            LinkedHashMap map = (LinkedHashMap) yaml.load(in);
            LinkedHashMap triggerDefs = (LinkedHashMap) map.get(TRIGGERS);
            triggerDefs.forEach((name, props) -> {
                scheduleTriggers((LinkedHashMap) props);
            });
        }
    }

    private void scheduleTriggers(LinkedHashMap props) {

        log.info("Triggering tasks {}", props);

        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                int relay = (int) props.get(RELAY);
                String sensorKey = null;
                if (props.containsKey(SENSOR)) {
                    sensorKey = (String) props.get(SENSOR);
                }
                try {
                    switchOn(relay, sensorKey);
                } catch (IOException e) {
                    log.error("Cannot swith on the relay.", e);
                }
            }
        }, new CronTrigger((String) props.getOrDefault(CRON, "")));
    }

    private void switchOn(int relay, String sensorKey) throws IOException {

        if (sensorKey != null && !sensors.isEnabled(sensorKey)) {
            log.warn("Sensor ({}) is false for relay {}, swith on is denied!", sensorKey, relay);
            return;
        }

        try {
            boolean[] statuses = rly8Service.getRelayStatus();

            boolean isOk;
            if(statuses[relay - 1]) {
                log.warn("The relay {} is already on", relay);
                isOk = true;
            } else {
                statuses[relay - 1] = true;
                Thread.sleep(300); // a minimum time between two RLY-8 access
                isOk = rly8Service.setRelayStatus(statuses);
            }
            if (isOk) {
                propertyStore.setTimestamp(relay, System.currentTimeMillis());
            }
        } catch (Exception e) {
            log.error("Cannot switch the relay {}", relay, e);
        }
    }

}
