import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DNSHeader {
    private int[] headerArray_;

    public DNSHeader(int[] headerIntArray) {
        headerArray_ = headerIntArray;
    }

    static DNSHeader decodeHeader(ByteArrayInputStream byteArrayInputStream) throws IOException {
        // Parse the header
        // combine these together to an int array
        int[] headerArray = new int[12];
        for (int i = 0; i < 12; i++) {
            headerArray[i] = byteArrayInputStream.read();
        }

        return new DNSHeader(headerArray);

    }

    static DNSHeader buildHeaderForResponse(DNSMessage request, DNSMessage response) throws IOException {

        // repsonse header id should be the same as the request header id
        int id = request.getHeader().getId();

        // response header flags
        int flags = 0x8180; // 1000 0001 1000 0000

        // combine these together to an array
        int[] headerArray = new int[12];

        //QDcount
        int qdcount = request.getHeader().getQdcount();
        //ANCount
        int ancount = response.getAnswers().length;
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


        return new DNSHeader(headerArray);
    }

    // the header bytes to be sent back to the client.
    public void writeBytes(OutputStream outputStream) throws IOException {
        for (int i = 0; i < 12; i++) {
            outputStream.write(headerArray_[i]);
        }
    }

    //String toString()` -- Return a human readable string version of a header object.
    @Override
    public String toString() {
        return "DNSHeader{" +
                "ID:0x" + Integer.toHexString(this.getId()) + "; " +
                "Flags:0x" + Integer.toHexString(this.getFlags()) + "; " +
                "QDCount:" + this.getQdcount() + "; " +
                "ANCount:" + this.getAncount() + "; " +
                "NSCount:" + this.getNscount() + "; " +
                "ARCount:" + this.getArcount() + "}";
    }

    // getters
    public int getId() {
        return (headerArray_[0] << 8 | headerArray_[1]);
    }
    public int getFlags() {
        return  (headerArray_[2] << 8 | headerArray_[3]);
    }
    public int getQdcount() {
        return (headerArray_[4] << 8 | headerArray_[5]);
    }

    public int getAncount() {
        return  (headerArray_[6] << 8 | headerArray_[7]);
    }

    public int getNscount() {
        return (headerArray_[8] << 8 | headerArray_[9]);
    }

    public int getArcount() {
        return (headerArray_[10] << 8 | headerArray_[11]);
    }


}
