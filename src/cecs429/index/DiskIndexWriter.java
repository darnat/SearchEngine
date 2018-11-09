package cecs429.index;

import libs.btree4j.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class DiskIndexWriter {
	public void writeIndex(Index idx, Path absolutePath) {
		List<String> vocab = idx.getVocabulary();
		try {
			List<Integer> postingsPos = createPostings(idx, vocab, absolutePath.resolve("postings.bin").toFile());
			createBPlusTreeIndex(absolutePath.resolve("bplustree.bin").toFile(), vocab, postingsPos);
                        //CalcDocWeights.procedure(corpus, absolutePath.resolve("docWeights.bin").toFile(), idx);
                        
			// Files below are not needed since implementing indx as B+ tree
			// List<Integer> vocabPos = createIndexVocab(vocab, absolutePath.resolve("vocab.bin").toFile());
			// createVocabTable(vocabPos, postingsPos, absolutePath.resolve("vocabTable.bin").toFile());
		} catch(BTreeException|IOException ex) {
			System.out.println("Error creating disk-based index: " + ex.getMessage());
		}
	}

	private List<Integer> createPostings(Index idx, List<String> vocab, File file) throws IOException {	
            List<Integer> pos = new ArrayList<>();
            int docIdGap;
            int posGap;

            try (DataOutputStream out = new DataOutputStream(new FileOutputStream(file))) {
                for (String term : vocab) {

                    docIdGap = 0;
                    List<Posting> postings = idx.getPostings(term);

                    pos.add(out.size()); // Save byte position
                    out.writeInt(postings.size()); // df(t) - # of docs containing term

                    //for all documents that the word occurs in
                    for (Posting p : postings) {
                        posGap = 0;
                        out.writeInt(p.getDocumentId() - docIdGap); // docId containing term utilizing gaps
                        docIdGap = p.getDocumentId();

                        List<Integer> positions = p.getPositions();
                        out.writeInt(positions.size()); // tf(td) - # of times term occurs in doc
                        
                        for (Integer i : positions) {
                            out.writeInt(i - posGap); // p(t) - ith position of term in doc utilizing gaps
                            posGap = i;
                        }
                    }
                }

                return pos;
            }
	}

	private void createBPlusTreeIndex(File file, List<String> vocab, List<Integer> postings) throws BTreeException {
		BIndexFile bPlusTree = new BIndexFile(file);
		bPlusTree.init(false);

		Iterator<String> iterVocab = vocab.iterator();
		Iterator<Integer> iterPos = postings.iterator();

		while (iterVocab.hasNext() && iterPos.hasNext()) {
			bPlusTree.addValue(new Value(iterVocab.next()), new Value(iterPos.next()));
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