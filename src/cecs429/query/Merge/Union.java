/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cecs429.query.Merge;

import cecs429.index.Posting;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author derdummkopf67
 */
public class Union implements Merge{
    @Override
    public List<Posting> merge(List<Posting> list1, List<Posting> list2) {
        int i = 0, j = 0;
        List<Posting> tempResults = new ArrayList<>();

        /*
        System.out.print("Size: " + mResults.size() + "list1UN: ");
        printList(mResults);
        System.out.print("Size: " + list.size() + "list2UN: ");
        printList(list);
        */

        int list1Size = list1.size();
        int list2Size = list2.size();

        //while both lists are inbounds
        while (i < list1Size && j < list2Size) {
            if (list1.get(i).getDocumentId() == list2.get(j).getDocumentId()) {
                tempResults.add(list1.get(i));
                ++i;
                ++j;
            }
            else if (list1.get(i).getDocumentId() < list2.get(j).getDocumentId()) {
                tempResults.add(list1.get(i));
                ++i;
            }
            else if (list2.get(j).getDocumentId() < list1.get(i).getDocumentId()) {
                tempResults.add(list2.get(j));
                ++j;
            }
        }

        //Add the rest of either list if lists are not the same size
        if (list1Size != list2Size) {
            if (i == list1.size()) {
                //add the rest of list2
                tempResults.addAll(list2.subList(j, list2Size));
            } else if (j == list2.size()) {
                //add the rest of list1
                tempResults.addAll(list1.subList(i, list1Size));
            }
        }

        return tempResults;
    }
}
