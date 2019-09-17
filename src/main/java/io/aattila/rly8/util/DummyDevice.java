package io.aattila.rly8.util;

import io.aattila.rly8.device.RLY8Service;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DummyDevice implements Device {

    Logger log = LoggerFactory.getLogger(DummyDevice.class);

    private String message;

    private String[] relay = new String[8];

    private String netconfig = "";
    private String name = "";
    private String version = "";
    private String rs485addr = "";
    private String baudrate = "";
    private String dhcp = "";

    public DummyDevice() {
        for (int i = 0; i < 8; i++) {
            relay[i] = "off";
        }
    }

    public void setMessage(String message) {
        this.message = message;
        log.info(">> {}", message);
        JSONObject json = new JSONObject(message);
        if (message.contains(RELAY + "1")) {
            for (int i = 1; i <= 8; i++) {
                relay[i - 1] = json.getString(RELAY + i);
            }
        } else if (message.contains(NETCONFIG)) {
            netconfig = json.getString(NETCONFIG);
        } else if (message.contains(NAME)) {
            name = json.getString(NAME);
        } else if (message.contains(VERSION)) {
            version = json.getString(VERSION);
        } else if (message.contains(RS485ADDR)) {
            rs485addr = json.getString(RS485ADDR);
        } else if (message.contains(BAUDRATE)) {
            baudrate = json.getString(BAUDRATE);
        } else if (message.contains(DHCP)) {
            dhcp = json.getString(DHCP);
        }
    }

    public String getResponse() {
        JSONObject json = new JSONObject();

        if (message.contains(GET)) {
            if (message.contains(RELAYSTATUS)) {
                for (int i = 1; i <= 8; i++) {
                    json.put(RELAY + i, relay[i - 1]);
                }
            } else if (message.contains(NETCONFIG)) {
                json.put(NETCONFIG, netconfig);
            } else if (message.contains(NAME)) {
                json.put(NAME, name);
            } else if (message.contains(VERSION)) {
                json.put(VERSION, version);
            } else if (message.contains(RS485ADDR)) {
                json.put(RS485ADDR, rs485addr);
            } else if (message.contains(BAUDRATE)) {
                json.put(BAUDRATE, baudrate);
            } else if (message.contains(DHCP)) {
                json.put(DHCP, dhcp);
            }
        } else {
            json.put(RESP, "ok");
        }

        String response = json.toString();
        log.info("<< {}", response);

        return response;
    }


}
