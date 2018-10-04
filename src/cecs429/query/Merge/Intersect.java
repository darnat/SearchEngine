package cecs429.query.Merge;

import cecs429.index.Posting;
import java.util.ArrayList;
import java.util.List;

public class Intersect implements Merge {
       
    @Override
    public List<Posting> merge(List<Posting> list1, List<Posting> list2) {
        int itr = 0, jtr = 0;
        List<Posting> tempResults = new ArrayList<>();

        /*
        System.out.println("list1IN: ");
        printList(mResults);
        System.out.println("list2IN: ");
        printList(list);
        */

        int list1Size = list1.size();
        int list2Size = list2.size();

        while (itr < list1Size && jtr < list2Size) {            
            if (list1.get(itr).getDocumentId() == list2.get(jtr).getDocumentId()) {

                //Maybe repurpose these postings (not necessary to collect)
                // Merge positions as well

                Posting posting1 = list1.get(itr);
                Posting posting2 = list2.get(jtr);

                posting2.getPositions().forEach((i) -> {
                    posting1.addPosition(i);
                });

                tempResults.add(list1.get(itr));
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

        return tempResults;
    }
}
