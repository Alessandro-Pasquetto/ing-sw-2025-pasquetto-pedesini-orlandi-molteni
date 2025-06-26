package org.progetto.client.connection;

public class ClientDisconnectionDetection {

    // =======================
    // ATTRIBUTES
    // =======================

    private static int serverDisconnectionDetectionInterval;
    private static boolean pingIsArrived;

    // =======================
    // CONSTRUCTORS
    // =======================

    public static void init(int serverDisconnectionDetectionInterval) {
        ClientDisconnectionDetection.serverDisconnectionDetectionInterval = serverDisconnectionDetectionInterval;
        ClientDisconnectionDetection.pingIsArrived = true;
    }

    public static boolean getIsWaitingPongGame() {
        return ClientDisconnectionDetection.pingIsArrived;
    }

    // =======================
    // SETTERS
    // =======================

    public static void setPingIsArrived(boolean pingIsArrived) {
        ClientDisconnectionDetection.pingIsArrived = pingIsArrived;
    }

    // =======================
    // OTHER METHODS
    // =======================

    public static void startWatchdog(Sender sender){
        Thread checkPingThread = new Thread(() -> {

            while (true) {

                if(!getIsWaitingPongGame()){
                    sender.disconnected();
                    return;
                }

                ClientDisconnectionDetection.setPingIsArrived(false);

                try {
                    Thread.sleep(serverDisconnectionDetectionInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        checkPingThread.setDaemon(true);
        checkPingThread.start();
    }
}