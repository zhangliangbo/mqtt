package mcivicm.mqtt;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class SubscribeObserver implements Observer<Pair<String, MqttMessage>> {
    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onNext(Pair<String, MqttMessage> pair) {

    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onComplete() {

    }
}
