package welc.dependencies.csvconverter;
import java.io.IOException;


public class FileBrowserHelper {


	public void browseFileInExternalWindow(String path) {
		String command = constructBrowseOSCommand(path);
		executeCommand(command);
	}

	private void executeCommand(String command)  {
		try {
			Runtime rt = Runtime.getRuntime();
			rt.exec( command );
		} catch (IOException e) {
			throw new Error(e.getCause());
		}
	}

	private String constructBrowseOSCommand(String path) {
		String selectedBrowser = selectBrowser();
		String readOnlyOptions = selectReadOnlyOptions();
		return selectedBrowser + " " + readOnlyOptions + " " + path;
	}

	private String selectReadOnlyOptions() {
		return SystemProperties.isRunningUnderWindows() ? "" : "-R";
	}

	private String selectBrowser() {
		return SystemProperties.isRunningUnderWindows() ? SystemProperties.getWindowBrowser() : SystemProperties.getLinuxBrowser();
	}

}
