package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An AndQuery composes other QueryComponents and merges their postings in an intersection-like operation.
 */
public class AndQuery implements QueryComponent {
    private List<QueryComponent> mComponents;

    public AndQuery(List<QueryComponent> components) {
            mComponents = components;
    }

    // TODO: program the merge for an AndQuery, by gathering the postings of the composed QueryComponents and
    // intersecting the resulting postings.
    @Override
    public List<Posting> getPostings(Index index) {
        List<Posting> result = new ArrayList<Posting>();
        Boolean init = true;
        
        for (QueryComponent qc : mComponents) {
            if (init) {
                result = qc.getPostings(index);
                init = false;
            } else {
                result = intersection(result, qc.getPostings(index));
            }
        }

        return result;
    }
	
    @Override
    public String toString() {
        return String.join(" ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()));
    }
    
    //TODO : Merge Positions
    private List<Posting> intersection(List<Posting> list1, List<Posting> list2) {
        //printList(list1);
        //printList(list2);
        
        List<Posting> result = new ArrayList<Posting>();
        //Iterator<Posting> i, j, iTemp, jTemp;

        for(int itr = 0, jtr = 0; itr < list1.size() && jtr < list2.size(); ) {
            if (list1.get(itr).getDocumentId() == list2.get(jtr).getDocumentId()) {
                result.add(list1.get(itr));
                //System.out.print("Add " + list1.get(itr).getDocumentId() + "; ");
                ++itr;
                ++jtr;
            }
            else if (list1.get(itr).getDocumentId() < list2.get(jtr).getDocumentId()) {
                ++itr;
            }
            else if (list2.get(jtr).getDocumentId() < list1.get(itr).getDocumentId()) {
                ++jtr;
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
}
