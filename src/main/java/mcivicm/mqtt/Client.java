package mcivicm.mqtt;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.*;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        String name = "application.properties";
        InputStream inner = Client.class.getClassLoader().getResourceAsStream(name);
        if (inner == null) {
            System.err.println("can not find outer application.properties.");
        } else {
            System.out.println("load inner properties.");
            System.getProperties().load(inner);
        }
        String jarDir = System.getProperty("user.dir");
        System.out.println("jarDir=" + jarDir);
        File file = new File(jarDir + File.separator + name);
        System.out.println("outer properties=" + file.getPath());
        if (file.exists()) {
            InputStream outer = new FileInputStream(file);
            System.out.println("load outer properties.");
            System.getProperties().load(outer);
            outer.close();
            String active = System.getProperty("active", "inner");
            File[] activeFiles = new File(jarDir).listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".properties") && name.contains("-") && name.substring(name.indexOf("-") + 1, name.indexOf(".")).equals(active);
                }
            });
            if (activeFiles != null && activeFiles.length > 0) {
                InputStream activeFile = new FileInputStream(activeFiles[0]);
                System.out.println("load active properties.");
                System.getProperties().load(activeFile);
                activeFile.close();
            } else {
                System.err.println("can not find active application.properties.");
            }
        } else {
            System.err.println("can not find outer application.properties.");
        }
        String host = System.getProperty("host", "tcp://10.1.1.122:1883");
        String id = System.getProperty("id", "mqtt");
        String username = System.getProperty("username", "admin");
        String password = System.getProperty("password", "public");
        String publish_topic = System.getProperty("publish_topic", "mqtt_publish");
        String subscribe_topic = System.getProperty("subscribe_topic", "mqtt_subscribe");
        System.out.println(">>host=" + host);
        System.out.println(">>username=" + username);
        System.out.println(">>password=" + password);
        System.out.println(">>id=" + id);
        System.out.println(">>publish_topic=" + publish_topic);
        System.out.println(">>subscribe_topic=" + subscribe_topic);
        IOptions iOptions = new IOptions.Builder().setHost(host).setId(id).build();
        iOptions.getOptions().setUserName(username);
        iOptions.getOptions().setPassword(password.toCharArray());
        iOptions.getOptions().setAutomaticReconnect(true);
        MQTT.instance().setMqttCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                System.err.println(cause.getMessage());
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
        //Á¬½Ó
        MQTT.instance().connect(iOptions).blockingAwait();
        final Disposable[] disposable = new Disposable[1];
        //¶©ÔÄ
        MQTT.instance().subscribeWithTopic(subscribe_topic, 2).subscribe(new Observer<Pair<String, MqttMessage>>() {
            @Override
            public void onSubscribe(Disposable d) {
                disposable[0] = d;
            }

            @Override
            public void onNext(Pair<String, MqttMessage> pair) {
                System.out.println(pair.getKey() + "-" + new String(pair.getValue().getPayload()));
            }

            @Override
            public void onError(Throwable e) {
                System.out.println(e.getMessage());
            }

            @Override
            public void onComplete() {
                System.out.println("OnComplete");
            }
        });
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("enter quit to quit: ");
            String line = scanner.nextLine();
            if ("quit".equals(line)) {
                if (disposable[0] != null) {
                    disposable[0].dispose();
                }
                MQTT.instance().unsubscribe(publish_topic).blockingAwait();
                MQTT.instance().disconnect().blockingAwait();
                break;
            } else {
                //·¢ËÍ
                if ("".equals(line)) {
                    MQTT.instance().publish(publish_topic, null, 2).blockingAwait();
                } else {
                    MQTT.instance().publish(publish_topic, line, 2).blockingAwait();
                }
            }
        }
        System.exit(1);
    }
}
