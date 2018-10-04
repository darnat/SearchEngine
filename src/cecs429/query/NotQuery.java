package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.query.Merge.Not;
import cecs429.text.TokenProcessor;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * An NotQuery exclude all the document containing this specific components
 */
public class NotQuery implements QueryComponent {
	// The component of the Not query.
	private List<QueryComponent> mComponents;
	
	public NotQuery(QueryComponent cm1, QueryComponent cm2) {
            mComponents = new ArrayList<>();
            mComponents.add(cm1);
            mComponents.add(cm2);
	}

	@Override
	public List<Posting> getPostings(Index index, TokenProcessor processor) {
            //initialize result with first postings
            List<Posting> results = new ArrayList<>();
            results.addAll(mComponents.get(0).getPostings(index, processor));
        
            //iterate thorugh all postings
            for (int i = 1; i < mComponents.size(); ++i) {
                //intersect with previous
                results = new Not().merge(results, mComponents.get(i).getPostings(index, processor));
            }
        
            return results;
	}
      
	@Override
	public String toString() {
            return String.join(" ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()));
	}
}