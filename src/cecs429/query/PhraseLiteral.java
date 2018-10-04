package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.DefaultTokenProcessor;
import cecs429.text.TokenProcessor;

import java.lang.Math;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

/**
 * Represents a phrase literal consisting of one or more terms that must occur in sequence.
 */
public class PhraseLiteral extends ProximityQuery {
	// The list of individual terms in the phrase.
	private List<String> mTerms = new ArrayList<>();
	
	/**
	 * Constructs a PhraseLiteral with the given individual phrase terms.
	 */
	public PhraseLiteral(List<String> terms) {
		mTerms.addAll(terms);
	}
	
	/**
	 * Constructs a PhraseLiteral given a string with one or more individual terms separated by spaces.
	 */
	public PhraseLiteral(String terms) {
		mTerms.addAll(Arrays.asList(terms.split(" ")));
	}

	public List<String> getTerms() {
		return mTerms;
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
        return pos == 1;
    }
	
	@Override
	public String toString() {
		return "\"" + String.join(" ", mTerms) + "\"";
	}
}