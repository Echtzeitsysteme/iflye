package metrics.handler;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A {@link ThreadFactory} that creates threads with a specific name format.
 * 
 * @author Janik Stracke {@literal <janik.stracke@stud.tu-darmstadt.de>}
 */
public class SampleThreadFactory implements ThreadFactory {

	/**
	 * The number of thread pools that are using this factory.
	 */
	private static final AtomicInteger poolNumber = new AtomicInteger(1);

	/**
	 * The {@link ThreadFactory} to delegate to.
	 */
	protected ThreadFactory delegated;

	/**
	 * The name format for the threads.
	 */
	protected String name;

	/**
	 * The number of the thread pool that is using this factory instance.
	 */
	protected final int assignedPoolNumber;

	/**
	 * The number of threads created by this factory.
	 */
	private final AtomicInteger threadNumber = new AtomicInteger(1);

	/**
	 * Creates a new {@link SampleThreadFactory} with the given name format.
	 * 
	 * @param name the name format for the threads. See
	 *             {@link String#format(String, Object...)} for details. The first
	 *             argument will be the pool number, the second the thread number.
	 */
	public SampleThreadFactory(String name) {
		this(name, Executors.defaultThreadFactory());
	}

	/**
	 * Creates a new {@link SampleThreadFactory} with the given name format and
	 * delegated {@link ThreadFactory}.
	 * 
	 * @param name      the name format for the threads. See
	 *                  {@link String#format(String, Object...)} for details. The
	 *                  first argument will be the pool number, the second the
	 *                  thread number.
	 * @param delegated the {@link ThreadFactory} to delegate to.
	 */
	public SampleThreadFactory(String name, ThreadFactory delegated) {
		this.name = name;
		this.delegated = delegated;
		this.assignedPoolNumber = poolNumber.getAndIncrement();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * The thread will be created by the delegated {@link ThreadFactory} and will be
	 * named according to the given name format.
	 */
	@Override
	public Thread newThread(Runnable r) {
		Thread t = delegated.newThread(r);
		t.setName(String.format(name, assignedPoolNumber, threadNumber.getAndIncrement()));
		return t;
	}

}
