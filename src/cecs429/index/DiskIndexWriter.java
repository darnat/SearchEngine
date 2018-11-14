package cecs429.index;

import cecs429.index.Index;
import cecs429.text.ByteEncode;
import libs.btree4j.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class DiskIndexWriter {
	public void writeIndex(Path absolutePath, Index idx) throws IOException, BTreeException {
		List<String> vocab = idx.getVocabulary();
		List<Integer> postingsPos = createPostings(absolutePath.resolve("postings.bin").toFile(), idx, vocab);
		createBPlusTreeIndex(absolutePath.resolve("bplustree.bin").toFile(), vocab, postingsPos);

		// Files below are not needed since implementing index as B+ tree
		// List<Integer> vocabPos = createIndexVocab(vocab, absolutePath.resolve("vocab.bin").toFile());
		// createVocabTable(vocabPos, postingsPos, absolutePath.resolve("vocabTable.bin").toFile());
	}

	private List<Integer> createPostings(File file, Index idx, List<String> vocab) throws IOException {
		List<Integer> pos = new ArrayList<>();
		int docIdGap;
		int posGap;

		try (DataOutputStream out = new DataOutputStream(new FileOutputStream(file))) {
			for (String term : vocab) {
				docIdGap = 0;
				List<Posting> postings = idx.getPostings(term);

				pos.add(out.size()); // Save byte position
				writeOn(out, postings.size());
				// out.writeInt(postings.size()); // df(t) - # of docs containing term
				for (Posting p : postings) {
					posGap = 0;
					writeOn(out, p.getDocumentId() - docIdGap);
					// out.writeInt(p.getDocumentId() - docIdGap); // docId containing term utilizing gaps
					docIdGap = p.getDocumentId();

					List<Integer> positions = p.getPositions();
					writeOn(out, positions.size());
					// out.writeInt(positions.size()); // tf(td) - # of times term occurs in doc

					for (Integer i : positions) {
						writeOn(out, i - posGap);
						// out.writeInt(i - posGap); // p(t) - ith position of term in doc utilizing gaps
						posGap = i;
					}
				}
			}

			return pos;
		}
	}

	private void writeOn(DataOutputStream out, int n) throws IOException {
		byte[] tmp;

		tmp = ByteEncode.numberToByteArray(n);
		out.write(tmp, 0, tmp.length);
	}

	private void createBPlusTreeIndex(File file, List<String> vocab, List<Integer> postings) throws BTreeException, IOException {
		BIndexFile bPlusTree = new BIndexFile(file);
		bPlusTree.init(false);

		Iterator<String> iterVocab = vocab.iterator();
		Iterator<Integer> iterPos = postings.iterator();

		while (iterVocab.hasNext() && iterPos.hasNext()) {
			bPlusTree.addValue(new Value(iterVocab.next()), new Value(iterPos.next()));
		}

		bPlusTree.flush(true, true);
		bPlusTree.close();
	}

	public void createDocumentWeights(Path absolutePath, Map<Integer, Map<String, Integer>> docWeightList)  throws IOException {
		try (DataOutputStream out = new DataOutputStream(new FileOutputStream(absolutePath.resolve("docWeights.bin").toFile()))) {
			for (Map<String, Integer> docWeights : docWeightList.values()) {
				double ld = docWeights.values()
					.stream()
					.map(a -> Math.pow(1.0 + Math.log(a), 2))
					.reduce(0.0, Double::sum);

				out.writeDouble(Math.sqrt(ld));
			}
		}
	}

	private List<Integer> createIndexVocab(List<String> vocab, File file) throws IOException {
		List<Integer> pos = new ArrayList<>();
		int length = 0; // 0-based
		try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
			for (String term : vocab) {
				pos.add(length); // Save string starting position
				length += term.length();
				out.write(term);
			}

			return pos;
		}
	}

	private void createVocabTable(List<Integer> vocab, List<Integer> postings, File file) throws IOException {
		try (DataOutputStream out = new DataOutputStream(new FileOutputStream(file))) {
			Iterator<Integer> iterVocab = vocab.iterator();
			Iterator<Integer> iterPostings = postings.iterator();
			while (iterVocab.hasNext() && iterPostings.hasNext()) {
				out.writeLong(iterVocab.next());
				out.writeLong(iterPostings.next());
			}
		}
	}
}
