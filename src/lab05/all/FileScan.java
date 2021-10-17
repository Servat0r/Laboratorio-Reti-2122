package lab05.all;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public final class FileScan {
	private static final String files_out_path = "files.txt";
	private static final String dirs_out_path = "directories.txt";
	private final String dirpath;
	private FileOutputStream files_stream;
	private FileOutputStream dirs_stream;
	
	public FileScan(String filepath) {
		this.dirpath = filepath;
		this.files_stream = null;
		this.dirs_stream = null;
	}
	
	public void closeStreams() throws IOException {
		if (this.files_stream != null) {
			this.files_stream.close();
			this.files_stream = null;
		}
		if (this.dirs_stream != null) {
			this.dirs_stream.close();
			this.dirs_stream = null;
		}
	}
	
	public boolean scan() throws IOException {
		List<File> directories = new LinkedList<File>();
		List<File> files = new LinkedList<File>();
		File dir = new File(this.dirpath);
		byte[] fbytes;
		if (!dir.isDirectory()) {
			System.err.printf("'%s' is not a directory%n", this.dirpath);
			return false;
		}
		File files_out = new File(files_out_path);
		File dirs_out = new File(dirs_out_path);
		files_out.createNewFile();
		dirs_out.createNewFile();
		this.files_stream = new FileOutputStream(files_out);
		this.dirs_stream = new FileOutputStream(dirs_out);
		directories.add(dir);
		while (!directories.isEmpty()) {
			dir = directories.remove(0);
			fbytes = dir.getAbsolutePath().getBytes();
			dirs_stream.write(fbytes);
			fbytes = "\n".getBytes();
			dirs_stream.write(fbytes);
			System.out.printf("DIRECTORY FOUND: '%s'%n", dir.getAbsolutePath());
			File[] content = dir.listFiles(); //Files and subdirs together
			for (File f : content) {
				if (f.isDirectory()) directories.add(f); //La directory corrente NON è inclusa in quelle
				//restituite da listFiles()
				else files.add(f);
			}
		}
		for (File f : files) {
			fbytes = f.getAbsolutePath().getBytes();
			files_stream.write(fbytes);
			fbytes = "\n".getBytes();
			files_stream.write(fbytes);
			System.out.printf("FILE FOUND: '%s'%n", f.getPath());
		}
		this.closeStreams();
		System.out.println("Finished");
		return true;
	}
	
	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("Provide file path!");
			System.exit(1);
		}
		FileScan fs = new FileScan(args[0]);
		boolean result;
		try {
			result = fs.scan();
			if (!result) System.exit(1);
		} catch (IOException ioe1) {
			ioe1.printStackTrace();
			try {
				fs.closeStreams();
			} catch (IOException ioe2) {
				ioe2.printStackTrace();
				System.exit(1);
			}
			System.exit(1);
		}
	}	
}