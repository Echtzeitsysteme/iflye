/**
 */
package model.impl;

import java.lang.reflect.InvocationTargetException;

import model.ModelFactory;
import model.ModelPackage;
import model.Status;
import model.SubstrateNetwork;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Substrate Network</b></em>'.
 * <!-- end-user-doc -->
 *
 * @generated
 */
public class SubstrateNetworkImpl extends NetworkImpl implements SubstrateNetwork {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected SubstrateNetworkImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ModelPackage.Literals.SUBSTRATE_NETWORK;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	@Override
	public SubstrateNetwork createSubstrateNetwork(String name, Status status) {
		final SubstrateNetwork net = ModelFactory.eINSTANCE.createSubstrateNetwork();
		net.setName(name);
		net.setStatus(status);
		return net;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eInvoke(int operationID, EList<?> arguments) throws InvocationTargetException {
		switch (operationID) {
		case ModelPackage.SUBSTRATE_NETWORK___CREATE_SUBSTRATE_NETWORK__STRING_STATUS:
			return createSubstrateNetwork((String) arguments.get(0), (Status) arguments.get(1));
		}
		return super.eInvoke(operationID, arguments);
	}

} //SubstrateNetworkImpl
