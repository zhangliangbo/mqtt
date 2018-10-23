package mcivicm.mqtt;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.Test;

public class MqttTest {
    @Test
    public void mqtt1() {
        MQTT.instance()
                .connect(new IOptions.Builder().setHost("tcp://10.1.1.122:1883").setId("receive").build())
                .blockingAwait();
        MQTT.instance().subscribe("weather", 1)
                .blockingSubscribe(new Observer<MqttMessage>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(MqttMessage mqttMessage) {
                        System.out.println(new String(mqttMessage.getPayload()));
                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.println(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
