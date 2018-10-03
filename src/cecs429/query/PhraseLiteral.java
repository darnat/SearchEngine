package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.DefaultTokenProcessor;
import cecs429.text.TokenProcessor;

import java.lang.Math;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

/**
 * Represents a phrase literal consisting of one or more terms that must occur in sequence.
 */
public class PhraseLiteral implements QueryComponent {
	// The list of individual terms in the phrase.
	private List<String> mTerms = new ArrayList<>();
	
	/**
	 * Constructs a PhraseLiteral with the given individual phrase terms.
	 */
	public PhraseLiteral(List<String> terms) {
		mTerms.addAll(terms);
	}
	
	/**
	 * Constructs a PhraseLiteral given a string with one or more individual terms separated by spaces.
	 */
	public PhraseLiteral(String terms) {
		mTerms.addAll(Arrays.asList(terms.split(" ")));
	}
	
	@Override
	public List<Posting> getPostings(Index index, TokenProcessor processor) {
		List<Posting> postings = new ArrayList<Posting>();
		Map<Integer, List<List<Integer>>> results = new HashMap<>();
		List<List<Integer>> tmp;
		List<Integer> tmpList;
		if (mTerms.size() == 0) { return postings; }

		postings = index.getPostings(((DefaultTokenProcessor) processor).normalizeAndStemToken(mTerms.get(0)));

		for (Posting p: postings) {
			tmp = new ArrayList<>();
			for (Integer position: p.getPositions()) {
				tmpList = new ArrayList<>();
				tmpList.add(position);
				tmp.add(tmpList);
			}
			results.put(p.getDocumentId(), tmp);
		}
		for (int i = 1; i < mTerms.size(); ++i) {
			results = getNextPostings(results, index.getPostings(((DefaultTokenProcessor) processor).normalizeAndStemToken(mTerms.get(i))));
		}

		return transformToPostings(results);
	}

	private Map<Integer, List<List<Integer>>> getNextPostings(Map<Integer, List<List<Integer>>> current, List<Posting> postings) {
		List<List<Integer>> tmp;
		Iterator<List<Integer>> it;
		List<Integer> lp;
		Boolean added;

		for (Posting p: postings) {
			if (current.containsKey(p.getDocumentId()) == false) { continue; }
			tmp = current.get(p.getDocumentId());
			it = tmp.iterator();
			while (it.hasNext()) {
				added = false;
				lp = it.next();
				for (Integer pos: p.getPositions()) {
					if ((pos - lp.get(lp.size() - 1)) == 1) {
						lp.add(pos);
						added = true;
						break;
					}
				}
				if (added == false) {
					it.remove();
				}
			}
		}
		return current;
	}

	private List<Posting> transformToPostings(Map<Integer, List<List<Integer>>> current) {
		List<Posting> result = new ArrayList<Posting>();
		Map.Entry<Integer, List<List<Integer>>> entry;
		Integer docID;
		Posting posting;
		List<List<Integer>> tmp;
		
		Iterator<Map.Entry<Integer, List<List<Integer>>>> itr = current.entrySet().iterator();
		while (itr.hasNext()) {
			entry = itr.next();
			docID = entry.getKey();
			tmp = entry.getValue();
			if (tmp.size() == 0) { continue; }
			posting = new Posting(docID);
			for (List<Integer> positions: tmp) {
				for (Integer pos: positions) {
					posting.addPosition(pos);
				}
			}
			result.add(posting);
		}

		return result;
	}
	
	@Override
	public String toString() {
		return "\"" + String.join(" ", mTerms) + "\"";
	}
}