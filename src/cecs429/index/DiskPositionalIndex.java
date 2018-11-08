package cecs429.index;

import cecs429.index.Posting;

import libs.btree4j.BIndexFile;

import java.nio.file.Path;
import java.util.*;

/**
 * Implements a B+ tree that maps terms to disk locations. 
 */
public class DiskPositionalIndex implements Index {
	private BIndexFile mBt;

	public DiskPositionalIndex(Path absolutePath) {
		// TODO
		// Get link to B+ tree
		// Get link to postings.bin
	}
	
	@Override
	public List<Posting> getPostings(String term) {
		// TODO
		return null;
	}
	
	@Override
	public List<String> getVocabulary() {
		// TODO
		return null;
	}
}
