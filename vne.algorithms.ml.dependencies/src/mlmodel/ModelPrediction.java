package mlmodel;

import transform.encoding.AbstractEncoding;
import transform.label.AbstractEmbeddingLabel;
import transform.label.AbstractFeasableLabel;

public class ModelPrediction {
	private String path = "/export";
	private AbstractEncoding inEncoding = null;
	private AbstractEmbeddingLabel embeddingLabel = null;
	private AbstractFeasableLabel feasableLabel = null;
	boolean standardizeInputVector = true;

	public ModelPrediction() {
		super();
	}

	public String getPath() {
		return path;
	}

	public void setInEncoding(AbstractEncoding in) {
		this.inEncoding = in;
	}

	public AbstractEncoding getInEncoding() {
		return inEncoding;
	}

	public boolean isStandardize() {
		return standardizeInputVector;
	}

	public void setStandardizeInputVector(boolean standardize) {
		this.standardizeInputVector = standardize;
	}

	public AbstractEmbeddingLabel getEmbeddingLabel() {
		return embeddingLabel;
	}

	public void setEmbeddingLabel(AbstractEmbeddingLabel modelResult) {
		this.embeddingLabel = modelResult;
	}

	public AbstractFeasableLabel getFeasableLabel() {
		return feasableLabel;
	}

	public void setFeasableLabel(AbstractFeasableLabel feasableLabel) {
		this.feasableLabel = feasableLabel;
	}

}
