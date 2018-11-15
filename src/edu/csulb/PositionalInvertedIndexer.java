package edu.csulb;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.documents.Snippet;
import cecs429.index.DiskIndexWriter;
import cecs429.index.DiskPositionalIndex;
import cecs429.index.Index;
import cecs429.index.PositionalInvertedIndex;
import cecs429.index.DiskIndexWriter;
import cecs429.index.Posting;
import cecs429.query.BooleanQueryParser;
import cecs429.query.QueryComponent;
import cecs429.query.RankedRetrieval;
import cecs429.text.DefaultTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.Stemmer;
import cecs429.text.TokenProcessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class PositionalInvertedIndexer {
	public static void main(String[] args) throws IOException, Exception {
		Scanner sc = new Scanner(System.in);	
		String res = null;
		CorpusInfo corpusInfo = null;

		while(true) {
			System.out.println(":q - To quit application");
			System.out.println("1. Build index.");
			System.out.println("2. Query index.");
			System.out.print("\n\nPlease make a selection: ");
			res = sc.nextLine();

			if (res.equals(":q")) {
				break;
			} else if (res.startsWith("1")) {
				corpusInfo = buildIndex(sc);
			} else if (res.startsWith("2")) {
				if (corpusInfo != null) {
					queryIndex(sc, corpusInfo);
				} else {
					System.out.println("Please build an index first before trying to query it.");
				}
			}
		}
		
		sc.close();
		System.exit(0);
	}

	private static CorpusInfo buildIndex(Scanner sc) {
		System.out.print("Please enter the name of a directory you would like to index: ");
		Path corpusPath = Paths.get(sc.nextLine()).toAbsolutePath().normalize();
		DocumentCorpus corpus = DirectoryCorpus.loadJsonDirectory(corpusPath, ".json");
		TokenProcessor processor = new DefaultTokenProcessor();

		System.out.println("\nIndexing in progress...");
		long start = System.currentTimeMillis();
		indexCorpus(corpus, processor, corpusPath.resolve("index"));
		long end = System.currentTimeMillis();
		System.out.println("Indexing completed in " + ((end - start) / 1000) + " seconds.");

		return new CorpusInfo(corpusPath, corpus);
	}

	private static void queryIndex(Scanner sc, CorpusInfo corpusInfo) {
		DocumentCorpus corpus = corpusInfo.getCorpus();

		try {
			DiskPositionalIndex dpi = new DiskPositionalIndex(corpusInfo.getCorpusPath().resolve("index"));
			String query = null;

			System.out.println("\n\n-----Ranked Retrieval Mode-----");
			System.out.println(":q - To quit application");
					
			Map<Integer, Double> accumulator;
			List<Map.Entry<Integer, Double>> results;

			while(true) {
				System.out.print("\nPlease enter your search query: ");
				query = sc.nextLine();

				if (query.equals(":q")) {
					break;
				}

				accumulator = RankedRetrieval.accumulate(dpi, corpus.getCorpusSize(), query);
				results = RankedRetrieval.getResults(accumulator);
				printResult(corpus, results);
																
				System.out.println("Number of documents: " + results.size());

				if (results.size() > 0) {
					System.out.print("\nDo you wish to select a document to view (y, n)? ");
					String docRequested = sc.nextLine();

					if (docRequested.toLowerCase().equals("y")) {
						System.out.print("Please enter a list number from the list above: ");
						int listNum = sc.nextInt();
						int docId = results.get(--listNum).getKey();
						BufferedReader in = new BufferedReader(corpus.getDocument(docId).getContent());
						String line = null;

						try {
							while ((line = in.readLine()) != null) {
								System.out.println(line);
							}
						} catch(IOException ex) {
							System.out.println("Error reading document.");
						}
						// Flush the buffer
						sc.nextLine();
					}
				} else {
					System.out.println("Term was not found.");
				}
				
				accumulator = null;
				results = null;
			}
			
			dpi.closeFiles();
		} catch (Exception ex) {
			System.out.println("Error querying disk index: " + ex.getMessage());
			for (StackTraceElement e : ex.getStackTrace()) {
				System.out.println(e);
			}
		}
	}
	
	private static void indexCorpus(DocumentCorpus corpus, TokenProcessor processor, Path absolutePath) {
		Iterable<Document> documents = corpus.getDocuments();
		PositionalInvertedIndex index = new PositionalInvertedIndex();
		DiskIndexWriter diw = new DiskIndexWriter();
		Map<Integer, Map<String, Integer>> docWeightList = new TreeMap<>();
		Map<String, Integer> tmpWeight;

		for (Document doc : documents) {
			int position = 0;
			tmpWeight = new HashMap<String, Integer>();
			Iterable<String> tokens = new EnglishTokenStream(doc.getContent()).getTokens();
			for (String token : tokens) {
				List<String> terms = processor.processToken(token);
				for (String term : terms) {
					index.addTerm(term, doc.getId(), position);
					
					// For document weight calculations
					// tmpWeight = docWeightList.getOrDefault(doc.getId(), new HashMap<String, Integer>());
					tmpWeight.put(term, tmpWeight.getOrDefault(term, 0) + 1);
					// docWeightList.put(doc.getId(), tmpWeight);

					// if (docWeightList.containsKey(doc.getId())) {
					// 	Map<String, Integer> docWeights = docWeightList.get(doc.getId());

					// 	if (docWeights.containsKey(term)) {
					// 		docWeights.put(term, docWeights.get(term) + 1); // increment term frequency
					// 	} else {
					// 		docWeights.put(term, 1);
					// 	}
					// } else {
					// 	Map<String, Integer> weight = new HashMap<String, Integer>();
					// 	weight.put(term, 1);
					// 	docWeightList.put(doc.getId(), weight);
					// }
				}
				position++;
			}
			docWeightList.put(doc.getId(), tmpWeight);
		}

		try {
			// Save disk-based index
			diw.writeIndex(absolutePath, index);

			// Save document weights
			diw.createDocumentWeights(absolutePath, docWeightList);

		} catch (Exception ex) {
			System.out.println("Error creating disk-based index: " + ex.getMessage());
		}
	}
        
	private static void printResult(DocumentCorpus corpus, List<Map.Entry<Integer, Double>> results) {
		for (int i = 0; i < results.size(); ++i) {
			System.out.print((i + 1) + ". " + corpus.getDocument(results.get(i).getKey()).getTitle());
			System.out.println("\" (ID " + results.get(i).getKey() + "): " + results.get(i).getValue());
		}
	}

	private static void milestone1() {
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
		// 	// } else if (query.startsWith(":vocab")) {
		// 	// 	List<String> vocabulary = index.getVocabulary();
		// 	// 	System.out.println("Printing first 1000 terms in vocabulary of corpus.");
		// 	// 	for (int i = 0; i < 1000 && i < vocabulary.size(); i++) {
		// 	// 		System.out.println(i + ": " + vocabulary.get(i));
		// 	// 	}
		// 	// 	System.out.println("Total number of vocabulary terms: " + vocabulary.size());
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
	}

	private static class CorpusInfo {
		private Path mCorpusPath;
		private DocumentCorpus mCorpus;

		public CorpusInfo(Path corpusPath, DocumentCorpus corpus) {
			mCorpusPath = corpusPath;
			mCorpus = corpus;
		}

		public Path getCorpusPath() {
			return mCorpusPath;
		}

		public DocumentCorpus getCorpus() {
			return mCorpus;
		}
	}
}
