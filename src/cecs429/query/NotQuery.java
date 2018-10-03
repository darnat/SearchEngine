package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.TokenProcessor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * An NotQuery exclude all the document containing this specific components
 */
public class NotQuery implements QueryComponent {
	// The component of the Not query.
	private QueryComponent mComponent;
	
	public NotQuery(QueryComponent component) {
		mComponent = component;
	}

	@Override
	public boolean isNegative() {
		return true;
	}

	@Override
	public List<Posting> getPostings(Index index, TokenProcessor processor) {
        return mComponent.getPostings(index, processor);
	}
      
	@Override
	public String toString() {
		return "-" + mComponent.toString();
	}
}
