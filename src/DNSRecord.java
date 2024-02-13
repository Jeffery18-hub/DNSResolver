import java.io.*;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class DNSRecord {
    // Fields for storing the DNS record data
    private String[] domainName_;
    private int type_;
    private int dnsClass_;
    private int ttl_;
    private byte[] resourceData_;
    private Date creationDate_;

    public DNSRecord(String[] domainName, int type, int dnsClass, int ttl, byte[] resourceData) {
        domainName_ = domainName;
        type_ = type;
        dnsClass_ = dnsClass;
        ttl_ = ttl;
        resourceData_ = resourceData;
        creationDate_ = Calendar.getInstance().getTime();
    }

    public static DNSRecord decodeRecord(ByteArrayInputStream inputStream, DNSMessage dnsMessage) throws IOException {
        inputStream = dnsMessage.getInputStream();
        // Read the answer
        int firstByte = inputStream.read();
        String[] domainName = dnsMessage.readDomainName(firstByte);

        // Read the rest of the answer
        // read type
        // when using bitwise operation, type promoted to int
        // here the addition and bitwise operation result in the same value
//        int type = (inputStream.read() << 8) + inputStream.read();
        int type = ((inputStream.read() << 8) | inputStream.read()) & 0xFFFF;

        // read dns class
//        int dnsClass = (inputStream.read() << 8) + inputStream.read();
        int dnsClass = ((inputStream.read() << 8) | inputStream.read()) & 0xFFFF;
        // read ttl
//        int ttl = (inputStream.read() << 24) + (inputStream.read() << 16) + (inputStream.read() << 8) + inputStream.read();
        int ttl = ((inputStream.read() << 24) | (inputStream.read() << 16) | (inputStream.read() << 8) | inputStream.read()) & 0xFFFFFFFF;

        // read resource data length
//        int dataLength = (inputStream.read() << 8) + inputStream.read();
        int dataLength = ((inputStream.read() << 8) + inputStream.read()) & 0xFFFF;

        // read resource data
        byte[] resourceData = new byte[dataLength];
        for (int i = 0; i < dataLength; i++)
            resourceData[i] = (byte) (inputStream.read() & 0xFF);

        return new DNSRecord(domainName, type, dnsClass, ttl, resourceData);
    }

    public void writeBytes(ByteArrayOutputStream outputStream, HashMap<String, Integer> domainNameLocations) throws IOException {
    // write the DNS record to the output stream
        // domain name first
        DNSMessage.writeDomainName(outputStream, domainNameLocations, domainName_);
        // type
        outputStream.write((byte) (type_ >> 8));
        outputStream.write((byte) (type_ & 0xFF));
        // dns class
        outputStream.write((byte) (dnsClass_ >> 8));
        outputStream.write((byte) (dnsClass_ & 0xFF));
        // ttl
        outputStream.write((byte) (ttl_ >> 24));
        outputStream.write((byte) (ttl_ >> 16));
        outputStream.write((byte) (ttl_ >> 8));
        outputStream.write((byte) (ttl_ & 0xFF));
        // resource data length
        outputStream.write((byte) (resourceData_.length >> 8));
        outputStream.write((byte) (resourceData_.length & 0xFF));
        // resource data
        outputStream.write(resourceData_);

    }

    @Override
    public String toString() {
        // Implement the method to return a human-readable string representation of the DNS record
        StringBuilder domainName = new StringBuilder();
        for (String label : domainName_){
            domainName.append(label);
            domainName.append(".");
        }
        return "DNSAnswer{" +
                "domainName:" + domainName + " " +
                "Type: " + type_ + " " +
                "Class: " + dnsClass_ + " " +
                "TTL: " + ttl_ + " " +
                "Data Length: " + resourceData_.length + " " +
                "Address: " + resourceData_ + "}";
    }


    //return whether the creation date + the time to live is after the current time.
    //       The Date and Calendar classes will be useful for this.
    public boolean isExpired() {
        if (creationDate_.getTime() + ttl_ > Calendar.getInstance().getTime().getTime()) return true;
        return false;
    }
}
