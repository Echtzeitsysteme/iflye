/**
 */
package model.impl;

import java.lang.reflect.InvocationTargetException;

import model.ModelPackage;
import model.Status;
import model.VirtualNetwork;

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
	public Object eInvoke(int operationID, EList<?> arguments) throws InvocationTargetException {
		switch (operationID) {
		case ModelPackage.VIRTUAL_NETWORK___CREATE_VIRTUAL_NETWORK__STRING_STATUS:
			return createVirtualNetwork((String) arguments.get(0), (Status) arguments.get(1));
		}
		return super.eInvoke(operationID, arguments);
	}

} //VirtualNetworkImpl
