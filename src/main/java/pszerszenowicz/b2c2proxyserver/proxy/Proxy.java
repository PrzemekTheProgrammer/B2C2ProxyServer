package pszerszenowicz.b2c2proxyserver.proxy;

import pszerszenowicz.b2c2proxyserver.client.TargetConnection;
import pszerszenowicz.b2c2proxyserver.server.Server;

public class Proxy {

    public static void main(String[] args) throws InterruptedException {
        start();
    }

    private static void start() throws InterruptedException {

        TargetConnection targetConnection = new TargetConnection();
        Server server = new Server();

        server.start(targetConnection);
        targetConnection.start(server);

    }
}
