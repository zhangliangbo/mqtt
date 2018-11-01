package mcivicm.mqtt;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Action;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MQTT {
    /**
     * 静态类持有实例
     */
    private static class Holder {
        static MQTT mqtt = new MQTT();
    }

    /**
     * 获取唯一的实例
     *
     * @return
     */
    public static MQTT instance() {
        return Holder.mqtt;
    }

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
                iMqttClient = new MqttClient(iOptions.getHost(), iOptions.getClientId(), new MemoryPersistence());
                //设置mqtt回调
                if (mqttCallback != null) {
                    iMqttClient.setCallback(mqttCallback);
                }
                MqttConnectOptions options = new MqttConnectOptions();
                options.setUserName(iOptions.getUsername());
                options.setPassword(iOptions.getPassword().toCharArray());
                options.setCleanSession(iOptions.getCleanSession());
                options.setConnectionTimeout(iOptions.getConnectionTimeout());
                options.setKeepAliveInterval(iOptions.getKeepAliveInterval());
                iMqttClient.connect(options);
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
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                MqttTopic mt = iMqttClient.getTopic(topic);
                MqttDeliveryToken token = mt.publish(text.getBytes(), qos, true);
                token.waitForCompletion();
            }
        });
    }

    /**
     * 订阅某个主题
     *
     * @return
     */
    public Observable<MqttMessage> subscribe(String topic, int qos) {
        return Observable
                .create(new ObservableOnSubscribe<MqttMessage>() {
                    @Override
                    public void subscribe(ObservableEmitter<MqttMessage> emitter) throws Exception {
                        iMqttClient.subscribe(topic, qos, new IMqttMessageListener() {
                            @Override
                            public void messageArrived(String topic, MqttMessage message) throws Exception {
                                emitter.onNext(message);
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
                iMqttClient.subscribe(topic);
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
