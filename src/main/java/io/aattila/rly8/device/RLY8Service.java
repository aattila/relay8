package io.aattila.rly8.device;

import io.aattila.rly8.util.Device;
import io.aattila.rly8.util.DummyDevice;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

@Service
public class RLY8Service implements Device {

    Logger log = LoggerFactory.getLogger(RLY8Service.class);

    private SocketAddress address;
    DummyDevice dummyDevice;

    @Value("${rly8.host}")
    private String host;

    @Value("${rly8.port}")
    private int port;

    @PostConstruct
    public void init() throws Exception {

        address = new InetSocketAddress(host, port);

        String dummy = System.getProperty("dummy.device", "false");
        if (Boolean.parseBoolean(dummy)) {
            log.warn("Dummy mode is set on, no RLY-8 device will used!");
            dummyDevice = new DummyDevice();
        }

    }

    public boolean setRelayStatus(byte pattern) throws Exception {
        return setRelayStatus(convert(pattern));
    }

    public boolean setRelayStatus(boolean[] relays) throws Exception {
        JSONObject json = new JSONObject();
        for (int i = 0; i < relays.length; i++) {
            json.put(RELAY + (i + 1), relays[i] ? ON : OFF);
        }
        String response = send(json.toString());
        return convertResponse(response);
    }

    public boolean[] getRelayStatus() throws Exception {
        String rsp = getValue(RELAYSTATUS);
        JSONObject json = new JSONObject(rsp);
        boolean[] relays = new boolean[8];
        for (int i = 0; i < relays.length; i++) {
            String value = json.getString(RELAY + (i + 1));
            relays[i] = ON.equalsIgnoreCase(value);
        }
        return relays;
    }


    public boolean setName(String name) throws Exception {
        String response = setValue(NAME, name);
        return convertResponse(response);

    }

    public String getName() throws Exception {
        JSONObject json = new JSONObject(getValue(NAME));
        return json.getString(NAME);
    }

    public boolean setNetconfig(String ip, String gateway, String netmask, String port) throws Exception {
        JSONObject json = new JSONObject();
        json.put(IPADDR, ip);
        json.put(GATEWAY, gateway);
        json.put(NETMASK, netmask);
        json.put(PORT, port);
        String response = send(json.toString());
        return convertResponse(response);

    }

    public String getNetconfig() throws Exception {
        return getValue(NETCONFIG);
    }

    public String getVersion() throws Exception {
        JSONObject json = new JSONObject(getValue(VERSION));
        return json.getString(VERSION);
    }

    public boolean setRS485addr(String addr) throws Exception {
        String response = setValue(RS485ADDR, addr);
        return convertResponse(response);

    }

    public String getRS485addr() throws Exception {
        JSONObject json = new JSONObject(getValue(RS485ADDR));
        return json.getString(RS485ADDR);
    }

    public boolean setBaudrate(String baudrate) throws Exception {
        String response = setValue(BAUDRATE, baudrate);
        return convertResponse(response);

    }

    public String getBaudrate() throws Exception {
        JSONObject json = new JSONObject(getValue(BAUDRATE));
        return json.getString(BAUDRATE);
    }

    public boolean setDhcp(boolean isEnabled) throws Exception {
        String response = setValue(DHCP, isEnabled ? ON : OFF);
        return convertResponse(response);

    }

    public boolean getDhcp() throws Exception {
        JSONObject json = new JSONObject(getValue(DHCP));
        return ON.equalsIgnoreCase(json.getString(DHCP));
    }

    public void restart() throws Exception {
        setValue(REBOOT, "1");
    }


    private String setValue(String key, String value) throws Exception {
        return send("{\"" + key + "\":\"" + value + "\"}");
    }

    private String getValue(String status) throws Exception {
        return send("{\"" + GET + "\":\"" + status + "\"}");
    }

    private String send(String msg) throws Exception {
        log.info("Connecting to RLY-8 at {}:{}", host, port);

        if (dummyDevice != null) {
            dummyDevice.setMessage(msg);
            return dummyDevice.getResponse();
        }

        Socket socket = new Socket();
        socket.connect(address);
        socket.setReuseAddress(true);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        log.info(">> {}", msg);
        out.println(msg);
        InputStream is = socket.getInputStream();
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int c = 0;
        while ((c = is.read()) > 0) {
            buf.write(c);
        }
        String response = buf.toString();
        log.info("<< {}", response);
        buf.close();
        out.close();
        socket.close();
        return response;
    }

    private boolean convertResponse(String response) {
        JSONObject json = new JSONObject(response);
        String value = json.getString(RESP);
        return OK.equalsIgnoreCase(value);
    }

    private boolean[] convert(byte pattern) {
        boolean[] result = new boolean[Byte.SIZE];
        for (int i = 0; i < Byte.SIZE; i++) {
            result[Byte.SIZE - i - 1] = (pattern >> i & 0x1) != 0x0;
        }
        return result;
    }
}
