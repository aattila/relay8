package io.aattila.rly8.device;

import io.aattila.rly8.util.PropertyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class RLY8Controller {

    Logger log = LoggerFactory.getLogger(RLY8Controller.class);

    @Autowired
    RLY8Service rly8Service;

    @Autowired
    PropertyStore propertyStore;

    @Value("${rly8.switchback.relay1}")
    private int switchbackTime1;
    @Value("${rly8.switchback.relay2}")
    private int switchbackTime2;
    @Value("${rly8.switchback.relay3}")
    private int switchbackTime3;
    @Value("${rly8.switchback.relay4}")
    private int switchbackTime4;
    @Value("${rly8.switchback.relay5}")
    private int switchbackTime5;
    @Value("${rly8.switchback.relay6}")
    private int switchbackTime6;
    @Value("${rly8.switchback.relay7}")
    private int switchbackTime7;
    @Value("${rly8.switchback.relay8}")
    private int switchbackTime8;


    @RequestMapping(value = "rly8/set", params = {"relay", "status"}, method = RequestMethod.POST)
    public boolean setRelays(@RequestParam("relay") int relay, @RequestParam("status") String pattern) throws Exception {
        boolean[] relays = new boolean[8];
        char[] chars = pattern.toCharArray();
        for (int i = 0; i < relays.length; i++) {
            relays[i] = ('1' == chars[i]);
        }
        propertyStore.setTimestamp(relay, System.currentTimeMillis());
        return rly8Service.setRelayStatus(relays);
    }

    @RequestMapping(value = "rly8/get", method = RequestMethod.GET)
    public String getRelays() throws Exception {
        String response = "";
        boolean[] relays = rly8Service.getRelayStatus();
        for (int i = 0; i < relays.length; i++) {
            response += relays[i] ? "1" : "0";
        }
        return response;
    }

    @RequestMapping(value = "rly8/name", method = RequestMethod.GET)
    public String getName() throws Exception {
        return rly8Service.getName();
    }


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

        if(switchbackDetected) {

            boolean isSwitchbackOk = false;

            try {
                boolean[] statuses =  rly8Service.getRelayStatus();
                for(int i=0; i<8; i++) {
                    if(switchbackChecks[i]) {
                        log.info("Switchback detected for the relay {}", i+1);
                        statuses[i] = !statuses[i];
                    }
                }
                Thread.sleep(300); // a minimum time between two RLY-8 access
                isSwitchbackOk = rly8Service.setRelayStatus(statuses);
            } catch (Exception e) {
                log.error("Cannot do switchback", e);
            }

            if(isSwitchbackOk) {
                for(int i=0; i<8; i++) {
                    if(switchbackChecks[i]) {
                        propertyStore.setTimestamp(i+1, 0);
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

}
