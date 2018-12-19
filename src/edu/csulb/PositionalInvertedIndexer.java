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
			Map<Integer, Double> weights = new HashMap<>();
			DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(corpusPath.resolve(author), ".txt");
			Iterable<Document> documents = corpus.getDocuments();
			
			PositionalInvertedIndex index = new PositionalInvertedIndex();

			for (Document doc : documents) {
				int position = 0;
				Map<String, Integer> termMap = new HashMap<>();
				Iterable<String> tokens = new EnglishTokenStream(doc.getContent()).getTokens();
				for (String token : tokens) {
					List<String> terms = processor.processToken(token);

					for (String term : terms) {
						index.addTerm(term, doc.getId(), position);

						// Compute tf(t,d). Used for document weight calculations
						termMap.put(term, termMap.getOrDefault(term, 0) + 1);
					}
					position++;
				}
				// Calculate L(d) for each document
				double ld = termMap.values()
					.stream()
					.map(a -> Math.pow(1.0 + Math.log(a), 2))
					.reduce(0.0, Double::sum);

				weights.put(doc.getId(), Math.sqrt(ld));
			}

			// Save into FederalistClass
			classes.put(author, new FederalistClass(corpus.getCorpusSize(), index, weights));
		}		

		// Testing: Find f(t,c) for judiciary in Hamilton class: Return df(t)
		String query = ((DefaultTokenProcessor) processor).normalizeAndStemToken("judiciary");
		System.out.println("f(t,c) = f(judiciary, HAMILTON): "
			+ classes.get("HAMILTON").getIndex().getPostings(query).size() + "\n");
		
		rocchioClassification2(processor, classes, corpusPath);
		//bayesianClassification(processor, AUTHORS, classes, corpusPath);
	}

	private static void rocchioClassification(TokenProcessor processor, Map<String, FederalistClass> classes, Path corpusPath) {
		Map<String, Double> centroids = new HashMap<>();
		for (Map.Entry<String, FederalistClass> fc : classes.entrySet()) {
			double v_d = fc.getValue().getWeights().values()
				.stream()
				.reduce(0.0, Double::sum);
			
			System.out.println("Centroid class: " + fc.getKey() + ", Centroid value: " + v_d / fc.getValue().getCorpusSize());
			centroids.put(fc.getKey(), (v_d / fc.getValue().getCorpusSize()));
		}

		System.out.println();

		// Classify disputed documents
		DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(corpusPath.resolve("DISPUTED"), ".txt");
		Iterable<Document> documents = corpus.getDocuments();

		for (Document doc: documents) {
			Map<String, Integer> termMap = new HashMap<>();
			Iterable<String> tokens = new EnglishTokenStream(doc.getContent()).getTokens();
			for (String token : tokens) {
				List<String> terms = processor.processToken(token);
				for (String term : terms) {
					// Compute tf(t,d). Used for document weight calculations
					termMap.put(term, termMap.getOrDefault(term, 0) + 1);
				}
			}
			// Calculate L(d)
			double ld = termMap.values()
				.stream()
				.map(a -> Math.pow(1.0 + Math.log(a), 2))
				.reduce(0.0, Double::sum);
			
			ld = Math.sqrt(ld);

			System.out.println("Euclidian Distance for: " +  doc.getTitle() + " with v(d): " + ld);
			double min = 0.0d;
			String className = null;
			for (Map.Entry<String, Double> centroid : centroids.entrySet()) {
				double euclidianDistance = Math.abs(centroid.getValue() - ld);

				if (min > euclidianDistance || min == 0.0d) {
					min = euclidianDistance;
					className = centroid.getKey();
				}
				// Print out value
				System.out.println(centroid.getKey() + ": " + euclidianDistance);
			}

			System.out.println(doc.getTitle() + " will be classified in the following class: " + className + "\n");
		}
	}
        
        private static TreeMap<String, Double> initVectorVocab(Map<String, FederalistClass> classes) {
            //Get all vocab from H, M, J            
            Set<String> allVocab = new HashSet<>();
            for (Map.Entry<String, FederalistClass> fc : classes.entrySet()) 
                allVocab.addAll(fc.getValue().getIndex().getVocabulary());
            
            TreeMap<String, Double> alphaVector = new TreeMap<>();
            for(String term : allVocab)
                alphaVector.put(term, 0.0);
            
            return alphaVector;
        }
        
        //Used for each class 
        private static void defClassVector(Path corpusPath, String tag, TokenProcessor processor, FederalistClass author, Map<String, Double> vec) {
            DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(corpusPath.resolve(tag), ".txt");
            
            int corpusSize = 0;
            for (Document doc : corpus.getDocuments()) {
                Iterable<String> tokens = new EnglishTokenStream(doc.getContent()).getTokens();
                
                for (String token : tokens) {
                    List<String> terms = processor.processToken(token);
                    for (String term : terms) {
                        vec.put(term, vec.get(term) + 1); // establish term frequency
                    }
                }
                ++corpusSize;
            }
            
            ////////////////////////////////////////////////
            double ld = 0.0;
            
            for (String key : vec.keySet()) {
                if(vec.get(key) != 0.0) {
                    vec.put(key, 1 + Math.log(vec.get(key))); // not normalized
                    ld += Math.pow(vec.get(key), 2); // store the sum of the squares
                    vec.put(key, vec.get(key) / corpusSize);
                }
            }
            
            ld = Math.sqrt(ld); //sqrt for normalization below
            
            //Normalize here via ld
            for (String key : vec.keySet()) {
                if(vec.get(key) != 0.0)
                    vec.put(key, vec.get(key) / ld); //normalized
                
                System.out.println(key + " => " + vec.get(key));
            }
        }
        
        private static void rocchioClassification2(TokenProcessor processor, Map<String, FederalistClass> classes, Path corpusPath) {
            //construct alphaVector -> get all vocab and set to 0
            Map<String, Double> jayVec = initVectorVocab(classes);
            Map<String, Double> hamiltonVec = initVectorVocab(classes);
            Map<String, Double> madisonVec = initVectorVocab(classes);
            
            //get normalized vector u_d
            defClassVector(corpusPath, "JAY", processor, classes.get("JAY"), jayVec);
            defClassVector(corpusPath, "HAMILTON", processor, classes.get("HAMILTON"), hamiltonVec);
            defClassVector(corpusPath,"MADISON", processor, classes.get("MADISON"), madisonVec);
            
            //Now, classify each disputed document
            DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(corpusPath.resolve("DISPUTED"), ".txt");
            Iterable<Document> disputed = corpus.getDocuments();

            //Initialize Maps DocTitle -> HashMap<term, wdt>
            Map<String, TreeMap<String,Double>> disputedVec = new TreeMap<>();
            for (int i = 0; i < corpus.getCorpusSize(); ++i) 
                disputedVec.put(corpus.getDocument(i).getTitle(), initVectorVocab(classes));
            
            
            //Compute similarity of each vector Author -> sum of weights
            TreeMap<String,Double> authorGuess = new TreeMap<>();
            
            for (Document doc: disputed) {
                // Do similiar procedure to defClassVector (not for the entire disputed corpus). 
                // However, only do for this document and put vector disputedVec
                Iterable<String> tokens = new EnglishTokenStream(doc.getContent()).getTokens();
                TreeMap<String,Double> map = new TreeMap<>();
                map = disputedVec.get(doc.getTitle());// placeholder
                 
                for (String token : tokens) {
                    List<String> terms = processor.processToken(token);
                    for (String term : terms) {
                        map.put(term, map.getOrDefault(term, 0.0) + 1); // establish term frequency
                    }
                }
                
                /*
                double ld = 0.0;
                
                
                
                for(String key : map.keySet()) {
                    map.put(key, 1 + Math.log(map.get(key)));
                    ld += Math.pow(map.get(key), 2);
                }
                
                for(String key : map.keySet()) {
                     map.put(key, map.get(key) / ld);
                }
                
                disputedVec.put(doc.getTitle(), map);
                */
                double ld = 0.0;
            
                for (String key : map.keySet()) {
                    if(map.get(key) != 0.0) {
                        map.put(key, 1 + Math.log(map.get(key))); // not normalized
                        ld += Math.pow(map.get(key), 2); // store the sum of the squares
                    }
                }

                ld = Math.sqrt(ld); //sqrt for normalization below

                //Normalize here via ld
                for (String key : map.keySet()) {
                    if(map.get(key) != 0.0)
                        map.put(key, map.get(key) / ld); //normalized

                    System.out.println(key + " => " + map.get(key));
                }
                
                disputedVec.put(doc.getTitle(), map);
            }
            
            double sum52 = 0.0;
            double sumH = 0.0;
            double sumM = 0.0;
            double sumJ = 0.0;
            //Now we can compare the vectors Add all up and subtract
                      
            for (String key : hamiltonVec.keySet()) 
                sumH += hamiltonVec.get(key);
            
            for (String key : jayVec.keySet()) 
                sumJ += jayVec.get(key);
            
            for (String key : madisonVec.keySet()) 
                sumM += madisonVec.get(key);
            
            //Now subtract
            for (int i = 49; i <= 57; ++i) {
                
            TreeMap<String,Double> paper52 = disputedVec.get("paper_52.txt");
            for (String key : paper52.keySet()) 
            sum52 += paper52.get(key);
            
            System.out.println("Doc" + i + ": " + sum52);
            System.out.println("Hamilton: " +  (sum52 - sumH));
            System.out.println("Madison: " +  (sum52 - sumM));
            System.out.println("Jay: " +  (sum52 - sumJ));
            }
            
	}

	private static void bayesianClassification(TokenProcessor processor, String[] AUTHORS, Map<String, FederalistClass> classes, Path corpusPath) {
	////////////////////////////////////////////////////////////////////////////
	//				Bayesian Classification									  //
	////////////////////////////////////////////////////////////////////////////
	System.out.println("Bayesian Classification");
	
	// Data structure for storing results of Mutual Information
	HashMap<String, ArrayList<HashMap<String, Double>>> mutualInformation = new HashMap<String, ArrayList<HashMap<String, Double>>>();
	
	for (String author : AUTHORS) {
		// Key will be from the AUTHORS set and value will be the p(t,c)
		mutualInformation.put(author, new ArrayList<HashMap<String, Double>>());
		
		Index classIndex = classes.get(author).getIndex();
		
		for(String term : classIndex.getVocabulary()) {
			int[][] mutualInfoArray = new int[2][2];
			
			mutualInfoArray[1][1] = classIndex.getPostings(term).size();
			
			mutualInfoArray[1][0] = classes.get(author).getCorpusSize() - mutualInfoArray[1][1];
			
			int numTermInOther = 0;
			int numTermNotOther = 0;
			
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
	
	for(String author : AUTHORS) {
		for(HashMap<String, Double> term : mutualInformation.get(author)) {
			Map.Entry<String,Double> entry = term.entrySet().iterator().next();
			mutualInformation50Term.add(new DataStore(entry.getValue(), entry.getKey()));
		}
	}
	
	for(DataStore store : mutualInformation50Term) {
		System.out.println(store.getTerm() + " - " + store.getScore());
	}
	
	// Data structure for storing results of the Probability
	HashMap<String, ArrayList<HashMap<String, Double>>> probabilityInformation = new HashMap<String, ArrayList<HashMap<String, Double>>>();
	
	for (String author : AUTHORS) {
		probabilityInformation.put(author, new ArrayList<HashMap<String, Double>>());
		
		for(DataStore discTerm : mutualInformation50Term) {
			int ftc = classes.get(author).getIndex().getPostings(discTerm.getTerm()).size() + 1;
			
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
		private Map<Integer, Double> mWeights;

		
		public FederalistClass(int size, PositionalInvertedIndex index, Map<Integer, Double> weights) {
			mCorpusSize = size;
			mIndex = index;
			mWeights = weights;
		}

		public int getCorpusSize() {
			return mCorpusSize;
		}

		public PositionalInvertedIndex getIndex() {
			return mIndex;
		}

		public Map<Integer, Double> getWeights() {
			return mWeights;
		}
	}
}
