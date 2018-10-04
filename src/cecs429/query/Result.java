package cecs429.query;

import cecs429.index.Posting;
import java.util.ArrayList;
import java.util.List;

public class Result {
    private List<Posting> mResults;
    private int list1Size;
    private int list2Size;
    public Merge util = null;
    
    Result(List<Posting> list) {
        mResults = new ArrayList<>();
        mResults.addAll(list);
    }
    
    public List<Posting> getmResults() {
        return mResults;
    }
    
    public void setmResults(List<Posting> list) {
        mResults = list;
    }
    
    private void printList(List<Posting> list) {
        System.out.print("[");
        list.forEach((p) -> {
            System.out.print(p.getDocumentId() + ", ");
        });
        System.out.println("]");
    }
    
    public interface Merge {
        public void mergeWith(List<Posting> list);
    }
    
    public class IntersectMerge implements Merge {
        
        @Override
        public void mergeWith(List<Posting> list) {
            int itr = 0, jtr = 0;
            List<Posting> tempResults = new ArrayList<>();

            /*
            System.out.println("list1IN: ");
            printList(mResults);
            System.out.println("list2IN: ");
            printList(list);
            */
            
            list1Size = mResults.size();
            list2Size = list.size();

            while (itr < list1Size && jtr < list2Size) {            
                if (mResults.get(itr).getDocumentId() == list.get(jtr).getDocumentId()) {
                    
                    //Maybe repurpose these postings (not necessary to collect)
                    // Merge positions as well
                    /*
                    Posting posting1 = list1.get(itr);
                    Posting posting2 = list2.get(jtr);


                    posting2.getPositions().forEach((i) -> {
                        posting1.addPosition(i);
                    });
                    */

                    tempResults.add(mResults.get(itr));
                    ++itr;
                    ++jtr;
                }
                else if (mResults.get(itr).getDocumentId() < list.get(jtr).getDocumentId()) {
                    ++itr;
                }
                else if (list.get(jtr).getDocumentId() < mResults.get(itr).getDocumentId()) {
                    ++jtr;
                }
            }

            mResults.clear();
            mResults.addAll(tempResults);
            tempResults.clear();
        }
    }

    public class NotMerge implements Merge {
        
        @Override
        public void mergeWith(List<Posting> list) {
            int itr = 0, jtr = 0;
            List<Posting> tempResults = new ArrayList<>();

            /*
            System.out.println("list1IN: ");
            printList(mResults);
            System.out.println("list2IN: ");
            printList(list);
            */
            
            list1Size = mResults.size();
            list2Size = list.size();

            while (itr < list1Size && jtr < list2Size) {            
                if (mResults.get(itr).getDocumentId() == list.get(jtr).getDocumentId()) {
                    // tempResults.add(mResults.get(itr));
                    ++itr;
                    ++jtr;
                }
                else if (mResults.get(itr).getDocumentId() < list.get(jtr).getDocumentId()) {
                    tempResults.add(mResults.get(itr));
                    ++itr;
                }
                else if (list.get(jtr).getDocumentId() < mResults.get(itr).getDocumentId()) {
                    ++jtr;
                }
            }

            mResults.clear();
            mResults.addAll(tempResults);
            tempResults.clear();
        }
    }
    
    public class UnionMerge implements Merge {
        
        @Override
        public void mergeWith(List<Posting> list) {
            int i = 0, j = 0;
            List<Posting> tempResults = new ArrayList<>();

            /*
            System.out.print("Size: " + mResults.size() + "list1UN: ");
            printList(mResults);
            System.out.print("Size: " + list.size() + "list2UN: ");
            printList(list);
            */
            
            list1Size = mResults.size();
            list2Size = list.size();

            //while both lists are inbounds
            while (i < list1Size && j < list2Size) {
                if (mResults.get(i).getDocumentId() == list.get(j).getDocumentId()) {
                    tempResults.add(mResults.get(i));
                    ++i;
                    ++j;
                }
                else if (mResults.get(i).getDocumentId() < list.get(j).getDocumentId()) {
                    tempResults.add(mResults.get(i));
                    ++i;
                }
                else if (list.get(j).getDocumentId() < mResults.get(i).getDocumentId()) {
                    tempResults.add(list.get(j));
                    ++j;
                }
            }

            //Add the rest of either list if lists are not the same size
            if (list1Size != list2Size) {
                if (i == mResults.size()) {
                    //add the rest of list2
                    tempResults.addAll(list.subList(j, list2Size));
                } else if (j == list.size()) {
                    //add the rest of list1
                    tempResults.addAll(mResults.subList(i, list1Size));
                }
            }

            mResults.clear();
            mResults.addAll(tempResults);
            tempResults.clear();
        }
    }
}
