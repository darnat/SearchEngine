package cecs429.index;

import cecs429.index.Posting;

import java.util.*;

/**
 * Implements an Index using a positional inverted index.
 */
public class PositionalInvertedIndex implements Index {
	private Map<String, List<Posting>> map = new HashMap<>();
	
	/**
	 * Associates the given documentId with the given term in the index.
	 */
	public void addTerm(String term, int documentId, int position) {
		List<Posting> postings = map.getOrDefault(term, Collections.emptyList());

		if (!postings.isEmpty()) {
			if (postings.get(postings.size() - 1).getDocumentId() != documentId) {
				postings.add(new Posting(documentId, position));
			} else {
				postings.get(postings.size() - 1).addPosition(position);
			}
		} else {
			List<Posting> newPosting = new ArrayList<>();
			newPosting.add(new Posting(documentId, position));
			map.put(term, newPosting);
		}
	}
	
	@Override
	public List<Posting> getPostings(String term) {
		return map.getOrDefault(term, Collections.emptyList());
	}
	
	public List<String> getVocabulary() {
		List<String> vocabulary = new ArrayList<>();

		for (String term : map.keySet()) {
			vocabulary.add(term);
		}

		Collections.sort(vocabulary);

		return Collections.unmodifiableList(vocabulary);
	}
}
