package welc.dependencies.csvconverter;

public class SystemProperties {

	static final String LINUX_BROWSE = "konsole -e vi";//"xterm -e vi"; 
	static final String WINDOWS_BROWSE = "notepad.exe";
	
	public static boolean isRunningUnderWindows() {
		return System.getProperty("os.name").startsWith("Win");
	}

	public static String getWindowBrowser() {
		return WINDOWS_BROWSE;
	}

	public static String getLinuxBrowser() {
		return LINUX_BROWSE;
	}

}
