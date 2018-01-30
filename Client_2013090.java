import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


public class Client_2013090 {

    private static Integer portNum;
    private static DatagramSocket udpSocket;
    private static List<Packet_2013090> sentPackets;
    private static Integer currentSeqNum = 0;
    private static Integer currentWindowSize = 256;
    private static ConnectionState_2013090 currentConnectionState;
    private static byte[] packetBytes;
    private static Integer initialServerSeqNum = 0;
    private static String[] message;
    private static Integer lastAck;
    private static Boolean isInterrupted = false;
    private static Integer lastSeqNum = 0;


    public static void main(String[] args) {

        sentPackets = new ArrayList<>();
        currentConnectionState = ConnectionState_2013090.HANDSHAKE_1;

        try {
            udpSocket = new DatagramSocket(Utils_2013090.getClientPort());
        } catch (Exception e){
            e.printStackTrace();
            return;
        }
        System.out.println("Client_2013090 listening...");


        message = new String[Utils_2013090.getMaxMessageSize()];
        lastAck = 0;
        receivingThread.start();

    }

    private static Thread receivingThread = new Thread(new Runnable() {

        @Override
        public void run() {
            Packet_2013090 firstSynPacket = new Packet_2013090();
            firstSynPacket.setSynFlag();
            currentSeqNum = Utils_2013090.getRandomNumber(Utils_2013090.getMinSeqNum(), Utils_2013090.getMaxSeqNum());
            firstSynPacket.setSequenceNumber(currentSeqNum);
            System.out.println("[Handshake]   Sending initial SYN to server.");
            Utils_2013090.send(udpSocket, firstSynPacket.toString(), Utils_2013090.getServerAddress(), Utils_2013090.getServerPort());
            currentConnectionState = ConnectionState_2013090.HANDSHAKE_2;

            while(!isInterrupted) {
                packetBytes = new byte[Utils_2013090.getMaxPacketSize()];
                DatagramPacket receivedPacket = new DatagramPacket(packetBytes, packetBytes.length);

                try {
                    udpSocket.receive(receivedPacket);
                    byte[] packetValidBytes = new byte[receivedPacket.getLength()];
                    System.arraycopy(receivedPacket.getData(), 0, packetValidBytes, 0, receivedPacket.getLength());
                    String packetString = new String(packetValidBytes);
                    Packet_2013090 packet = StringToPacket_2013090.convert(packetString);

                    //===================== testing ============
//                    Random rand = new Random();
//                    // whether to delay or drop
//                    Integer delay = rand.nextInt(3) + 1;
//                    if (delay % 3 == 0){
//                        // Sleep for 1 second.
//                        try {
//                            Thread.sleep(1000);
//                            System.out.println("[D Received]  " + packetString);
//                            Runnable r = new processingThread(packet);
//                            new Thread(r).start();
//                        } catch (InterruptedException e){
//                            e.printStackTrace();
//                            // drop if exception is raised.
//                        }
//                    } else if (delay % 3 == 1){
//                        System.out.println("[Received]    " + packetString);
//                        Runnable r = new processingThread(packet);
//                        new Thread(r).start();
//                    }
//                    else {
//                        // drop the packet
////                        System.out.println("[Dropped]     " + packetString);
//                        continue;
//                    }
//
                    //=================================

                    //=======================================

                    int randomNum = ThreadLocalRandom.current().nextInt(0, 4);
                    if (randomNum % 3 == 0){
                        try {
                            Thread.sleep(1000);
                        } catch(InterruptedException e){
                            e.printStackTrace();
                        }
                        System.out.println("[Received]    " + packetString);
                        Runnable r = new processingThread(packet);
                        new Thread(r).start();
                    } else if(randomNum % 3 == 1){
                        System.out.println("[Received]    " + packetString);
                        Runnable r = new processingThread(packet);
                        new Thread(r).start();
                    } else {
                        //dropped packet
                        continue;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    static class processingThread extends Thread implements Runnable {
        Packet_2013090 packet;

        public processingThread(Packet_2013090 packet){
            this.packet = packet;
        }

        @Override
        public void run(){
            if (currentConnectionState == ConnectionState_2013090.HANDSHAKE_2) {
                if (packet.hasSynFlagSet() && packet.hasAckFlagSet() && packet.getAckNumber().equals(currentSeqNum + 1)) {
                    System.out.println("[Handshake]   Received SYN + ACK from server, sending ACK");

                    currentSeqNum += 1;
                    initialServerSeqNum = packet.getSequenceNumber();

                    Packet_2013090 ansPacket = new Packet_2013090();
                    ansPacket.setAckFlag();
                    ansPacket.setAckNumber(packet.getSequenceNumber() + 1);

//                            ansPacket.setSynFlag();
//                    ansPacket.setSequenceNumber(currentSeqNum);
                    ansPacket.setWindowSize(1);

                    Utils_2013090.send(udpSocket, ansPacket.toString(), Utils_2013090.getServerAddress(), Utils_2013090.getServerPort());
                    currentConnectionState = ConnectionState_2013090.ESTABLISHED;
                }

            } else if (currentConnectionState == ConnectionState_2013090.ESTABLISHED || currentConnectionState == ConnectionState_2013090.FINISHED_1) {
//                System.out.println("[Established] ");

                if (packet.hasFinFlagSet() && currentConnectionState != ConnectionState_2013090.FINISHED_1){
                    System.out.println("[Finish]      Received FIN packet, waiting for all messages to be received");
                    currentConnectionState = ConnectionState_2013090.FINISHED_1;
                    lastSeqNum = packet.getSequenceNumber();
                } else if (packet.getSequenceNumber() - initialServerSeqNum < Utils_2013090.getMaxMessageSize() && message[packet.getSequenceNumber() - initialServerSeqNum] == null) {
                    message[packet.getSequenceNumber() - initialServerSeqNum] = packet.getData();

                    if (lastAck + 1 == packet.getSequenceNumber() - initialServerSeqNum) {
                        // Got the missing packet.
                        for (;;) {
                            if (message[lastAck + 1] != null) {
                                lastAck += 1;
                            } else {
                                break;
                            }
                        }
                    }

                    Packet_2013090 newPacket = new Packet_2013090();
                    newPacket.setAckFlag();
                    newPacket.setAckNumber(lastAck + initialServerSeqNum + 1);
                    newPacket.setWindowSize(currentWindowSize);

                    Utils_2013090.send(udpSocket, newPacket.toString(), Utils_2013090.getServerAddress(), Utils_2013090.getServerPort());
                }

                if (currentConnectionState == ConnectionState_2013090.FINISHED_1){
//                    System.out.println(lastAck);
//                    System.out.println(lastSeqNum);
//                    System.out.println(initialServerSeqNum);
                    if (lastAck == lastSeqNum - initialServerSeqNum - 1){
                        Packet_2013090 newPacket = new Packet_2013090();
                        newPacket.setFinFlag();
                        newPacket.setAckFlag();
                        newPacket.setAckNumber(lastSeqNum + 1);
                        System.out.println("[Finish]     Received all packets, sending FIN + ACK");
                        Utils_2013090.send(udpSocket, newPacket.toString(), Utils_2013090.getServerAddress(), Utils_2013090.getServerPort());

                        System.out.println("Connection closed");
                        System.out.print("Message: ");
                        for(int i=1; i<lastAck+1; i++){
                            if (message[i] != null){
                                System.out.print(message[i]);
                            } else {
                                break;
                            }
                        }

                        System.out.print("\n");
//                        super.interrupt();
                        isInterrupted = true;
                        System.exit(0);
                    }
                }

            }

        }
    }


//    private static Thread thread = new Thread(new Runnable() {
//        @Override
//        public void run() {
//
//            Packet_2013090 firstSynPacket = new Packet_2013090();
//            firstSynPacket.setSynFlag();
//            currentSeqNum = Utils_2013090.getRandomNumber(Utils_2013090.getMinSeqNum(), Utils_2013090.getMaxSeqNum());
//            firstSynPacket.setSequenceNumber(currentSeqNum);
//            Utils_2013090.send(udpSocket, firstSynPacket.toString(), Utils_2013090.getServerAddress(), Utils_2013090.getServerPort());
//            System.out.println("[Handshake]   Sending initial SYN to server.");
//            currentConnectionState = ConnectionState_2013090.HANDSHAKE_2;
//
//            for(;;){
//                packetBytes = new byte[Utils_2013090.getMaxPacketSize()];
//                DatagramPacket recievedPacket = new DatagramPacket(packetBytes, packetBytes.length);
//
//                try {
//                    udpSocket.receive(recievedPacket);
//                    byte[] packetValidBytes = new byte[recievedPacket.getLength()];
//                    System.arraycopy(recievedPacket.getData(), 0, packetValidBytes, 0, recievedPacket.getLength());
//                    String packetString = new String(packetValidBytes);
//                    System.out.println("[Received]    " + packetString);
//                    Packet_2013090 packet = StringToPacket_2013090.convert(packetString);
//
//                    if (currentConnectionState == ConnectionState_2013090.HANDSHAKE_2){
//                        if (packet.hasSynFlagSet() && packet.hasAckFlagSet() && packet.getAckNumber().equals(currentSeqNum + 1)){
//                            System.out.println("[Handshake]   Received SYN + ACK from server, sending ACK");
//
//                            currentSeqNum += 1;
//
//                            Packet_2013090 ansPacket = new Packet_2013090();
//                            ansPacket.setAckFlag();
//                            ansPacket.setAckNumber(packet.getSequenceNumber() + 1);
//
////                            ansPacket.setSynFlag();
//                            ansPacket.setSequenceNumber(currentSeqNum);
//                            ansPacket.setWindowSize(Utils_2013090.getMaxDataSize());
//
//                            Utils_2013090.send(udpSocket, ansPacket.toString(), Utils_2013090.getServerAddress(), Utils_2013090.getServerPort());
//                            currentConnectionState = ConnectionState_2013090.ESTABLISHED;
//                        }
//
//                    } else if (currentConnectionState == ConnectionState_2013090.ESTABLISHED) {
//                        System.out.println("[Established] ");
//
//                    }
//
//
//                } catch (IOException e){
//                    e.printStackTrace();
//                }
//
//
//            }
//        }
//    });
}
