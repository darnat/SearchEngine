package cecs429.query;

import cecs429.index.Posting;
import java.util.ArrayList;
import java.util.List;

//Gererate 1, 2, and 3-grams for each vocabulary type in the vocabulary
// Represent the k gram index with a new class

//generate the largest k-grams it can for its literal 
//retrieve and intersect the list of vocabulary  types for each k-gram from the k-gram index
//Note: Wildcard must have access to kgram index
//the intersected list will contain all candidate type strings
//implement a post filtering step to ensure that each candidate matches the wildcard pattern
//OR together the postings fir the processed term from each final wildcard candiate and return them as the postings list for wild cardLiteral      

public class KGram {
    // Will retain all subsequence of 
    private static List<String> subsequence = new ArrayList<>();
    //Index kIndex = new KGramIndex(index);
    
    private static List<Integer> wildCardPositions;
    
    // Will retain the component to be grammified
    private static String mGram;
    
    public KGram(String component) {
        //Where is the WildCard char? Leading, Trailing, Middle
        for (int i = 0; i < component.length(); ++i) {
            if (component.charAt(i) == '*') {
                wildCardPositions.add(i);
            }
        }
        
        //Set up for grammification
        grammify(component);
        
        System.out.println("Subsquence: ");
        for(String str : subsequence) {
            System.out.println(str);
        }
        
        //What kind of wild card query are we doing?
        for (Integer i : wildCardPositions) {
            if (i == 0) { //Leading
                leadingWildCard();
            } else if (i == component.length() - 1) { //Trailing
                trailingWildCard();
            }
            else { //middle
                // example: colo*r
                //intersection of Trailing Colo* and Leading *r
            }  
        }
    }
    
    private static List<Posting> leadingWildCard() {
        //Vocabulary must be reversed

        return null;
    }
    
    private static List<Posting> trailingWildCard() {
        return null;
    }
    
    private static void grammify(String component) {
        //Generate 1grams   
        for (int i = 0; i < component.length(); ++i) {
            //ignore wild card positions
            if (i != wildCardPositions.get(i))
                subsequence.add(String.valueOf(component.charAt(i)));
        }
        
        mGram = "$" + component + "$";
        
        // Generate 2 and 3-grams
        for (int gramLength = 2; gramLength < 3 + 1; ++gramLength) {
            //traverse string
            for (int j = 0; j < mGram.length(); ++j) {

                // if not out of bounds
                if (j + gramLength < mGram.length()) {
                    subsequence.add(mGram.substring(j, j + 2));
                }
            }            
        }
    }
}
