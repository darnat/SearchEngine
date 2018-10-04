package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.TokenProcessor;
import cecs429.query.Result.NotMerge;

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
		mComponents = new ArrayList<QueryComponent>();
        mComponents.add(cm1);
        mComponents.add(cm2);
	}

	@Override
	public boolean isNegative() {
		return true;
	}

	@Override
	public List<Posting> getPostings(Index index, TokenProcessor processor) {
		Result results = new Result(mComponents.get(0).getPostings(index, processor));

        //iterate thorugh all postings
        for (int i = 1; i < mComponents.size(); ++i) {
            results.util = results.new NotMerge();

            //intersect with previous
            results.util.mergeWith(mComponents.get(i).getPostings(index, processor));
        }            
        
        return results.getmResults();
	}
      
	@Override
	public String toString() {
		return String.join(" ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()));
	}
}
