# TCPoverUDP
Implementation of TCP over UDP in Java

Overview
=========

1. TCPFlag is an ENUM with values of TCP flags:
    a. SYN
    b. ACK
    c. FIN
    d. RST
2. ConnectionState is an ENUM with values stating state of the current connection.
    a. HANDSHAKE_1
    b. HANDSHAKE_2
    c. ESTABLISHED
    d. FINISHED_1
    e. FINISHED_2
3. Sending Thread: retransmits the packet after one second infinitely unless and until it is stopped.
3. Client is the client class which initiates TCP connection and sends ACKs after receiving data. It has two threads:
    a. receiving thread: infinite loop which waits for incoming packets and decides whether to add delay, drop or accept the incoming packet.
    b. processing thread: processes packets, maintains/changes states and sends ACKs.
4. Server is the server class which accepts TCP connection and sends data one character at a time and then initiates FIN.
    a. receiving thread: infinite loop which waits for incoming packets and drops duplicate packets before processing.
    b. processing thread: completes TCP handshake, sends data one char at a time and initiates Connection closing with FIN flag.
    c. currentWindowSize variable: states limit of maximum number of packets that can be in the network at one time.
    d. currentWindow array: contains instances of 'SendingThread', i.e. current packets in the network.
    c. send function: checks whether or not there is space in the network based on currentWindowSize variable, if there is no space, it waits for one seconds and then retries, if there is, it adds it to the network i.e. currentWindow array.
5. Utils:
    a. contains constants and a basic send function which sends a string over udp.
6. Packet: class to define a TCP packet which contains:
    a. flags (syn, ack, fin, rst)
    b. seq num
    c. ack num
    e. data
    f. window size
    It can be converted to a string before passing it to udp socket and then 'StringToPacket' class convert it back to Packet object.
7. message file contains the  data that needs to be sent.

Working
=============
1. Client and Server are at state HANDSHAKE_1.
2. Client initiates TCP connection be sending SYN. It generates an random Seq number before sending first packet and then increments that by one for each new packet.
3. Server replies with SYN+ACK, again inital sequence number is random i.e. between 1 and 5000
4. Client replies with ACK and hankshake is done. Both server and client changes their states to "ESTABLISHED";
5. Server starts sending data one character at a time, by creating an instance of 'SendingThread' using send method.
6. Whenever server receives a packet and it is not in the currentWindow array, it is considered a duplicate.
7. SendingThread retransmittes packet until it is stopped, which is done by Server when its ACK is received.
8. Client, for testing, randomly decides to either delay by 1 second, drop or accept incoming packet upon receiving a packet.
9. Utils contains maximum size of data that can be sent, currently it is 512. change it as per your needs.
10. Client sets windowSize in packet which is used by server to increase or decrease number of packets in the network i.e. flow control.
11. After all of the data has been sent, Server sends packet with FIN flag set.
12. Client upon receiving FIN flagged packet, calculates size of data and waits for all the missing packets if any
13. When client receives all the data, it replies back with FIN + ACK
14. Server upon receiving FIN+ACK checks whether currentWindow is empty or not, if yes, it closes the connection and exits.
15. Client exits.

