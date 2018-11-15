package mcivicm.mqtt;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.paho.client.mqttv3.*;

import java.util.concurrent.Callable;

public class MQTT {

    private IMqttClient iMqttClient;
    private IOptions iOptions;
    private MqttCallback mqttCallback;

    /**
     * 连接服务器
     *
     * @return
     */
    public Completable connect(IOptions options) {
        this.iOptions = options;
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                iMqttClient = new MqttClient(iOptions.getHost(), iOptions.getClientId(), iOptions.getPersistence());
                //设置mqtt回调
                if (mqttCallback != null) {
                    iMqttClient.setCallback(mqttCallback);
                }
                iMqttClient.connect(options.getOptions());
            }
        });
    }

    /**
     * 断开连接服务器
     *
     * @return
     */
    public Completable disconnect() {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                iMqttClient.disconnectForcibly();
            }
        });
    }

    /**
     * 发布主题
     *
     * @param topic
     * @param text
     * @return
     */
    public Completable publish(String topic, String text, int qos) {
        return publish(topic, text, qos, false);
    }

    /**
     * 发布主题
     *
     * @param topic
     * @param text
     * @return
     */
    public Completable publish(String topic, String text, int qos, boolean retained) {
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload(text == null ? new byte[0] : text.getBytes());
        mqttMessage.setQos(qos);
        mqttMessage.setRetained(retained);
        return publish(topic, mqttMessage);
    }

    /**
     * 发布主题
     *
     * @param topic
     * @param message
     * @return
     */
    public Completable publish(String topic, MqttMessage message) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                MqttTopic mt = iMqttClient.getTopic(topic);
                MqttDeliveryToken token = mt.publish(message);
                token.waitForCompletion();
            }
        });
    }

    /**
     * 有应答
     *
     * @param topic
     * @param text
     * @param qos
     * @param retained
     * @return
     */
    public Observable<IMqttDeliveryToken> publishWithResponse(String topic, String text, int qos, boolean retained) {
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload(text == null ? new byte[0] : text.getBytes());
        mqttMessage.setQos(qos);
        mqttMessage.setRetained(retained);
        return publishWithResponse(topic, mqttMessage);
    }

    /**
     * 发布主题，有应答
     *
     * @param topic
     * @param message
     * @return
     */
    public Observable<IMqttDeliveryToken> publishWithResponse(String topic, MqttMessage message) {
        return Observable.fromCallable(new Callable<IMqttDeliveryToken>() {
            @Override
            public IMqttDeliveryToken call() throws Exception {
                MqttTopic mt = iMqttClient.getTopic(topic);
                return mt.publish(message);
            }
        });
    }

    /**
     * 订阅某个主题
     *
     * @return
     */
    public Observable<MqttMessage> subscribe(String topic, int qos) {
        return subscribeWithTopic(topic, qos)
                .map(new Function<Pair<String, MqttMessage>, MqttMessage>() {
                    @Override
                    public MqttMessage apply(Pair<String, MqttMessage> stringMqttMessagePair) throws Exception {
                        return stringMqttMessagePair.getValue();
                    }
                });
    }

    /**
     * 订阅某个主题
     *
     * @return
     */
    public Observable<Pair<String, MqttMessage>> subscribeWithTopic(String topic, int qos) {
        return Observable
                .create(new ObservableOnSubscribe<Pair<String, MqttMessage>>() {
                    @Override
                    public void subscribe(ObservableEmitter<Pair<String, MqttMessage>> emitter) throws Exception {
                        iMqttClient.subscribe(topic, qos, new IMqttMessageListener() {
                            @Override
                            public void messageArrived(String topic, MqttMessage message) throws Exception {
                                emitter.onNext(Pair.of(topic, message));
                            }
                        });
                    }
                });
    }

    /**
     * 取消订阅某个主题
     *
     * @param topic
     */
    public Completable unsubscribe(String topic) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                iMqttClient.unsubscribe(topic);
            }
        });
    }

    /**
     * 设置mqtt回调
     *
     * @param callback
     */
    public void setMqttCallback(MqttCallback callback) {
        if (iMqttClient == null) {
            //没有初始化先存着
            mqttCallback = callback;
        } else {
            iMqttClient.setCallback(callback);
        }
    }

}
