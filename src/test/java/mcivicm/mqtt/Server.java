package mcivicm.mqtt;

import java.util.Scanner;

public class Server {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("enter host: ");
        String host = scanner.nextLine();
        System.out.println("enter id: ");
        String id = scanner.nextLine();
        MQTT.instance().connect(new IOptions.Builder().setHost(host).setId(id).build()).blockingAwait();
        String line;
        while (true) {
            System.out.println("enter message or quit: ");
            line = scanner.nextLine();
            if ("quit".equals(line)) {
                break;
            } else {
                MQTT.instance().publish("weather", line, 1).blockingAwait();
            }
        }
        System.exit(1);
    }
}
