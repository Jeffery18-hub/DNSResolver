import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

public class DNSQuestion {
    private String[] domainName_;
    private int queryType_;
    private int queryClass_;

    public DNSQuestion(String[] domainName, int queryType, int queryClass){
        domainName_ = domainName;
        queryType_ = queryType;
        queryClass_ = queryClass;

    }

    static DNSQuestion decodeQuestion(ByteArrayInputStream inputStream, DNSMessage msg) throws IOException {
        //read question
        int firstByte = inputStream.read();
        String[] domainName = msg.readDomainName(firstByte);
        inputStream = msg.getInputStream();
        int type = (inputStream.read() << 8 | inputStream.read());
        int clazz  = (inputStream.read() << 8 | inputStream.read());
        return new DNSQuestion(domainName, type, clazz);
    }

    // Write the question bytes which will be sent to the client.
    // The hash map is used for us to compress the message, see the DNSMessage class for more details.
    void writeBytes(ByteArrayOutputStream byteArrayOutputStream, HashMap<String,Integer> domainNameLocations) throws IOException {
        DNSMessage.writeDomainName(byteArrayOutputStream, domainNameLocations, domainName_);
        byteArrayOutputStream.write(queryType_ >> 8);
        byteArrayOutputStream.write(queryType_);
        byteArrayOutputStream.write(queryClass_ >> 8);
        byteArrayOutputStream.write(queryClass_);
    }

    //toString(), equals(), and hashCode() methods
    @Override
    public String toString() {
        StringBuilder domainName = new StringBuilder();
        for (String label : domainName_){
            domainName.append(label);
            domainName.append(".");
        }
        return "DNSQuestion{" +
                "domainName:" + domainName + " " +
                "queryType: " + queryType_ + " " +
                "queryClass: " + queryClass_ + "}";
    }

    @Override
    public boolean equals(Object o) {
        return this.hashCode() == o.hashCode();
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(domainName_);
        result = 31 * result + queryType_;
        result = 31 * result + queryClass_;
        return result;
    }

    public String[] getDomainName() {
        return domainName_;
    }

    public int getQueryType() {
        return queryType_;
    }

    public int getQueryClass() {
        return queryClass_;
    }
}
