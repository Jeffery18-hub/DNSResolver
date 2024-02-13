import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

/* 1. open up a UDP socket ('DatagramSocket' class in Java), and listen for requests.
   2. When it gets one, it should look at all the questions in the request.
   3. If there is a valid answer in cache, add that the response,
   4. otherwise create another UDP socket to forward the request Google (8.8.8.8) and then await their response.
   5. Once you've dealt with all the questions, send the response back to the client.
*/

public class DNSServer {

    public static void main(String[] args) throws IOException {
        // Member variables
        final int BUFFER_SIZE = 1024;
        final int MyServerPort = 8053;
        final int GoogleServerPort = 53;

        // Google's DNS server ip address
        InetAddress GOOGLE_DNS_SERVER= InetAddress.getByName("8.8.8.8");

        // Open up a UDP socket
        DatagramSocket clientSocket = new DatagramSocket(MyServerPort);

        // Open up a UDP socket to forward the request to Google
        DatagramSocket googleSocket = new DatagramSocket(); // port is chosen by OS, wait for packets back

        while (true) {
            System.out.println("**** While loop starts ****");
            System.out.println("1. My server is running on port:" + MyServerPort);
            // Listen for requests
            byte[] buffer = new byte[BUFFER_SIZE];
            // request packet from client side
            DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length);
//            System.out.println("Client side port when contructing the packet:" + requestPacket.getPort());
            clientSocket.receive(requestPacket);
            System.out.println("2. Client side port when reiceiving packet:" + requestPacket.getPort());


            // Deal with all the questions in the request
            DNSMessage request = DNSMessage.decodeMessage(requestPacket.getData());

            ArrayList<DNSRecord> answers = new ArrayList<>();

            for (DNSQuestion question : request.getQuestions()) {
                // If there is a valid answer in cache, add that to the response
                if (DNSCache.query(question) != null) {
                    answers.add(DNSCache.query(question));
                }else {
                   // otherwise forward the question in the request to Google (
                    DatagramPacket googleRequest = new DatagramPacket(requestPacket.getData(), requestPacket.getLength(),
                            GOOGLE_DNS_SERVER, GoogleServerPort); // this port is the port of google dns server
                    System.out.println("3. Send to Google -> Google port:" + googleRequest.getPort());
                    googleSocket.send(googleRequest);

                    // Await their response
                    byte[] googleBuffer = new byte[BUFFER_SIZE];
                    DatagramPacket googleResponsePacket = new DatagramPacket(googleBuffer, googleBuffer.length);
                    googleSocket.receive(googleResponsePacket);
                    System.out.println("4. Receive from Google -> Google port:" + googleResponsePacket.getPort());

                    //add the google response to answers
                    DNSMessage googleAnswer = DNSMessage.decodeMessage(googleResponsePacket.getData());
                    System.out.println("---------------------------");
                    System.out.println("google response packet:" + googleAnswer);

                    for (DNSRecord record : googleAnswer.getAnswers()) {
                        answers.add(record);
                    }

                    //insert the first google answer to the cache
                    if (googleAnswer.getAnswers().length != 0)
                        DNSCache.insert(question,googleAnswer.getAnswers()[0]);
                }
            }

            // convert the answers to array
            DNSRecord[] answersArray = new DNSRecord[answers.size()];
            for (int i = 0; i < answers.size(); i++) {
                answersArray[i] = answers.get(i);
            }

            // Send the response back to the client
            if (answersArray.length != 0) {
                DNSMessage response = DNSMessage.buildResponse(request, answersArray);
                byte[] responseBytes = response.toBytes();
                // send the response to the client
                DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length,
                        requestPacket.getAddress(), requestPacket.getPort());
                clientSocket.send(responsePacket);
                System.out.println("5. Response sent to client from my server " + responsePacket.getPort());
                System.out.println("---------------------------");
                System.out.println("Cache on my server:");
                System.out.println(DNSCache.getCache());
            }else {
                // if the host is nonexistent, send a response with no answers
                // build header of the response, the RCODE is 3
                DNSMessage errorMsg = ErrorHandler.buildErrorResponse(request, answersArray);
                byte[] errorMsgBytes = errorMsg.toBytes();
                // send the response to the client
                DatagramPacket errorMsgPacket = new DatagramPacket(errorMsgBytes, errorMsgBytes.length,
                        requestPacket.getAddress(), requestPacket.getPort());
                clientSocket.send(errorMsgPacket);
                System.out.println("Error Response sent to client");
                System.out.println(DNSCache.getCache());
            }
        }
    }
}


