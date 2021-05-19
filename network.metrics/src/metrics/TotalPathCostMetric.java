package metrics;

import model.Link;
import model.Node;
import model.SubstrateLink;
import model.SubstrateNetwork;
import model.SubstratePath;
import model.SubstrateServer;
import model.SubstrateSwitch;
import model.VirtualLink;
import model.VirtualNetwork;
import model.VirtualServer;
import model.VirtualSwitch;

/**
 * Implementation of the cost function of paper [1]. [1] Tomaszek S., Leblebici E., Wang L., Schürr
 * A. (2018) Virtual Network Embedding: Reducing the Search Space by Model Transformation
 * Techniques. In: Rensink A., Sánchez Cuadrado J. (eds) Theory and Practice of Model
 * Transformation. ICMT 2018. Lecture Notes in Computer Science, vol 10888. Springer, Cham
 *
 * @author Maximilian Kratz {@literal <maximilian.kratz@stud.tu-darmstadt.de>}
 */
public class TotalPathCostMetric implements IMetric {

  /**
   * Calculated cost.
   */
  private double cost;

  /**
   * Creates a new instance of this metric for the provided substrate network.
   * 
   * @param sNet Substrate network to calculate the metric for.
   */
  public TotalPathCostMetric(final SubstrateNetwork sNet) {
    double cost = 0;

    // Iterate over all virtual (guest) networks of the substrate network
    for (final VirtualNetwork guest : sNet.getGuests()) {
      // Networks
      // Network on network cost
      // cost += 0;

      // Links
      for (final Link l : guest.getLinks()) {
        final VirtualLink vl = (VirtualLink) l;

        if (vl.getHost() instanceof SubstrateLink) {
          // Virtual link to substrate link cost = Hop cost with only one hop.
          cost += 2;
        }

        if (vl.getHost() instanceof SubstratePath) {
          final SubstratePath sp = (SubstratePath) vl.getHost();

          // Virtual link to substrate path cost = Hop cost
          if (sp.getHops() == 1) {
            cost += 2;
          } else {
            cost += Math.pow(4.0, sp.getHops());
          }
        }


        if (vl.getHost() instanceof SubstrateServer) {
          // Virtual link to substrate server cost
          cost += 1;
        }
        // TODO: The paper also mentions virtual links embedded on substrate switches. This is
        // currently not supported by the ModelFacade. If it will get supported in the future, add
        // it to this cost function.
      }

      // Nodes
      for (final Node n : guest.getNodes()) {
        if (n instanceof VirtualServer) {
          // Virtual server to substrate server cost
          cost += 1;
        } else if (n instanceof VirtualSwitch) {
          final VirtualSwitch sw = (VirtualSwitch) n;
          if (sw.getHost() instanceof SubstrateServer) {
            // Virtual switch to substrate server cost
            cost += 2;
          } else if (sw.getHost() instanceof SubstrateSwitch) {
            // Virtual switch to substrate switch cost
            cost += 1;
          }
        }
      }
    }

    this.cost = cost;
  }

  @Override
  public double getValue() {
    return cost;
  }

}
