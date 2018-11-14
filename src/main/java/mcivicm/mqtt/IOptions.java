package mcivicm.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public interface IOptions {
    /**
     * 服务器主机地址
     * 比如：tcp://localhost:1883
     *
     * @return
     */
    String getHost();

    /**
     * 客户端ID
     *
     * @return
     */
    String getClientId();


    /**
     * 序列化方式
     *
     * @return
     */
    MqttClientPersistence getPersistence();

    /**
     * 选项
     *
     * @return
     */
    MqttConnectOptions getOptions();

    class Builder {

        private String host = "tcp://localhost:1883";
        private String id = "client";
        private MqttClientPersistence persistence = new MemoryPersistence();
        private MqttConnectOptions options = new MqttConnectOptions();

        public IOptions build() {
            return new IOptions() {
                @Override
                public String getHost() {
                    return host;
                }

                @Override
                public String getClientId() {
                    return id;
                }

                @Override
                public MqttClientPersistence getPersistence() {
                    return persistence;
                }

                @Override
                public MqttConnectOptions getOptions() {
                    return options;
                }


            };
        }

        public Builder setHost(String host) {
            this.host = host;
            return this;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setPersistence(MqttClientPersistence persistence) {
            this.persistence = persistence;
            return this;
        }

        public Builder setOptions(MqttConnectOptions options) {
            this.options = options;
            return this;
        }
    }

}
