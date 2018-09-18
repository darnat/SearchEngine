package edu.csulb;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.index.PositionalInvertedIndex;
import cecs429.text.DefaultTokenProcessor;
import cecs429.text.EnglishTokenStream;

import java.nio.file.Paths;
import java.util.*;

public class PositionalInvertedIndexer {
	public static void main(String[] args) {
		DocumentCorpus corpus = DirectoryCorpus.loadJsonDirectory(Paths.get("").toAbsolutePath(), ".json");
		System.out.println("Indexing in progress...");
		long start = System.currentTimeMillis();
		Index index = indexCorpus(corpus);
		long end = System.currentTimeMillis();
		System.out.println("Indexing completed in " + ((end - start) / 1000) + " seconds.");

		Scanner sc = new Scanner(System.in);
		String query = "";

		do {
			System.out.println("\nPlease enter a term to search for, or 'q' to quit: ");
			query = sc.nextLine().replaceAll("\\W", "").toLowerCase();

			if (!query.isEmpty()) {
				List<Posting> postings = index.getPostings(query);

				if (!postings.isEmpty()) {
					for (Posting p : postings) {
						System.out.println("Document " + corpus.getDocument(p.getDocumentId()).getTitle());
					}
				} else if (!query.equals("q")) {
					System.out.println("Term was not found.");
				}
			}
		} while (!query.equals("q"));

		sc.close();
		System.exit(0);
	}
	
	private static Index indexCorpus(DocumentCorpus corpus) {
		DefaultTokenProcessor processor = new DefaultTokenProcessor();
		processor.initStemmer();
		Iterable<Document> documents = corpus.getDocuments();
		PositionalInvertedIndex index = new PositionalInvertedIndex();

		for (Document doc : documents) {
			int position = 0;
			Iterable<String> tokens = new EnglishTokenStream(doc.getContent()).getTokens();
			for (String token : tokens) {
				List<String> terms = processor.processToken(token);
				for (String term : terms) {
					index.addTerm(term, doc.getId(), position);
				}
				position++;
			}
		}

		return index;
	}
}
