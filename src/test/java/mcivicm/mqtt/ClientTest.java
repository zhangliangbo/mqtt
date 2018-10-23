package mcivicm.mqtt;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Scanner;

public class ClientTest {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("enter host: ");
        String host = scanner.nextLine();
        System.out.println("enter id: ");
        String id = scanner.nextLine();
        System.out.println("enter topic: ");
        String topic = scanner.nextLine();
        MQTT.instance().connect(new IOptions.Builder().setHost(host).setId(id).build()).blockingAwait();
        final Disposable[] disposable = new Disposable[1];
        MQTT.instance().subscribe(topic, 1).subscribe(new Observer<MqttMessage>() {
            @Override
            public void onSubscribe(Disposable d) {
                disposable[0] = d;
            }

            @Override
            public void onNext(MqttMessage mqttMessage) {
                System.out.println(new String(mqttMessage.getPayload()));
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
                break;
            }
        }
        System.exit(1);
    }
}