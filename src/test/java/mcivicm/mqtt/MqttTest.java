package mcivicm.mqtt;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Scanner;

public class MqttTest {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("enter host: ");
        String host = scanner.nextLine();
        System.out.println("enter id: ");
        String id = scanner.nextLine();
        System.out.println("enter publish topic: ");
        String publish_topic = scanner.nextLine();
        System.out.println("enter subscribe topic: ");
        String subscribe_topic = scanner.nextLine();
        MQTT.instance().connect(new IOptions.Builder().setHost(host).setId(id).build()).blockingAwait();
        final Disposable[] disposable = new Disposable[1];
        MQTT.instance().subscribe(subscribe_topic, 1).subscribe(new Observer<MqttMessage>() {
            @Override
            public void onSubscribe(Disposable d) {
                disposable[0] = d;
            }

            @Override
            public void onNext(MqttMessage mqttMessage) {
                System.out.println("receive: " + new String(mqttMessage.getPayload()));
            }

            @Override
            public void onError(Throwable e) {
                System.err.println(e.getMessage());
            }

            @Override
            public void onComplete() {
                System.out.println("OnComplete");
            }
        });
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
                MQTT.instance().publish(publish_topic, line, 1).blockingAwait();
            }
        }
        System.exit(1);
    }
}
