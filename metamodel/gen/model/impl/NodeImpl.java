/**
 */
package model.impl;

import java.util.Collection;

import model.Link;
import model.ModelPackage;
import model.Network;
import model.Node;
import model.Path;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EObjectWithInverseResolvingEList;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Node</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link model.impl.NodeImpl#getDepth <em>Depth</em>}</li>
 *   <li>{@link model.impl.NodeImpl#getNetwork <em>Network</em>}</li>
 *   <li>{@link model.impl.NodeImpl#getOutgoingLinks <em>Outgoing Links</em>}</li>
 *   <li>{@link model.impl.NodeImpl#getIncomingLinks <em>Incoming Links</em>}</li>
 *   <li>{@link model.impl.NodeImpl#getPaths <em>Paths</em>}</li>
 *   <li>{@link model.impl.NodeImpl#getOutgoingPaths <em>Outgoing Paths</em>}</li>
 *   <li>{@link model.impl.NodeImpl#getIncomingPaths <em>Incoming Paths</em>}</li>
 * </ul>
 *
 * @generated
 */
public abstract class NodeImpl extends ElementImpl implements Node {
	/**
	 * The default value of the '{@link #getDepth() <em>Depth</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDepth()
	 * @generated
	 * @ordered
	 */
	protected static final int DEPTH_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getDepth() <em>Depth</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDepth()
	 * @generated
	 * @ordered
	 */
	protected int depth = DEPTH_EDEFAULT;

	/**
	 * The cached value of the '{@link #getNetwork() <em>Network</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNetwork()
	 * @generated
	 * @ordered
	 */
	protected Network network;

	/**
	 * The cached value of the '{@link #getOutgoingLinks() <em>Outgoing Links</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOutgoingLinks()
	 * @generated
	 * @ordered
	 */
	protected EList<Link> outgoingLinks;

	/**
	 * The cached value of the '{@link #getIncomingLinks() <em>Incoming Links</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIncomingLinks()
	 * @generated
	 * @ordered
	 */
	protected EList<Link> incomingLinks;

	/**
	 * The cached value of the '{@link #getPaths() <em>Paths</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPaths()
	 * @generated
	 * @ordered
	 */
	protected EList<Path> paths;

	/**
	 * The cached value of the '{@link #getOutgoingPaths() <em>Outgoing Paths</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOutgoingPaths()
	 * @generated
	 * @ordered
	 */
	protected EList<Path> outgoingPaths;

	/**
	 * The cached value of the '{@link #getIncomingPaths() <em>Incoming Paths</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIncomingPaths()
	 * @generated
	 * @ordered
	 */
	protected EList<Path> incomingPaths;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected NodeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ModelPackage.Literals.NODE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getDepth() {
		return depth;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDepth(int newDepth) {
		int oldDepth = depth;
		depth = newDepth;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.NODE__DEPTH, oldDepth, depth));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Network getNetwork() {
		if (network != null && network.eIsProxy()) {
			InternalEObject oldNetwork = (InternalEObject) network;
			network = (Network) eResolveProxy(oldNetwork);
			if (network != oldNetwork) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ModelPackage.NODE__NETWORK, oldNetwork,
							network));
			}
		}
		return network;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Network basicGetNetwork() {
		return network;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetNetwork(Network newNetwork, NotificationChain msgs) {
		Network oldNetwork = network;
		network = newNetwork;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ModelPackage.NODE__NETWORK,
					oldNetwork, newNetwork);
			if (msgs == null)
				msgs = notification;
			else
				msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setNetwork(Network newNetwork) {
		if (newNetwork != network) {
			NotificationChain msgs = null;
			if (network != null)
				msgs = ((InternalEObject) network).eInverseRemove(this, ModelPackage.NETWORK__NODES, Network.class,
						msgs);
			if (newNetwork != null)
				msgs = ((InternalEObject) newNetwork).eInverseAdd(this, ModelPackage.NETWORK__NODES, Network.class,
						msgs);
			msgs = basicSetNetwork(newNetwork, msgs);
			if (msgs != null)
				msgs.dispatch();
		} else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.NODE__NETWORK, newNetwork, newNetwork));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Link> getOutgoingLinks() {
		if (outgoingLinks == null) {
			outgoingLinks = new EObjectWithInverseResolvingEList<Link>(Link.class, this,
					ModelPackage.NODE__OUTGOING_LINKS, ModelPackage.LINK__SOURCE);
		}
		return outgoingLinks;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Link> getIncomingLinks() {
		if (incomingLinks == null) {
			incomingLinks = new EObjectWithInverseResolvingEList<Link>(Link.class, this,
					ModelPackage.NODE__INCOMING_LINKS, ModelPackage.LINK__TARGET);
		}
		return incomingLinks;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Path> getPaths() {
		if (paths == null) {
			paths = new EObjectWithInverseResolvingEList.ManyInverse<Path>(Path.class, this, ModelPackage.NODE__PATHS,
					ModelPackage.PATH__NODES);
		}
		return paths;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Path> getOutgoingPaths() {
		if (outgoingPaths == null) {
			outgoingPaths = new EObjectWithInverseResolvingEList<Path>(Path.class, this,
					ModelPackage.NODE__OUTGOING_PATHS, ModelPackage.PATH__SOURCE);
		}
		return outgoingPaths;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Path> getIncomingPaths() {
		if (incomingPaths == null) {
			incomingPaths = new EObjectWithInverseResolvingEList<Path>(Path.class, this,
					ModelPackage.NODE__INCOMING_PATHS, ModelPackage.PATH__TARGET);
		}
		return incomingPaths;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
		case ModelPackage.NODE__NETWORK:
			if (network != null)
				msgs = ((InternalEObject) network).eInverseRemove(this, ModelPackage.NETWORK__NODES, Network.class,
						msgs);
			return basicSetNetwork((Network) otherEnd, msgs);
		case ModelPackage.NODE__OUTGOING_LINKS:
			return ((InternalEList<InternalEObject>) (InternalEList<?>) getOutgoingLinks()).basicAdd(otherEnd, msgs);
		case ModelPackage.NODE__INCOMING_LINKS:
			return ((InternalEList<InternalEObject>) (InternalEList<?>) getIncomingLinks()).basicAdd(otherEnd, msgs);
		case ModelPackage.NODE__PATHS:
			return ((InternalEList<InternalEObject>) (InternalEList<?>) getPaths()).basicAdd(otherEnd, msgs);
		case ModelPackage.NODE__OUTGOING_PATHS:
			return ((InternalEList<InternalEObject>) (InternalEList<?>) getOutgoingPaths()).basicAdd(otherEnd, msgs);
		case ModelPackage.NODE__INCOMING_PATHS:
			return ((InternalEList<InternalEObject>) (InternalEList<?>) getIncomingPaths()).basicAdd(otherEnd, msgs);
		}
		return super.eInverseAdd(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
		case ModelPackage.NODE__NETWORK:
			return basicSetNetwork(null, msgs);
		case ModelPackage.NODE__OUTGOING_LINKS:
			return ((InternalEList<?>) getOutgoingLinks()).basicRemove(otherEnd, msgs);
		case ModelPackage.NODE__INCOMING_LINKS:
			return ((InternalEList<?>) getIncomingLinks()).basicRemove(otherEnd, msgs);
		case ModelPackage.NODE__PATHS:
			return ((InternalEList<?>) getPaths()).basicRemove(otherEnd, msgs);
		case ModelPackage.NODE__OUTGOING_PATHS:
			return ((InternalEList<?>) getOutgoingPaths()).basicRemove(otherEnd, msgs);
		case ModelPackage.NODE__INCOMING_PATHS:
			return ((InternalEList<?>) getIncomingPaths()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
		case ModelPackage.NODE__DEPTH:
			return getDepth();
		case ModelPackage.NODE__NETWORK:
			if (resolve)
				return getNetwork();
			return basicGetNetwork();
		case ModelPackage.NODE__OUTGOING_LINKS:
			return getOutgoingLinks();
		case ModelPackage.NODE__INCOMING_LINKS:
			return getIncomingLinks();
		case ModelPackage.NODE__PATHS:
			return getPaths();
		case ModelPackage.NODE__OUTGOING_PATHS:
			return getOutgoingPaths();
		case ModelPackage.NODE__INCOMING_PATHS:
			return getIncomingPaths();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
		case ModelPackage.NODE__DEPTH:
			setDepth((Integer) newValue);
			return;
		case ModelPackage.NODE__NETWORK:
			setNetwork((Network) newValue);
			return;
		case ModelPackage.NODE__OUTGOING_LINKS:
			getOutgoingLinks().clear();
			getOutgoingLinks().addAll((Collection<? extends Link>) newValue);
			return;
		case ModelPackage.NODE__INCOMING_LINKS:
			getIncomingLinks().clear();
			getIncomingLinks().addAll((Collection<? extends Link>) newValue);
			return;
		case ModelPackage.NODE__PATHS:
			getPaths().clear();
			getPaths().addAll((Collection<? extends Path>) newValue);
			return;
		case ModelPackage.NODE__OUTGOING_PATHS:
			getOutgoingPaths().clear();
			getOutgoingPaths().addAll((Collection<? extends Path>) newValue);
			return;
		case ModelPackage.NODE__INCOMING_PATHS:
			getIncomingPaths().clear();
			getIncomingPaths().addAll((Collection<? extends Path>) newValue);
			return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
		case ModelPackage.NODE__DEPTH:
			setDepth(DEPTH_EDEFAULT);
			return;
		case ModelPackage.NODE__NETWORK:
			setNetwork((Network) null);
			return;
		case ModelPackage.NODE__OUTGOING_LINKS:
			getOutgoingLinks().clear();
			return;
		case ModelPackage.NODE__INCOMING_LINKS:
			getIncomingLinks().clear();
			return;
		case ModelPackage.NODE__PATHS:
			getPaths().clear();
			return;
		case ModelPackage.NODE__OUTGOING_PATHS:
			getOutgoingPaths().clear();
			return;
		case ModelPackage.NODE__INCOMING_PATHS:
			getIncomingPaths().clear();
			return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
		case ModelPackage.NODE__DEPTH:
			return depth != DEPTH_EDEFAULT;
		case ModelPackage.NODE__NETWORK:
			return network != null;
		case ModelPackage.NODE__OUTGOING_LINKS:
			return outgoingLinks != null && !outgoingLinks.isEmpty();
		case ModelPackage.NODE__INCOMING_LINKS:
			return incomingLinks != null && !incomingLinks.isEmpty();
		case ModelPackage.NODE__PATHS:
			return paths != null && !paths.isEmpty();
		case ModelPackage.NODE__OUTGOING_PATHS:
			return outgoingPaths != null && !outgoingPaths.isEmpty();
		case ModelPackage.NODE__INCOMING_PATHS:
			return incomingPaths != null && !incomingPaths.isEmpty();
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy())
			return super.toString();

		StringBuilder result = new StringBuilder(super.toString());
		result.append(" (depth: ");
		result.append(depth);
		result.append(')');
		return result.toString();
	}

} //NodeImpl
