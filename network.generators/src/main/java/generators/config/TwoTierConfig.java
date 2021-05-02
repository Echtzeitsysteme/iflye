package generators.config;

/**
 * Configuration (container-)class for the two tier network generator.
 * 
 * @author Maximilian Kratz <maximilian.kratz@stud.tu-darmstadt.de>
 */
public class TwoTierConfig implements IGeneratorConfig {

  /**
   * One OneTierConfig is a rack in a two tier network.
   */
  private OneTierConfig rack;
  private int numberOfRacks;
  private int numberOfCoreSwitches;
  private boolean coreSwitchesConnected;
  private int coreBandwidth;

  /**
   * Default constructor that uses default values.
   */
  public TwoTierConfig() {
    rack = new OneTierConfig(2, 1, false, 10, 10, 10, 20);
    numberOfRacks = 2;
    numberOfCoreSwitches = 1;
    coreSwitchesConnected = false;
    coreBandwidth = 40;
  }

  public TwoTierConfig(final OneTierConfig rack, final int numberOfCoreSwitches,
      final int numberOfRacks, final boolean coreSwitchesConnected, final int coreBandwidth) {
    this.rack = rack;
    this.numberOfRacks = numberOfRacks;
    this.numberOfCoreSwitches = numberOfCoreSwitches;
    this.coreSwitchesConnected = coreSwitchesConnected;
    this.coreBandwidth = coreBandwidth;
  }

  public OneTierConfig getRack() {
    return rack;
  }

  public void setRack(final OneTierConfig rack) {
    this.rack = rack;
  }

  public int getNumberOfRacks() {
    return numberOfRacks;
  }

  public void setNumberOfRacks(final int numberOfRacks) {
    this.numberOfRacks = numberOfRacks;
  }

  public int getNumberOfCoreSwitches() {
    return numberOfCoreSwitches;
  }

  public void setNumberOfCoreSwitches(final int numberOfCoreSwitches) {
    this.numberOfCoreSwitches = numberOfCoreSwitches;
  }

  public boolean isCoreSwitchesConnected() {
    return coreSwitchesConnected;
  }

  public void setCoreSwitchesConnected(final boolean coreSwitchesConnected) {
    this.coreSwitchesConnected = coreSwitchesConnected;
  }

  public int getCoreBandwidth() {
    return coreBandwidth;
  }

  public void setCoreBandwidth(final int coreBandwidth) {
    this.coreBandwidth = coreBandwidth;
  }

}
