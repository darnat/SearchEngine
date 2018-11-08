package edu.csulb;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.documents.Snippet;
import cecs429.index.DiskIndexWriter;
import cecs429.index.Index;
import cecs429.index.PositionalInvertedIndex;
import cecs429.index.DiskIndexWriter;
import cecs429.index.Posting;
import cecs429.query.BooleanQueryParser;
import cecs429.query.QueryComponent;
import cecs429.text.DefaultTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.Stemmer;
import cecs429.text.TokenProcessor;

import java.io.File;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class PositionalInvertedIndexer {
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);	
		String query = null;
		Path corpusPath = null;

		System.out.print("Please enter the name of a directory you would like to index: ");
		corpusPath = Paths.get(sc.nextLine()).toAbsolutePath().normalize();
		DocumentCorpus corpus = DirectoryCorpus.loadJsonDirectory(corpusPath, ".json");
		TokenProcessor processor = new DefaultTokenProcessor();
		BooleanQueryParser queryParser = new BooleanQueryParser();
		DiskIndexWriter diskIndexWriter = new DiskIndexWriter();

		System.out.println("\nIndexing in progress...");
		long start = System.currentTimeMillis();
		Index index = indexCorpus(corpus, processor);
		diskIndexWriter.writeIndex(index, Paths.get(sc.nextLine(), "disk"));
		long end = System.currentTimeMillis();
		System.out.println("Indexing completed in " + ((end - start) / 1000) + " seconds.");

		// Save disk-based index
		DiskIndexWriter diw = new DiskIndexWriter();
		diw.writeIndex(index, corpusPath.resolve("index"));

		System.exit(0);

		// while(true) {
		// 	System.out.println("\nSpecial queries available:");
		// 	System.out.println(":q - To quit application");
		// 	System.out.println(":stem token - To stem a token");
		// 	System.out.println(":index directoryname - To index a directory");
		// 	System.out.println(":vocab - To print the first 1000 terms in the corpus vocabulary");
		// 	System.out.print("\n\nPlease enter your search query: ");
		// 	query = sc.nextLine();

		// 	if (query.equals(":q")) {
		// 		break;
		// 	} else if (query.startsWith(":stem")) {
		// 		System.out.println("Stemmed token: " + Stemmer.getInstance().stemToken(query.split(" ")[1]));
		// 	} else if (query.startsWith(":index")) {
		// 		corpus = DirectoryCorpus.loadJsonDirectory(Paths.get(query.split(" ")[1]).toAbsolutePath(), ".json");
		// 		System.out.println("Indexing in progress...");
		// 		start = System.currentTimeMillis();
		// 		index = indexCorpus(corpus, processor);
		// 		end = System.currentTimeMillis();
		// 		System.out.println("Indexing completed in " + ((end - start) / 1000) + " seconds.");
		// 	} else if (query.startsWith(":vocab")) {
		// 		List<String> vocabulary = index.getVocabulary();
		// 		System.out.println("Printing first 1000 terms in vocabulary of corpus.");
		// 		for (int i = 0; i < 1000 && i < vocabulary.size(); i++) {
		// 			System.out.println(i + ": " + vocabulary.get(i));
		// 		}
		// 		System.out.println("Total number of vocabulary terms: " + vocabulary.size());
		// 	} else {
		// 		QueryComponent qc = queryParser.parseQuery(query);

		// 		if (qc != null) {
		// 			List<Posting> postings = qc.getPostings(index, processor);

		// 			if (!postings.isEmpty()) {
		// 				for (int i = 0; i < postings.size(); i++) {
		// 					System.out.println(i + ": " + corpus.getDocument(postings.get(i).getDocumentId()).getTitle());
		// 				}
		// 				System.out.println("Number of documents: " + postings.size());
		// 				System.out.print("\n\nDo you wish to select a document to view? (y, n) ");
		// 				String docRequested = sc.nextLine();
		// 				if (docRequested.toLowerCase().equals("y")) {
		// 					System.out.print("Please enter a list number from the list above: ");
		// 					int listNum = sc.nextInt();
		// 					int docId = postings.get(listNum).getDocumentId();
		// 					BufferedReader in = new BufferedReader(corpus.getDocument(docId).getContent());
		// 					String line = null;
		// 					// Print entire document content
		// 					// TODO: Remove after Snippet is fully working
		// 					try {
		// 						while ((line = in.readLine()) != null) {
		// 							System.out.println(line);
		// 						}
		// 					} catch(IOException ex) {
		// 						System.out.println("Error reading document.");
		// 					}
		// 					// Flush the buffer
		// 					sc.nextLine();

		// 					// Print snippet
		// 					Snippet snip = new Snippet(
		// 						corpus.getDocument(docId).getContent(),
		// 						postings.get(listNum).getPositions()
		// 					);
		// 					System.out.println("\n\nSnippet: " + snip.getContent());
		// 				}
		// 			} else {
		// 				System.out.println("Term was not found.");
		// 			}
		// 		}
		// 	}
		// }

		// sc.close();
		// System.exit(0);
	}
	
	private static Index indexCorpus(DocumentCorpus corpus, TokenProcessor processor) {
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
