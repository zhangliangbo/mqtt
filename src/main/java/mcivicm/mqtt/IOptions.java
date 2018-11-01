package mcivicm.mqtt;

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
     * 用户名
     *
     * @return
     */
    String getUsername();

    /**
     * 是否清空会话
     *
     * @return
     */
    boolean getCleanSession();


    /**
     * 密码
     *
     * @return
     */
    String getPassword();

    /**
     * 连接超时时间，秒
     *
     * @return
     */
    int getConnectionTimeout();

    /**
     * 发送心跳间隔，秒
     *
     * @return
     */
    int getKeepAliveInterval();

    class Builder {

        private String host = "tcp://localhost:1883";
        private String id = "client";
        private String username = "admin";
        private String password = "public";
        private int connectionTimeout = 6;
        private int keepAliveInterval = 20;
        private boolean cleanSession=true;

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
                public String getUsername() {
                    return username;
                }

                @Override
                public String getPassword() {
                    return password;
                }

                @Override
                public int getConnectionTimeout() {
                    return connectionTimeout;
                }

                @Override
                public int getKeepAliveInterval() {
                    return keepAliveInterval;
                }

                @Override
                public boolean getCleanSession() {
                    return cleanSession;
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

        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder setConnectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        public Builder setKeepAliveInterval(int keepAliveInterval) {
            this.keepAliveInterval = keepAliveInterval;
            return this;
        }

        public Builder setCleanSession(boolean cleanSession) {
            this.cleanSession = cleanSession;
            return this;
        }
    }

}
