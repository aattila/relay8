package io.aattila.rly8.device;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RLY8Controller {

    @Autowired
    RLY8Service rly8Service;


    @RequestMapping(value = "rly8/set", params = {"status"}, method = RequestMethod.POST)
    public boolean setRelays(@RequestParam("status") String pattern) throws Exception {
        boolean[] relays = new boolean[8];
        char[] chars = pattern.toCharArray();
        for (int i = 0; i < relays.length; i++) {
            relays[i] = ('1' == chars[i]);
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
