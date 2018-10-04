package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.DefaultTokenProcessor;
import cecs429.text.TokenProcessor;

import java.lang.Math;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

/**
 * Represents a phrase literal consisting of one or more terms that must occur in sequence.
 */
public abstract class ProximityQuery implements QueryComponent {

	public List<Posting> getProximity(List<String> terms, Index index, TokenProcessor processor) {
		List<Posting> postings = new ArrayList<Posting>();
		Map<Integer, List<List<Integer>>> results = new TreeMap<>();
		List<List<Integer>> tmp;
		List<Integer> tmpList;
		
		postings = index.getPostings(((DefaultTokenProcessor) processor).normalizeAndStemToken(terms.get(0)));

		for (Posting p: postings) {
			tmp = new ArrayList<>();
			for (Integer position: p.getPositions()) {
				tmpList = new ArrayList<>();
				tmpList.add(position);
				tmp.add(tmpList);
			}
			results.put(p.getDocumentId(), tmp);
		}
		for (int i = 1; i < terms.size(); ++i) {
			results = getNextPostings(results, index.getPostings(((DefaultTokenProcessor) processor).normalizeAndStemToken(terms.get(i))));
		}

		return transformToPostings(results);
	}

	// Condition to validate if the distance is valid or not
	public abstract Boolean validProximity(int pos);
	// What should be the final size of the phrase literal to be validated or not
	public abstract Integer finalSize();

	/**
	 * Get the next postings which match with the last position of the last word in the phrase Literal
	 */
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
					if (validProximity((pos - lp.get(lp.size() - 1)))) {
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

	/**
	 * Transform the Map, into a well formate array of Posting including the positions
	 */
	private List<Posting> transformToPostings(Map<Integer, List<List<Integer>>> current) {
		List<Posting> result = new ArrayList<Posting>();
		Map.Entry<Integer, List<List<Integer>>> entry;
		Integer docID;
		Posting posting;
		Boolean added;
		List<List<Integer>> tmp;
		
		Iterator<Map.Entry<Integer, List<List<Integer>>>> itr = current.entrySet().iterator();
		while (itr.hasNext()) {
			entry = itr.next();
			docID = entry.getKey();
			tmp = entry.getValue();
			if (tmp.size() == 0) { continue; }
			posting = new Posting(docID);
			added = false;
			for (List<Integer> positions: tmp) {
				if (positions.size() < finalSize()) { continue; }
				added = true;
				for (Integer pos: positions) {
					posting.addPosition(pos);
				}
			}
			if (added) {
				result.add(posting);
			}
		}

		return result;
	}
}