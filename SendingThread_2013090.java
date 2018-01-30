import java.net.DatagramSocket;

public class SendingThread_2013090 extends Thread implements  Runnable{
    public Integer sleepTime = 1; //seconds
    private DatagramSocket udpSocket;
    private String address;

    private Integer port;
    private Packet_2013090 packet;
    private Boolean isInterrupted = false;

    public SendingThread_2013090(DatagramSocket udpSocket, Packet_2013090 packet, String address, Integer port){
        this.udpSocket = udpSocket;
        this.packet = packet;
        this.address = address;
        this.port = port;
    }

    @Override
    public void run(){
        // Retransmission
        while(!isInterrupted) {
            System.out.println("[Send]        " + packet.toString());
            Utils_2013090.send(this.udpSocket, this.packet.toString(), this.address, this.port);
            try {
                Thread.sleep(this.sleepTime * 1000);
            } catch (InterruptedException e){
                e.printStackTrace();
                break;
            }

//            System.out.println("[Resend]      " + packet.toString());
        }
    }

    public Packet_2013090 getPacket() {
        return this.packet;
    }

    public void interruptThread(){
        isInterrupted = true;
        System.out.println("[Resend]      Stopping... " + this.packet.toString());
//        this.isInterrupted = true;
    }

}
