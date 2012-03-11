import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class AutoBurn implements IAutoBurn {

	private static AutoBurn instance;

	public static AutoBurn getInstance() {
		if (instance == null) {
			instance = new AutoBurn();
		}
		return instance;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		getInstance().loadConfig();
		getInstance().addFilesFromFolder();
		getInstance().sortFile();
		getInstance().burn();

	}

	private String burnerPath;
	private List<MP3> fileList;
	private List<MP3> finalList;

	public List<MP3> getFinalList() {
		return finalList;
	}

	public void setFinalList(List<MP3> finalList) {
		this.finalList = finalList;
	}

	private List<String> folderList;

	private Integer supportLength;

	private Integer total;

	private AutoBurn() {
		super();
	}

	/**
	 * Liste de tous les fichiers contenus dans les repertoires
	 */
	public void addFilesFromFolder() {

		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith("mp3");
			}
		};

		Iterator<String> iter = getFolderList().iterator();
		while (iter.hasNext()) {
			String folder = iter.next();
			File[] listFile = new File(folder).listFiles(filter);
			for (int i = 0; i < listFile.length; i++) {
				File file = listFile[i];

				getFileList().add(
						new MP3(file.getAbsolutePath(), AudioFileUtility
								.getMp3Size(file.getAbsolutePath())));
				System.out.println("Detect " + file.getAbsolutePath());

			}

		}
	}

	/***
	 * Grave le cd
	 * 
	 * @throws IOException
	 */
	public void burn() {
		List<String> commandList = new ArrayList<String>();
		List<MP3> copyList = new ArrayList<MP3>();
		String tmpDir = System.getProperty("java.io.tmpdir");
		File tmpFile;
		File dest;
		try {
			for (MP3 mp3 : getFinalList()) {
				tmpFile = new File(mp3.getPath());
				dest = new File(tmpDir + "\\"
						+ tmpFile.getName().replace(" ", ""));
				Files.copy(tmpFile.toPath(), dest.toPath(),
						StandardCopyOption.REPLACE_EXISTING);

				copyList.add(new MP3(dest.getAbsolutePath(), mp3.getLength()));
			}

			String commandLine = "";
			commandLine = burnerPath + ERASE + "\n";
			commandList.add(commandLine);
			commandLine = burnerPath + EJECT + "\n";
			commandList.add(commandLine);
			commandLine = burnerPath + LOAD + "\n";
			commandList.add(commandLine);
			commandLine = burnerPath + BURN_AUDIO;

			for (MP3 mp3 : copyList) {
				commandLine = commandLine + ADD_FILE + "\""
						+ mp3.getPath().replace("/", "\\") + "\" ";
			}

			commandList.add(commandLine + "\n\r");
			commandLine = burnerPath + EJECT + "\n";
			commandList.add(commandLine);

			for (MP3 mp3 : copyList) {
				commandLine = REMOVE + "\"" + mp3.getPath().replace("/", "\\")
						+ "\"\n";
				commandList.add(commandLine);

			}

			commandLine = "pause" + "\"\n";
			commandList.add(commandLine);

			for (MP3 mp3 : getFinalList()) {
				commandLine = REMOVE + "\"" + mp3.getPath().replace("/", "\\")
						+ "\"\n";
				commandList.add(commandLine);

			}

			BufferedWriter sortie = null;
			try {
				sortie = new BufferedWriter(new FileWriter(BAT, false));
				for (String string : commandList) {
					sortie.write(string);
				}

				sortie.close();

			} catch (Exception ex) {
				System.out.println(ex.toString() + "\n\r");
			}
		} catch (IOException e) {
			System.out.println(e.toString() + "\n\r");
		}
		/*
		 * Runtime r = Runtime.getRuntime(); try { r.exec(BAT);
		 * 
		 * } catch (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */

	}

	private int fillList(MP3 mp3, Integer length) {
		int result = length;

		if (mp3.getLength() <= length && !mp3.getUsed()) {
			getFinalList().add(mp3);
			mp3.setUsed(true);
			result = length - mp3.getLength();

			System.out.println("	Ajout de " + mp3.getPath() + " "
					+ mp3.getLength() / 60 + " mn");
		}

		return result;

	}

	public String getBurnerPath() {
		return burnerPath;
	}

	public List<MP3> getFileList() {
		return fileList;
	}

	public List<String> getFolderList() {
		return folderList;
	}

	public int getSupportLength() {
		return supportLength;
	}

	private boolean isExistingFile(String name) {
		return new File(name).exists();
	}

	public void loadConfig() {
		fileList = new ArrayList<MP3>();
		folderList = new ArrayList<String>();
		finalList = new ArrayList<MP3>();

		File file = new File(CONFIG_NAME);

		Properties prop = new Properties();
		FileInputStream in;
		try {
			in = new FileInputStream(file);

			prop.load(in);
			in.close();

			for (int i = 0; i < MAX_FILE_FOLDER; i++) {
				if (prop.get(PREFIX_FILE + i) != null) {
					if (isExistingFile(String
							.valueOf(prop.get(PREFIX_FILE + i)))) {
						getFileList().add(
								new MP3(String.valueOf(prop
										.get(PREFIX_FILE + i)),
										AudioFileUtility.getMp3Size(String
												.valueOf(prop.get(PREFIX_FILE
														+ i)))));
					}
				}

				if (prop.get(PREFIX_FOLDER + i) != null) {
					if (isExistingFile(String.valueOf(prop.get(PREFIX_FOLDER
							+ i)))) {
						getFolderList().add(
								String.valueOf(prop.get(PREFIX_FOLDER + i)));
					}
				}

			}

			burnerPath = String.valueOf(prop.get(PATH));
			supportLength = Integer.valueOf(String.valueOf(prop
					.get(SUPPORT_LENGTH))) * 60;

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void setBurnerPath(String burnerPath) {
		this.burnerPath = burnerPath;
	}

	public void setFileList(List<MP3> fileList) {
		this.fileList = fileList;
	}

	public void setFolderList(List<String> folderList) {
		this.folderList = folderList;
	}

	public void setSupportLength(int supportLength) {
		this.supportLength = supportLength;
	}

	public void sortFile() {
		Collections.sort(getFileList());

		int length = supportLength;
		for (MP3 mp3 : getFileList()) {

			length = fillList(mp3, length);
		}

	}

}
