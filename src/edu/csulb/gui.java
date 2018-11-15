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
import cecs429.index.DiskIndexWriter;
import cecs429.index.Posting;
import cecs429.query.BooleanQueryParser;
import cecs429.query.QueryComponent;
import cecs429.text.DefaultTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.Stemmer;
import cecs429.text.TokenProcessor;
import libs.btree4j.BTreeException;

import javax.swing.JTextField;
import javax.swing.JScrollPane;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.io.File;
import java.util.*;

import javax.swing.JFileChooser;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.ScrollPaneConstants;
import javax.swing.JTable;
import javax.swing.border.EtchedBorder;
import java.awt.Font;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingConstants;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JRadioButton;
import libs.btree4j.BTreeException;

public class gui {

	private JFrame frame;
	private JTextField queryInput;
	@SuppressWarnings("rawtypes")
	private static JList outputList = new JList();

	DocumentCorpus corpus;
	Index index;
	DiskIndexWriter diskIndexWriter = new DiskIndexWriter();
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

	private static Index indexCorpus(DocumentCorpus corpus, TokenProcessor processor, Map<Integer, Map<String, Integer>> docWeightList) {
		Iterable<Document> documents = corpus.getDocuments();
		PositionalInvertedIndex index = new PositionalInvertedIndex();
		Map<String, Integer> tmpWeight;

		for (Document doc : documents) {
			int position = 0;
			tmpWeight = new HashMap<String, Integer>();
			Iterable<String> tokens = new EnglishTokenStream(doc.getContent()).getTokens();
			for (String token : tokens) {
				List<String> terms = processor.processToken(token);
				for (String term : terms) {
					index.addTerm(term, doc.getId(), position);
					tmpWeight.put(term, tmpWeight.getOrDefault(term, 0) + 1);
				}
				position++;
			}
			docWeightList.put(doc.getId(), tmpWeight);
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
		// Create Frame for GUI to reside
		frame = new JFrame();
		frame.setBounds(100, 100, 916, 559);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Initialize FileChooser
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		// Initialize Labels for UI
		JLabel directoryLabel = new JLabel("Nothing Selected.");
		JLabel indexIndicator = new JLabel("Index Empty.");
		JLabel lblQuery = new JLabel("Query");
		JLabel scrollPaneTitle = new JLabel("");
		JLabel lblqQuit = new JLabel(":q = quit application");
		JLabel lblsteamStem = new JLabel(":stem <query> = stem word");
		JLabel lblvocabFirst = new JLabel(":vocab = first 1000 terms");

		// Initialize snippet output area
		JTextPane documentSnippetOutput = new JTextPane();
		documentSnippetOutput.setEditable(false);

		// Set input text field properties
		queryInput = new JTextField();
		queryInput.setEditable(false);
		queryInput.setColumns(10);

		// Initialize ScrollPane for outputs
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		scrollPane.setViewportView(outputList);

		scrollPaneTitle.setFont(new Font("Lucida Grande", Font.PLAIN, 20));
		scrollPaneTitle.setHorizontalAlignment(SwingConstants.CENTER);
		scrollPane.setColumnHeaderView(scrollPaneTitle);

		JButton stemButton = new JButton("Stem");
		JButton vocabButton = new JButton("Vocab");
		vocabButton.setHorizontalAlignment(SwingConstants.RIGHT);

		// Create Search Button
		JButton searchButton = new JButton("Search");
		// Add listener on Search Button Click
		searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String query = queryInput.getText();
				documentSnippetOutput.setText("");
				// Quit Command :Q
				if (query.equals(":q")) {
					frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));

					// Stem Command :stem <Query>
				} else if (query.startsWith(":stem")) {
					scrollPaneTitle.setText("Stem: " + Stemmer.getInstance().stemToken(query.split(" ")[1]));

					// Vocab Command :vocab
				} else if (query.startsWith(":vocab")) {
					scrollPaneTitle.setText("Vocabulary");
					List<String> vocabulary = index.getVocabulary();

					// Create list for vocabs to reside in UI
					DefaultListModel<String> listModel = new DefaultListModel<String>();

					// Iterate through vocabulary and add to list
					for (int i = 0; i < 1000 && i < vocabulary.size(); i++) {
						listModel.addElement(vocabulary.get(i));
					}

					// Add list to ui scrollpane
					outputList = new JList<String>(listModel);
					outputList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					scrollPane.setViewportView(outputList);
					// Query Command <Query>
				} else {
					scrollPaneTitle.setText("Query");
					QueryComponent qc = queryParser.parseQuery(query);

					if (qc != null) {
						// TODO: need to swap this section
						List<Posting> postings = qc.getPostings(index, processor);

						if (!postings.isEmpty()) {
							scrollPaneTitle.setText(scrollPaneTitle.getText() + ": " + postings.size() + " results.");
							// Create list for query results to reside in UI
							DefaultListModel<String> listModel = new DefaultListModel<String>();

							// Iterate through and add to list
							for (int i = 0; i < postings.size(); i++) {
								listModel.addElement(corpus.getDocument(postings.get(i).getDocumentId()).getTitle());
							}

							// Initialize UI component for outputs
							outputList = new JList<String>(listModel);
							outputList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

							// Add double click listener to get snippet of document
							outputList.addMouseListener(new MouseAdapter() {
								public void mouseClicked(MouseEvent evt) {
									JList list = (JList) evt.getSource();
									if (evt.getClickCount() == 2) {

										// Double-click detected
										int index = list.locationToIndex(evt.getPoint());
										// Print snippet
										Snippet snip = new Snippet(
												corpus.getDocument(postings.get(index).getDocumentId()).getContent(),
												postings.get(index).getPositions());

										documentSnippetOutput.setText(snip.getContent());
									}
								}
							});
							scrollPane.setViewportView(outputList);
						} else {
							DefaultListModel<String> listModel = new DefaultListModel<String>();
							listModel.addElement("Term was not found.");
							outputList = new JList<String>(listModel);
							outputList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
							scrollPane.setViewportView(outputList);
							documentSnippetOutput.setText("");
						}
					}
				}
			}
		});
		searchButton.setEnabled(false);
		stemButton.setEnabled(false);
		vocabButton.setEnabled(false);
		// Create Select Corpus button to get directory of corpus
		JButton selectCorpusBtn = new JButton("Select Corpus");

		// Add listener to when button is clicked
		selectCorpusBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				// Check to see if search button is enabled
				if (searchButton.isEnabled()) {
					searchButton.setEnabled(false);
				}

				// Check to see if query input box is enabled
				if (queryInput.isEditable()) {
					queryInput.setEditable(false);
				}

				if (stemButton.isEnabled()) {
					stemButton.setEnabled(false);
				}

				if (vocabButton.isEnabled()) {
					vocabButton.setEnabled(false);
				}
				// Open FileChooser UI to user
				int result = fileChooser.showDialog(new JFrame("Select corpus"), "Select");
				// Check if directory has been selected
				if (result == JFileChooser.APPROVE_OPTION) {
					directoryLabel.setText(fileChooser.getSelectedFile().getAbsolutePath());
					indexIndicator.setText("Indexing in progress...");

					// created a new thread to not interfere with swing
					new Thread() {
						public void run() {
							corpus = DirectoryCorpus.loadJsonDirectory(
									Paths.get(fileChooser.getSelectedFile().getAbsolutePath()).toAbsolutePath(),
									".json");

							long start = System.currentTimeMillis();
							Map<Integer, Map<String, Integer>> docWeightList = new TreeMap<>();
							index = indexCorpus(corpus, processor, docWeightList);
							try {
								System.out.println("------------" + docWeightList.size());
								diskIndexWriter.createDocumentWeights(
										Paths.get(fileChooser.getSelectedFile().getAbsolutePath(), "index"), docWeightList);
								diskIndexWriter.writeIndex(
										Paths.get(fileChooser.getSelectedFile().getAbsolutePath(), "index"), index);
							} catch (IOException e) {
                                Logger.getLogger(gui.class.getName()).log(Level.SEVERE, null, ex);
							} catch (BTreeException e) {
                                Logger.getLogger(gui.class.getName()).log(Level.SEVERE, null, ex);
							}
							long end = System.currentTimeMillis();
							indexIndicator.setText("Indexed: " + ((end - start) / 1000) + " seconds.");
							// Check if search button was disabled, to enable it
							if (!searchButton.isEnabled()) {
								searchButton.setEnabled(true);
							}

							// Check if query input box was disabled, to enable it.
							if (!queryInput.isEditable()) {
								queryInput.setEditable(true);
							}

							if (!stemButton.isEnabled()) {
								stemButton.setEnabled(true);
							}

							if (!vocabButton.isEnabled()) {
								vocabButton.setEnabled(true);
							}
						}
					}.start();
					// Condition if cancel was clicked
				} else if (result == JFileChooser.CANCEL_OPTION) {
					index = null;
					directoryLabel.setText("Nothing Selected");
					indexIndicator.setText("Index Empty.");
					if (searchButton.isEnabled()) {
						searchButton.setEnabled(false);
					}

					if (queryInput.isEditable()) {
						queryInput.setEditable(false);
					}

					if (stemButton.isEnabled()) {
						stemButton.setEnabled(false);
					}

					if (vocabButton.isEnabled()) {
						vocabButton.setEnabled(false);
					}
				}
			}
		});

		stemButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				queryInput.setText(":stem ");
			}
		});

		vocabButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				queryInput.setText(":vocab");
				searchButton.doClick();
			}
		});

		JRadioButton booleanQuerySelect = new JRadioButton("Boolean Retrieval Mode");
		JRadioButton rankedQuerySelect = new JRadioButton("Ranked Retrieval Mode");
		booleanQuerySelect.setSelected(true);

		ButtonGroup radioButtonGroup = new ButtonGroup();
		radioButtonGroup.add(booleanQuerySelect);
		radioButtonGroup.add(rankedQuerySelect);

		booleanQuerySelect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// If this is reached, boolean retrieval mode is selected

			}
		});

		rankedQuerySelect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// if this is reached, ranked retrieval mode is selected
				
			}
		});
		// UI grouping. Auto-generated by WindowBuilder in Eclipse
		GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout
				.createSequentialGroup().addGap(68)
				.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup().addComponent(booleanQuerySelect)
								.addPreferredGap(ComponentPlacement.RELATED).addComponent(rankedQuerySelect)
								.addGap(640))
						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout
								.createSequentialGroup()
								.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
										.addGroup(groupLayout.createSequentialGroup().addComponent(lblqQuit)
												.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE,
														Short.MAX_VALUE)
												.addComponent(lblsteamStem).addGap(73).addComponent(lblvocabFirst))
										.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 628,
												GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(documentSnippetOutput, GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)
								.addGap(67))
								.addGroup(groupLayout.createSequentialGroup().addGroup(groupLayout
										.createParallelGroup(Alignment.LEADING, false)
										.addGroup(groupLayout.createSequentialGroup().addComponent(lblQuery)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(queryInput, GroupLayout.PREFERRED_SIZE, 464,
														GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(ComponentPlacement.RELATED).addComponent(searchButton,
														GroupLayout.PREFERRED_SIZE, 124, GroupLayout.PREFERRED_SIZE))
										.addGroup(groupLayout.createSequentialGroup()
												.addComponent(selectCorpusBtn, GroupLayout.PREFERRED_SIZE, 150,
														GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(ComponentPlacement.UNRELATED)
												.addComponent(directoryLabel, GroupLayout.DEFAULT_SIZE,
														GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addPreferredGap(ComponentPlacement.UNRELATED)
												.addComponent(indexIndicator)))
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(vocabButton, GroupLayout.PREFERRED_SIZE, 82,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED).addComponent(stemButton,
												GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addGap(100))))));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup().addGap(11)
						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(selectCorpusBtn)
								.addComponent(directoryLabel, GroupLayout.PREFERRED_SIZE, 19,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(indexIndicator))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(booleanQuerySelect)
								.addComponent(rankedQuerySelect))
						.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(queryInput, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(lblQuery).addComponent(searchButton).addComponent(vocabButton)
								.addComponent(stemButton))
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
								.addComponent(documentSnippetOutput, GroupLayout.PREFERRED_SIZE,
										GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblqQuit)
								.addComponent(lblvocabFirst).addComponent(lblsteamStem))
						.addGap(24)));

		frame.getContentPane().setLayout(groupLayout);
	}
}
