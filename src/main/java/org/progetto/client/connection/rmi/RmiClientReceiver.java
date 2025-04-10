package org.progetto.client.connection.rmi;

import org.progetto.client.connection.GuiHandlerMessage;
import org.progetto.client.connection.TuiHandlerMessage;
import org.progetto.client.model.GameData;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RmiClientReceiver extends UnicastRemoteObject implements VirtualClient {

    private static RmiClientReceiver instance = null;

    protected RmiClientReceiver() throws RemoteException {
        super();
    }

    public static RmiClientReceiver getInstance() throws RemoteException {
        if (instance == null)
            instance = new RmiClientReceiver();

        return instance;
    }

    /**
     * Method called by the server to notify changes to the client
     */
    @Override
    public void sendMessage(Object objMessage) throws RemoteException {
        if(GameData.getUIType().equals("GUI"))
            GuiHandlerMessage.handleMessage(objMessage);
        else if (GameData.getUIType().equals("TUI"))
            TuiHandlerMessage.handleMessage(objMessage);
    }
}