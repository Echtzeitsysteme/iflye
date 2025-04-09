package metrics.manager;

import java.util.Objects;
import java.util.Set;

import io.micrometer.observation.Observation;
import model.SubstrateNetwork;
import model.VirtualNetwork;

/**
 * An observation context for all VNE related algorithms.
 * 
 * @author Janik Stracke {@literal <janik.stracke@stud.tu-darmstadt.de>}
 */
public abstract class Context extends Observation.Context {

	/**
	 * @return The Virtual Network(s) to be embedded in the current context.
	 * @throws IllegalStateException If the context is not a child of a
	 *                               {@link VnetEmbeddingContext}.
	 */
	public Set<VirtualNetwork> getVirtualNetwork() {
		Observation.ContextView contextView = this.getParentObservation().getContextView();
		String msg = "The context " + this.getClass().getSimpleName() + " is required to be a child to "
				+ Context.class.getSimpleName() + ", was ";
		if (Objects.requireNonNull(contextView, msg + "NULL") instanceof Context) {
			return ((Context) contextView).getVirtualNetwork();
		}

		throw new IllegalStateException(msg + contextView.getClass().getSimpleName());
	}

	/**
	 * @return The substrate network to be used in the current context.
	 * @throws IllegalStateException If the context is not a child of a
	 *                               {@link VnetEmbeddingContext}.
	 */
	public SubstrateNetwork getSubstrateNetwork() {
		Observation.ContextView contextView = this.getParentObservation().getContextView();
		String msg = "The context " + this.getClass().getSimpleName() + " is required to be a child to "
				+ Context.class.getSimpleName() + ", was ";
		if (Objects.requireNonNull(contextView, msg + "NULL") instanceof Context) {
			return ((Context) contextView).getSubstrateNetwork();
		}

		throw new IllegalStateException(msg + contextView.getClass().getSimpleName());
	}

	/**
	 * @return The number of embeddings found in the current context.
	 * @throws IllegalStateException If the context is not a child of a
	 *                               {@link VnetEmbeddingContext}.
	 */
	public int getCounter() {
		Observation.ContextView contextView = this.getParentObservation().getContextView();
		String msg = "The context " + this.getClass().getSimpleName() + " is required to be a child to "
				+ Context.class.getSimpleName() + ", was ";
		if (Objects.requireNonNull(contextView, msg + "NULL") instanceof Context) {
			return ((Context) contextView).getCounter();
		}

		throw new IllegalStateException(msg + contextView.getClass().getSimpleName());
	}

	/**
	 * A context dedicated to the embedding of a virtual network. Used as a parent
	 * to all contexts that are related to the embedding of a virtual network.
	 */
	public static class VnetEmbeddingContext extends Context {

		/**
		 * The virtual network(s) to be embedded in the current context.
		 */
		protected final Set<VirtualNetwork> vNet;

		/**
		 * The substrate network to be used in the current context.
		 */
		protected final SubstrateNetwork sNet;

		/**
		 * The number of embeddings found in the current context.
		 */
		protected final int counter;

		/**
		 * Creates a new {@link VnetEmbeddingContext} with the given parameters.
		 * 
		 * @param sNet    The substrate network to be used in the current context.
		 * @param vNet    The virtual network(s) to be embedded in the current context.
		 * @param counter The number of embeddings found in the current context.
		 */
		public VnetEmbeddingContext(final SubstrateNetwork sNet, final Set<VirtualNetwork> vNet, final int counter) {
			this.vNet = vNet;
			this.sNet = sNet;
			this.counter = counter;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Set<VirtualNetwork> getVirtualNetwork() {
			return this.vNet;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public SubstrateNetwork getSubstrateNetwork() {
			return sNet;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int getCounter() {
			return counter;
		}

	}

	/**
	 * A context to distinguish between different phases (stage or step) of the
	 * algorithm.
	 */
	public static abstract class PhaseContext extends Context {

		/**
		 * The phase of the algorithm.
		 */
		protected final String phase;

		/**
		 * Creates a new {@link PhaseContext} with the given parameters.
		 * 
		 * @param phase The phase of the algorithm.
		 */
		public PhaseContext(final String phase) {
			this.phase = phase;
		}

		/**
		 * @return The current phase of the algorithm.
		 */
		public String getPhase() {
			return this.phase;
		}

	}

	/**
	 * A context to distinguish between different stages (prepare or execute) of the
	 * algorithm.
	 */
	public static abstract class StageContext extends PhaseContext {

		/**
		 * Creates a new {@link StageContext} with the given parameters.
		 * 
		 * @param stage The stage of the algorithm.
		 */
		public StageContext(final String stage) {
			super(stage);
		}

		/**
		 * @return The current stage of the algorithm.
		 */
		public String getStage() {
			return this.getPhase();
		}

	}

	/**
	 * A {@link StageContext} for the prepare stage of the algorithm.
	 */
	public static class PrepareStageContext extends StageContext {
		public PrepareStageContext() {
			super("prepare");
		}
	}

	/**
	 * A {@link StageContext} for the execute stage of the algorithm.
	 */
	public static class ExecuteStageContext extends StageContext {
		public ExecuteStageContext() {
			super("execute");
		}
	}

	/**
	 * A context to distinguish between different steps (ilp, pm or deploy) of the
	 * algorithm.
	 */
	public static abstract class StepContext extends PhaseContext {

		/**
		 * Creates a new {@link StepContext} with the given parameters.
		 * 
		 * @param step The step of the algorithm.
		 */
		public StepContext(final String step) {
			super(step);
		}

		/**
		 * @return The current step of the algorithm.
		 */
		public String getStep() {
			return this.getPhase();
		}
	}

	/**
	 * A {@link StepContext} for the ILP step of the algorithm.
	 */
	public static class IlpStepContext extends StepContext {
		public IlpStepContext() {
			super("ilp");
		}
	}

	/**
	 * A {@link StepContext} for the PM step of the algorithm.
	 */
	public static class PmStepContext extends StepContext {
		public PmStepContext() {
			super("pm");
		}
	}

	/**
	 * A {@link StepContext} for the deploy step of the algorithm.
	 */
	public static class DeployStepContext extends StepContext {
		public DeployStepContext() {
			super("deploy");
		}
	}
}