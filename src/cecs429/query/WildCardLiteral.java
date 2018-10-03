package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.TokenProcessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//Gererate 1, 2, and 3-grams for each vocabulary type in the vocabulary
// Represent the k gram index with a new class

public class WildCardLiteral implements QueryComponent{
    
    // The list of individual terms in the phrase.
    private List<String> mTerms = new ArrayList<>();

    public WildCardLiteral(List<String> terms) {
            mTerms.addAll(terms);
    }

    public WildCardLiteral(String terms) {
        new KGram(terms);
            mTerms.addAll(Arrays.asList(terms.split(" ")));
    }

    @Override
    public List<Posting> getPostings(Index index, TokenProcessor processor) {
        List<Posting> results = new ArrayList<>();
        
        //generate the largest k-grams it can for its literal 
        //retrieve and intersect the list of vocabulary  types for each k-gram from the k-gram index
        //Note: Wildcard index must have access to kgram index
        //the intersected list will contain all candidate type strings
        //implement a post filtering step to ensure that each candidate matches the wildcard pattern
        //OR together the postings fir the processed term from each final wildcard candiate and return them as the postings list for wild cardLiteral
        
        return results;
    }


    @Override
    public String toString() {
        return "\"" + String.join(" ", mTerms) + "\"";
    }
}
