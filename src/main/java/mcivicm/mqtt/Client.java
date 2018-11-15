package mcivicm.mqtt;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.*;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

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
        MQTT mqtt = new MQTT();
        IOptions iOptions = new IOptions.Builder().setHost(host).setId(id).build();
        iOptions.getOptions().setUserName(username);
        iOptions.getOptions().setPassword(password.toCharArray());
        iOptions.getOptions().setAutomaticReconnect(true);
        mqtt.setMqttCallback(new MqttCallback() {
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
        //连接
        mqtt.connect(iOptions).blockingAwait();
        AtomicReference<Disposable> disposable = new AtomicReference<>();
        //订阅
        mqtt.subscribeWithTopic(subscribe_topic, 2).subscribe(new Observer<Pair<String, MqttMessage>>() {
            @Override
            public void onSubscribe(Disposable d) {
                disposable.set(d);
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
                if (disposable.get() != null) {
                    disposable.get().dispose();
                }
                mqtt.unsubscribe(publish_topic).blockingAwait();
                mqtt.disconnect().blockingAwait();
                break;
            } else {
                //发送
                MqttMessage mm = new MqttMessage();
                mm.setRetained(true);
                mm.setQos(2);
                mm.setPayload("".equals(line) ? new byte[0] : line.getBytes());
                mqtt.publishWithResponse(publish_topic, mm)
                        .subscribe(new Observer<IMqttDeliveryToken>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onNext(IMqttDeliveryToken iMqttDeliveryToken) {
                                //等待执行结束
                                try {
                                    iMqttDeliveryToken.waitForCompletion();
                                } catch (MqttException e) {
                                    System.err.println("等待发送完成错误:" + e.getMessage());
                                }
                                if (iMqttDeliveryToken.isComplete()) {
                                    Exception exception = iMqttDeliveryToken.getException();
                                    if (exception == null) {
                                        System.out.println("发送成功");
                                    } else {
                                        System.err.println("发送错误:" + exception.getMessage());
                                    }
                                } else {
                                    System.err.println("发送未完成");
                                }
                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onComplete() {

                            }
                        });
            }
        }
        System.exit(1);
    }
}
