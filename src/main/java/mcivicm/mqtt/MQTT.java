package mcivicm.mqtt;

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


}
