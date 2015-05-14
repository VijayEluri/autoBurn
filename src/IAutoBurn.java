public interface IAutoBurn {
	public final String CONFIG_NAME = "config.properties";
	public final Integer MAX_FILE_FOLDER = 30;
	public final String PREFIX_FILE = "file";
	public final String PREFIX_FOLDER = "folder";
	public final String PATH = "path";
	public final String DEVICE = "device";
	public final String SUPPORT_LENGTH = "supportLength";

	public final String LOAD = "cdbxpcmd.exe --load";
	public final String EJECT = "cdbxpcmd.exe --eject";
	public final String ADD_FILE = "-file[\\]:";
	public final String REMOVE = "del /S /Q ";
	public final String BURN_AUDIO = "cdbxpcmd.exe --burn-audio ";
	public final String ERASE = "cdbxpcmd.exe --erase ";
	public final String BAT = "burn.bat ";

}
