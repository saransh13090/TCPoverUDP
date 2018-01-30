import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class Server_2013090 {

    private static DatagramSocket udpSocket;
    private static String messageFile = "src/message_2013090";
    private static List<Packet_2013090> sentPackets;
    private static byte[] packetBytes;
    private static Integer currentSeqNum = 0;
    private static Integer firstSeqNum = 0;
    private static ConnectionState_2013090 currentConnectionState;
    private static String msgString;
    private static Integer currentWindowSize;
    private static ArrayList<SendingThread_2013090> currentWindow;
    private static Integer sleepTime = 1; //seconds
    private static Integer lastAck;
    private static Boolean isInterrupted = false;


    public static void main(String[] args){

        sentPackets = new ArrayList<>();
        currentConnectionState = ConnectionState_2013090.HANDSHAKE_1;

        try {
            udpSocket = new DatagramSocket(Utils_2013090.getServerPort());
        } catch(SocketException e){
            e.printStackTrace();
            return;
        }

        System.out.println("Server_2013090 listening...");

        currentSeqNum = Utils_2013090.getRandomNumber(Utils_2013090.getMinSeqNum(), Utils_2013090.getMaxSeqNum());

        currentWindowSize = 1;
        currentWindow = new ArrayList<SendingThread_2013090>();
        lastAck = 0;

        receivingThread.start();
    }

    private static Thread receivingThread = new Thread(new Runnable() {

        @Override
        public void run() {
            while(!isInterrupted) {
                for (; ; ) {
                    packetBytes = new byte[Utils_2013090.getMaxPacketSize()];
                    DatagramPacket recievedPacket = new DatagramPacket(packetBytes, packetBytes.length);

                    try {
                        udpSocket.receive(recievedPacket);
                        byte[] packetValidBytes = new byte[recievedPacket.getLength()];
                        System.arraycopy(recievedPacket.getData(), 0, packetValidBytes, 0, recievedPacket.getLength());
                        String packetString = new String(packetValidBytes);
                        System.out.println("[Received]    " + packetString);
                        Packet_2013090 packet = StringToPacket_2013090.convert(packetString);
//                    System.out.println(new String(packetValidBytes));

                        boolean packetInWindow = false;
                        for (int i = 0; i < currentWindow.size(); i++) {
                            SendingThread_2013090 thread = currentWindow.get(i);
                            if (thread.getPacket().getSequenceNumber().equals(packet.getAckNumber() - 1)) {
                                currentWindow.remove(i);
                                thread.interruptThread();
                                packetInWindow = true;
                            }
                        }

                        if (packetInWindow || currentConnectionState == ConnectionState_2013090.HANDSHAKE_1) {
                            if (currentSeqNum < packet.getAckNumber()) {
                                currentSeqNum = packet.getAckNumber();
                            }
                            Thread r = new processingThread(packet);
                            r.run();
                        } else {
                            // drop packet
                            continue;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    });


    static class processingThread extends Thread implements Runnable {

        Packet_2013090 packet;

        public processingThread(Packet_2013090 packet) {
            this.packet = packet;
        }

        @Override
        public void run() {
            if (currentConnectionState == ConnectionState_2013090.HANDSHAKE_1) {
                if (packet.hasSynFlagSet()) {
                    System.out.println("[Handshake]   Received SYN packet from client.");
                    System.out.println("[Handshake]   Sending SYN + ACK to client.");
                    Packet_2013090 ansPacket = new Packet_2013090();
                    ansPacket.setSynFlag();
                    ansPacket.setAckFlag();
                    ansPacket.setAckNumber(packet.getSequenceNumber() + 1);

                    ansPacket.setSequenceNumber(currentSeqNum);
//                            currentSeqNum += 1;

                    currentConnectionState = ConnectionState_2013090.HANDSHAKE_2;
                    send(udpSocket, ansPacket, Utils_2013090.getClientAddress(), Utils_2013090.getClientPort());


//                    Utils_2013090.send(udpSocket, ansPacket.toString(), Utils_2013090.getClientAddress(), Utils_2013090.getClientPort());
                }
            } else if (currentConnectionState == ConnectionState_2013090.HANDSHAKE_2) {
                if (packet.hasAckFlagSet() && packet.getAckNumber().equals(currentSeqNum)) {
//                    currentSeqNum += 1;
                    firstSeqNum = currentSeqNum;
                    currentConnectionState = ConnectionState_2013090.ESTABLISHED;
                    System.out.println("[Handshake]   Received ACK packet from client.");
                    System.out.println("[Established] Handshake completed, sending first byte of data.");
                    File file = new File(messageFile);
//                    System.out.println(file.getAbsolutePath());

                    try {
                        FileInputStream fis = new FileInputStream(file);
                        byte[] data = new byte[(int) file.length()];
                        fis.read(data);
                        fis.close();

                        msgString = new String(data, "UTF-8");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        super.interrupt();
                        isInterrupted = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                        super.interrupt();
                        isInterrupted = true;
                    }

                    Packet_2013090 firstDataPacket = new Packet_2013090();
                    firstDataPacket.setSynFlag();
                    firstDataPacket.setSequenceNumber(currentSeqNum);
                    String sendingData = msgString.charAt(currentSeqNum - firstSeqNum) + "";
                    firstDataPacket.setData(sendingData);
                    send(udpSocket, firstDataPacket, Utils_2013090.getClientAddress(), Utils_2013090.getClientPort());

//                    Utils_2013090.send(udpSocket, firstDataPacket.toString(), Utils_2013090.getClientAddress(), Utils_2013090.getClientPort());
                }
            } else if (currentConnectionState == ConnectionState_2013090.ESTABLISHED) {

                // Flow control
                if (packet.getWindowSize() >= 0){
                    currentWindowSize = packet.getWindowSize();
                }

                if ((currentSeqNum - firstSeqNum) < msgString.length()) {
//                        System.out.println("[Established] ");
                    Packet_2013090 newPacket = new Packet_2013090();
                    newPacket.setSynFlag();
                    newPacket.setSequenceNumber(currentSeqNum);
                    newPacket.setData(msgString.charAt(currentSeqNum - firstSeqNum) + "");
//                    currentSeqNum += 1;
                    send(udpSocket, newPacket, Utils_2013090.getClientAddress(), Utils_2013090.getClientPort());
                } else {
                    System.out.println("[Finish]      Message finished, sending FIN");
                    currentConnectionState = ConnectionState_2013090.FINISHED_1;
                    Packet_2013090 finPacket = new Packet_2013090();
                    finPacket.setFinFlag();
                    finPacket.setSequenceNumber(currentSeqNum);
                    send(udpSocket, finPacket, Utils_2013090.getClientAddress(), Utils_2013090.getClientPort());
                }
            } else if (currentConnectionState == ConnectionState_2013090.FINISHED_1) {
//                System.out.println("Closing connection... 1");
                if (packet.hasFinFlagSet() && packet.hasAckFlagSet()) {
//                    System.out.println("Closing connection... 2");
                    currentConnectionState = ConnectionState_2013090.FINISHED_2;
                    Packet_2013090 newPacket = new Packet_2013090();
                    newPacket.setAckFlag();
                    newPacket.setAckNumber(packet.getSequenceNumber() + 1);
                    System.out.println("[Finish]      Received FIN + ACK, sending ACK");
                    Utils_2013090.send(udpSocket, newPacket.toString(), Utils_2013090.getServerAddress(), Utils_2013090.getServerPort());
                    System.out.println("Closing connection...");

//                    super.interrupt();
                    for(;;){
                        if (currentWindow.size() == 0){
                            isInterrupted = true;
                            System.exit(0);
                        } else {
                            try {
                                Thread.sleep(1000);
                            } catch(InterruptedException e){
                                e.printStackTrace();
                                isInterrupted = true;
                                System.exit(0);
                            }
                        }
                    }
//                    isInterrupted = true;
                }
            } else {
                super.interrupt();
                isInterrupted = true;
            }
        }

    }

    //Flow control
    public static void send(DatagramSocket socket, Packet_2013090 message, String address, Integer port){
        for(;;) {
            if (currentWindowSize > currentWindow.size()) {
                SendingThread_2013090 sendingThread = new SendingThread_2013090(socket, message, address, port);
//                if (!(message_2013090.hasFinFlagSet() && message_2013090.hasAckFlagSet())){
                currentWindow.add(sendingThread);
//                }
                new Thread(sendingThread).start();
                break;
            }
            try {
                Thread.sleep(sleepTime * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }

//    private static Thread thread = new Thread(new Runnable() {
//        @Override
//        public void run() {
//            for(;;){
//
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
////                    System.out.println(new String(packetValidBytes));
//
//                    if (currentConnectionState == ConnectionState_2013090.HANDSHAKE_1){
//                        if (packet.hasSynFlagSet()){
//                            System.out.println("[Handshake]   Received SYN packet from client.");
//                            System.out.println("[Handshake]   Sending SYN + ACK to client.");
//                            Packet_2013090 ansPacket = new Packet_2013090();
//                            ansPacket.setSynFlag();
//                            ansPacket.setAckFlag();
//                            ansPacket.setAckNumber(packet.getSequenceNumber() + 1);
//
//                            ansPacket.setSequenceNumber(currentSeqNum);
////                            currentSeqNum += 1;
//
//                            Utils_2013090.send(udpSocket, ansPacket.toString(), Utils_2013090.getClientAddress(), Utils_2013090.getClientPort());
//                            currentConnectionState = ConnectionState_2013090.HANDSHAKE_2;
//                        }
//                    } else if (currentConnectionState == ConnectionState_2013090.HANDSHAKE_2){
//                        if (packet.hasAckFlagSet() && packet.getAckNumber().equals(currentSeqNum + 1)){
//                            currentSeqNum += 1;
//                            firstSeqNum = currentSeqNum;
//                            currentConnectionState = ConnectionState_2013090.ESTABLISHED;
//                            System.out.println("[Handshake]   Received ACK packet from client.");
//                            System.out.println("[Established] Handshake completed, sending first byte of data.");
//                            File file = new File(messageFile);
//                            System.out.println(file.getAbsolutePath());
//                            FileInputStream fis = new FileInputStream(file);
//                            byte[] data = new byte[(int) file.length()];
//                            fis.read(data);
//                            fis.close();
//
//                            msgString = new String(data, "UTF-8");
//
//                            Packet_2013090 firstDataPacket = new Packet_2013090();
//                            firstDataPacket.setSequenceNumber(currentSeqNum);
//                            String sendingData = msgString.charAt(currentSeqNum - firstSeqNum) + "";
//                            firstDataPacket.setData(sendingData);
//                            Utils_2013090.send(udpSocket, firstDataPacket.toString(), Utils_2013090.getClientAddress(), Utils_2013090.getClientPort());
//                        }
//                    }
//                    else if (currentConnectionState == ConnectionState_2013090.ESTABLISHED){
//
//                            System.out.println("[Established] ");
//
//                    } else {
//                        System.exit(0);
//                    }
//
//                } catch (IOException e){
//                    e.printStackTrace();
//
//                }
//            }
//        }
//    });
}
