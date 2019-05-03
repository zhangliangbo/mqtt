package mcivicm.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;

public class MaxClient {
    public static void main(String[] args) throws IOException {
        for (int i = 0; i < 10000; i++) {
            String username = "user" + i;
            MQTT mqtt = new MQTT();
            IOptions iOptions = new IOptions.Builder().setHost("tcp://39.96.173.63:1883").setId("client" + i).build();
            iOptions.getOptions().setUserName(username);
            iOptions.getOptions().setAutomaticReconnect(true);
            mqtt.setMqttCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    System.err.println(cause.getMessage());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    System.out.println("from callback : " + topic + new String(message.getPayload()));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });
            //Á¬½Ó
            mqtt.connect(iOptions).blockingAwait();
            mqtt.subscribe(username, 1).subscribe();
            mqtt.publish(username, username, 1).blockingAwait();
            System.out.println(username + " connected.");
        }
        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(1);
    }
}
