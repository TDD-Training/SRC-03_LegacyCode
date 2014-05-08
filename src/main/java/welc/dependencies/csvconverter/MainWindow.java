package welc.dependencies.csvconverter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpringLayout;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import welc.thirdparty.SpringUtilities;

public class MainWindow extends JFrame {

	private static final long serialVersionUID = 3921026055392339551L;
	private static final int WINDOW_WIDTH = 800;
	private static final int WINDOW_HEIGHT = 600;
	private Action exitAction = null;
	private Action openFileAction = null;
	private Action selectPathToConvertedFileAction = null;
	private Action concatenateColumnsAction;
	private Action swapColumnsAction;
	private Action skipColumnAction;
	private Action executeConversionAction;
	private Action browseFileAction;

	private JTextField fileToOpen;
	private JTextField fileToSave;
	private JTextField separators;
	private JTextField skippedFooter;
	private JTextField skippedHeader;
	private JTable table;
	private CSVOptions options;

	List<OperationInfo> operationInfos;

	public static void main(String[] args) {
		MainWindow mw = new MainWindow("Converter");
		mw.setVisible(true);
	}

	public MainWindow(String name) {
		options = new CSVOptions(this);

		setTitle(name);
		setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		setLayout(new BorderLayout());

		JMenuBar menuBar = buildMainManu();
		setJMenuBar(menuBar);

		JPanel contentPanel = buildContentPanel();
		setContentPane(contentPanel);
	}

	private JMenuBar buildMainManu() {
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = setupFileMenu();
		menuBar.add(fileMenu);

		JMenu actionsMenu = setupActionsMenu();
		menuBar.add(actionsMenu);
		return menuBar;
	}

	private JMenu setupFileMenu() {
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);

		Action openFileToConvertAction = getFileToConvertAction();
		JMenuItem menuItem = new JMenuItem(openFileToConvertAction);
		fileMenu.add(menuItem);

		Action selectPathToConvertedFileAction = getSelectPathToConvertedFileAction();
		menuItem = new JMenuItem(selectPathToConvertedFileAction);
		fileMenu.add(menuItem);

		fileMenu.add(new JSeparator());

		Action exitAction = getExitAction();
		menuItem = new JMenuItem(exitAction);

