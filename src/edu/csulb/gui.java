package edu.csulb;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JProgressBar;
import javax.swing.JLabel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.documents.Snippet;
import cecs429.index.Index;
import cecs429.index.PositionalInvertedIndex;
import cecs429.index.Posting;
import cecs429.query.BooleanQueryParser;
import cecs429.query.QueryComponent;
import cecs429.text.DefaultTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.Stemmer;
import cecs429.text.TokenProcessor;

import javax.swing.JTextField;
import javax.swing.JScrollPane;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.JFileChooser;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.ScrollPaneConstants;
import javax.swing.JTable;
import javax.swing.border.EtchedBorder;
import java.awt.Font;
import javax.swing.SwingConstants;
import javax.swing.Box;

public class gui {

	private JFrame frame;
	private JTextField queryInput;
	
	DocumentCorpus corpus;
	Index index;
	TokenProcessor processor = new DefaultTokenProcessor();
	BooleanQueryParser queryParser = new BooleanQueryParser();
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					gui window = new gui();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
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

	/**
	 * Create the application.
	 */
	public gui() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setResizable(false);
		frame.setBounds(100, 100, 872, 512);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		JLabel directoryLabel = new JLabel("Nothing Selected.");
		JLabel indexIndicator = new JLabel("Index Empty.");
		
		queryInput = new JTextField();
		queryInput.setEditable(false);
		queryInput.setColumns(10);
		
		JLabel lblQuery = new JLabel("Query");
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		JButton searchButton = new JButton("Search");
		searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String query = queryInput.getText();
				if (query.equals(":q")) {
					// need to close application
				} else if (query.startsWith(":stem")) {
					System.out.println("Stemmed token: " + Stemmer.getInstance().stemToken(query.split(" ")[1]));
//				} else if (query.startsWith(":index")) {
//					corpus = DirectoryCorpus.loadJsonDirectory(Paths.get(query.split(" ")[1]).toAbsolutePath(), ".json");
//					System.out.println("Indexing in progress...");
//					start = System.currentTimeMillis();
//					index = indexCorpus(corpus, processor);
//					end = System.currentTimeMillis();
//					System.out.println("Indexing completed in " + ((end - start) / 1000) + " seconds.");
//				} else if (query.startsWith(":vocab")) {
				} else if (query.startsWith(":vocab")) {
					List<String> vocabulary = index.getVocabulary();
					System.out.println("Printing first 1000 terms in vocabulary of corpus.");
					for (int i = 0; i < 1000 && i < vocabulary.size(); i++) {
						System.out.println(i + ": " + vocabulary.get(i));
					}
					System.out.println("Total number of vocabulary terms: " + vocabulary.size());
				} else {
					QueryComponent qc = queryParser.parseQuery(query);

					if (qc != null) {
						List<Posting> postings = qc.getPostings(index, processor);

						if (!postings.isEmpty()) {
							for (int i = 0; i < postings.size(); i++) {
								System.out.println(i + ": " + corpus.getDocument(postings.get(i).getDocumentId()).getTitle());
							}
							System.out.println("Number of documents: " + postings.size());
//							System.out.print("\n\nDo you wish to select a document to view? (y, n) ");
//							String docRequested = sc.nextLine();
//							if (docRequested.toLowerCase().equals("y")) {
//								System.out.print("Please enter a list number from the list above: ");
//								int listNum = sc.nextInt();
//								int docId = postings.get(listNum).getDocumentId();
//								BufferedReader in = new BufferedReader(corpus.getDocument(docId).getContent());
//								String line = null;
//								// Print entire document content
//								// TODO: Remove after Snippet is fully working
//								try {
//									while ((line = in.readLine()) != null) {
//										System.out.println(line);
//									}
//								} catch(IOException ex) {
//									System.out.println("Error reading document.");
//								}
//								// Flush the buffer
//								sc.nextLine();
//
//								// Print snippet
//								Snippet snip = new Snippet(
//									corpus.getDocument(docId).getContent(),
//									postings.get(listNum).getPositions()
//								);
//								System.out.println("\n\nSnippet: " + snip.getContent());
//							}
						} else {
							System.out.println("Term was not found.");
						}
					}
				}
			}
		});
		searchButton.setEnabled(false);
		
		JButton selectCorpusBtn = new JButton("Select Corpus");
		
		selectCorpusBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				int result = fileChooser.showDialog(new JFrame("Select corpus"), "Select");
				if(result == JFileChooser.APPROVE_OPTION) {
					directoryLabel.setText(fileChooser.getSelectedFile().getAbsolutePath());
					indexIndicator.setText("Indexing in progress...");
					
					 // created a new thread to not interfere with swing
					new Thread() {
						public void run() {
							corpus = DirectoryCorpus.loadJsonDirectory(Paths.get(fileChooser.getSelectedFile().getAbsolutePath()).toAbsolutePath(), ".json");

							long start = System.currentTimeMillis();
							index = indexCorpus(corpus, processor);
							long end = System.currentTimeMillis();
							indexIndicator.setText("Indexed: " + ((end - start) / 1000) + " seconds.");
							if(!searchButton.isEnabled()) {
								searchButton.setEnabled(true);
							}
							
							if(!queryInput.isEditable()) {
								queryInput.setEditable(true);
							}
						}
					}.start();
					
					
					
				} else if( result == JFileChooser.CANCEL_OPTION) {
					index = null;
					directoryLabel.setText("Nothing Selected");
					indexIndicator.setText("Index Empty.");
					if(searchButton.isEnabled()) {
						searchButton.setEnabled(false);
					}
					
					if(queryInput.isEditable()) {
						queryInput.setEditable(false);
					}
				}
			}
		});
		
		JLabel lblqQuit = new JLabel(":q = quit application");
		
		JLabel lblsteamStem = new JLabel(":steam <query> = stem word");
		
		JLabel lblvocabFirst = new JLabel(":vocab = first 1000 terms");

		
		GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(68)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
							.addGroup(Alignment.LEADING, groupLayout.createSequentialGroup()
								.addComponent(lblQuery)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(queryInput, GroupLayout.PREFERRED_SIZE, 464, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(searchButton, GroupLayout.PREFERRED_SIZE, 124, GroupLayout.PREFERRED_SIZE))
							.addGroup(Alignment.LEADING, groupLayout.createSequentialGroup()
								.addComponent(selectCorpusBtn, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(ComponentPlacement.UNRELATED)
								.addComponent(directoryLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addPreferredGap(ComponentPlacement.UNRELATED)
								.addComponent(indexIndicator)))
						.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 628, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(lblvocabFirst)
						.addComponent(lblqQuit)
						.addComponent(lblsteamStem))
					.addContainerGap(16, Short.MAX_VALUE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(11)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(selectCorpusBtn)
						.addComponent(directoryLabel, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)
						.addComponent(indexIndicator))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(lblqQuit)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(lblsteamStem)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(lblvocabFirst))
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(queryInput, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblQuery)
								.addComponent(searchButton))
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 393, Short.MAX_VALUE)))
					.addContainerGap())
		);
		
		JLabel scrollPaneTitle = new JLabel("");
		scrollPaneTitle.setHorizontalAlignment(SwingConstants.CENTER);
		scrollPaneTitle.setFont(new Font("Tahoma", Font.PLAIN, 24));
		scrollPane.setColumnHeaderView(scrollPaneTitle);
		frame.getContentPane().setLayout(groupLayout);
	}
}
