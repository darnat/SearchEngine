package cecs429.query;

import cecs429.index.Posting;
import java.util.ArrayList;
import java.util.List;

public class KGram {
    private String mTerm;
    private List<Integer> wildCardPositions = null;
    private static List<String> subsequence = null;
    private List<String> parsedTerm = null;
    
    public KGram(String component) {
        mTerm = component; //retains original literal
        wildCardPositions = new ArrayList<>();
        subsequence = new ArrayList<>();
        parsedTerm = new ArrayList<>();
        
        setWCIndices(); // sets all positions of '*'
        parseTerm(); //parse mTerm into substring across '*'
        
        /* This is for kgram Index? */
        grammify();//Set up for grammification
        
        printSubSequence();
        
        //What kind of wild card query are we doing?
        /* Not sure if this belongs here??? */
        wildCardPositions.forEach((i) -> {
            if (i == 0) { //Leading
                leadingWildCard();
            } else if (i == component.length() - 1) { //Trailing
                trailingWildCard();
            }
            else { //middle
                // example: colo*r
                //intersection of Trailing Colo* and Leading *r
            }
        });
    }
    
    /* Will set up the indices of '*' */
    private void setWCIndices() {
        //Where is the WildCard char? Leading, Trailing, Middle
        for (int i = 0; i < mTerm.length(); ++i) {
            if (mTerm.charAt(i) == '*') {
                wildCardPositions.add(i);
            }
        }
    }
    
    /* Will parse mTerm into substrings split accross '*' */
    private void parseTerm() {
        Integer front = 0;
        for (Integer i : wildCardPositions) {
            parsedTerm.add(mTerm.substring(front, i - 1));
            front = i + 1; // '*' is at i, so go to next index
        }
    }
    
    private List<Posting> leadingWildCard() {
        //Vocabulary must be reversed

        return null;
    }
    
    private List<Posting> trailingWildCard() {
        return null;
    }
    
    /* This is for the KGRAM index */
    private void grammify() {
        String mGram = mTerm;
        
        //Generate 1grams   
        for (int i = 0; i < mTerm.length(); ++i) {
            //ignore wild card positions
            if ('*' != mTerm.charAt(i))
                subsequence.add(String.valueOf(mTerm.charAt(i)));
        }
        
        mGram = "$" + mTerm + "$";
        
        // Generate 2 and 3-grams
        for (int gramLength = 2; gramLength < 3 + 1; ++gramLength) {
            //traverse string
            for (int j = 0; j < mGram.length(); ++j) {
                // if not out of bounds
                if (j + gramLength <= mGram.length()) {
                    if (!mGram.substring(j, j + gramLength).contains("*")) {
                        subsequence.add(mGram.substring(j, j + gramLength));
                    }
                }
            }            
        }
    }
    
    public List<String> getParsedTerm() {
        return parsedTerm;
    }
    
    public String getmTerm() {
        return mTerm;
    }
       
    private void printSubSequence() {
        System.out.println("Subsquence: ");
        subsequence.forEach((str) -> {
            System.out.println(str);
        });
    }
}
