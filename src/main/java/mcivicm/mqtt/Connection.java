package mcivicm.mqtt;

import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Connection {

    private boolean reconnectQ = true;

    private String host;
    private String clientId;
    private String username;
    private String password;
    //0未连接，1正在连接，2已连接
    private AtomicInteger connectedQ = new AtomicInteger(0);
    private OnConnectionListener listener;
    private MQTT mqtt = new MQTT();
    private Scheduler scheduler = Schedulers.from(Executors.newSingleThreadExecutor());
    private List<Pair<String, MqttMessage>> toPublish = new LinkedList<>();

    public Connection(String host, String clientId, String username, String password, OnConnectionListener listener) {
        this.host = host;
        this.clientId = clientId;
        this.username = username;
        this.password = password;
        this.listener = listener;
        this.mqtt.setMqttCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                connectedQ.set(0);
                if (reconnectQ) {
                    connectTask().subscribe(new SimpleCompletableObserver());
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

            }
        });
        //开始连接
        connectTask().subscribe(new SimpleCompletableObserver());
    }

    public Completable disconnect() {
        //强制关闭连接后不重新连接
        reconnectQ = false;
        return this.mqtt.disconnect().subscribeOn(scheduler);
    }

    public int getConnectedQ() {
        return connectedQ.get();
    }

    /**
     * 发布
     *
     * @param topic
     * @param mqttMessage
     * @return
     */
    public Observable<IMqttDeliveryToken> publish(String topic, MqttMessage mqttMessage) {
        if (connectedQ.get() == 2) {
            return mqtt.publishWithResponse(topic, mqttMessage)
                    .doOnNext(new Consumer<IMqttDeliveryToken>() {
                        @Override
                        public void accept(IMqttDeliveryToken iMqttDeliveryToken) throws Exception {
                            iMqttDeliveryToken.waitForCompletion();
                            if (iMqttDeliveryToken.getException() != null) {
                                //判断当前的连接状态
                                if (connectedQ.get() == 2) {
                                    //抛出异常给下游，触发重试机制
                                    throw iMqttDeliveryToken.getException();
                                } else {
                                    //未连接状态下直接放入缓存中
                                    toPublish.add(Pair.of(topic, mqttMessage));
                                }
                            }
                        }
                    })
                    .retry(new Predicate<Throwable>() {
                        @Override
                        public boolean test(Throwable throwable) throws Exception {
                            return throwable instanceof MqttException;
                        }
                    })
                    .subscribeOn(scheduler);
        } else {
            //cache.
            toPublish.add(Pair.of(topic, mqttMessage));
            //do nothing.
            return Observable.empty();
        }
    }

    /**
     * 订阅
     *
     * @param topic
     * @param qos
     * @return
     */
    public Observable<Pair<String, MqttMessage>> subscribe(final String topic, final int qos) {
        if (connectedQ.get() == 2) {
            return mqtt.subscribeWithTopic(topic, qos).subscribeOn(scheduler);
        } else {
            return Observable.empty();
        }
    }

    /**
     * 连接
     *
     * @return
     */
    private Completable connectTask() {
        return Observable
                .fromCallable(new Callable<IOptions>() {
                    @Override
                    public IOptions call() throws Exception {
                        //配置参数
                        IOptions iOptions = new IOptions.Builder().setHost(host).setId(clientId).build();
                        if (!StringUtils.isEmpty(username)) {
                            iOptions.getOptions().setUserName(username);
                        }
                        if (!StringUtils.isEmpty(password)) {
                            iOptions.getOptions().setPassword(password.toCharArray());
                        }
                        return iOptions;
                    }
                })
                .flatMapCompletable(new Function<IOptions, CompletableSource>() {
                    @Override
                    public CompletableSource apply(IOptions iOptions) throws Exception {
                        //连接
                        return mqtt
                                .connect(iOptions)
                                .doOnSubscribe(new Consumer<Disposable>() {
                                    @Override
                                    public void accept(Disposable disposable) throws Exception {
                                        connectedQ.set(1);
                                    }
                                })
                                .doOnComplete(new Action() {
                                    @Override
                                    public void run() throws Exception {
                                        connectedQ.set(2);
                                        //发送连接成功的消息
                                        if (listener != null) {
                                            //订阅该订阅的主题
                                            listener.onConnected(mqtt);
                                        }
                                        publishRemnant();
                                    }
                                })
                                .doOnError(new Consumer<Throwable>() {
                                    @Override
                                    public void accept(Throwable throwable) throws Exception {
                                        connectedQ.set(0);
                                    }
                                });
                    }
                })
                //在后台进行
                .subscribeOn(scheduler);
    }

    /**
     * 发送由于未连接或者连接中而缓存起来的消息
     */
    private void publishRemnant() {
        if (toPublish.size() > 0) {
            ListIterator<Pair<String, MqttMessage>> iterator = toPublish.listIterator();
            while (iterator.hasNext()) {
                Pair<String, MqttMessage> next = iterator.next();
                publish(next.getKey(), next.getValue()).subscribe(new PublishObserver());
                iterator.remove();
            }
        }
    }

    public interface OnConnectionListener {
        void onConnected(MQTT mqtt);
    }

    private class SimpleCompletableObserver implements CompletableObserver {

        @Override
        public void onComplete() {

        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onSubscribe(Disposable d) {

        }
    }

}
