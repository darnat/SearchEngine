package cecs429.index;

import java.lang.*;
import java.util.*;

/**
 * A Posting encapulates a document ID associated with a search query component.
 */
public class Posting {
	private List<Integer> mPositions = new ArrayList<>();
	private int mDocumentId;
	private double mTermFrequency = 0.0;
	
	public Posting(int documentId, int position) {
		mDocumentId = documentId;
		addPosition(position);
	}

	public Posting(int documentId) {
		mDocumentId = documentId;
	}
	
	public int getDocumentId() {
		return mDocumentId;
	}

	public List<Integer> getPositions() {
		return mPositions;
	}

	public void addPosition(int position) {
		mPositions.add(position);
		// mTermFrequency = 1.0 + Math.log((double) mPositions.size());
	}

	public double getTermFrequency() {
		return mTermFrequency;
	}

	public void setTermFrequency(double frequency) {
		mTermFrequency = frequency;
	}

	@Override
	public String toString() {
		StringBuilder postingSB = new StringBuilder();
		postingSB.append("DocumentId: " + mDocumentId + ", Positions: [ ");
		for (Integer pos : mPositions) {
			postingSB.append(pos + " ");
		}
		postingSB.append("]");
		return postingSB.toString();
	}
}
