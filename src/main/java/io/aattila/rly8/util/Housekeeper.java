package io.aattila.rly8.util;

import io.aattila.rly8.device.RLY8Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class Housekeeper {

    Logger log = LoggerFactory.getLogger(Housekeeper.class);

    @Autowired
    PropertyStore propertyStore;

    @Autowired
    RLY8Service rly8Service;

    @Value("${rly8.relay1.switchback}")
    private int switchbackTime1;
    @Value("${rly8.relay1.auto}")
    private boolean auto1;

    @Value("${rly8.relay2.switchback}")
    private int switchbackTime2;
    @Value("${rly8.relay2.auto}")
    private boolean auto2;

    @Value("${rly8.relay3.switchback}")
    private int switchbackTime3;
    @Value("${rly8.relay3.auto}")
    private boolean auto3;

    @Value("${rly8.relay4.switchback}")
    private int switchbackTime4;
    @Value("${rly8.relay4.auto}")
    private boolean auto4;

    @Value("${rly8.relay5.switchback}")
    private int switchbackTime5;
    @Value("${rly8.relay5.auto}")
    private boolean auto5;

    @Value("${rly8.relay6.switchback}")
    private int switchbackTime6;
    @Value("${rly8.relay6.auto}")
    private boolean auto6;

    @Value("${rly8.relay7.switchback}")
    private int switchbackTime7;
    @Value("${rly8.relay7.auto}")
    private boolean auto7;

    @Value("${rly8.relay8.switchback}")
    private int switchbackTime8;
    @Value("${rly8.relay8.auto}")
    private boolean auto8;

    @Scheduled(initialDelay = 30000, fixedRate = 30000)
    public void switchback() {

        Boolean[] switchbackChecks = new Boolean[8];
        switchbackChecks[0] = isSwitchbackNeeded(1, switchbackTime1);
        switchbackChecks[1] = isSwitchbackNeeded(2, switchbackTime2);
        switchbackChecks[2] = isSwitchbackNeeded(3, switchbackTime3);
        switchbackChecks[3] = isSwitchbackNeeded(4, switchbackTime4);
        switchbackChecks[4] = isSwitchbackNeeded(5, switchbackTime5);
        switchbackChecks[5] = isSwitchbackNeeded(6, switchbackTime6);
        switchbackChecks[6] = isSwitchbackNeeded(7, switchbackTime7);
        switchbackChecks[7] = isSwitchbackNeeded(8, switchbackTime8);

        boolean switchbackDetected = Arrays.asList(switchbackChecks).stream()
                .filter(switchbackCheck -> switchbackCheck)
                .collect(Collectors.toList())
                .size() > 0;

        if (switchbackDetected) {

            boolean isSwitchbackOk = false;

            try {
                boolean[] statuses = rly8Service.getRelayStatus();
                for (int i = 0; i < 8; i++) {
                    if (switchbackChecks[i]) {
                        log.info("Switchback detected for the relay {}", i + 1);
                        statuses[i] = !statuses[i];
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

    @Scheduled(cron = "${rly8.relay1.cron}")
    public void relay1() {
        if (auto1) {
            switchOn(1);
        }
    }

    @Scheduled(cron = "${rly8.relay2.cron}")
    public void relay2() {
        if (auto2) {
            switchOn(2);
        }
    }

    @Scheduled(cron = "${rly8.relay3.cron}")
    public void relay3() {
        if (auto3) {
            switchOn(3);
        }
    }

    @Scheduled(cron = "${rly8.relay4.cron}")
    public void relay4() {
        if (auto4) {
            switchOn(4);
        }
    }

    @Scheduled(cron = "${rly8.relay5.cron}")
    public void relay5() {
        if (auto5) {
            switchOn(5);
        }
    }

    @Scheduled(cron = "${rly8.relay6.cron}")
    public void relay6() {
        if (auto6) {
            switchOn(6);
        }
    }

    @Scheduled(cron = "${rly8.relay7.cron}")
    public void relay7() {
        if (auto7) {
            switchOn(7);
        }
    }

    @Scheduled(cron = "${rly8.relay8.cron}")
    public void relay8() {
        if (auto8) {
            switchOn(8);
        }
    }

    private void switchOn(int relay) {
        try {
            boolean[] statuses = rly8Service.getRelayStatus();
            statuses[relay-1] = true;
            Thread.sleep(300); // a minimum time between two RLY-8 access
            boolean isOk = rly8Service.setRelayStatus(statuses);
            if (isOk) {
                propertyStore.setTimestamp(relay, System.currentTimeMillis());
            }
        } catch (Exception e) {
            log.error("Cannot switch the relay {}", relay, e);
        }
    }
}
