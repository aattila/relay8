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

    @Autowired
    RLY8Service rly8Service;

    @Autowired
    PropertyStore propertyStore;

    @RequestMapping(value = "rly8/set", params = {"relay", "status"}, method = RequestMethod.POST)
    public boolean setRelays(@RequestParam("relay") int relay, @RequestParam("status") String pattern) throws Exception {
        boolean[] relays = new boolean[8];
        char[] chars = pattern.toCharArray();
        for (int i = 0; i < relays.length; i++) {
            relays[i] = ('1' == chars[i]);
            if(relays[i]) {
                // relay on
                propertyStore.setTimestamp(relay, System.currentTimeMillis());
            }
            else {
                // relay off
                propertyStore.setTimestamp(relay, 0);
            }
        }
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


}
