package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.TokenProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An NotQuery exclude all the document containing these specific components
 */
public class NotQuery implements QueryComponent {
	// The components of the Not query.
	private List<QueryComponent> mComponents;
	
	public NotQuery(List<QueryComponent> components) {
		mComponents = components;
	}
        
	// TODO: program the merge for an OrQuery, by gathering the postings of the composed QueryComponents and
	// unioning the resulting postings.
	@Override
	public List<Posting> getPostings(Index index, TokenProcessor processor) {
            List<Posting> result = new ArrayList<Posting>();
            return result;
	}
      
	@Override
	public String toString() {
		// Returns a string of the form "[SUBQUERY] + [SUBQUERY] + [SUBQUERY]"
		return "(" +
		 String.join(" + ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()))
		 + " )";
	}
}
