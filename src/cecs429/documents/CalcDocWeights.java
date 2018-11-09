package cecs429.documents;

import cecs429.index.Index;
import cecs429.index.Posting;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CalcDocWeights {
    
    // of a particular term
    private static double calcWeight(int tftd) {
        return 1.0 + Math.log(tftd);
    }
    
    // BRUTE FORCE: FULL POWER!!!
    private static List<String> getAllVocabInDoc(int docID, Index index) {
        List<String> subset = new ArrayList<>();

        for (String element : index.getVocabulary()) {
            for (Posting p : index.getPostings(element)) {
                if (p.getDocumentId() == docID)
                    subset.add(element);
            }
        }

        return subset;
    }

    private static HashMap<String, Integer> getAllTF_TD(Index index) {

        HashMap<String, Integer> frequencies = new HashMap<>();

        for (String term : index.getVocabulary()) {
            for (Posting p : index.getPostings(term)) {
                if (frequencies.containsKey(term)) 
                    frequencies.put(term, frequencies.get(term) + p.getPositions().size());
                else
                    frequencies.put(term, p.getPositions().size());
            }
        }

        return frequencies;
    }
    
    public static void procedure(DocumentCorpus corpus, File file, Index index) throws IOException {
        List<Double> finalWeights = new ArrayList<>();
        HashMap<String, Integer> tf_td = getAllTF_TD(index);
        
        // Get all vocab in each file
        for (int i = 0; i < corpus.getCorpusSize(); ++i) {
            double weight = 0.0;
            
            // for all vocab in doc
            for (String element : getAllVocabInDoc(i, index)) {
                //add all the weights from the map
                weight += calcWeight(tf_td.get(element));
            }
            
            finalWeights.add(Math.sqrt(weight)); //This doc done, Next Doc
        }
        
        //System.out.println(finalWeights);
        
        writer(finalWeights, file);
        reader(finalWeights, file);
        
        // Still need to modify DiskPositional Index so it knows how to open 
        // this file and skip to an apporpiate location and read 8-byte double
        
    }
    
    // write finalWeights to disk here
    private static void writer(List<Double> weights, File file) throws FileNotFoundException, IOException {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(file))) {
            out.writeInt(weights.size());
            for (Double val : weights) 
                out.writeDouble(val);
        }
    }
    
    private static void reader(List<Double> weights, File file) throws FileNotFoundException, IOException {
        try (DataInputStream in = new DataInputStream(new FileInputStream(file))) {
            //first is an int (number of doubles in file)
            int limit = in.readInt();
            for (int i = 0; i < limit; ++i) 
                System.out.println(in.readDouble());
        }
    }
}
