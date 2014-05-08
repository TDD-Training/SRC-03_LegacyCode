package welc.dependencies.csvconverter;

import java.util.ArrayList;

public class CSVOptions {

	private int skippedLinedOfHeader;
	private int skippedLinedOfFooter;
	private String originalDelimiter;
	private String newDelimiter;
	MainWindow mainWindow;

	public CSVOptions(MainWindow mainWindow) {
		this(mainWindow, 0, 0, "", "");
	}
	
	public CSVOptions(MainWindow mainWindow, int skippedLinedOfHeader, int skippedLinedOfFooter, String originalDelimiter, String newDelimiter) {
		this.skippedLinedOfHeader = skippedLinedOfHeader;
		this.skippedLinedOfFooter = skippedLinedOfFooter;
		this.originalDelimiter  = originalDelimiter;
		this.newDelimiter  = newDelimiter;
		this.mainWindow = mainWindow;
		mainWindow.setOperationInfos( new ArrayList<OperationInfo>() );
	}

	public String getOriginalDelimiter() {
		return originalDelimiter;
	}

	public String getNewDelimiter() {
		return newDelimiter;
	}
	
	public int getNumberOfSkippedHederLines() {
		return skippedLinedOfHeader;
	}

	public int getNumberOfSkippedFooterLines() {
		return skippedLinedOfFooter;
	}

}
