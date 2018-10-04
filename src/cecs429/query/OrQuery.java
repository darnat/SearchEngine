package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.query.Result.UnionMerge;
import cecs429.text.TokenProcessor;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * An OrQuery composes other QueryComponents and merges their postings with a union-type operation.
 */
public class OrQuery implements QueryComponent {
	// The components of the Or query.
	private List<QueryComponent> mComponents;
	
	public OrQuery(List<QueryComponent> components) {
		mComponents = components;
    }
    
    public OrQuery(QueryComponent cm1, QueryComponent cm2) {
        mComponents = new ArrayList<QueryComponent>();
        mComponents.add(cm1);
        mComponents.add(cm2);
    }
    
	@Override
	public List<Posting> getPostings(Index index, TokenProcessor processor) {
            //intialize with the reulsts from the first postings
            Result results = new Result(mComponents.get(0).getPostings(index, processor));

            for (int i = 1; i < mComponents.size(); ++i) {
                results.util = results.new UnionMerge();
                
                //union with prev results
                results.util.mergeWith(mComponents.get(i).getPostings(index, processor));
            }

            return results.getmResults();
	}
    
    //Print list of doc ids (for debugging)
    private void printList(List<Posting> list) {
        System.out.print("[");
        list.forEach((docID) -> {
            System.out.print(docID.getDocumentId() + ", ");
            });
        System.out.println("]");
    }
      
    @Override
    public String toString() {
        // Returns a string of the form "[SUBQUERY] + [SUBQUERY] + [SUBQUERY]"
        return "(" +
         String.join(" + ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()))
         + " )";
    }
}
