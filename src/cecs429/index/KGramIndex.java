package cecs429.index;

import java.util.ArrayList;
import java.util.List;


public class KGramIndex implements Index {
    //BST
    List<String> mVocabulary = new ArrayList<>();
    
    // will be composed by the vocabulary from positionalInvertedOndex
    public KGramIndex(Index index) {
        mVocabulary = index.getVocabulary();
    }
    
    @Override
    public List<Posting> getPostings(String term) {
        
        return null;
    }

    public void reverseVocab() {
        mVocabulary.forEach((str) -> {
            str = new StringBuilder().append(str).reverse().toString();
        });
    }
    
    @Override
    public List<String> getVocabulary() {
        return mVocabulary;
    }
}
