package org.progetto.server.connection.rmi;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class RmiServer extends Thread {

    public RmiServer() {
        this.setName("RmiServerThread");
    }

    @Override
    public void run() {
        try {
            InetAddress localIP = getLocalNonLoopbackAddress();
            if (localIP == null) {
                throw new RuntimeException("Failed to determine local IP address");
            }

            String ipAddress = localIP.getHostAddress();
            System.setProperty("java.rmi.server.hostname", ipAddress);

            VirtualServer rmiClientHandler = new RmiServerReceiver();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("VirtualServer", rmiClientHandler);

            System.out.println("RMIServer listening on port 1099...");
        } catch (Exception e) {
            System.out.println("Error RMI server: " + e.getMessage());
        }
    }

    /**
     * Returns address of the local machine
     *
     * @return InetAddress of the first non-loopback IPv4 address, or null if none found
     */
    private InetAddress getLocalNonLoopbackAddress() throws SocketException {
        for (NetworkInterface ni : java.util.Collections.list(NetworkInterface.getNetworkInterfaces())) {
            if (ni.isUp() && !ni.isLoopback()) {
                for (InetAddress addr : java.util.Collections.list(ni.getInetAddresses())) {
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        return addr;
                    }
                }
            }
        }
        return null;
    }
}