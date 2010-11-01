import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class AutoBurn implements IAutoBurn {

	private static AutoBurn instance;

	private List<String> fileList;
	private List<String> finalList;
	private Map<String, Integer> fileLength;
	private List<String> folderList;
	private String burnerPath;
	private Integer supportLength;

	private Integer total;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		getInstance().loadConfig();
		getInstance().addFilesFromFolder();
		getInstance().findLenght();
		getInstance().sortFile();
		getInstance().burn();

	}

	private AutoBurn() {
		super();
	}

	/**
 * 
 */
	public void sortFile() {
		total = 0;
		List<String> finalListTemp = new ArrayList<String>();
		finalList = new ArrayList<String>();

		Iterator<String> iter = fileList.iterator();
		while (iter.hasNext()) {
			int i = 0;
			boolean ok = false;
			String string = (String) iter.next();
			if (fileLength.get(string) <= supportLength) {
				Iterator<String> iter2 = finalListTemp.iterator();
				while (iter2.hasNext()) {
					String string2 = (String) iter2.next();
					if (fileLength.get(string) <= fileLength.get(string2)) {
						finalListTemp.add(i, string);
						ok = true;
						break;
					}
					i++;
					if (ok) {
						finalListTemp.add(string);
					}
				}

			} else {
				System.out.println(string + " trop grand");
			}

		}

		while (fileList.size() > 0) {
			if (total + fileLength.get(fileList.get(fileList.size() - 1))
					+ fileLength.get(fileList.get(0)) > supportLength) {
				fileList.remove(fileList.size() - 1);
			} else {
				finalList.add(fileList.get(fileList.size() - 1));
				total = total
						+ fileLength.get(fileList.get(fileList.size() - 1));
				fileList.remove(fileList.size() - 1);
			}
		}
	}

	/***
	 * Grave le cd
	 */
	public void burn() {
		List<String> commandList = new ArrayList<String>();

		String commandLine = "";
		commandLine = burnerPath + ERASE + "\n";
		commandList.add(commandLine);
		commandLine = burnerPath + EJECT + "\n";
		commandList.add(commandLine);
		commandLine = burnerPath + LOAD + "\n";
		commandList.add(commandLine);
		commandLine = burnerPath + BURN_AUDIO;

		Iterator<String> iter = finalList.iterator();
		while (iter.hasNext()) {
			String string = (String) iter.next();
			System.out.println("*" + string);
			commandLine = commandLine + ADD_FILE +"\""+ string.replace("/", "\\")
					+ "\" ";

		}
		commandList.add(commandLine + "\n\r");
		commandLine = burnerPath + EJECT + "\n";
		commandList.add(commandLine);

		iter = finalList.iterator();
		while (iter.hasNext()) {
			String string = (String) iter.next();
			System.out.println("*" + string);
			commandLine = REMOVE + "\"" + string.replace("/", "\\") + "\"\n";
			commandList.add(commandLine);

		}

		System.out.println(total + "/" + supportLength);

		BufferedWriter sortie = null;
		try {
			sortie = new BufferedWriter(new FileWriter(BAT, false));
			iter = commandList.iterator();
			while (iter.hasNext()) {
				String string = (String) iter.next();

				sortie.write(string);

			}
			sortie.close();

		} catch (Exception ex) {
			System.out.println(ex.toString() + "\n\r");
		}

	/*	Runtime r = Runtime.getRuntime();
		try {
			r.exec(BAT);

		} catch (IOException e) { // TODO Auto-generated catch block
			e.printStackTrace();
		}*/

	}

	/**
	 * Tableau Fichier/taille
	 */
	public void findLenght() {
		int size;
		fileLength = new HashMap<String, Integer>();
		Iterator<String> iter = getFileList().iterator();
		while (iter.hasNext()) {
			String file = (String) iter.next();
			size = AudioFileUtility.getMp3Size(file);
			if (size != 0) {
				fileLength.put(file, size);
				System.out.println(file + " " + size);
			}

		}
	}

	public static AutoBurn getInstance() {
		if (instance == null) {
			instance = new AutoBurn();
		}
		return instance;
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
			String folder = (String) iter.next();
			File[] listFile = new File(folder).listFiles(filter);
			for (int i = 0; i < listFile.length; i++) {
				File file = listFile[i];
				getFileList().add(file.getAbsolutePath());

			}

		}
	}

	public List<String> getFileList() {
		return fileList;
	}

	public void setFileList(List<String> fileList) {
		this.fileList = fileList;
	}

	public List<String> getFolderList() {
		return folderList;
	}

	public void setFolderList(List<String> folderList) {
		this.folderList = folderList;
	}

	public String getBurnerPath() {
		return burnerPath;
	}

	public void setBurnerPath(String burnerPath) {
		this.burnerPath = burnerPath;
	}

	public void loadConfig() {
		fileList = new ArrayList<String>();
		folderList = new ArrayList<String>();

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
						fileList.add(String.valueOf(prop.get(PREFIX_FILE + i)));
					}
				}

				if (prop.get(PREFIX_FOLDER + i) != null) {
					if (isExistingFile(String.valueOf(prop.get(PREFIX_FOLDER
							+ i)))) {
						folderList.add(String.valueOf(prop.get(PREFIX_FOLDER
								+ i)));
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

	public Map<String, Integer> getFileLength() {
		return fileLength;
	}

	public void setFileLength(Map<String, Integer> fileLength) {
		this.fileLength = fileLength;
	}

	public int getSupportLength() {
		return supportLength;
	}

	public void setSupportLength(int supportLength) {
		this.supportLength = supportLength;
	}

	private boolean isExistingFile(String name) {
		return new File(name).exists();
	}

}
