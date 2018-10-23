package mcivicm.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Scanner;

public class MqttTest {

    public static final String uri = "tcp://localhost:1883";

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void async() {

    }

    public static void main(String[] args) {
        try {
            MqttAsyncClient mqttAsyncClient = new MqttAsyncClient(uri, "zlb");
            MqttConnectOptions options = new MqttConnectOptions();
            options.setConnectionTimeout(60);
            options.setCleanSession(false);
            options.setKeepAliveInterval(20);
            options.setUserName("zhangliangbo");
            options.setPassword("123456".toCharArray());
            IMqttToken iMqttToken = mqttAsyncClient.connect(options);
            iMqttToken.waitForCompletion();
            System.out.println("connected.");
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("please enter:");
                String line = scanner.nextLine();
                if ("quit".equals(line)) {
                    break;
                } else {
                    MqttMessage mqttMessage = new MqttMessage();
                    mqttMessage.setId(1);
                    mqttMessage.setQos(1);
                    mqttMessage.setRetained(true);
                    mqttMessage.setPayload(line.getBytes());
                    IMqttDeliveryToken token = iMqttToken.getClient().publish("order", mqttMessage);
                    token.waitForCompletion();
                    System.out.println("send.");
                }
            }
            System.exit(0);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
