package metrics.time;

/**
 * Runtime metric implementation. This one extends the normal runtime metric and
 * calculates three values: (1) Pattern matching runtime (2) ILP solver runtime
 * (3) rest.
 *
 * Please keep in mind that this implementation adds around 0.05 to 0.1
 * milliseconds of latency to the measurement. Therefore, it is not well suited
 * for capturing very small time frames.
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class RuntimeDetailedMetric extends RuntimeMetric {

	// TODO: Documentation.

	private long pmStart = 0;
	private long ilpStart = 0;
	private long deployStart = 0;

	private long pmCumulative = 0;
	private long ilpCumulative = 0;
	private long deployCumulative = 0;

	public void startPmTime() {
		this.pmStart = System.nanoTime();
	}

	public void endPmTime() {
		pmCumulative += System.nanoTime() - pmStart;
		pmStart = 0;
	}

	public void startIlpTime() {
		this.ilpStart = System.nanoTime();
	}

	public void endIlpTime() {
		ilpCumulative += System.nanoTime() - ilpStart;
		ilpStart = 0;
	}

	public void startDeployTime() {
		this.deployStart = System.nanoTime();
	}

	public void endDeployTime() {
		deployCumulative += System.nanoTime() - deployStart;
		deployStart = 0;
	}

	public double getPmValue() {
		return pmCumulative;
	}

	public double getIlpValue() {
		return ilpCumulative;
	}

	public double getDeployValue() {
		return deployCumulative;
	}

	public double getRestValue() {
		return getValue() - pmCumulative - ilpCumulative - deployCumulative;
	}

}
