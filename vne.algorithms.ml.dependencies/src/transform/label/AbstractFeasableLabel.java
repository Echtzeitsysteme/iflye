package transform.label;

import javax.management.InvalidAttributeValueException;

public abstract class AbstractFeasableLabel extends AbstractLabel {

	public AbstractFeasableLabel() {
	}

	public abstract boolean processModelResult(float[] result)
			throws InvalidAttributeValueException;

}
