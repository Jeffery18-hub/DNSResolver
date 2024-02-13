//Your program should be robust to basic queries from dig, including queries for nonexistant hosts.

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ErrorHandler {
    static DNSMessage buildErrorResponse(DNSMessage request, DNSRecord[] answers) throws IOException {

        // repsonse header id should be the same as the request header id
        int id = request.getHeader().getId();

        // response header flags
        int flags = 0x8183; // 1000 0001 1000 0011 -> RCODE = 3

        // combine these together to an array
        int[] headerArray = new int[12];

        //QDcount
        int qdcount = request.getHeader().getQdcount();
        //ANCount
        int ancount = answers.length;
        //NSCount
        int nscount = 0;
        //ARCount
        int arcount = 0;

        headerArray[0] = (id >> 8) & 0xFF;
        headerArray[1] = id & 0xFF;
        headerArray[2] = (flags >> 8) & 0xFF;
        headerArray[3] = flags & 0xFF;
        headerArray[4] = (qdcount >> 8) & 0xFF;
        headerArray[5] = qdcount & 0xFF;
        headerArray[6] = (ancount >> 8) & 0xFF;
        headerArray[7] = ancount & 0xFF;
        headerArray[8] = (nscount >> 8) & 0xFF;
        headerArray[9] = nscount & 0xFF;
        headerArray[10] = (arcount >> 8) & 0xFF;
        headerArray[11] = arcount & 0xFF;


        // combine  the header array , request questions, answers to a byte array;
        byte[] bytes = new byte[1024];
        for (int i = 0; i < 12; i++) {
            bytes[i] = (byte) headerArray[i];
        }

        // copy the request bytes to the response
        for (int i = 12; i < request.toBytes().length; i++) {
            bytes[i] = request.toBytes()[i];
        }

        return new DNSMessage(bytes);
    }
}
