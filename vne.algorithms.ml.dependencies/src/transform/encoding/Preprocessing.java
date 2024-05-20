package transform.encoding;

import java.util.ArrayList;

import generator.FileUtil;

public class Preprocessing {
	private static String exportedModelPath = "./../models/export";

	public Preprocessing() {

	}

	public static float[] standardize(float[] values) {
		ArrayList<Float> means = FileUtil.getVectorFromFile(exportedModelPath + "/means.txt");
		ArrayList<Float> standDeviation = FileUtil.getVectorFromFile(exportedModelPath + "/standDeviations.txt");

		for (int i = 0; i < values.length; i++) {
			float z = (values[i] - means.get(i)) / standDeviation.get(i);
			values[i] = z;
		}
		return values;
	}

}
