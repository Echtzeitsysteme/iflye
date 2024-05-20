package transform.encoding;

import org.apache.commons.lang3.ArrayUtils;

import matrix.Matrix;
import matrix.ResourceMatrix;

public class NodeAttributesEncoding extends AbstractEncoding {
	private static boolean verbose = true;
	private boolean normalize = false;
	private static boolean normalizeWithSubstrateValues = false;
	
	/*
	 * Hard-coded values copied from custom scenario file.
	 */
	public int substrateCpu = 32;
	public int substrateMem = 512;
	public int substrateSto = 1000;
	public int virtualCpuMax = 32;
	public int virtualMemMax = 511;
	public int virtualStoMax = 300;

	private float[] concatenateAllNodeRessources(String sNetId, String vNetId) {
		ResourceMatrix subMatrix = new ResourceMatrix(new String[] { sNetId });
		float[] subCpu = subMatrix.getCpuValues();
		float[] subMem = subMatrix.getMemValues();
		float[] subStorage = subMatrix.getStorageValues();

		ResourceMatrix virtMatrix = new ResourceMatrix(new String[] { vNetId });
		float[] virtCpu = virtMatrix.getCpuValues();
		float[] virtMem = virtMatrix.getMemValues();
		float[] virtStorage = virtMatrix.getStorageValues();

		if (verbose) {
			subMatrix.print();
			virtMatrix.print();
		}

		if (normalize) {
			subCpu = Matrix.normalizeArray(subCpu, 0, substrateCpu);
			subMem = Matrix.normalizeArray(subMem, 0, substrateMem);
			subStorage = Matrix.normalizeArray(subStorage, 0, substrateSto);

			if (normalizeWithSubstrateValues == true) {
				virtCpu = Matrix.normalizeArray(virtCpu, 0, substrateCpu);
				virtMem = Matrix.normalizeArray(virtMem, 0, substrateMem);
				virtStorage = Matrix.normalizeArray(virtStorage, 0, substrateSto);
			} else {
				virtCpu = Matrix.normalizeArray(virtCpu, 0, virtualCpuMax);
				virtMem = Matrix.normalizeArray(virtMem, 0, virtualMemMax);
				virtStorage = Matrix.normalizeArray(virtStorage, 0, virtualStoMax);
			}
		}

		float[] substrateNodes = ArrayUtils.addAll(ArrayUtils.addAll(subCpu, subMem), subStorage);
		// every virtual node has the same values so only include first
		float[] virtNodes = new float[] { virtCpu[0], virtMem[0], virtStorage[0] };
		return ArrayUtils.addAll(substrateNodes, virtNodes);
	}

	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}

	@Override
	public float[] get(String sNetId, String vNetId) {
		System.out.println("NodeAttributesEncoding::get with " + sNetId + ", " + vNetId);
		return this.concatenateAllNodeRessources(sNetId, vNetId);
	}

}
