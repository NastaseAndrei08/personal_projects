import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import {
    Navbar, NavbarBrand, Nav, NavItem, Button, Container, Row, Col, Table, Card, CardHeader, CardBody,
    Modal, ModalHeader, ModalBody, ModalFooter, Form, FormGroup, Label, Input
} from 'reactstrap';
import Chat from './Chat';

export default function Admin({ stompClient }) {
    const [users, setUsers] = useState([]);
    const [devices, setDevices] = useState([]);

    // Modal States
    const [userModal, setUserModal] = useState(false);
    const [deviceModal, setDeviceModal] = useState(false);
    const [editingUser, setEditingUser] = useState(null);
    const [editingDevice, setEditingDevice] = useState(null);

    // Form Data
    const [userData, setUserData] = useState({ username: '', password: '', role: 'CLIENT' });
    // [FIX] We use 'ownerUsername' because M3 Backend stores String username, not UUID
    const [deviceData, setDeviceData] = useState({ name: '', maxConsumption: '', ownerUsername: '' });

    const navigate = useNavigate();
    const token = localStorage.getItem('token');
    const headers = { Authorization: `Bearer ${token}` };

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        try {
            const uRes = await axios.get('http://localhost:8080/api/users', { headers });
            setUsers(uRes.data);
            const dRes = await axios.get('http://localhost:8080/api/devices', { headers });
            setDevices(dRes.data);
        } catch (err) {
            if (err.response?.status === 401) navigate('/login');
        }
    };

    // --- User Operations ---
    const openUserModal = (user = null) => {
        setEditingUser(user);
        setUserData(user ? { username: user.username, role: user.role, password: '' } : { username: '', password: '', role: 'CLIENT' });
        setUserModal(true);
    };

    const saveUser = async () => {
        try {
            if (editingUser) {
                await axios.put(`http://localhost:8080/api/users/${editingUser.id}`, userData, { headers });
            } else {
                await axios.post('http://localhost:8080/api/auth/register', userData);
            }
            setUserModal(false);
            fetchData();
        } catch (e) { alert("Action Failed: " + e.message); }
    };

    const deleteUser = async (id) => {
        if (window.confirm("Delete User?")) {
            await axios.delete(`http://localhost:8080/api/users/${id}`, { headers });
            fetchData();
        }
    };

    // --- Device Operations ---
    const openDeviceModal = (device = null) => {
        setEditingDevice(device);

        // [FIX] Load 'ownerUsername' from the device object.
        // M3 DeviceController sends 'ownerUsername', not 'userId' or 'user'.
        setDeviceData(device
            ? { name: device.name, maxConsumption: device.maxConsumption, ownerUsername: device.ownerUsername || '' }
            : { name: '', maxConsumption: '', ownerUsername: '' }
        );
        setDeviceModal(true);
    };

    const saveDevice = async () => {
        try {
            // [FIX] Send 'ownerUsername' to match M3 DeviceDto
            const payload = {
                name: deviceData.name,
                maxConsumption: Number(deviceData.maxConsumption),
                ownerUsername: deviceData.ownerUsername === '' ? null : deviceData.ownerUsername
            };

            if (editingDevice) {
                await axios.put(`http://localhost:8080/api/devices/${editingDevice.id}`, payload, { headers });
            } else {
                await axios.post('http://localhost:8080/api/devices', payload, { headers });
            }
            setDeviceModal(false);
            fetchData();
        } catch (e) {
            console.error(e);
            alert("Action Failed. Check console.");
        }
    };

    const deleteDevice = async (id) => {
        if (window.confirm("Delete Device?")) {
            await axios.delete(`http://localhost:8080/api/devices/${id}`, { headers });
            fetchData();
        }
    };

    return (
        <div style={{ backgroundColor: '#f8f9fa', minHeight: '100vh' }}>
            <Navbar color="dark" dark expand="md" className="mb-4 px-4">
                <NavbarBrand href="/">Admin Dashboard</NavbarBrand>
                <Nav className="ms-auto" navbar>
                    <NavItem>
                        <Button color="danger" outline onClick={() => { localStorage.clear(); navigate('/login'); }}>Logout</Button>
                    </NavItem>
                </Nav>
            </Navbar>

            <Container fluid>
                <Row>
                    {/* Users Table */}
                    <Col md="5">
                        <Card className="shadow-sm mb-4">
                            <CardHeader className="d-flex justify-content-between">
                                <h5>Users</h5>
                                <Button size="sm" color="success" onClick={() => openUserModal()}>+ New</Button>
                            </CardHeader>
                            <CardBody className="p-0">
                                <Table striped borderless className="mb-0">
                                    <thead><tr><th>Username</th><th>Role</th><th>Actions</th></tr></thead>
                                    <tbody>
                                    {users.map(u => (
                                        <tr key={u.id}>
                                            <td>{u.username}</td>
                                            <td>{u.role}</td>
                                            <td>
                                                <Button size="sm" color="warning" outline className="me-2" onClick={() => openUserModal(u)}>Edit</Button>
                                                <Button size="sm" color="danger" onClick={() => deleteUser(u.id)}>X</Button>
                                            </td>
                                        </tr>
                                    ))}
                                    </tbody>
                                </Table>
                            </CardBody>
                        </Card>
                    </Col>

                    {/* Devices Table */}
                    <Col md="7">
                        <Card className="shadow-sm">
                            <CardHeader className="d-flex justify-content-between">
                                <h5>Devices</h5>
                                <Button size="sm" color="success" onClick={() => openDeviceModal()}>+ New</Button>
                            </CardHeader>
                            <CardBody className="p-0">
                                <Table striped borderless className="mb-0">
                                    <thead><tr><th>Name</th><th>Max (kWh)</th><th>Owner</th><th>Actions</th></tr></thead>
                                    <tbody>
                                    {devices.map(d => (
                                        <tr key={d.id}>
                                            <td>{d.name}</td>
                                            <td>{d.maxConsumption}</td>
                                            {/* [FIX] Display ownerUsername directly */}
                                            <td className={d.ownerUsername ? "text-primary" : "text-muted"}>
                                                {d.ownerUsername || 'Unassigned'}
                                            </td>
                                            <td>
                                                <Button size="sm" color="warning" outline className="me-2" onClick={() => openDeviceModal(d)}>Edit</Button>
                                                <Button size="sm" color="danger" onClick={() => deleteDevice(d.id)}>X</Button>
                                            </td>
                                        </tr>
                                    ))}
                                    </tbody>
                                </Table>
                            </CardBody>
                        </Card>
                    </Col>
                </Row>
            </Container>

            {/* User Modal */}
            <Modal isOpen={userModal} toggle={() => setUserModal(!userModal)}>
                <ModalHeader>{editingUser ? 'Edit User' : 'Add User'}</ModalHeader>
                <ModalBody>
                    <Form>
                        <FormGroup><Label>Username</Label><Input value={userData.username} onChange={e => setUserData({...userData, username: e.target.value})} /></FormGroup>
                        <FormGroup><Label>Password</Label><Input type="password" value={userData.password} onChange={e => setUserData({...userData, password: e.target.value})} /></FormGroup>
                        <FormGroup><Label>Role</Label><Input type="select" value={userData.role} onChange={e => setUserData({...userData, role: e.target.value})}><option>CLIENT</option><option>ADMIN</option></Input></FormGroup>
                    </Form>
                </ModalBody>
                <ModalFooter><Button color="primary" onClick={saveUser}>Save</Button></ModalFooter>
            </Modal>

            {/* Device Modal */}
            <Modal isOpen={deviceModal} toggle={() => setDeviceModal(!deviceModal)}>
                <ModalHeader>{editingDevice ? 'Edit Device' : 'Add Device'}</ModalHeader>
                <ModalBody>
                    <Form>
                        <FormGroup><Label>Name</Label><Input value={deviceData.name} onChange={e => setDeviceData({...deviceData, name: e.target.value})} /></FormGroup>
                        <FormGroup><Label>Max Consumption</Label><Input type="number" value={deviceData.maxConsumption} onChange={e => setDeviceData({...deviceData, maxConsumption: e.target.value})} /></FormGroup>

                        {/* [FIX] Owner Dropdown uses USERNAME as value */}
                        <FormGroup>
                            <Label>Owner</Label>
                            <Input type="select" value={deviceData.ownerUsername} onChange={e => setDeviceData({...deviceData, ownerUsername: e.target.value})}>
                                <option value="">-- No Owner --</option>
                                {users.filter(u => u.role === 'CLIENT').map(u => (
                                    // Value is u.username because that is what the backend expects
                                    <option key={u.id} value={u.username}>{u.username}</option>
                                ))}
                            </Input>
                        </FormGroup>
                    </Form>
                </ModalBody>
                <ModalFooter><Button color="primary" onClick={saveDevice}>Save</Button></ModalFooter>
            </Modal>

            <Chat stompClient={stompClient} username="admin" isAdmin={true} />
        </div>
    );
}