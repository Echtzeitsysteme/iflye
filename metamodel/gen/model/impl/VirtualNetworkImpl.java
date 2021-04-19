/**
 */
package model.impl;

import java.lang.reflect.InvocationTargetException;

import model.ModelPackage;
import model.Status;
import model.VirtualLink;
import model.VirtualNetwork;
import model.VirtualServer;
import model.VirtualSwitch;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Virtual Network</b></em>'.
 * <!-- end-user-doc -->
 *
 * @generated
 */
public class VirtualNetworkImpl extends NetworkImpl implements VirtualNetwork {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected VirtualNetworkImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ModelPackage.Literals.VIRTUAL_NETWORK;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public VirtualNetwork createVirtualNetwork(String name, Status status) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public VirtualServer createServer(String name, int cpu, int memory, int storage, int depth, Status status) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public VirtualSwitch createSwitch(String name, int depth, Status status) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public VirtualLink createLink(String name, String sourceName, String targetName, int bandwidth, Status status) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eInvoke(int operationID, EList<?> arguments) throws InvocationTargetException {
		switch (operationID) {
		case ModelPackage.VIRTUAL_NETWORK___CREATE_VIRTUAL_NETWORK__STRING_STATUS:
			return createVirtualNetwork((String) arguments.get(0), (Status) arguments.get(1));
		case ModelPackage.VIRTUAL_NETWORK___CREATE_SERVER__STRING_INT_INT_INT_INT_STATUS:
			return createServer((String) arguments.get(0), (Integer) arguments.get(1), (Integer) arguments.get(2),
					(Integer) arguments.get(3), (Integer) arguments.get(4), (Status) arguments.get(5));
		case ModelPackage.VIRTUAL_NETWORK___CREATE_SWITCH__STRING_INT_STATUS:
			return createSwitch((String) arguments.get(0), (Integer) arguments.get(1), (Status) arguments.get(2));
		case ModelPackage.VIRTUAL_NETWORK___CREATE_LINK__STRING_STRING_STRING_INT_STATUS:
			return createLink((String) arguments.get(0), (String) arguments.get(1), (String) arguments.get(2),
					(Integer) arguments.get(3), (Status) arguments.get(4));
		}
		return super.eInvoke(operationID, arguments);
	}

} //VirtualNetworkImpl
