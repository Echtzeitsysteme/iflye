package transform.label;

import java.util.HashMap;

import javax.management.InvalidAttributeValueException;
import javax.sound.sampled.AudioFormat.Encoding;

import facade.ModelFacade;
import matrix.Matrix;
import transform.encoding.AbstractEncoding;

public abstract class AbstractEmbeddingLabel extends AbstractLabel {
	
	public abstract HashMap<String, String> processModelResult(float[] result, String sNetId, String vNetId) throws InvalidAttributeValueException;
	
}
