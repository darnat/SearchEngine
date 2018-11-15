package test;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.DocumentCorpus;
import cecs429.index.Index;
import cecs429.text.DefaultTokenProcessor;
import cecs429.text.TokenProcessor;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Stream;
import static org.junit.Assert.assertEquals;

public class RankedRetrieval {
    static DocumentCorpus corpus;
    static Index index;
    static Set<List<String>> querySet;
    static String mRegex;
        
    public static void initializeTest() {
        Path corpusPath = Paths.get("small-corpus").toAbsolutePath().normalize();
        corpus = DirectoryCorpus.loadJsonDirectory(corpusPath, ".json");
        TokenProcessor processor = new DefaultTokenProcessor();

        mRegex = "\\s+";
        
        querySet = new HashSet<>();
        querySet.add(getTokens("cake boy"));
        querySet.add(getTokens("spoon fork"));
        querySet.add(getTokens("boy lied"));
        querySet.add(getTokens("spoon experiments"));
        querySet.add(getTokens("start using grammar"));   
        
        /** The test case. */
        Map<Integer, Double> testAccumulator = new HashMap<>();
        testAccumulator.put(0,1.0);
        testAccumulator.put(1,0.0);
        testAccumulator.put(3,2.0);
        testAccumulator.put(4,0.5);
        testAccumulator.put(2,3.0);
        
        /** The correct answer. */
        List<Map.Entry<Integer, Double>> compAccumulator = new ArrayList<>();
        Map<Integer, Double> data = new HashMap<>();
        data.put(2,3.0);
        data.put(3,2.0);
        for (Map.Entry<Integer, Double> entry: data.entrySet()) 
            compAccumulator.add(entry);
        
        assertEquals(compAccumulator, findKGreatest(testAccumulator, 2));
    }
    
    private static List<String> getTokens(String query) {
        List<String> tokenList = new ArrayList<>();
        TokenProcessor processor = new DefaultTokenProcessor();
        String[] tokens = query.split(mRegex);
        
        for (String token : tokens)
            tokenList.addAll(processor.processToken(token));
        
        return tokenList;
    }
    
    // <DocID, Value>
    private static List<Map.Entry<Integer, Double>> findKGreatest(Map<Integer, Double> map, int k) {
        List<Map.Entry<Integer, Double>> results = new ArrayList<>();
        
        // For comparison for "sorting" the priority queue
        Comparator<? super Map.Entry<Integer, Double>> comparator = 
            (Map.Entry<Integer, Double> val1, Map.Entry<Integer, Double> val2) -> {
                return val2.getValue().compareTo(val1.getValue());
        };
        
        PriorityQueue<Map.Entry<Integer, Double>> greatest = new PriorityQueue<>(k, comparator);
        
        //Build Priority Queue based on entries
        map.entrySet().forEach((entry) -> {
            greatest.add(entry); //Indiscriminately insert
        });

        for (int i = 0; i < k; ++i)
            results.add(greatest.poll()); //retrieve and pop head
        
        return results;
    }
}