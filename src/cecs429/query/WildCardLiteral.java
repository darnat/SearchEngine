package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.TokenProcessor;
import java.util.ArrayList;
import java.util.List;

//Gererate 1, 2, and 3-grams for each vocabulary type in the vocabulary
// Represent the k gram index with a new class

public class WildCardLiteral implements QueryComponent{
    
    // The list of individual terms in the phrase.
    private KGram kGram = null;

    public WildCardLiteral(String term) {
        kGram = new KGram(term);
    }

    @Override
    public List<Posting> getPostings(Index index, TokenProcessor processor) {
        List<Posting> results = new ArrayList<>();
        
        // generate the largest k-grams it can for its literal 
        // kGram.getParsed() will retain largest kGram
        String lrgKGram = getLargestKGram();
        
        //retrieve and intersect the list of vocabulary  types for each k-gram from the k-gram index
        //Note: Wildcard index must have access to kgram index
        
        //the intersected list will contain all candidate type strings
        //implement a post filtering step to ensure that each candidate matches the wildcard pattern
        //OR together the postings for the processed term from each final wildcard candiate and return them as the postings list for wild cardLiteral
        
        return results;
    }

    private String getLargestKGram() {
        int largestLength = 0;
        int indexLarge = 0;
        
        for (int i = 0; i < kGram.getParsedTerm().size(); ++i) {
            if (kGram.getParsedTerm().get(i).length() > largestLength) {
                largestLength = kGram.getParsedTerm().get(i).length();
                indexLarge = i; 
            }
        }
        
        return kGram.getParsedTerm().get(indexLarge);
    }

    @Override
    public String toString() {
        return kGram.getmTerm();
    }
}
