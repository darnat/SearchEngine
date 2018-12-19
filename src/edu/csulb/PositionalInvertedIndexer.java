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
import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;

public class PositionalInvertedIndexer {
	public static void main(String[] args) throws IOException, Exception {
            
		Scanner sc = new Scanner(System.in);	
		String res = null;
		CorpusInfo corpusInfo = null;

		while(true) {
			System.out.println(":q - To quit application");
			System.out.println("1. Build index.");
			System.out.println("2. Query index.");
			System.out.println("3. Who Wrote the Federalist Papers?");
			System.out.print("\n\nPlease make a selection: ");
			res = sc.nextLine();

			if (res.equals(":q")) {
				break;
			} else if (res.startsWith("1")) {
				corpusInfo = buildIndex(sc);
			} else if (res.startsWith("2")) {
				if (corpusInfo != null) {
					System.out.println("\n1. Boolean retrieval.");
					System.out.println("2. Ranked retrieval.");
					System.out.print("\n\nPlease make a selection: ");
					res = sc.nextLine();

					if (res.startsWith("1")) {
						milestone1(sc, corpusInfo);
					} else if (res.startsWith("2")) {
						queryIndex(sc, corpusInfo);
					}
				} else {
					System.out.println("Please build an index first before trying to query it.");
				}
			} else if (res.startsWith(("3"))) {
				milestone3(sc);
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

	private static void milestone1(Scanner sc, CorpusInfo corpusInfo) {
		String query = null;

		System.out.println("\n\n-----Boolean Retrieval Mode-----");
		System.out.println(":q - To quit application");

		while(true) {
			System.out.print("\n\nPlease enter your search query: ");
			query = sc.nextLine();

			if (query.equals(":q")) {
				break;
			}
			// } else if (query.startsWith(":stem")) {
			// 	System.out.println("Stemmed token: " + Stemmer.getInstance().stemToken(query.split(" ")[1]));
			// } else if (query.startsWith(":index")) {
			// 	corpus = DirectoryCorpus.loadJsonDirectory(Paths.get(query.split(" ")[1]).toAbsolutePath(), ".json");
			// 	System.out.println("Indexing in progress...");
			// 	start = System.currentTimeMillis();
			// 	index = indexCorpus(corpus, processor);
			// 	end = System.currentTimeMillis();
			// 	System.out.println("Indexing completed in " + ((end - start) / 1000) + " seconds.");
			// } else if (query.startsWith(":vocab")) {
			// 	List<String> vocabulary = index.getVocabulary();
			// 	System.out.println("Printing first 1000 terms in vocabulary of corpus.");
			// 	for (int i = 0; i < 1000 && i < vocabulary.size(); i++) {
			// 		System.out.println(i + ": " + vocabulary.get(i));
			// 	}
			// 	System.out.println("Total number of vocabulary terms: " + vocabulary.size());

			try {
				DiskPositionalIndex dpi = new DiskPositionalIndex(corpusInfo.getCorpusPath().resolve("index"));			
				DocumentCorpus corpus = corpusInfo.getCorpus();
				BooleanQueryParser queryParser = new BooleanQueryParser();
				TokenProcessor processor = new DefaultTokenProcessor();
				QueryComponent qc = queryParser.parseQuery(query);

				if (qc != null) {
					List<Posting> postings = qc.getPostings(dpi, processor);

					if (!postings.isEmpty()) {
						for (int i = 0; i < postings.size(); i++) {
							System.out.println(i + ": " + corpus.getDocument(postings.get(i).getDocumentId()).getTitle());
						}
						System.out.println("Number of documents: " + postings.size());
						System.out.print("\n\nDo you wish to select a document to view? (y, n) ");
						String docRequested = sc.nextLine();
						if (docRequested.toLowerCase().equals("y")) {
							System.out.print("Please enter a list number from the list above: ");
							int listNum = sc.nextInt();
							int docId = postings.get(listNum).getDocumentId();
							BufferedReader in = new BufferedReader(corpus.getDocument(docId).getContent());
							String line = null;
							// Print entire document content
							// TODO: Remove after Snippet is fully working
							try {
								while ((line = in.readLine()) != null) {
									System.out.println(line);
								}
							} catch(IOException ex) {
								System.out.println("Error reading document.");
							}
							// Flush the buffer
							sc.nextLine();

							// Print snippet
							Snippet snip = new Snippet(
								corpus.getDocument(docId).getContent(),
								postings.get(listNum).getPositions()
							);
							System.out.println("\n\nSnippet: " + snip.getContent());
						}
					} else {
						System.out.println("Term was not found.");
					}
				}
			} catch (Exception ex) {
				System.out.println("Error getting DiskPositionalIndex when querying using Boolean retrieval.");
			}
		}
	}

	private static void milestone3(Scanner sc) {
		Map<String, FederalistClass> classes = new HashMap<>();
		System.out.print("Please enter the name of the directory containing the Federalist folders: ");
		Path corpusPath = Paths.get(sc.nextLine()).toAbsolutePath().normalize();

		TokenProcessor processor = new DefaultTokenProcessor();
		String[] AUTHORS = {"HAMILTON", "JAY", "MADISON"};
		
		// Load training set
		for (String author : AUTHORS) {
			DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(corpusPath.resolve(author), ".txt");
			Iterable<Document> documents = corpus.getDocuments();
			
			PositionalInvertedIndex index = new PositionalInvertedIndex();
			Map<Integer, Map<String, Double>> wDTs = new HashMap<>();

			for (Document doc : documents) {
				int position = 0;
				Map<String, Integer> termMap = new HashMap<>();
				Iterable<String> tokens = new EnglishTokenStream(doc.getContent()).getTokens();
				for (String token : tokens) {
					List<String> terms = processor.processToken(token);

					for (String term : terms) {
						index.addTerm(term, doc.getId(), position);

						// Compute tf(t,d) for each term
						termMap.put(term, termMap.getOrDefault(term, 0) + 1);
					}
					position++;
				}
				
				// Calculate w(d,t) for each term in doc
				Map<String, Double> wDT = new HashMap<>();
				for (Map.Entry<String, Integer> term : termMap.entrySet()) {
					wDT.put(term.getKey(), 1 + Math.log(term.getValue()));
				}

				// Calculate L(d) for each document
				double ld = wDT.values()
					.stream()
					.map(a -> Math.pow(a, 2)) // Square each w(d,t)
					.reduce(0.0, Double::sum); // Sum all w(d,t) squares

				// Normalize all w(d,t) with L(d)
				wDT.replaceAll((k, v) -> v / Math.sqrt(ld));

				// Save computed w(d,t)
				wDTs.put(doc.getId(), wDT);
			}			

			// Save into FederalistClass
			classes.put(author, new FederalistClass(corpus.getCorpusSize(), index, wDTs));
		}
		
		rocchioClassification(processor, classes, corpusPath);
		bayesianClassification(processor, AUTHORS, classes, corpusPath);
	}

	private static void rocchioClassification(TokenProcessor processor, Map<String, FederalistClass> classes, Path corpusPath) {
		Map<String, Map<String, Double>> centroids = new HashMap<>();

		// Find centroid for each class 
		for (Map.Entry<String, FederalistClass> fc : classes.entrySet()) {
			FederalistClass f = fc.getValue();
			List<String> vocab = f.getIndex().getVocabulary();
			Map<Integer, Map<String, Double>> wDTs = f.getWDTs();
			Map<String, Double> centroidVector = new HashMap<>();

			// Loop thru all terms in class
			for (String term : vocab) {
				List<Double> accum = new ArrayList<>();
				for (Map<String, Double> wDT : wDTs.values()) {
					accum.add(wDT.getOrDefault(term, 0.0));
				}
				centroidVector.put(term, accum.stream().reduce(0.0, Double::sum) / f.getCorpusSize());
			}

			centroids.put(fc.getKey(), centroidVector);
		}		

		// Classify disputed documents
		DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(corpusPath.resolve("DISPUTED"), ".txt");
		Iterable<Document> documents = corpus.getDocuments();

		for (Document doc: documents) {
			Map<String, Integer> termMap = new HashMap<>();
			Iterable<String> tokens = new EnglishTokenStream(doc.getContent()).getTokens();
			for (String token : tokens) {
				List<String> terms = processor.processToken(token);
				for (String term : terms) {
					// Compute tf(t,d) for each term
					termMap.put(term, termMap.getOrDefault(term, 0) + 1);
				}
			}

			// Calculate w(d,t) for each term
			Map<String, Double> wDT = new HashMap<>();
			for (Map.Entry<String, Integer> term : termMap.entrySet()) {
				wDT.put(term.getKey(), 1 + Math.log(term.getValue()));
			}

			// Calculate L(d)
			double ld = wDT.values()
				.stream()
				.map(a -> Math.pow(a, 2)) // Square each w(d,t)
				.reduce(0.0, Double::sum); // Sum all w(d,t) squares

			// Normalize all w(d,t) with L(d)
			wDT.replaceAll((k, v) -> v / Math.sqrt(ld));

			Map<String, Double> distances = new HashMap<>();

			// Compare wDT of document to each centroid
			for (Map.Entry<String, Map<String, Double>> centroid : centroids.entrySet()) {
				Map<String, Double> centroidVector = centroid.getValue();
				//List<Double> result = new ArrayList<>();
				List<Double> accum = new ArrayList<>();
				for (Map.Entry<String, Double> entry : centroidVector.entrySet()) {
					// Accumulate each term and square
					accum.add(Math.pow(entry.getValue() - wDT.getOrDefault(entry.getKey(), 0.0), 2));
				}

				//result.add(Math.sqrt(accum.stream().reduce(0.0, Double::sum)));

				distances.put(centroid.getKey(), Math.sqrt(accum.stream().reduce(0.0, Double::sum)));
			}

			String className = null;
			double min = 0.0d;
			for (Map.Entry<String, Double> distance : distances.entrySet()) {
				double val = distance.getValue();

				if (min > val || min == 0.0d) {
					min = val;
					className = distance.getKey();
				}
			}

			// Print out closest centroid
			System.out.println(doc.getTitle() + " will be classified in the following class: " + className + "\n");

			// Print out information for Paper 52
			if (doc.getTitle().equals("paper_52.txt")) {
				List<String> vocabulary = new ArrayList<>();

				for (String term : wDT.keySet()) {
					vocabulary.add(term);
				}

				Collections.sort(vocabulary);

				System.out.println("First 30 components in Paper 52:");
				for (int i = 0; i < 30; i++) {
					System.out.println(vocabulary.get(i) + " : " + wDT.get(vocabulary.get(i)));
				}

				for (Map.Entry<String, Double> distance : distances.entrySet()) {
					System.out.println(distance.getKey() + ": " + distance.getValue());
				}
			}
		}
	} 

	private static void bayesianClassification(TokenProcessor processor, String[] AUTHORS, Map<String, FederalistClass> classes, Path corpusPath) {
		////////////////////////////////////////////////////////////////////////////
		//				Bayesian Classification									  //
		////////////////////////////////////////////////////////////////////////////
		System.out.println("Bayesian Classification");
		
		// Data structure for storing results of Mutual Information
		// Author key maps to Arraylist of HashMap but IMPORTANT: hashmaps only have one key value pair
		HashMap<String, ArrayList<HashMap<String, Double>>> mutualInformation = new HashMap<String, ArrayList<HashMap<String, Double>>>();
		
		for (String author : AUTHORS) {
			// Key will be from the AUTHORS set and value will be the p(t,c)
			mutualInformation.put(author, new ArrayList<HashMap<String, Double>>());
			
			Index classIndex = classes.get(author).getIndex();
			
			// Iterate through each term in Index
			for(String term : classIndex.getVocabulary()) {
				int[][] mutualInfoArray = new int[2][2];
				// Reflects N_11 for mutual information (in class, has term)
				mutualInfoArray[1][1] = classIndex.getPostings(term).size();
				// Reflects N_10 for mutual information (in class, no term)
				mutualInfoArray[1][0] = classes.get(author).getCorpusSize() - mutualInfoArray[1][1];
				
				int numTermInOther = 0;
				int numTermNotOther = 0;
				
				// Check for other classes other than current class
				for (String auth: AUTHORS) {
					if (!auth.equals(author)) {
						numTermInOther += classes.get(auth).getIndex().getPostings(term).size();
						numTermNotOther = numTermNotOther + (classes.get(auth).getCorpusSize() 
								- classes.get(auth).getIndex().getPostings(term).size());
					}
				}
				
				// Reflects N_01 for mutual information (other class, has term)
				mutualInfoArray[0][1] = numTermInOther;
				// Reflects N_00 for mutual information (other classes, no term)
				mutualInfoArray[0][0] = numTermNotOther;
				
				int totalDocuments = mutualInfoArray[1][1] + mutualInfoArray[1][0] 
				+ mutualInfoArray[0][1] + mutualInfoArray[0][0];
				
				// BIG equation for I(c,t)
				double mutualInfoResult = (
				((double)mutualInfoArray[1][1]/totalDocuments) * 
				log2(
						(double)(totalDocuments * mutualInfoArray[1][1])
						/
						((double)(mutualInfoArray[1][0] + mutualInfoArray[1][1]) * (mutualInfoArray[0][1] + mutualInfoArray[1][1]))
					)
				) + 
				(
				((double)mutualInfoArray[1][0]/totalDocuments) * 
				log2(
						(double)(totalDocuments * mutualInfoArray[1][0])
						/
						((double)(mutualInfoArray[1][0] + mutualInfoArray[1][1]) * (mutualInfoArray[0][0] + mutualInfoArray[1][0]))
					)
				) + 
				(
				((double)mutualInfoArray[0][1]/totalDocuments) * 
				log2(
						(double)(totalDocuments * mutualInfoArray[0][1])
						/
						((double)(mutualInfoArray[0][0] + mutualInfoArray[0][1]) * (mutualInfoArray[0][1] + mutualInfoArray[1][1]))
					)
				) + 
				(
				((double)mutualInfoArray[0][0]/totalDocuments) * 
				log2(
						(double)(totalDocuments * mutualInfoArray[0][0])
						/
						((double)(mutualInfoArray[0][0] + mutualInfoArray[0][1]) * (mutualInfoArray[0][0] + mutualInfoArray[1][0]))
					)
				);
				
	//				System.out.println("I(" + author + ", " + term + ") = " + mutualInfoResult);
				HashMap<String, Double> resultMap = new HashMap<String, Double>();
				resultMap.put(term, mutualInfoResult);
				mutualInformation.get(author).add(resultMap);
			}
		}
		
		// Im sorting the HashMap so that I can get top 50 terms
		for (String author : AUTHORS) {
			System.out.println("Author: " + author);
			
			ArrayList<HashMap<String, Double>> mutualInfoArr = mutualInformation.get(author);
			// Sort the arraylist of hashmaps
			mutualInfoArr.sort(new Comparator<Map<String, Double>>() {
				public int compare(Map<String, Double> o1, Map<String, Double> o2) {
					Collection<Double> values1 = o1.values();
					Collection<Double> values2 = o2.values();
					if(!values1.isEmpty() && !values2.isEmpty()){
						return values2.iterator().next().compareTo(values1.iterator().next());
					}else{
						return 0;
					}
				}
			});
			// Remove any NaN in the List
			int temp = 0;
			for(int x = 0; x < mutualInfoArr.size(); x++) {
				if(!mutualInfoArr.get(x).containsValue(Double.NaN)) {
					temp = x;
					break;
				}
			}
	
			mutualInformation.put(author, new ArrayList<HashMap<String, Double>>(mutualInfoArr.subList(temp, temp + 50)));
		}
		
		PriorityQueue<DataStore> mutualInformation50Term = new PriorityQueue<DataStore>();
		
		// get terms and put in the priority queue
		for(String author : AUTHORS) {
			for(HashMap<String, Double> term : mutualInformation.get(author)) {
				Map.Entry<String,Double> entry = term.entrySet().iterator().next();
				mutualInformation50Term.add(new DataStore(entry.getValue(), entry.getKey()));
			}
		}
		
		// display the results
		for(DataStore store : mutualInformation50Term) {
			System.out.println(store.getTerm() + " - " + store.getScore());
		}
		
		// Data structure for storing results of the Probability
		HashMap<String, ArrayList<HashMap<String, Double>>> probabilityInformation = new HashMap<String, ArrayList<HashMap<String, Double>>>();
		
		// calculate the probability of Classes
		for (String author : AUTHORS) {
			probabilityInformation.put(author, new ArrayList<HashMap<String, Double>>());
			
			// go through discriminate term set
			for(DataStore discTerm : mutualInformation50Term) {
				// calculate f_tc
				int ftc = classes.get(author).getIndex().getPostings(discTerm.getTerm()).size() + 1;
				
				// calculate f_t'c
				int fnottc = 0;
				for(DataStore notTerm : mutualInformation50Term) {
					if(!discTerm.getTerm().equals(notTerm.getTerm())) {
						fnottc = fnottc + classes.get(author).getIndex().getPostings(notTerm.getTerm()).size() + 1;
					}
				}
				
				double probability = (double)ftc / fnottc;
				HashMap<String, Double> store = new HashMap<String, Double>();
				store.put(discTerm.getTerm(), probability);
				probabilityInformation.get(author).add(store);
			}
		}
		
		// Calculate total number of training doc
		int totalTrainingSet = 0;
		for (String author : AUTHORS) {
			totalTrainingSet += classes.get(author).getCorpusSize();
		}
		
		// Index Disputed Corpus
		DocumentCorpus disputedCorpus = DirectoryCorpus.loadTextDirectory(corpusPath.resolve("DISPUTED"), ".txt");
		Iterable<Document> documents = disputedCorpus.getDocuments();
		
		PositionalInvertedIndex disputedIndex = new PositionalInvertedIndex();
	
		for (Document doc : documents) {
			int position = 0;
			Map<String, Integer> termMap = new HashMap<>();
			Iterable<String> tokens = new EnglishTokenStream(doc.getContent()).getTokens();
			for (String token : tokens) {
				List<String> terms = processor.processToken(token);
				for (String term : terms) {
					disputedIndex.addTerm(term, doc.getId(), position);
	
					// Compute tf(t,d). Used for document weight calculations
					termMap.put(term, termMap.getOrDefault(term, 0) + 1);
				}
				position++;
			}
			double max = Double.NEGATIVE_INFINITY;
			String authorOwns = "";
			for(String author : AUTHORS) {
				double probabilityC = Math.log10((double)classes.get(author).getCorpusSize()/ totalTrainingSet);
				double sumLogs = 0.0;
				
				for (String term : termMap.keySet()) {
					for (HashMap<String, Double> prob : probabilityInformation.get(author)) {
						Map.Entry<String, Double> entry = prob.entrySet().iterator().next();
						
						if (entry.getKey().equals(term)) {
							sumLogs += Math.log10(entry.getValue());
							break;
						}
					}
				}
				if(Double.compare(max, (probabilityC + sumLogs)) < 0) {
					max = probabilityC + sumLogs;
					authorOwns = author;
				}
				System.out.println(doc.getTitle() + "- Prob. of " + author + " = " + (probabilityC + sumLogs));
			}
			System.out.println(doc.getTitle() + " is owned by " + authorOwns + " with prob: " + max + "\n");
		}
		}	
		
		public static double log2(double d) {
			double result = Math.log(d)/Math.log(2.0);
			if(result <= 0.0) {
				return 0.0;
			}
			return result;
		}
		
		private static class DataStore implements Comparable<DataStore> {
			private double score;
			private String term;
			
			DataStore(double score, String term) {
				this.score = score;
				this.term = term;
			}
			
			public double getScore() {
				return score;
			}
			
			public String getTerm() {
				return term;
			}
			@Override
			public int compareTo(DataStore o) {
				return Double.compare(o.getScore(), this.score);
			}
			
			
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

	private static class FederalistClass {
		private int mCorpusSize;
		private PositionalInvertedIndex mIndex;
		private Map<Integer, Map<String, Double>> mWDTs;

		
		public FederalistClass(int size, PositionalInvertedIndex index, Map<Integer, Map<String, Double>> wDTs) {
			mCorpusSize = size;
			mIndex = index;
			mWDTs = wDTs;
		}

		public int getCorpusSize() {
			return mCorpusSize;
		}

		public PositionalInvertedIndex getIndex() {
			return mIndex;
		}

		public Map<Integer, Map<String, Double>> getWDTs() {
			return mWDTs;
		}
	}
}
