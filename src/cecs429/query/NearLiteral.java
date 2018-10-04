package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.DefaultTokenProcessor;
import cecs429.text.TokenProcessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NearLiteral implements QueryComponent {

    // The list of individual terms in the phrase.
    private List<String> mTerms = new ArrayList<>();

    public NearLiteral(List<String> terms) {
            mTerms.addAll(terms);
    }

    public NearLiteral(String terms) {
            mTerms.addAll(Arrays.asList(terms.split(" ")));
    }

    @Override
    public List<Posting> getPostings(Index index, TokenProcessor processor) {
        List<Posting> results = new ArrayList<>();
        List<Posting> postings1;
        List<Posting> postings2;
        
        // Ensure that we have a list of 3
        if (mTerms.size() == 3) {
            postings1 = index.getPostings(((DefaultTokenProcessor) processor).normalizeAndStemToken(mTerms.get(0)));
            postings2 = index.getPostings(((DefaultTokenProcessor) processor).normalizeAndStemToken(mTerms.get(2)));

            // Traverse all documents in postings 1 and 2
            int itr = 0, jtr = 0;

            while (itr < postings1.size() && jtr < postings2.size()) {
                //If docIDs match
                if (postings1.get(itr).getDocumentId() == postings2.get(jtr).getDocumentId()) {
                    //if postions are within k
                    if (areNearFrom(postings1.get(itr).getPositions(), postings2.get(jtr).getPositions(), Integer.valueOf(mTerms.get(1).replace("/","")))) {
                        //add posting to results
                        results.add(postings1.get(itr));
                    }
                    ++itr;
                    ++jtr;
                }      

                // else which index is smaller? catchup
                else if (postings1.get(itr).getDocumentId() < postings2.get(jtr).getDocumentId()) {
                    ++itr;
                }
                else if (postings2.get(jtr).getDocumentId() < postings1.get(itr).getDocumentId()) {
                    ++jtr;
                }
            }
        }
        
        return results;
    }

    private boolean areNearFrom(List<Integer> positions1, List<Integer> positions2, int k) {
        // Sanity Check
        System.out.println("k = " + k);
        System.out.print("positions1: ");
        printPositions(positions1);

        System.out.print("positions2: ");
        printPositions(positions2);
        // End

        for (int i = 0; i < positions1.size(); ++i) {
            for (int j = i; j < positions2.size(); ++j) {
                //Only consider queries where positions2 word comes after word from positions1 
                if (positions2.get(j) > positions1.get(i)) {      
                    if (positions2.get(j) - positions1.get(i) <= k) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public String toString() {
            return "\"" + String.join(" ", mTerms) + "\"";
    }

    private void printPositions(List<Integer> positions) {
        System.out.print("[");
        positions.forEach((i) -> {
            System.out.print(i + " ");
        });
        System.out.println("]");
    }
}