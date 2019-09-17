package io.aattila.rly8.util;

public interface DynamicProperties {

    String FILE_NAME = System.getProperty("user.dir") + "/triggers.yml";
    String TRIGGERS = "triggers";
    String CRON = "cron";
    String RELAY = "relay";
    String SWITCHBACK = "switchback";
    String SENSORS = "sensors";
    String SENSOR = "sensor";
    String TYPE = "type";
    String SOURCE = "source";
    String KEY = "key";

}
