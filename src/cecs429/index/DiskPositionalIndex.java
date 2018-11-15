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

	private static final int BLOCK_SIZE = 512;

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
			if (v.getLength() > 0) {
				DataInputStream in = new DataInputStream(v.getInputStream());
				long location = in.readLong();

				// Move file-pointer to location
				mPostings.seek(location);
				System.out.println("Location : " + location);

				int dft = nextInt(); // read df(t)
				System.out.println("Document Frequency : " + dft);

				int docGap = 0;
				int posGap;
				for (int i = 0; i < dft; i++) {
					int docId = nextInt() + docGap; // read docId
					docGap = docId;

					Posting posting = new Posting(docId);
					List<Integer> positions = new ArrayList<>();
					int pos;
					posGap = 0;
					int tftd = nextInt(); // read tf(td)
					for (int j = 0; j < tftd; j++) {
						pos = nextInt(); // read p(t)
						posting.addPosition(pos + posGap);
						posGap += pos;
					}
					postings.add(posting);
				}
			}
		} catch (Exception ex) {
			// B+ tree crashes when term not found with NullPointerException :/
			// for (StackTraceElement e : ex.getStackTrace()) {
			// 	System.out.println(e);
			// }
			System.out.println("Term was not found in corpus...");
		}

		return postings;
	}

	private int nextInt() throws Exception {
		// return mPostings.readInt();
		int n = 0;
		byte b;
		// System.out.println("Number : ");
		for (;;) {
			b = mPostings.readByte();
			// System.out.print("Byte : " + String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0') + " ");
			if ((b & 0xFF) < 128) {
				n = 128 * n + (b & 0xFF);
			} else {
				n = 128 * n + ((b & 0xFF) - 128);
				return n;
			}
		}
	}
	
	@Override
	public List<String> getVocabulary() {
		// TODO
		return null;
	}

	public double getDocWeight(int docId) throws IOException {
		double docWeight = 0.0;
		try {
			mDocWeights.seek(docId * 8);
			docWeight=  mDocWeights.readDouble();
		} catch (IOException ex) {
			System.out.println("Error reading from DocumentWeight file with docId: " + docId);
		}

		return docWeight;
	}

	public void closeFiles() throws Exception {
		mPostings.close();
		mDocWeights.close();
		mBt.close();
	}
}
