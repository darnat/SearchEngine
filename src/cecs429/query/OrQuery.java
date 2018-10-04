package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.query.Merge.Union;
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
            results = new Union().merge(results, mComponents.get(i).getPostings(index, processor));
        }
        
        return results;
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