		fileMenu.add(menuItem);
		return fileMenu;
	}

	private JMenu setupActionsMenu() {
		JMenu fileMenu = new JMenu("Actions");
		fileMenu.setMnemonic(KeyEvent.VK_A);

		Action skipAction = getSkipColumnActionAction();
		JMenuItem menuItem = new JMenuItem(skipAction);
		fileMenu.add(menuItem);

		Action swapAction = getSwapColumnsActionAction();
		menuItem = new JMenuItem(swapAction);
		fileMenu.add(menuItem);

		Action concatenateAction = getConcatenateColumnsActionAction();
		menuItem = new JMenuItem(concatenateAction);
		fileMenu.add(menuItem);

		fileMenu.add(menuItem);
		return fileMenu;
	}

	private Action getConvertAction() {
		if (executeConversionAction == null) {
			Action ret = new AbstractAction("Execute conversion", null) {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					executeConversion();
				}
			};
			ret.putValue(Action.SHORT_DESCRIPTION, "Execute conversion");
			ret.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_E);

			executeConversionAction = ret;

			boolean bothFilesSelected = !(fileToOpen.getText().equals("") || fileToSave.getText().equals(""));
			getConvertAction().setEnabled(bothFilesSelected);
		}
		return executeConversionAction;
	}

	private BufferedWriter prepareWriter() {
		try {
			return new BufferedWriter(new FileWriter(fileToSave.getText()));
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		return null;
	}

	protected void executeConversion() {
		// TODO Auto-generated method stub
		options = new CSVOptions(this, getSkippedLines(), getSkippedFooterLines(), getSeparators(), getSeparators());
		BufferedWriter writer = prepareWriter();
		BufferedReader reader = prepareReader();

		try {
			convert(reader, writer);
		} catch (IOException e) {
			conversionErrorMessage("Error during conversion");
		}
	}

	private Action getExitAction() {
		if (exitAction == null) {
			Action ret = new AbstractAction("Exit", null) {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					exit();
				}
			};
			ret.putValue(Action.SHORT_DESCRIPTION, "Exit");
			ret.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_X);
			exitAction = ret;
		}
		return exitAction;
	}

	private Action getSelectPathToConvertedFileAction() {
		if (this.selectPathToConvertedFileAction == null) {
			Action ret = new AbstractAction("Save converted file as", null) {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					selectFileToSaveConvertedData();
					boolean bothFilesSelected = !(fileToOpen.getText().equals("") || fileToSave.getText().equals(""));
					getConvertAction().setEnabled(bothFilesSelected);
				}
			};
			ret.putValue(Action.SHORT_DESCRIPTION, "Converted File");
			ret.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
			selectPathToConvertedFileAction = ret;
		}
		return selectPathToConvertedFileAction;
	}

	private Action getSkipColumnActionAction() {
		if (skipColumnAction == null) {
			Action ret = new AbstractAction("Skip column", null) {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					String columnNumberStr = JOptionPane.showInputDialog(MainWindow.this, "Enter column number that shall be skipped");
					try {
						int i = Integer.valueOf(columnNumberStr);
						setSkipColumn(i);
					}
					catch (NumberFormatException ignore) {
						wrongParametersMessage("Enter column number");
					}
				}
			};
			ret.putValue(Action.SHORT_DESCRIPTION, "Skip column");
			ret.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_P);
			skipColumnAction = ret;
		}
		return skipColumnAction;
	}

	private Action getSwapColumnsActionAction() {
		if (swapColumnsAction == null) {
			String actionMessage = "Swap columns";
			int mnemonic = KeyEvent.VK_W;

			Action ret = new AbstractAction(actionMessage, null) {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					String columnNumberStr = JOptionPane.showInputDialog(MainWindow.this, "Enter two column numbers separaded by coma that shall be swapped");
					StringTokenizer paramsTokenizer = new StringTokenizer(columnNumberStr, ",");
					if (paramsTokenizer.countTokens() != 2) {
						wrongParametersMessage("Enter two numbers separated by coma...");
					}
					else {
						try {
							String token = paramsTokenizer.nextToken();
							int i = Integer.valueOf(token);
							token = paramsTokenizer.nextToken();
							int j = Integer.valueOf(token);
							setFirstColumnToSwap(i > j ? j : i);
							setSecondColumnToSwap(i < j ? j : i);
						}
						catch (NumberFormatException ignore) {
							wrongParametersMessage("Enter two numbers separated by coma...");
						}
					}
				}
			};
			ret.putValue(Action.SHORT_DESCRIPTION, actionMessage);
			ret.putValue(Action.MNEMONIC_KEY, mnemonic);
			swapColumnsAction = ret;
		}
		return swapColumnsAction;
	}

	private Action getConcatenateColumnsActionAction() {
		if (concatenateColumnsAction == null) {
			Action ret = new AbstractAction("Concatenate columns", null) {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					String columnNumberStr = JOptionPane.showInputDialog(MainWindow.this, "Enter two column numbers and (optionally) saparation string. All separaded by coma");
					StringTokenizer paramsTokenizer = new StringTokenizer(columnNumberStr, ",");
					int tokensNo = paramsTokenizer.countTokens();
					if (tokensNo < 2 && tokensNo > 3) {
						wrongParametersMessage("Enter two numbers separated by coma or two nambers and separation string");
					}
					else {
						try {
							String token = paramsTokenizer.nextToken();
							int i = Integer.valueOf(token);
							token = paramsTokenizer.nextToken();
							int j = Integer.valueOf(token);
							setFirstColumnToConctenate(i > j ? j : i);
							setSecondColumnToConcatenate(i < j ? j : i);
							if (paramsTokenizer.hasMoreTokens())
								setSepar1ationString(paramsTokenizer.nextToken());
						}
						catch (NumberFormatException ignore) {
							wrongParametersMessage("Enter two numbers separated by coma or two nambers and separation string");
						}
					}
				}
			};
			ret.putValue(Action.SHORT_DESCRIPTION, "Concatenate columns");
			ret.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
			concatenateColumnsAction = ret;
		}
		return concatenateColumnsAction;
	}
	
	private Action getBrowseFileAction() {
		if (browseFileAction == null) {
			Action ret = new AbstractAction("...", null) {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					browseFileToConvers();
				}
			};
			ret.putValue(Action.SHORT_DESCRIPTION, "Browse file");
			ret.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_B);
			browseFileAction = ret;
		}
		return browseFileAction;
	}

	protected void browseFileToConvers() {
		FileBrowserHelper browseFileHelper = new FileBrowserHelper();
		browseFileHelper.browseFileInExternalWindow(fileToOpen.getText());
	}

	private Action getFileToConvertAction() {
		if (openFileAction == null) {
			Action ret = new AbstractAction("File to Convert", null) {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					openFileToConvers();
					boolean bothFilesSelected = !(fileToOpen.getText().equals("") || fileToSave.getText().equals(""));
					getConvertAction().setEnabled(bothFilesSelected);
					updateData();
				}
			};
			ret.putValue(Action.SHORT_DESCRIPTION, "File to Convert");
			ret.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_O);
			openFileAction = ret;
		}
		return openFileAction;
	}

	private String createFileNameFilterDescriptionFromExtensions(String description,
			String[] extensions) {
		String fullDescription = (description == null) ? "(" : description + " (";
		fullDescription += "." + extensions[0];
		for (int i = 1; i < extensions.length; i++) {
			fullDescription += ", .";
			fullDescription += extensions[i];
		}
		fullDescription += ")";
		return fullDescription;
	}

	private FileFilter createFileFilter(String description, boolean showExtensionInDescription,
			String... extensions) {
		if (showExtensionInDescription) {
			description = createFileNameFilterDescriptionFromExtensions(description, extensions);
		}
		return new FileNameExtensionFilter(description, extensions);
	}

	private void selectFileToSaveConvertedData() {
		JFileChooser c = new JFileChooser();
		setupFileChooser(c);

		int rVal = c.showSaveDialog(MainWindow.this);
		if (rVal == JFileChooser.APPROVE_OPTION) {
			fileToSave.setText(c.getCurrentDirectory().toString() + "/" + c.getSelectedFile().getName());
		}
		if (rVal == JFileChooser.CANCEL_OPTION) {
			// fileToSave.setText("");
		}
	}

	private void setupFileChooser(JFileChooser c) {
		boolean showExtensionInDescription = true;
		FileFilter bothFilter = createFileFilter("CSV and TXT Image Files", showExtensionInDescription, "csv", "txt");
		FileFilter txtFilter = createFileFilter("TXT files", showExtensionInDescription, "txt");
		FileFilter csvFilter = createFileFilter("CSV files", showExtensionInDescription, "csv");
		c.addChoosableFileFilter(bothFilter);
		c.addChoosableFileFilter(txtFilter);
		c.addChoosableFileFilter(csvFilter);
	}

	private void openFileToConvers() {
		JFileChooser c = new JFileChooser();
		setupFileChooser(c);

		int rVal = c.showOpenDialog(MainWindow.this);
		if (rVal == JFileChooser.APPROVE_OPTION) {
			fileToOpen.setText(c.getCurrentDirectory().toString() + "/" + c.getSelectedFile().getName());
		}
		if (rVal == JFileChooser.CANCEL_OPTION) {
			// fileToOpen.setText("");
		}
	}

	private void exit() {
		System.exit(0);
	}

	private JPanel buildContentPanel() {
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout());

		JPanel optionsPanel = buildOptionsPanel();
		contentPanel.add(optionsPanel, BorderLayout.PAGE_START);

		JPanel dataPanel = buildDataPanel();
		contentPanel.add(dataPanel, BorderLayout.CENTER);

		JPanel operationsPanel = buildOperationsPanel();
		contentPanel.add(operationsPanel, BorderLayout.LINE_END);

		JPanel buttonPanel = buildButtonPanel();
		contentPanel.add(buttonPanel, BorderLayout.PAGE_END);

		return contentPanel;
	}

	public class GrayedRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;
		private Color originalForeground = null;
		private Color originalBackground = null;

		public GrayedRenderer() {
		}

		@Override
		public Component getTableCellRendererComponent(JTable arg0, Object data, boolean isSelected, boolean hasFocus, int row, int col) {

			if (originalBackground == null)
				originalBackground = getBackground();
			if (originalForeground == null)
				originalForeground = getForeground();

			int i = getSkippedLines();
			int footerSkipped = getSkippedFooterLines();
			int lines = getNumberOfLines();
			if (row < i || row >= (lines - footerSkipped)) {
				setForeground(new Color(100, 100, 100));
				setBackground(new Color(200, 200, 200));
			} else {
				if (originalForeground != null)
					setForeground(null);
				if (originalBackground != null)
					setBackground(null);
			}
			setText(data.toString());
			return this;
		}

	}

	class CVSDataTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1392179213712732759L;
		private int numberOfCols;

		@Override
		public String getColumnName(int colunIdx) {
			int i = (getSkippedLines().intValue());
			if (i > 0) {
				return (getValueAt(i - 1, colunIdx)).toString();
			}
			return super.getColumnName(colunIdx);
		}

		@Override
		public int getColumnCount() {
			numberOfCols = getNumberOfCols();
			return numberOfCols;
		}

		@Override
		public int getRowCount() {
			return getNumberOfLines();
		}

		@Override
		public Object getValueAt(int rowIdx, int colIdx) {
			String data = getData(rowIdx, colIdx, numberOfCols);
			return data;
		}

	};

	private JPanel buildDataPanel() {
		JPanel dataPanel = new JPanel();
		dataPanel.setLayout(new BorderLayout());

		AbstractTableModel tableModel = new CVSDataTableModel();
		table = new JTable(tableModel);
		table.setDefaultRenderer(Object.class, new GrayedRenderer());

		JScrollPane scrollpane = new JScrollPane(table);
		scrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		dataPanel.add(scrollpane);

		return dataPanel;
	}

	public String getData(int rowIdx, int colIdx, int numberOfCols) {
		String ret = "";
		BufferedReader reader = getData();
		int skippedHeaderLines = rowIdx;
		int i = 0;
		String redLine = "";
		while (i < skippedHeaderLines) {
			try {
				redLine = reader.readLine();
			} catch (IOException e) {
				return "";
			}
			i++;
		}

		try {
			redLine = reader.readLine();
		} catch (IOException e) {
		}
		String delimiter = getSeparators();
		StringTokenizer tokenizer = new StringTokenizer(redLine, delimiter);
		int col = 0;
		int tokensNo = tokenizer.countTokens();
		while (col < tokensNo && col < colIdx) {
			tokenizer.nextToken();
			col++;
		}

		if (colIdx == numberOfCols - 1) {
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				ret += (ret.equals("") ? token : delimiter + token);
			}
		} else {
			if (tokenizer.hasMoreTokens())
				ret = tokenizer.nextToken();
		}
		return ret;
	}

	public int getNumberOfLines() {
		int linesNo = 0;

		BufferedReader reader = getData();

		while (true) {
			String redLine = "";
			try {
				redLine = reader.readLine();
			} catch (IOException e) {
				return 0;
			}
			if (redLine != null) {
				linesNo++;
			} else {
				break;
			}
		}
		return linesNo;
	}

	private BufferedReader prepareReader() {
		try {
			return new BufferedReader(new FileReader(fileToOpen.getText()));
		} catch (FileNotFoundException e) {
		}
		return new BufferedReader(new StringReader("\n"));
	}

	public int getNumberOfCols() {
		int linesNo = 0;

		BufferedReader reader = getData();
		int skippedHeaderLines = getSkippedLines();
		int i = 0;
		String redLine = "";
		while (i < skippedHeaderLines) {
			try {
				redLine = reader.readLine();
			} catch (IOException e) {
				return 0;
			}
			if (redLine != null) {
				linesNo++;
			} else {
				break;
			}
			i++;
		}

		try {
			redLine = reader.readLine();
		} catch (IOException e) {
			return 0;
		}

		String delimiter = getSeparators();
		StringTokenizer tokenizer = new StringTokenizer(redLine, delimiter);

		int countTokens = tokenizer.countTokens();
		return countTokens;
	}

	private JPanel buildButtonPanel() {
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());

		JButton exitButton = new JButton(getExitAction());
		buttonPanel.add(exitButton);

		JButton convertButton = new JButton(getConvertAction());
		buttonPanel.add(convertButton);

		return buttonPanel;
	}

	private JPanel buildOperationsPanel() {
		String[] labels = { "Skip: ", "Swap: ", "Join: " };
		Action[] actions = { getSkipColumnActionAction(), getSwapColumnsActionAction(), getConcatenateColumnsActionAction() };
		int numPairs = labels.length;

		JPanel optionsPanel = new JPanel(new SpringLayout());

		// Create and populate the panel.
		for (int i = 0; i < numPairs; i++) {
			if (i < actions.length)
				optionsPanel.add(new JButton(actions[i]));
			else
				optionsPanel.add(new JLabel());
		}

		// Lay out the panel.
		SpringUtilities.makeCompactGrid(optionsPanel, numPairs, 1, 6, 6, 6, 6);

		return optionsPanel;
	}

	private JPanel buildOptionsPanel() {

		String[] labels = { "Source file: ", "Destination file: ", "Separators: ", "Skipped header: ", "Skipped footer: " };
		Action[] actions = { getFileToConvertAction(), getSelectPathToConvertedFileAction() };
		Action[] secondaryActions = { getBrowseFileAction(), null, null, null };

		int numPairs = labels.length;

		JPanel optionsPanel = new JPanel(new SpringLayout());

		fileToOpen = new JTextField(10);
		fileToOpen.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent arg0) {
				updateData();
				boolean bothFilesSelected = !(fileToOpen.getText().equals("") || fileToSave.getText().equals(""));
				getConvertAction().setEnabled(bothFilesSelected);
			}

			@Override
			public void focusGained(FocusEvent arg0) {
			}
		});
		addOption(labels, actions, optionsPanel, 0, fileToOpen, secondaryActions);

		fileToSave = new JTextField(10);
		fileToSave.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent arg0) {
				updateData();
				boolean bothFilesSelected = !(fileToOpen.getText().equals("") || fileToSave.getText().equals(""));
				getConvertAction().setEnabled(bothFilesSelected);
			}

			@Override
			public void focusGained(FocusEvent arg0) {
			}
		});
		addOption(labels, actions, optionsPanel, 1, fileToSave, secondaryActions);

		separators = new JTextField(10);
		separators.setText(",");
		separators.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent arg0) {
				updateData();
			}

			@Override
			public void focusGained(FocusEvent arg0) {
			}
		});

		addOption(labels, actions, optionsPanel, 2, separators, secondaryActions);

		skippedHeader = new JTextField(3);
		skippedHeader.setText("0");
		skippedHeader.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent arg0) {
				try {
					int skipedHeaders = Integer.valueOf(skippedHeader.getText());
					if (skipedHeaders >= getNumberOfLines()) {
						skippedHeader.setText("0");
						wrongParametersMessage("Number of skipped header lines shall be lesserer than number of lines");
					}
					else {
						skippedHeader.setText(String.valueOf(skipedHeaders));
						updateData();
					}
				}
				catch (NumberFormatException e) {
					skippedHeader.setText("0");
				}
			}

			@Override
			public void focusGained(FocusEvent arg0) {
			}
		});

		addOption(labels, actions, optionsPanel, 3, skippedHeader, secondaryActions);

		skippedFooter = new JTextField(3);
		skippedFooter.setText("0");
		skippedFooter.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent arg0) {
				try {
					int skipedFooters = Integer.valueOf(skippedFooter.getText());
					skippedFooter.setText(String.valueOf(skipedFooters));
					updateData();
				}
				catch (NumberFormatException e) {
					skippedFooter.setText("0");
				}
			}

			@Override
			public void focusGained(FocusEvent arg0) {
			}
		});
		addOption(labels, actions, optionsPanel, 4, skippedFooter, secondaryActions);

		SpringUtilities.makeCompactGrid(optionsPanel, numPairs, 4, 6, 6, 6, 6);
		return optionsPanel;
	}

	private void addOption(String[] labels, Action[] actions, JPanel optionsPanel, int option, JTextField textField, Action[] secondaryActions) {
		String labelText = labels[option];
		JLabel l = new JLabel(labelText, JLabel.TRAILING);
		optionsPanel.add(l);
		l.setLabelFor(textField);
		optionsPanel.add(textField);

		if (option < actions.length) {
			Action action = actions[option];
			optionsPanel.add(new JButton(action));
		} else {
			JButton invisibleButton = new JButton("Invisible");
			invisibleButton.setVisible(false);
			optionsPanel.add(invisibleButton);
		}
		if (option < secondaryActions.length && secondaryActions[option] != null ) {
			Action action = secondaryActions[option];
			optionsPanel.add(new JButton(action));
		} else {
			JButton invisibleButton = new JButton("Invisible");
			invisibleButton.setVisible(false);
			optionsPanel.add(invisibleButton);
		}
	}

	protected void conversionErrorMessage(String message) {
		JOptionPane.showMessageDialog(MainWindow.this, message, "Conversion Operation Error", JOptionPane.ERROR_MESSAGE);
	}

	protected void wrongParametersMessage(String message) {
		JOptionPane.showMessageDialog(MainWindow.this, message, "Wrong parameters", JOptionPane.ERROR_MESSAGE);
	}

	protected void updateData() {
		table.setModel(new CVSDataTableModel());
	}

	private BufferedReader getData() {
		BufferedReader reader = prepareReader();
		return reader;
	}

	private String getSeparators() {
		String separatorStr = separators.getText();
		if (separatorStr.equals(""))
			return ";";
		return separatorStr;
	}

	private Integer getSkippedLines() {
		try {
			int i = Integer.valueOf(skippedHeader.getText());
			return i;
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private int getSkippedFooterLines() {
		try {
			int i = Integer.valueOf(skippedFooter.getText());
			return i;
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public void convert(BufferedReader reader, BufferedWriter writer) throws IOException {
		// TODO
		int linesNo = getNumberOfLines();

		int i = 0;
		while (true) {
			String redLine = reader.readLine();
			if (redLine != null) {
				if (i >= options.getNumberOfSkippedHederLines()) {
					if (i < linesNo - options.getNumberOfSkippedFooterLines()) {
						String newLine = "";
						if (options.getNewDelimiter().equals(options.getOriginalDelimiter()) && ! hasChangeColumns()) {
							newLine = redLine;
						} else {
							int j = 0;
							if (hasChangeColumns()) {
								String changedLine = redLine;
								List<OperationInfo> infos = getOperationInfos();
								for (OperationInfo operationInfo : infos) {
									switch (operationInfo.getOperationCode()) {
									case 1: {
										StringTokenizer seekTokenizer = new StringTokenizer(changedLine, options.getOriginalDelimiter());
										StringTokenizer concatenateTokenizer = new StringTokenizer(changedLine, options.getOriginalDelimiter());
										// seek for tokens
										String firstToken = "";
										String secondToken = "";
										j = 0;
										while (seekTokenizer.countTokens() > 0) {
											String token = seekTokenizer.nextToken();
											if (j == operationInfo.getIdxOfFirstColumn()) {
												firstToken = token;
											}
											if (j == operationInfo.getIdxOfSecondColumn()) {
												secondToken = token;
											}
											j++;
										}
										// print out tokens
										changedLine = "";
										j = 0;
										while (concatenateTokenizer.countTokens() > 0) {
											String token = concatenateTokenizer.nextToken();
											if (j == operationInfo.getIdxOfFirstColumn()) {
												changedLine += firstToken + operationInfo.getSeparatorRepleacement() + secondToken + options.getOriginalDelimiter();
											} else if (j == operationInfo.getIdxOfSecondColumn()) {
												;
											} else {
												changedLine += token + options.getOriginalDelimiter();
											}
											j++;
										}
									}
										break;
									case 2: {
										StringTokenizer swapTokenizer = new StringTokenizer(changedLine, options.getOriginalDelimiter());
										StringTokenizer concatenateTokenizer = new StringTokenizer(changedLine, options.getOriginalDelimiter());
										// seek for tokens
										String firstToken = "";
										String secondToken = "";
										j = 0;
										while (swapTokenizer.countTokens() > 0) {
											String token = swapTokenizer.nextToken();
											if (j == operationInfo.getIdxOfFirstColumn()) {
												firstToken = token;
											}
											if (j == operationInfo.getIdxOfSecondColumn()) {
												secondToken = token;
											}
											j++;
										}
										// print out tokens
										changedLine = "";
										j = 0;
										while (concatenateTokenizer.countTokens() > 0) {
											String token = concatenateTokenizer.nextToken();
											if (j == operationInfo.getIdxOfFirstColumn()) {
												changedLine += secondToken + options.getOriginalDelimiter();
											} else if (j == operationInfo.getIdxOfSecondColumn()) {
												changedLine += firstToken + options.getOriginalDelimiter();
											} else {
												changedLine += token + options.getOriginalDelimiter();
											}
											j++;
										}

									}
										break;
									case 3: {
										StringTokenizer skipTokenizer = new StringTokenizer(changedLine, options.getOriginalDelimiter());
										// print out tokens
										changedLine = "";
										j = 0;
										while (skipTokenizer.countTokens() > 0) {
											String token = skipTokenizer.nextToken();
											if (j == operationInfo.getIdxOfFirstColumn()) {
												// nop
											} else if (j == operationInfo.getIdxOfSecondColumn()) {
												// nop
											} else {
												changedLine += token + options.getOriginalDelimiter();
											}
											j++;
										}

									}
										break;
									}
								}

								StringTokenizer st2 = new StringTokenizer(changedLine, options.getOriginalDelimiter());
								while (st2.countTokens() > 0) {
									newLine += st2.nextToken() + options.getNewDelimiter();
								}
							} else {
								StringTokenizer st = new StringTokenizer(redLine, options.getOriginalDelimiter());
								while (st.countTokens() > 0) {
									newLine += st.nextToken() + options.getNewDelimiter();
								}
							}
						}
						System.out.println(newLine);
						if (i == linesNo - 1 - options.getNumberOfSkippedFooterLines())
							writer.write(newLine);
						else
							writer.write(newLine + "\n");
					}
				}
				i++;
			} else {
				break;
			}
		}
		writer.close();
		reader.close();
	}

	protected void setSkipColumn(int i) {
	}

	protected void setFirstColumnToConctenate(int i) {
	}

	protected void setSecondColumnToConcatenate(int i) {
	}

	protected void setSepar1ationString(String nextToken) {
	}

	protected void setSecondColumnToSwap(int i) {
	}

	protected void setFirstColumnToSwap(int i) {
	}

	public void setOperationInfos(List<OperationInfo> operationInfos) {
		this.operationInfos = operationInfos;
	}

	public List<OperationInfo> getOperationInfos() {
		return this.operationInfos;
	}

	public boolean hasChangeColumns() {
		return getOperationInfos().size() > 0;
	}

	public void concatenateColumns(int idxOfFirstColumn, int idxOfSecondColumn, String separatorRepleacement) {
		getOperationInfos().add( new OperationInfo(1, idxOfFirstColumn, idxOfSecondColumn, separatorRepleacement ));
	}

	public void swapColumns(int i, int j) {
		getOperationInfos().add( new OperationInfo(2, i, j, null ));
	}

	public void skipColumns(int i) {
		getOperationInfos().add( new OperationInfo(3, i, -1, null ));
	}

}
