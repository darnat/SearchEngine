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
    private static Map<Integer, Double> accumulator;
    private static List<Entry<Integer, Double>> results;
    private static List<String> mQueryTokens;
    
    /* Regex split query into tokens */
    private String mRegex;
        
    public RankedRetrieval() {
        mRegex = "\\s+"; //one or more whitespace
        accumulator = new HashMap<>();
        results = new ArrayList<>();
    }
    
    public void printResults(DocumentCorpus corpus) {
        for (int i = 0; i < results.size(); ++i) 
            System.out.println(10 - i + ". \033[1mTitle: \033[0m" + corpus.getDocument(results.get(i).getKey()).getTitle()
            + "\033[1m\n   Accumulator: \033[0m" + results.get(i).getValue());
    }
    
    public  List<Entry<Integer, Double>> getResults() {
        return results;
    }
    
    /** 
     * Will return a Map<DocID, Score> which correspond to the most relevant document 
     * @param index
     * @param corpus 
     * @param query 
     * @throws java.io.IOException 
     */
    public void getMostRelevant(Index index, DocumentCorpus corpus, String query) throws IOException {
        mQueryTokens = getTokens(query);
        results.clear();
        
        for (String token : mQueryTokens) {
            //Calculate w(q,t) 
            double weightQT = 0.0;
            
            if (!index.getPostings(token).isEmpty()) {
                weightQT = Math.log(1 + (corpus.getCorpusSize() / index.getPostings(token).size()));
            }
            
            for (Posting p : index.getPostings(token)) {
                if (!accumulator.containsKey(p.getDocumentId())) //Never encountered
                    accumulator.put(p.getDocumentId(), 0.0);

                double accu = accumulator.get(p.getDocumentId());

                //calculate weight of doc = 1 + ln(tf(t,d)) //term frequency
                double weightOfDoc = 1.0 + Math.log(p.getPositions().size());

                accumulator.replace(p.getDocumentId(), accu + (weightQT * weightOfDoc));
            }

        }
        
        for (Integer key : accumulator.keySet()) {
            if (accumulator.get(key) != 0.0)
               accumulator.put(key, accumulator.get(key) / DiskPositionalIndex.getDocWeight(key));
        }
        
        findNGreatest(accumulator, 10);
    }
    
    private void findNGreatest(Map<Integer, Double> map, int n)
    {
        Comparator<? super Entry<Integer, Double>> comparator = 
            (Entry<Integer, Double> e0, Entry<Integer, Double> e1) -> {
                Double v0 = e0.getValue();
                Double v1 = e1.getValue();
                return v0.compareTo(v1);
        };
        
        PriorityQueue<Entry<Integer, Double>> highest = new PriorityQueue<>(n, comparator);
        
        for (Entry<Integer, Double> entry : map.entrySet()) {
            highest.offer(entry);
            
            while (highest.size() > n)
                highest.poll();
        }

        while (highest.size() > 0)
            results.add(highest.poll());
    }
    
    public List<String> getTokens(String query) {
        TokenProcessor processor = new DefaultTokenProcessor();
        return Arrays.asList(processor.processToken(query).get(0).split(mRegex));
    }
}
