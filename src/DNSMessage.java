import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DNSMessage {
    private DNSHeader header_;
    private DNSQuestion[] questions_;
    private DNSRecord[] answers_;
    private byte[] allBytes_;
    private ByteArrayInputStream inputStream_;
    private HashMap<String, Integer> domainNameLocations_ = new HashMap<>();


    public DNSMessage(){};
    public DNSMessage(byte[] bytes) throws IOException {
        // Save the bytes for later
        allBytes_ = bytes;
        // Save the input stream for later
       inputStream_ = new ByteArrayInputStream(bytes);

        // Parse the header
        DNSHeader dnsHeader = DNSHeader.decodeHeader(inputStream_);
        header_ = dnsHeader;

        // Parse the questions
        int numQuestions = dnsHeader.getQdcount();
        DNSQuestion[] questions = new DNSQuestion[numQuestions];
        for (int i = 0; i < numQuestions; i++){
            questions[i] = DNSQuestion.decodeQuestion(inputStream_, this);
        }
        questions_ = questions;

        // Parse the answers
        int numAnswers = dnsHeader.getAncount();
        DNSRecord[] answers = new DNSRecord[numAnswers];
        for (int i = 0; i < numAnswers; i++)
            answers[i] = DNSRecord.decodeRecord(inputStream_, this);
        answers_ = answers;

    }
    public static DNSMessage decodeMessage(byte[] bytes) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        DNSMessage message = new DNSMessage(bytes);
        return message;
    }

    public String[] readDomainName(InputStream inputStream) throws IOException {
        int totalBytes= allBytes_.length;
        int availableBytes = inputStream.available();
        int offset = totalBytes - availableBytes;

        List<String> domainName = new ArrayList<>();
        while (true) {
            StringBuilder label = new StringBuilder();
            int length = inputStream.read();
            if (length == 0) {
                break;
            }

            for (int i = 0; i < length; i++) {
                label.append((char) inputStream.read());
            }
            domainName.add(label.toString());
        }

        String[] domainNameArray = domainName.toArray(new String[0]);
        String domainNameString = this.joinDomainName(domainNameArray);
        domainNameLocations_.put(domainNameString, offset);
        return domainNameArray;
    }

    /*
    used when there's compression and we need to find the domain from earlier in the message.
    This method should make a ByteArrayInputStream that starts at the specified byte
    and call the other version of this method
     */
    public String[] readDomainName(int firstByte) throws IOException {
        if (firstByte >> 6 == 0b11) {
            int secondByte = inputStream_.read();
            int offset = (firstByte << 8 | secondByte) & 0x3FFF;
            return readDomainName(new ByteArrayInputStream(allBytes_, offset, allBytes_.length - offset));
        } else {
            int offset = allBytes_.length - inputStream_.available() - 1;
            inputStream_ = new ByteArrayInputStream(allBytes_, offset, allBytes_.length - offset);
            return readDomainName(inputStream_);
        }

    }

    //build a response based on the request and the answers you intend to send back.
    static DNSMessage buildResponse(DNSMessage request, DNSRecord[] answers) throws IOException {
        DNSMessage response = new DNSMessage();
        response.questions_ = request.questions_;
        response.answers_ = answers;
        DNSHeader responseHeader = DNSHeader.buildHeaderForResponse(request, response);
        response.header_ = responseHeader;
        return response;
    }

    //byte[] toBytes()` -- get the bytes to put in a packet and send back
    byte[] toBytes() throws IOException {
        // write the header
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        header_.writeBytes(outputStream);
        // write the questions
        for (DNSQuestion question : questions_) {
            question.writeBytes(outputStream, domainNameLocations_);
        }
        // write the answers
        for (DNSRecord answer : answers_) {
            answer.writeBytes(outputStream, domainNameLocations_);
        }
        return outputStream.toByteArray();
    }


    /*
    static void writeDomainName(ByteArrayOutputStream, HashMap<String,Integer> domainLocations, String[] domainPieces)
    -- If this is the first time we've seen this domain name in the packet,
    write it using the DNS encoding (each segment of the domain prefixed with its length, 0 at the end)
    , and add it to the hash map.
    Otherwise, write a back pointer to where the domain has been seen previously.
     */
    static void writeDomainName(ByteArrayOutputStream outputStream, HashMap<String,Integer> domainLocations, String[] domainPieces) throws IOException {
        String domainName = String.join(".", domainPieces);
        if (!domainLocations.containsKey(domainName)) {
            for (String piece : domainPieces) {
                outputStream.write(piece.length());
                outputStream.write(piece.getBytes());
            }
            outputStream.write(0);
        }

        // write a back pointer to where the domain has been seen previously, compressed
        else {
            int offset = domainLocations.get(domainName);
            outputStream.write(0b11000000 | (offset >> 8));
            outputStream.write(offset & 0xFF);
        }
    }

      //join the pieces of a domain name with dots ([ "utah", "edu"] -> "utah.edu" )
    private String joinDomainName(String[] pieces) {
        StringBuilder domainName = new StringBuilder();
        for (String piece : pieces) {
            domainName.append(piece).append(".");
        }
        return domainName.toString();
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(header_.toString()).append("\n");
        for (DNSQuestion question : questions_) {
            sb.append(question.toString()).append("\n");
        }
        for (DNSRecord answer : answers_) {
            sb.append(answer.toString()).append("\n");
        }
        return sb.toString();
    }


    public DNSHeader getHeader() {
        return header_;
    }

    public DNSQuestion[] getQuestions() {
        return questions_;
    }

    public DNSRecord[] getAnswers() {
        return answers_;
    }

    public byte[] getAllBytes() {
        return allBytes_;
    }

    public ByteArrayInputStream getInputStream() {
        return inputStream_;
    }

}
