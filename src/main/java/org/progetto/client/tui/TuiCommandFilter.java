package org.progetto.client.tui;

import org.progetto.client.connection.rmi.RmiClientSender;
import org.progetto.client.connection.socket.SocketClient;
import org.progetto.client.model.GameData;

import java.util.Scanner;

public class TuiCommandFilter {

    // =======================
    // OTHER METHODS
    // =======================

    private static Scanner scanner = new Scanner(System.in);

    public static void setProtocol(){
        System.out.println();
        System.out.println("Select Socket/Rmi:");
        while(true){
            String protocol = scanner.nextLine().toUpperCase();

            if(protocol.equals("SOCKET")){
                GameData.setSender(new SocketClient());
                System.out.println("Socket selected");
                break;
            } else if (protocol.equals("RMI")){
                GameData.setSender(new RmiClientSender());
                System.out.println("RMI selected");
                break;
            } else{
                System.out.println("Command not found");
            }
        }
    }

   public static void listenCommand(){

       while(true){
           System.out.println();

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

   public static String requestNotEmptyData(String request){

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

            //todo add an help command to read from a .json file

           case "Connect":
               if(!isValidCommand(commandParts.length, 3))
                   return;
               ConnectionsCommands.connect(commandParts);
               break;

           case "CreateGame":
               if(!isValidCommand(commandParts.length, 2))
                   return;
               ConnectionsCommands.createGame(commandParts);
               break;

           case "JoinGame":
               if(!isValidCommand(commandParts.length, 3))
                   return;
               ConnectionsCommands.joinGame(commandParts);
               break;

           case "PickHidden":
               if(!isValidCommand(commandParts.length, 1))
                   return;
               BuildingCommands.pickHiddenComponent(commandParts);
               break;

           case "PickVisible":
               if(!isValidCommand(commandParts.length, 2))
                   return;
               BuildingCommands.pickVisibleComponent(commandParts);
               break;

           case "PlaceLast":
               if(!isValidCommand(commandParts.length, 4))
                   return;
               BuildingCommands.placeLastComponent(commandParts);
               break;

           case "PlaceAndPickHidden":
               if(!isValidCommand(commandParts.length, 4))
                   return;
               BuildingCommands.placeHandComponentAndPickHiddenComponent(commandParts);
               break;

           case "PlaceAndPickVisible":
               if(!isValidCommand(commandParts.length, 5))
                   return;
               BuildingCommands.placeHandComponentAndPickVisibleComponent(commandParts);
               break;

           case "PlaceAndPickEvent":
               if(!isValidCommand(commandParts.length, 5))
                   return;
               BuildingCommands.placeHandComponentAndPickUpEventCardDeck(commandParts);
               break;

           case "PlaceAndPickBooked":
               if(!isValidCommand(commandParts.length, 5))
                   return;
               BuildingCommands.placeHandComponentAndPickBookedComponent(commandParts);
               break;

           case "PlaceAndReady":
               if (!isValidCommand(commandParts.length, 4))
                   return;
               BuildingCommands.placeHandComponentAndReady(commandParts);
               break;

           case "Discard":
               if (!isValidCommand(commandParts.length, 1))
                   return;
               BuildingCommands.discardComponent(commandParts);
               break;

           case "Book":
               if (!isValidCommand(commandParts.length, 2))
                   return;
               BuildingCommands.bookComponent(commandParts);
               break;

           case "PickBooked":
               if (!isValidCommand(commandParts.length, 2))
                   return;
               BuildingCommands.pickBookedComponent(commandParts);
               break;

           case "PickDeck":
               if (!isValidCommand(commandParts.length, 2))
                   return;
               BuildingCommands.pickUpEventCardDeck(commandParts);
               break;

           case "PutDownDeck":
               if (!isValidCommand(commandParts.length, 1))
                   return;
               BuildingCommands.putDownEventCardDeck(commandParts);
               break;

           case "Destroy":
               if (!isValidCommand(commandParts.length, 3))
                   return;
               BuildingCommands.destroyComponent(commandParts);
               break;

           case "Ready":
               if (!isValidCommand(commandParts.length, 1))
                   return;
               BuildingCommands.readyPlayer(commandParts);
               break;

           case "TimerReset":
               if (!isValidCommand(commandParts.length, 1))
                   return;
               BuildingCommands.resetTimer(commandParts);
               break;

           case "Roll":
               if (!isValidCommand(commandParts.length, 1))
                   return;
               BuildingCommands.rollDice(commandParts);
               break;

           case "Close":
               if (!isValidCommand(commandParts.length, 1))
                   return;
               BuildingCommands.close(commandParts);
               break;

           case "ShowSpaceship":
               if(!isValidCommand(commandParts.length, 2))
                   return;
               GameCommands.showSpaceship(commandParts);
               break;

           case "Help":
               GameCommands.printHelp();
               break;

           default:
               System.out.println("Command not found");
               break;
       }
   }
}