package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.TokenProcessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a phrase literal consisting of one or more terms that must occur in sequence.
 */
public class PhraseLiteral implements QueryComponent {
	// The list of individual terms in the phrase.
	private List<String> mTerms = new ArrayList<>();
	private TokenProcessor mProcessor;
	
	/**
	 * Constructs a PhraseLiteral with the given individual phrase terms.
	 */
	public PhraseLiteral(List<String> terms, TokenProcessor processor) {
		mTerms.addAll(terms);
		mProcessor = processor;
	}
	
	/**
	 * Constructs a PhraseLiteral given a string with one or more individual terms separated by spaces.
	 */
	public PhraseLiteral(String terms, TokenProcessor processor) {
		mTerms.addAll(Arrays.asList(terms.split(" ")));
		mProcessor = processor;
	}
	
	@Override
	public List<Posting> getPostings(Index index) {
		List<Posting> result = new ArrayList<Posting>();
		if (mTerms.size() > 0) {
			result = index.getPostings(mTerms.get(0));
		}
		if (mTerms.size() == 1) {
			return result;
		}
		for (int i = 1; i < mTerms.size(); ++i) {
			result = getNextPostings(result, index.getPostings(mTerms.get(i)));
		}
		return result;

		// TODO: program this method. Retrieve the postings for the individual terms in the phrase,
		// and positional merge them together.
	}

	private List<Posting> getNextPostings(List<Posting> postings1, List<Posting> postings2) {
		List<Posting> result = new ArrayList<Posting>();

		for (int itr1 = 0, itr2 = 0; itr1 < postings1.size() && itr2 < postings2.size();) {
			if (postings1.get(itr1).getDocumentId() == postings2.get(itr2).getDocumentId()
				&& areNearFrom(postings1.get(itr1).getPositions(), postings2.get(itr2).getPositions(), 1)) {
					result.add(postings2.get(itr2));
					++itr1;
					++itr2;
			} else if (postings1.get(itr1).getDocumentId() < postings2.get(itr2).getDocumentId()) {
				++itr1;
			} else {
				++itr2;
			}
		}

		return result;
	}

	private Boolean areNearFrom(List<Integer> positions1, List<Integer> positions2, int k) {
		for (int i = 0; i < positions1.size(); ++i) {
			for (int j = i; j < positions2.size(); ++j) {
				if (positions2.get(j) < positions1.get(i)) {
					continue;
				}
				if ((positions2.get(j) - positions1.get(i)) == k) {
					return true;
				}
			}
		}
		return false;
	}

	
	@Override
	public String toString() {
		return "\"" + String.join(" ", mTerms) + "\"";
	}
}
