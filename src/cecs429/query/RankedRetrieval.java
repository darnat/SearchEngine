package cecs429.query;

import cecs429.documents.DocumentCorpus;
import cecs429.index.DiskPositionalIndex;
import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.DefaultTokenProcessor;
import cecs429.text.TokenProcessor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;

public class RankedRetrieval {
    /* Regex split query into tokens */
    private static String mRegex;
        
    static {
        mRegex = "\\s+"; //one or more whitespace
    }
    
    /** 
     * Will return a Map<DocID, Score> which correspond to the most relevant document 
     * @param index
     * @param corpus 
     * @param query  
     * @return   
     * @throws java.io.IOException 
     */
    public static Map<Integer, Double> accumulate(Index index, int corpusSize, String query) throws IOException {
        Map<Integer, Double> accumulator = new HashMap<>();

        getTokens(query).forEach((token) -> {
            List<Posting> postings = index.getPostings(token);
            
            //Calculate w(q,t) 
            double wQT = 0.0, accu = 0.0, wDT = 0.0;
            
            if (!postings.isEmpty()) {
                wQT = Math.log(1.0 + ((double) corpusSize / (double) postings.size()));
            }
 
            for (Posting p : postings) {
                //Never encountered, add and initialize
                if (!accumulator.containsKey(p.getDocumentId())) 
                    accumulator.put(p.getDocumentId(), 0.0);
                
                //calculate weight of doc = 1 + ln(tf(t,d)) 
                wDT = 1.0 + Math.log((double)p.getPositions().size());
                
                //acquire A(d)
                accu = accumulator.get(p.getDocumentId()); 
                
                accumulator.replace(p.getDocumentId(), accu + (wQT * wDT));
            }
        });
        
        // Divide all A(d) by L(d)
        for (Integer key : accumulator.keySet()) {
            if (accumulator.get(key) != 0.0) {
                // Accumulator = Acculmulator / L(d)
                accumulator.replace(key, (double) accumulator.get(key) / (double) DiskPositionalIndex.getDocWeight(key));
            }
        }
        
        return accumulator;  
    }
    
    private static List<Entry<Integer, Double>> findKGreatest(Map<Integer, Double> map, int k)
    {
        List<Entry<Integer, Double>> results = new ArrayList<>();
        
        // For comparison for "sorting" the priority queue
        Comparator<? super Entry<Integer, Double>> comparator = 
            (Entry<Integer, Double> val1, Entry<Integer, Double> val2) -> {
                return val2.getValue().compareTo(val1.getValue());
        };
        
        PriorityQueue<Entry<Integer, Double>> greatest = new PriorityQueue<>(k, comparator);
        
        //Build Priority Queue based on entries
        map.entrySet().forEach((entry) -> {
            greatest.add(entry); //Indiscriminately insert
        });

        for (int i = 0; i < k; ++i) {
            if (greatest.peek() != null) {
                results.add(greatest.poll()); //retrieve and pop head
            }
        }
        
        return results;
    }
    
    private static List<String> getTokens(String query) {
        List<String> tokenList = new ArrayList<String>();
        TokenProcessor processor = new DefaultTokenProcessor();
        String[] tokens = query.split(mRegex);
        for (String token : tokens) {
            tokenList.addAll(processor.processToken(token));
        }
        return tokenList;
    }
    
    public static List<Entry<Integer, Double>> getResults(Map<Integer, Double> accumulator) {
        return findKGreatest(accumulator, 10);
    }
}
