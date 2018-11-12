package cecs429.index;

import cecs429.index.Posting;

import libs.btree4j.*;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

/**
 * Implements a B+ tree that maps terms to disk locations. 
 */
public class DiskPositionalIndex implements Index {
	private RandomAccessFile mPostings;
	private RandomAccessFile mDocWeights;
	private BIndexFile mBt;

	public DiskPositionalIndex(Path absolutePath) throws Exception {
		mBt = new BIndexFile(absolutePath.resolve("bplustree.bin").toFile());
		mBt.init(true);

		// Open files in read mode
		mPostings = new RandomAccessFile(absolutePath.resolve("postings.bin").toFile(), "r");
		mDocWeights = new RandomAccessFile(absolutePath.resolve("docWeights.bin").toFile(), "r");
	}
	
	@Override
	public List<Posting> getPostings(String term) {
		List<Posting> postings = new ArrayList<>();

		try {
			// Get posting location from B+ tree
			Value v = mBt.getValue(new Value(term));
			DataInputStream in = new DataInputStream(v.getInputStream());
			long location = in.readLong();

			// Move file-pointer to location
			mPostings.seek(location);

			int dft = mPostings.readInt(); // read df(t)
			int docGap = 0;
			int posGap;
			for (int i = 0; i < dft; i++) {
				int docId = mPostings.readInt() + docGap; // read docId
				docGap = docId;

				Posting posting = new Posting(docId);
				List<Integer> positions = new ArrayList<>();
				int pos;
				posGap = 0;
				int tftd = mPostings.readInt(); // read tf(td)
				for (int j = 0; j < tftd; j++) {
					pos = mPostings.readInt(); // read p(t)
					posting.addPosition(pos + posGap);
					posGap += pos;
				}
				postings.add(posting);
			}
		} catch (Exception ex) {
			for (StackTraceElement e : ex.getStackTrace()) {
				System.out.println(e);
			}
		}

		return postings;
	}
	
	@Override
	public List<String> getVocabulary() {
		// TODO
		return null;
	}

	public double getDocWeight(int docId) throws IOException {
		mDocWeights.seek(docId * 8);
		return mDocWeights.readDouble();
	}

	public void closeFiles() throws Exception {
		mPostings.close();
		mBt.close();
	}
}
