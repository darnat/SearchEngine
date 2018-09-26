package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import java.util.ArrayList;

import java.util.List;
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
        
	// TODO: program the merge for an OrQuery, by gathering the postings of the composed QueryComponents and
	// unioning the resulting postings.
	@Override
	public List<Posting> getPostings(Index index) {
            List<Posting> result = new ArrayList<Posting>();
            Boolean init = true;

            for (QueryComponent qc : mComponents) {
                if (init) {
                    result = qc.getPostings(index);
                    init = false;
                } else {
                    result = union(result, qc.getPostings(index));
                }
            }
		
            return result;
	}
	
    private List<Posting> union(List<Posting> list1, List<Posting> list2) {
        //printList(list1);
        //printList(list2);
        
        List<Posting> result = new ArrayList<Posting>();

        for(int i = 0, j = 0; i < list1.size() && j < list2.size(); ) {
            if (list1.get(i).getDocumentId() == list2.get(j).getDocumentId()) {
                result.add(list1.get(i));
                ++i;
                ++j;
            }
            else if (list1.get(i).getDocumentId() < list2.get(j).getDocumentId()) {
                result.add(list1.get(i));
                ++i;
            }
            else if (list2.get(j).getDocumentId() < list1.get(i).getDocumentId()) {
                result.add(list2.get(j));
                ++j;
            }
        }
        
        //System.out.print("Result: ");
        //printList(result);

        return result;
    }
    
    private void printList(List<Posting> list) {
        System.out.print("[");
        for (Posting p : list) {
            System.out.print(p.getDocumentId() + ", ");
        }
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
