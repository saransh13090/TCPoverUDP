import java.util.ArrayList;
import java.util.List;

public class Packet_2013090 {

    List<TCPFlag_2013090> Flags;
    Integer SeqNumber;
    Integer AckNumber;
    Integer WindowSize;
    String data;

    public Packet_2013090(List<TCPFlag_2013090> Flags, int SeqNumber, int AckNumber, int WindowSize, String data){
        this.Flags = Flags;
        this.SeqNumber = SeqNumber;
        this.AckNumber = AckNumber;
        this.WindowSize = WindowSize;
        this.data = data;
    }

    public Packet_2013090(){
        this.Flags  = new ArrayList<TCPFlag_2013090>();
        this.SeqNumber = 0;
        this.AckNumber = 0;
        this.WindowSize = 0;
        this.data = null;
    }

    public Boolean hasSynFlagSet(){
        if (Flags.contains(TCPFlag_2013090.SYN)){
            return true;
        }
        return false;
    }

    public Boolean hasAckFlagSet(){
        if (Flags.contains(TCPFlag_2013090.ACK)){
            return true;
        }
        return false;
    }

    public Boolean hasFinFlagSet(){
        if (Flags.contains(TCPFlag_2013090.FIN)){
            return true;
        }
        return false;
    }

    public Boolean hasResetFlagSet(){
        if (Flags.contains(TCPFlag_2013090.RST)){
            return true;
        }
        return false;
    }

    public void setFlags(List<TCPFlag_2013090> flags){
        this.Flags = flags;
    }

    public Integer getSequenceNumber(){
        return this.SeqNumber;
    }

    public Integer getAckNumber(){
        return this.AckNumber;
    }

    public Integer getWindowSize(){
        return this.WindowSize;
    }

    public String getData(){
        return this.data;
    }

    public void setSynFlag(){
        if (!Flags.contains(TCPFlag_2013090.SYN)){
            Flags.add(TCPFlag_2013090.SYN);
        }
    }

    public void setAckFlag(){
        if (!Flags.contains(TCPFlag_2013090.ACK)){
            Flags.add(TCPFlag_2013090.ACK);
        }
    }

    public void setFinFlag(){
        if (!Flags.contains(TCPFlag_2013090.FIN)){
            Flags.add(TCPFlag_2013090.FIN);
        }
    }

    public void setResetFlag(){
        if (!Flags.contains(TCPFlag_2013090.RST)){
            Flags.add(TCPFlag_2013090.RST);
        }
    }

    public void setSequenceNumber(Integer seqNumber){
        this.SeqNumber = seqNumber;
    }

    public void setAckNumber(Integer ackNumber){
        this.AckNumber = ackNumber;
    }

    public void setWindowSize(Integer windowSize){
        this.WindowSize = windowSize;
    }

    public void setData(String data){
        this.data = data;
    }

    @Override
    public String toString(){
        String packetString = "";

        if (this.Flags.contains(TCPFlag_2013090.SYN)){
            packetString += "SYN=1,";
        } else {
            packetString += "SYN=0,";
        }

        if (this.Flags.contains(TCPFlag_2013090.FIN)){
            packetString += "FIN=1,";
        } else {
            packetString += "FIN=0,";
        }

        if (this.Flags.contains(TCPFlag_2013090.ACK)){
            packetString += "ACK=1,";
        } else {
            packetString += "ACK=0,";
        }

        if (this.Flags.contains(TCPFlag_2013090.RST)){
            packetString += "RST=1,";
        } else {
            packetString += "RST=0,";
        }

        packetString += "SEQ=" + this.SeqNumber + ",";
        packetString += "ACKN=" + this.AckNumber + ",";
        packetString += "WIN=" + this.WindowSize + ",";
        packetString += "DATA=" + this.data;

        return packetString;
    }
}
