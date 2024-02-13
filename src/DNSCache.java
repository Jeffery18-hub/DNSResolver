import java.util.HashMap;

/*
This class is the local cache.
It should basically just have a `HashMap<DNSQuestion, DNSRecord>` in it.
You can just store the first answer for any question in the cache
 (a response for google.com might return 10 IP addresses,just store the first one).
This class should have methods for querying and inserting records into the cache.
When you look up an entry, if it is too old (its TTL has expired), remove it and return "not found."
 */
public class DNSCache {
    private static HashMap<DNSQuestion, DNSRecord> cache = new HashMap<>();

   static DNSRecord query(DNSQuestion question) {
        DNSRecord answer = cache.get(question);
        if (answer == null) {
            return null;
        }
        if (answer.isExpired()) {
            cache.remove(question);
            return null;
        }
        return answer;
    }

    static void insert(DNSQuestion question, DNSRecord record) {
        cache.put(question, record);
    }

    public static String getCache() {
        StringBuilder sb = new StringBuilder();
        for(DNSQuestion question : cache.keySet()) {
            sb.append(question.toString()).append(": ").append(cache.get(question)).append("\n");
        }
        return sb.toString();
    }


}
