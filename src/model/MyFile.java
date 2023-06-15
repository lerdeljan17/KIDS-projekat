package model;

import app.AppConfig;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;

public class MyFile implements Serializable {

	private String name;
	private String content;
	private boolean directory;
		
	private int version;

	public MyFile(String name, String content, boolean directory) {
		super();
		this.name = name;
		this.directory = directory;
		this.content = content;
		this.version = 0;
	}

	public void setVersion(int version) {
		String newName = name.split("_")[0] + "_";
		if(directory) {
			newName += "dir";
			this.version = 0;
		} else {
			newName += version;
			this.version = version;
		}
		
		this.name = newName;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof MyFile)) {
			return false;
		}
		
		MyFile d = (MyFile) obj;
		
		return name.equals(d.name);
	}

	public String getName() {
		return name;
	}

	public String getContent() {
		return content;
	}

	public boolean isDirectory() {
		return directory;
	}
	
	public int getVersion() {
		return version;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return "Data{" +
				"name='" + name + '\'' +
				", content='" + content + '\'' +
				", directory=" + directory +
				", version=" + version +
				'}';
	}



	public static MyFile workingFileToMyFile(File file, String where) {

		String name = getDataName(file.getPath(), where);

		boolean directory = file.isDirectory();

		StringBuilder sb = new StringBuilder();
		String content = "";


		if(directory) {


			for(File f : file.listFiles()) {

				String fileName = getDataName(f.getPath(), where);

				sb.append(fileName);
				sb.append(",");
			}

			if(sb.length() > 0) {
				sb.deleteCharAt(sb.length()-1);
			}

			content = sb.toString();

		} else {

			try {
				content = new String(Files.readAllBytes(file.toPath()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return new MyFile(name, content, directory);
	}


	public static File myFileToStorageFile(MyFile myFile) {

		String filePath = AppConfig.myServentInfo.getStorage() + File.separator + myFile.getName();


		if(myFile.isDirectory() && !myFile.getName().split("_")[1].equals("dir")) {
			filePath = filePath.concat("_dir");
		}

		File file = new File(filePath);

		if(file.exists()) {
			AppConfig.timestampedErrorPrint("File " + myFile.getName() + " already exists on node " + AppConfig.myServentInfo.getChordId());
			return null;
		}

		boolean success = false;

		if(myFile.isDirectory()) {
			try {
				success = file.createNewFile();
			} catch (IOException e) {
				AppConfig.timestampedErrorPrint(e.getMessage() + "Errors" + Arrays.toString(e.getStackTrace()));
			}
		} else {

			try {
				int index = filePath.lastIndexOf(File.separator);
				String path = filePath.substring(0, index);

				File dirFile = new File(path);
				if(!dirFile.exists()) {
					dirFile.mkdirs();
				}

				success = file.createNewFile();
			} catch (Exception e) {
				AppConfig.timestampedErrorPrint("Error while writing file!");
				AppConfig.timestampedErrorPrint(e.getMessage() + "Error" + Arrays.toString(e.getStackTrace()));
			}

		}



		if(!success) {
			AppConfig.timestampedErrorPrint("File " + myFile.getName() + " could not be saved on node " + AppConfig.myServentInfo.getChordId());
			return null;
		}

		PrintWriter pw = null;
		try {
			pw = new PrintWriter(file);

			pw.write(myFile.getContent());

		} catch (FileNotFoundException e) {
			AppConfig.timestampedErrorPrint(e.getMessage() + "Error " + Arrays.toString(e.getStackTrace()));
		} finally {
			if(pw != null) pw.close();
		}

		return file;
	}

	public static File myFileToWorkingFile(MyFile myFile) {

		String filePath = AppConfig.myServentInfo.getWorkingDirectory() + File.separator + myFile.getName();


		File file = null;
		boolean flag = false;
		boolean directory = false;

		if(myFile.getName().contains("_dir") || myFile.isDirectory()) {
			directory = true;
		}

		if(directory) {
			filePath = filePath.split("_")[0];

			file = new File(filePath);
			flag = file.mkdir();
			if(!flag) {
				AppConfig.timestampedErrorPrint("Directory " + filePath + " could not be made ");
			} else {
				AppConfig.timestampedErrorPrint("Directory " + filePath + " is created!");
			}

		} else {

			file = new File(filePath);
			if(file.exists()) {
				AppConfig.timestampedErrorPrint("File " + filePath + " already exists!");
			} else {
				int index = filePath.lastIndexOf(File.separator);
				String path = filePath.substring(0, index);

				File dirFile = new File(path);
				if(!dirFile.exists()) {
					dirFile.mkdirs();
				}

				try {
					flag = file.createNewFile();
				} catch (IOException e) {
					AppConfig.timestampedErrorPrint(e.getMessage() + "Error" + Arrays.toString(e.getStackTrace()));
				}
				if(!flag) {
					AppConfig.timestampedErrorPrint("File " + filePath + " could not be made!");
				}
			}

			PrintWriter pw = null;
			try {
				pw = new PrintWriter(file);

				pw.write(myFile.getContent());

			} catch (FileNotFoundException e) {
				AppConfig.timestampedErrorPrint(e.getMessage() + "Error" + Arrays.toString(e.getStackTrace()));
			} catch (IOException e) {
				AppConfig.timestampedErrorPrint(e.getMessage() + "Error" + Arrays.toString(e.getStackTrace()));
			} finally {
				if(pw != null) {
					pw.close();
				}
			}

		}


		return file;
	}

	private static String getDataName(String filename, String where) {
		int index = (int) (where.length() + 1);
		return filename.substring(index);
	}



	public static void changeFileName(String name) {
		String parts[] = name.split("_");
		String fileName = AppConfig.myServentInfo.getWorkingDirectory() + File.separator  + parts[0];

		int version = -1;
		if(parts.length == 1) {
			version = 0;
		} else {
			if(parts[1].equals("dir")) {
				version = 0;
			} else {
				try {
					version = Integer.parseInt(parts[1]);
				} catch (NumberFormatException e) {
					AppConfig.timestampedErrorPrint(e.getMessage() + "Errors : " + Arrays.toString(e.getStackTrace()));
				}
			}
		}

		File file = new File(fileName);

		findRec(file, version);
	}

	private static void findRec(File file, int version) {
		String fileName = file.getPath().split("_")[0].concat("_" + version);

		try {
			if(!file.isDirectory()) {

				File newFile = new File(fileName);

				boolean b = file.renameTo(newFile);

			} else {
				if(file.isDirectory()) {
					for(File f : file.listFiles()) {
						findRec(f, version);
					}
				}
			}

		} catch (Exception e) {
			AppConfig.timestampedErrorPrint(e.getMessage() + "Error" + Arrays.toString(e.getStackTrace()));
		}
	}

	/**
	 * Prima Data ciji ce content da upise u neki fajl. Koristi se priliokm push resolve
	 * @param value vrednost koja se upisuje u fajl
	 */
	public static void pushToFile(MyFile value) {
		String filePath = AppConfig.myServentInfo.getStorage() + File.separator + value.getName();

		File file = new File(filePath);
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(file);

			pw.write(value.getContent());

		} catch (FileNotFoundException e) {
			AppConfig.timestampedErrorPrint(e.getMessage() + "Error" + Arrays.toString(e.getStackTrace()));
		} finally {
			if(pw != null) pw.close();
		}

	}

}
