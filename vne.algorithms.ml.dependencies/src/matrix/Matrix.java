package matrix;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.management.InvalidAttributeValueException;

public class Matrix implements IMatrix {
	protected int columns = -1;
	protected int rows = -1;
	protected float[][] matrix;
	protected int defaultValue = 0;
	// this width is being used if the matrix has no table header
	protected int defaultColumnWidth = 9;

	public Matrix() {
	}

	public Matrix(int rows, int columns) {
		this.rows = rows;
		this.columns = columns;
		this.initialize();
	}

	protected void initialize() {
		this.matrix = new float[this.rows][this.columns];
		for (int i = 0; i < this.rows; i++) {
			for (int j = 0; j < this.columns; j++) {
				this.matrix[i][j] = this.defaultValue;
			}
		}
	}

	protected String[] getTableHeader() {
		// default matrix has no table header
		return new String[] {};
	}

	protected int printTableHeader(String[] columns) {
		// int columnWidth =
		// Arrays.stream(columns).map(String::length).max(Integer::compareTo).get();
		int columnWidth = this.defaultColumnWidth;
		// print upper left dead space of table
		System.out.print(" ".repeat(columnWidth + 1));
		// print table header
		for (String entry : columns) {
			this.printValueInCell(entry, columnWidth);
		}
		System.out.println();
		return columnWidth;
	}

	// print the given content right aligned and adds padding on the left to account
	// for column width
	protected void printValueInCell(String value, int columnWidth) {
		// cut content if longer than column width
		String content = value.substring(0, Math.min(value.length(), this.defaultColumnWidth));
		// padding
		System.out.print(" ".repeat(this.defaultColumnWidth - content.length()));
		System.out.print(content + " ");
	}

	protected void printValueInCell(float value, int columnWidth) {
		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.setScale(2, RoundingMode.HALF_UP);
		this.printValueInCell(String.valueOf(bd.floatValue()), columnWidth);
	}

	public void print() {
		// this.printTableHeader(this.getTableHeader());
		for (int i = 0; i < this.rows; i++) {
			for (int j = 0; j < this.columns; j++) {
				this.printValueInCell(this.matrix[i][j], this.defaultColumnWidth);
			}
			System.out.println();
		}
	};

	// row wise
	public float[] flatten() {
		int total = this.rows * this.columns;
		float[] result = new float[total];

		for (int i = 0; i < this.rows; i++) {
			for (int j = 0; j < this.columns; j++) {
				result[i * this.columns + j] = this.matrix[i][j];
			}
		}
		return result;
	}

	public void unflatten(float[] vector) throws InvalidAttributeValueException {
		if (vector.length != (this.rows * this.columns)) {
			throw new InvalidAttributeValueException("vector does not fit to matrix dimensions");
		}

		for (int i = 0; i < vector.length; i++) {
			int row = i / this.columns;
			int column = i % this.columns;
			this.matrix[row][column] = vector[i];
		}
	}

	public static float[] normalizeArray(float[] values, float min, float max) {
		for (int i = 0; i < values.length; i++) {
			values[i] = (values[i] - min) / (max - min);
		}
		return values;
	}

	public float[] getColumnValues(int columnNumber) {
		float[] result = new float[this.rows];
		for (int i = 0; i < this.rows; i++) {
			result[i] = this.matrix[i][columnNumber];
		}
		return result;
	}

	public void setValue(float value, int row, int column) {
		this.matrix[row][column] = value;
	}

	public float getValue(int row, int column) {
		return this.matrix[row][column];
	}
}
