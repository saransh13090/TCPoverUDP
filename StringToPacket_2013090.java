import java.util.ArrayList;
import java.util.List;

public class StringToPacket_2013090 {

    private static Packet_2013090 packet;

    public static Packet_2013090 convert(String packetString){

        packet = new Packet_2013090();

        List<TCPFlag_2013090> Flags = new ArrayList<>();

        String[] keyValuePairs = packetString.split(",");

        for(int i = 0; i< keyValuePairs.length; i++){
            String[] keyValuePair = keyValuePairs[i].split("=");
            String key = keyValuePair[0];
//            System.out.println(key);
            String value = keyValuePair[1];

            switch(key){
                case "SYN":
                    if (value.equals("1")){
                        Flags.add(TCPFlag_2013090.SYN);
                    }
                    break;
                case "ACK":
                    if (value.equals("1")){
                        Flags.add(TCPFlag_2013090.ACK);
                    }
                    break;
                case "FIN":
                    if (value.equals("1")){
                        Flags.add(TCPFlag_2013090.FIN);
                    }
                    break;
                case "RST":
                    if (value.equals("1")){
                        Flags.add(TCPFlag_2013090.RST);
                    }
                    break;
                case "SEQ":
                    packet.setSequenceNumber(Integer.parseInt(value));
                    break;
                case "ACKN":
                    packet.setAckNumber(Integer.parseInt(value));
                    break;
                case "WIN":
                    packet.setWindowSize(Integer.parseInt(value));
                    break;
                case "DATA":
                    packet.setData(value);
                    break;
            }
        }
        packet.setFlags(Flags);
        return packet;
    }

}
