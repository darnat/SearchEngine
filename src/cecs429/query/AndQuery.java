package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.query.Result.IntersectMerge;
import cecs429.text.TokenProcessor;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * An AndQuery composes other QueryComponents and merges their postings in an
 * intersection-like operation.
 */
public class AndQuery implements QueryComponent {

    private List<QueryComponent> mComponents;

    public AndQuery(List<QueryComponent> components) {
        mComponents = components;
    }

    public AndQuery(QueryComponent cm1, QueryComponent cm2) {
        mComponents = new ArrayList<QueryComponent>();
        mComponents.add(cm1);
        mComponents.add(cm2);
    }

    @Override
    public List<Posting> getPostings(Index index, TokenProcessor processor) {
        //initialize result with first postings
        Result results = new Result(mComponents.get(0).getPostings(index, processor));
        
        //iterate thorugh all postings
        for (int i = 1; i < mComponents.size(); ++i) {
            results.util = results.new IntersectMerge();

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
