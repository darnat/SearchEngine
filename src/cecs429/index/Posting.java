package cecs429.index;

import java.lang.*;
import java.util.*;

/**
 * A Posting encapulates a document ID associated with a search query component.
 */
public class Posting {
	List<Integer> positions = new ArrayList<>();
	private int mDocumentId;
	
	public Posting(int documentId, int position) {
		mDocumentId = documentId;
		positions.add(position);
	}
	
	public int getDocumentId() {
		return mDocumentId;
	}

	public void addPosition(int position) {
		positions.add(position);
	}

	@Override
	public String toString() {
		StringBuilder postingSB = new StringBuilder();
		postingSB.append("DocumentId: " + mDocumentId + ", Positions: [ ");
		for (Integer pos : positions) {
			postingSB.append(pos + " ");
		}
		postingSB.append("]");
		return postingSB.toString();
	}
}
