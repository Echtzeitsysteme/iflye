package config;

public class OneTierConfig {

	private int numberOfServers;
	private int numberOfSwitches;
	private boolean switchesConnected;
	private int cpuPerServer;
	private int memoryPerServer;
	private int storagePerServer;
	private int bandwidthPerLink;
	
	public OneTierConfig(final int numberOfServers, final int numberOfSwitches, 
			final boolean switchesConnected, final int cpuPerServer, final int memoryPerServer,
			final int storagePerServer, final int bandwidthPerLink) {
		this.numberOfServers = numberOfServers;
		this.numberOfSwitches = numberOfSwitches;
		this.switchesConnected = switchesConnected;
		this.cpuPerServer = cpuPerServer;
		this.memoryPerServer = memoryPerServer;
		this.storagePerServer = storagePerServer;
		this.bandwidthPerLink = bandwidthPerLink;
	}
	
	public int getNumberOfServers() {
		return numberOfServers;
	}
	public void setNumberOfServers(final int numberOfServers) {
		this.numberOfServers = numberOfServers;
	}
	public int getNumberOfSwitches() {
		return numberOfSwitches;
	}
	public void setNumberOfSwitches(final int numberOfSwitches) {
		this.numberOfSwitches = numberOfSwitches;
	}
	public boolean isSwitchesConnected() {
		return switchesConnected;
	}
	public void setSwitchesConnected(final boolean switchesConnected) {
		this.switchesConnected = switchesConnected;
	}
	public int getCpuPerServer() {
		return cpuPerServer;
	}
	public void setCpuPerServer(final int cpuPerServer) {
		this.cpuPerServer = cpuPerServer;
	}
	public int getMemoryPerServer() {
		return memoryPerServer;
	}
	public void setMemoryPerServer(final int memoryPerServer) {
		this.memoryPerServer = memoryPerServer;
	}
	public int getStoragePerServer() {
		return storagePerServer;
	}
	public void setStoragePerServer(final int storagePerServer) {
		this.storagePerServer = storagePerServer;
	}
	public int getBandwidthPerLink() {
		return bandwidthPerLink;
	}
	public void setBandwidthPerLink(final int bandwidthPerLink) {
		this.bandwidthPerLink = bandwidthPerLink;
	}
	
}
