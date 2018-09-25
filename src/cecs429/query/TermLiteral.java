package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.DefaultTokenProcessor;
import cecs429.text.TokenProcessor;

import java.util.List;

/**
 * A TermLiteral represents a single term in a subquery.
 */
public class TermLiteral implements QueryComponent {
	private String mTerm;
	private TokenProcessor mProcessor;
	
	public TermLiteral(String term, TokenProcessor processor) {
		mTerm = term;
		mProcessor = processor;
	}
	
	public String getTerm() {
		return mTerm;
	}
	
	@Override
	public List<Posting> getPostings(Index index) {
		return index.getPostings(((DefaultTokenProcessor) mProcessor).normalizeToken(mTerm));
	}
	
	@Override
	public String toString() {
		return mTerm;
	}
}
