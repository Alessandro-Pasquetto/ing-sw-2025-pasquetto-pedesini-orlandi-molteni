package org.progetto.client;

import org.progetto.client.connection.rmi.RmiClientSender;
import org.progetto.client.connection.socket.SocketClient;
import org.progetto.client.model.GameData;

import java.io.IOException;
import java.util.Scanner;

public class TuiCommandFilter {

    private static Scanner scanner = new Scanner(System.in);

    public static void setProtocol(){
        System.out.println();
        System.out.println("Select Socket/Rmi:");
        while(true){
            String protocol = scanner.nextLine().toUpperCase();

            if(protocol.equals("SOCKET")){
                GameData.setSender(new SocketClient());
                break;
            } else if (protocol.equals("RMI")){
                GameData.setSender(new RmiClientSender());
                break;
            } else{
                System.out.println("Command not found");
            }
        }
    }

   public static void listenCommand(){

       while(true){
           System.out.println();
           System.out.println("Command: ");

           String command = scanner.nextLine();

           if(command.equals("exit")) break;

           handleCommand(command);
       }


       GameData.getSender().close();
   }

   private static boolean isValidCommand(int commandLength, int l){
        if(commandLength == l)
            return true;
        else {
            System.out.println("Invalid command format");
            return false;
        }

   }

   private static String requestNotEmptyData(String request){

       System.out.println();
       System.out.println(request + ":");
       while(true){
           String data = scanner.nextLine();

           if(!data.isEmpty())
               return data;
           else
               System.out.println("Invalid data");
       }
   }

   public static void handleCommand(String command){

       String[] commandParts = command.split(" ");

       switch(commandParts[0]){

           case "Connect":
               if(!isValidCommand(commandParts.length, 3))
                   return;

               String ip = commandParts[1];
               int port = Integer.parseInt(commandParts[2]);

               GameData.getSender().connect(ip, port);
               break;

           case "CreateGame":
               if(!isValidCommand(commandParts.length, 1))
                   return;

               GameData.setNamePlayer(requestNotEmptyData("Name"));
               GameData.getSender().createGame();
               break;

           case "JoinGame":
               if(!isValidCommand(commandParts.length, 2))
                   return;

               GameData.setNamePlayer(requestNotEmptyData("Name"));
               GameData.getSender().tryJoinToGame(Integer.parseInt(commandParts[1]));
               break;

           default:
               System.out.println("Command not found");
               break;
       }
   }
}