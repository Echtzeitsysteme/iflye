/**
 */
package model.impl;

import java.lang.reflect.InvocationTargetException;

import java.util.Collection;

import model.Link;
import model.ModelPackage;
import model.Network;
import model.Node;
import model.Path;
import model.Root;
import model.Server;
import model.Status;
import model.Switch;

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
 * An implementation of the model object '<em><b>Network</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link model.impl.NetworkImpl#getRoot <em>Root</em>}</li>
 *   <li>{@link model.impl.NetworkImpl#getNodes <em>Nodes</em>}</li>
 *   <li>{@link model.impl.NetworkImpl#getLinks <em>Links</em>}</li>
 *   <li>{@link model.impl.NetworkImpl#getPaths <em>Paths</em>}</li>
 * </ul>
 *
 * @generated
 */
public abstract class NetworkImpl extends ElementImpl implements Network {
	/**
	 * The cached value of the '{@link #getRoot() <em>Root</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRoot()
	 * @generated
	 * @ordered
	 */
	protected Root root;

	/**
	 * The cached value of the '{@link #getNodes() <em>Nodes</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNodes()
	 * @generated
	 * @ordered
	 */
	protected EList<Node> nodes;

	/**
	 * The cached value of the '{@link #getLinks() <em>Links</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLinks()
	 * @generated
	 * @ordered
	 */
	protected EList<Link> links;

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
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected NetworkImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ModelPackage.Literals.NETWORK;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Root getRoot() {
		if (root != null && root.eIsProxy()) {
			InternalEObject oldRoot = (InternalEObject) root;
			root = (Root) eResolveProxy(oldRoot);
			if (root != oldRoot) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ModelPackage.NETWORK__ROOT, oldRoot,
							root));
			}
		}
		return root;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Root basicGetRoot() {
		return root;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetRoot(Root newRoot, NotificationChain msgs) {
		Root oldRoot = root;
		root = newRoot;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ModelPackage.NETWORK__ROOT,
					oldRoot, newRoot);
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
	public void setRoot(Root newRoot) {
		if (newRoot != root) {
			NotificationChain msgs = null;
			if (root != null)
				msgs = ((InternalEObject) root).eInverseRemove(this, ModelPackage.ROOT__NETWORKS, Root.class, msgs);
			if (newRoot != null)
				msgs = ((InternalEObject) newRoot).eInverseAdd(this, ModelPackage.ROOT__NETWORKS, Root.class, msgs);
			msgs = basicSetRoot(newRoot, msgs);
			if (msgs != null)
				msgs.dispatch();
		} else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.NETWORK__ROOT, newRoot, newRoot));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Node> getNodes() {
		if (nodes == null) {
			nodes = new EObjectWithInverseResolvingEList<Node>(Node.class, this, ModelPackage.NETWORK__NODES,
					ModelPackage.NODE__NETWORK);
		}
		return nodes;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Link> getLinks() {
		if (links == null) {
			links = new EObjectWithInverseResolvingEList<Link>(Link.class, this, ModelPackage.NETWORK__LINKS,
					ModelPackage.LINK__NETWORK);
		}
		return links;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Path> getPaths() {
		if (paths == null) {
			paths = new EObjectWithInverseResolvingEList<Path>(Path.class, this, ModelPackage.NETWORK__PATHS,
					ModelPackage.PATH__NETWORK);
		}
		return paths;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Server createServer(String name, int depth, int cpu, int memory, int storage, Status status) {
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
	public Switch createSwitch(String name, int depth, Status status) {
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
	public Link createLink(String name, Node source, Node target, int bandwidth, Status status) {
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
	public Path createPath(String name, Node source, Node target, Status status) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
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
		case ModelPackage.NETWORK__ROOT:
			if (root != null)
				msgs = ((InternalEObject) root).eInverseRemove(this, ModelPackage.ROOT__NETWORKS, Root.class, msgs);
			return basicSetRoot((Root) otherEnd, msgs);
		case ModelPackage.NETWORK__NODES:
			return ((InternalEList<InternalEObject>) (InternalEList<?>) getNodes()).basicAdd(otherEnd, msgs);
		case ModelPackage.NETWORK__LINKS:
			return ((InternalEList<InternalEObject>) (InternalEList<?>) getLinks()).basicAdd(otherEnd, msgs);
		case ModelPackage.NETWORK__PATHS:
			return ((InternalEList<InternalEObject>) (InternalEList<?>) getPaths()).basicAdd(otherEnd, msgs);
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
		case ModelPackage.NETWORK__ROOT:
			return basicSetRoot(null, msgs);
		case ModelPackage.NETWORK__NODES:
			return ((InternalEList<?>) getNodes()).basicRemove(otherEnd, msgs);
		case ModelPackage.NETWORK__LINKS:
			return ((InternalEList<?>) getLinks()).basicRemove(otherEnd, msgs);
		case ModelPackage.NETWORK__PATHS:
			return ((InternalEList<?>) getPaths()).basicRemove(otherEnd, msgs);
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
		case ModelPackage.NETWORK__ROOT:
			if (resolve)
				return getRoot();
			return basicGetRoot();
		case ModelPackage.NETWORK__NODES:
			return getNodes();
		case ModelPackage.NETWORK__LINKS:
			return getLinks();
		case ModelPackage.NETWORK__PATHS:
			return getPaths();
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
		case ModelPackage.NETWORK__ROOT:
			setRoot((Root) newValue);
			return;
		case ModelPackage.NETWORK__NODES:
			getNodes().clear();
			getNodes().addAll((Collection<? extends Node>) newValue);
			return;
		case ModelPackage.NETWORK__LINKS:
			getLinks().clear();
			getLinks().addAll((Collection<? extends Link>) newValue);
			return;
		case ModelPackage.NETWORK__PATHS:
			getPaths().clear();
			getPaths().addAll((Collection<? extends Path>) newValue);
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
		case ModelPackage.NETWORK__ROOT:
			setRoot((Root) null);
			return;
		case ModelPackage.NETWORK__NODES:
			getNodes().clear();
			return;
		case ModelPackage.NETWORK__LINKS:
			getLinks().clear();
			return;
		case ModelPackage.NETWORK__PATHS:
			getPaths().clear();
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
		case ModelPackage.NETWORK__ROOT:
			return root != null;
		case ModelPackage.NETWORK__NODES:
			return nodes != null && !nodes.isEmpty();
		case ModelPackage.NETWORK__LINKS:
			return links != null && !links.isEmpty();
		case ModelPackage.NETWORK__PATHS:
			return paths != null && !paths.isEmpty();
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eInvoke(int operationID, EList<?> arguments) throws InvocationTargetException {
		switch (operationID) {
		case ModelPackage.NETWORK___CREATE_SERVER__STRING_INT_INT_INT_INT_STATUS:
			return createServer((String) arguments.get(0), (Integer) arguments.get(1), (Integer) arguments.get(2),
					(Integer) arguments.get(3), (Integer) arguments.get(4), (Status) arguments.get(5));
		case ModelPackage.NETWORK___CREATE_SWITCH__STRING_INT_STATUS:
			return createSwitch((String) arguments.get(0), (Integer) arguments.get(1), (Status) arguments.get(2));
		case ModelPackage.NETWORK___CREATE_LINK__STRING_NODE_NODE_INT_STATUS:
			return createLink((String) arguments.get(0), (Node) arguments.get(1), (Node) arguments.get(2),
					(Integer) arguments.get(3), (Status) arguments.get(4));
		case ModelPackage.NETWORK___CREATE_PATH__STRING_NODE_NODE_STATUS:
			return createPath((String) arguments.get(0), (Node) arguments.get(1), (Node) arguments.get(2),
					(Status) arguments.get(3));
		}
		return super.eInvoke(operationID, arguments);
	}

} //NetworkImpl
