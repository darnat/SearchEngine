package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.DefaultTokenProcessor;
import cecs429.text.TokenProcessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class NearLiteral extends ProximityQuery {

    // The list of individual terms in the phrase.
    private List<String> mTerms = new ArrayList<>();

    // up to this Distance
    private Integer mK;

    // Regular expression match the near
    private String mRegex = "^near/([0-9]+)$";

    public NearLiteral(List<String> terms) {
        Pattern p = Pattern.compile(mRegex);
        Matcher m;

        mTerms.addAll(terms);
        String neark = mTerms.remove(1);
        m = p.matcher(neark.toLowerCase());
        m.find();
        mK = Integer.valueOf(m.group(1));
    }

    public NearLiteral(String terms) {
            this(Arrays.asList(terms.split(" ")));
    }

    public NearLiteral(String term1, String nearOp, String term2) {
        this(Arrays.asList(term1, nearOp, term2));
    }

    @Override
    public List<Posting> getPostings(Index index, TokenProcessor processor) {
        return getProximity(mTerms, index, processor);
    }

    @Override
    public Integer finalSize() {
        return mTerms.size();
    }

    @Override
    public Boolean validProximity(int pos) {
        return Math.abs(pos) <= mK;
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