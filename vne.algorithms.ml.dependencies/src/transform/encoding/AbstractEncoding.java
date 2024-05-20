package transform.encoding;

import facade.ModelFacade;

// Encoding is a Vector
// networks are represented by undirected graphs without "Mehrfachkanten"
public abstract class AbstractEncoding {
	protected static ModelFacade facade = ModelFacade.getInstance();

	public abstract float[] get(String sNetId, String vNetId);
}
