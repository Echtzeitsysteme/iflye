package generator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FileUtil {
	public static String outputDirectory = "./output";
	public static String filePrefix = "/instance";

	public FileUtil() {
	}

	public static void deleteFile(String path) {
		File file = new File(path);
		if (file.exists()) {
			file.delete();
		}
	}

	// deletes a directory and recreates it
	public static void renewDirectory(File dir) {
		if (dir.exists()) {
			for (File file : dir.listFiles()) {
				file.delete();
			}
			dir.delete();
		}
		dir.mkdir();
	}

	public static void persistVectorToFile(ArrayList<Float> vector, PrintWriter out) {
		float[] outVector = new float[vector.size()];
		for (int i = 0; i < vector.size(); i++) {
			outVector[i] = vector.get(i);
		}
		persistVectorToFile(outVector, out);
	}

	public static void persistVectorToFile(float[] vector, PrintWriter out) {
		System.out.println("FileUtil::persistVectorToFile");
		for (int i = 0; i < vector.length; i++) {
			out.write(String.valueOf(vector[i] + " "));
		}
		out.println();
	}

	public static Set<Integer> parseDataPointNumerationsFromFolder(String path) {
		System.out.println("FileUtil::parseDataPointNumerationsFromFileNames");
		File folder = new File(path);
		HashSet<String> files = new HashSet<String>();
		for (File file : folder.listFiles()) {
			if (file.isFile()) {
				files.add(file.getName());
			}
		}
		Pattern pattern = Pattern.compile(filePrefix.substring(1) + "[0-9]*.xmi");
		List<String> matching = files.stream().filter(pattern.asPredicate()).collect(Collectors.toList());

		HashSet<Integer> inputs = new HashSet<Integer>();
		for (String name : matching) {
			// strip string so only number is left
			inputs.add(Integer.decode(name.replaceAll("[^0-9]", "")));
		}

		// check both files exist
		Iterator<Integer> iterator = inputs.iterator();
		while (iterator.hasNext()) {
			Integer i = iterator.next();
			try {
				FileUtil.checkFileExist(path + filePrefix + i + "-solved.xmi");
			} catch (Exception e) {
				System.out.println("no label for instance " + i);
				iterator.remove();
			}
		}

		return inputs;
	}

	public static void checkFileExist(String path) throws Exception {
		File f = new File(path);
		if (!f.exists()) {
			throw new Exception("file does not exist: " + path);
		}
	}

	public static ArrayList<Float> getVectorFromFile(String filePath) {
		BufferedReader reader = null;
		ArrayList<Float> numbers = new ArrayList<Float>();
		try {
			reader = new BufferedReader(new FileReader(filePath));
			String input = null;
			while ((input = reader.readLine()) != null) {
				String nums[] = input.trim().split("\\s+");
				for (int i = 0; i < nums.length; i++) {
					numbers.add(Float.parseFloat(nums[i]));
				}
			}
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
		return numbers;
	}

	/*
	 * protected String getFileType(File file) { String type = ""; int i =
	 * file.getName().lastIndexOf('.'); if (i > 0) { type =
	 * file.getName().substring(i + 1); } return type; }
	 * 
	 * protected static void deleteEverythingInDirectory(File dir) { for (File file
	 * : dir.listFiles()) { file.delete(); } }
	 */

}
