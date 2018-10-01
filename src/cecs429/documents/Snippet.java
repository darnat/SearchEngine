package cecs429.documents;

import cecs429.query.QueryComponent;
import cecs429.text.*;

import java.lang.StringBuilder;
import java.io.BufferedReader;
import java.io.Reader;
import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.*;

public class Snippet {
    public static int MAX_LENGTH = 300;
    private Reader mContent;
    private List<Integer> mPositions;

    public Snippet(Reader content, List<Integer> positions) {
        mContent = content;
        mPositions = positions;
    }

    public String getContent() {
        DefaultTokenProcessor processor = new DefaultTokenProcessor();
        Stemmer stemmer = Stemmer.getInstance();
        BufferedReader in = new BufferedReader(mContent);
        Scanner sc = new Scanner(in);
        List<String> terms = new ArrayList<>();

        while (sc.hasNext()) {
            terms.add(processor.normalizeToken(sc.next()));
        }

        sc.close();

        // Map unique terms to positions
        Map<String, List<Integer>> mappings = new HashMap<>();
        for (Integer i : mPositions) {
            String term = stemmer.stemToken(terms.get(i));
            if (mappings.containsKey(term)) {
                mappings.get(term).add(i);
            } else {
                List<Integer> posList = new ArrayList<Integer>();
                posList.add(i);
                mappings.put(term, posList);
            }
        }

        // Get values from mapping
        Collection<List<Integer>> positionList = mappings.values();
        int size = positionList.size();
        StringBuilder sb = new StringBuilder();

        int startPos = -1;
        int endPos = startPos;

        // Handle one term
        if (size == 1) {
            startPos = positionList.stream().findFirst().get().get(0);
            endPos = startPos;
            // Count characters and record list position
            while (endPos < terms.size() && sb.length() <= MAX_LENGTH) {                
                sb.append(terms.get(endPos));
                endPos++;
            }
            // TODO: Add terms before startPos if length is still less than MAX_LENGTH
        } else { // Handle multiple terms
            boolean foundSnippet = false;

            while (!foundSnippet) {
                final int newSize = size;
                Supplier<IntStream> supplier = () -> positionList.stream().limit(newSize).mapToInt(v -> v.get(0));

                if (supplier.get().sum() <= MAX_LENGTH) {
                    int[] values = supplier.get().sorted().toArray();
                    startPos = values[0];
                    endPos = values[values.length - 1];
                    // Add all terms from start to end
                    for (int i = startPos; i <= endPos; i++) {
                        sb.append(terms.get(i));
                    }
                    // Add additional terms if below MAX_LENGTH
                    while (endPos < terms.size() && sb.length() <= MAX_LENGTH) {
                        endPos++;
                        sb.append(terms.get(endPos));
                    }

                    foundSnippet = true;
                    break;
                } else {
                    size--;
                }
            }
        }


        // Sanity Check
        // for (Map.Entry<String, List<Integer>> map : mappings.entrySet()) {
        //     System.out.println("Key: " + map.getKey() + " Value: " + map.getValue());
        // }

        return String.join(" ",
            terms.subList(startPos, endPos)
            .stream().map(c -> c.toString())
            .collect(Collectors.toList())
        );
    }
}