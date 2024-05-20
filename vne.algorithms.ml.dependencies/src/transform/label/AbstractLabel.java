package transform.label;

import transform.encoding.AbstractEncoding;

public abstract class AbstractLabel extends AbstractEncoding {
	protected boolean verbose = false;
	protected int rows = -1;
	protected int columns = -1;

	protected void setExpectedResultDimensionality(int r, int c) {
		this.rows = r;
		this.columns = c;
	}

	protected void validateModelResultLength(int resultLength) {
		if (this.rows == -1 || this.columns == -1) {
			throw new IllegalArgumentException("Result dimension were not set");
		}
		int expectedLength = this.rows * this.columns;
		if (expectedLength != resultLength) {
			throw new IllegalArgumentException(
					"Result dimension mismatch; expected: " + expectedLength + ", actual: " + resultLength);
		}
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

}
