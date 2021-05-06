package facade.pathgen;

import java.util.List;
import java.util.Map;
import model.SubstrateLink;
import model.SubstrateNetwork;
import model.SubstrateNode;

/**
 * Interface for all path generators.
 * 
 * @author Maximilian Kratz <maximilian.kratz@stud.tu-darmstadt.de>
 */
public interface IPathGen {

  /**
   * Returns a map between all target nodes to a list of links forming the path from start to
   * target.
   * 
   * @param net Substrate network to work on.
   * @param start Substrate node that is the node to start with.
   * @return Map from all target nodes to a list of links forming the path.
   */
  public Map<SubstrateNode, List<SubstrateLink>> getAllPaths(final SubstrateNetwork net,
      final SubstrateNode start);

}
