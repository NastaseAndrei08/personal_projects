import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import {
    Navbar, NavbarBrand, Nav, NavItem, Button, Container, Row, Col, Card, CardBody, CardTitle, CardText
} from 'reactstrap';
import EnergyChart from './EnergyChart';
import Chat from './Chat';
import { toast } from 'react-toastify';

// [FIX] Destructure 'isConnected' from props
export default function Client({ stompClient, isConnected }) {
    const [devices, setDevices] = useState([]);
    const [selectedDeviceId, setSelectedDeviceId] = useState(null);
    const navigate = useNavigate();

    const token = localStorage.getItem('token');
    // We use username for topics now, not userId
    const username = localStorage.getItem('username') || "client";

    // 1. Fetch Devices on Load
    useEffect(() => {
        if (!token) { navigate('/login'); return; }

        axios.get('http://localhost:8080/api/my/devices', {
            headers: { Authorization: `Bearer ${token}` }
        })
            .then(res => setDevices(res.data))
            .catch((err) => {
                console.error("Failed to fetch devices:", err);
                if (err.response && (err.response.status === 401 || err.response.status === 403)) {
                    navigate('/login');
                }
            });
    }, [token, navigate]);

    // 2. WebSocket: Listen for Personal Notifications
    useEffect(() => {
        // [FIX] Check 'isConnected' instead of 'stompClient.connected'
        if (stompClient && isConnected && username) {
            console.log("Subscribing to alerts for User: " + username);

            // Subscribe to private topic using the username
            const sub = stompClient.subscribe(`/topic/alerts/${username}`, (message) => {
                const body = JSON.parse(message.body);
                // Trigger Toast Popup
                toast.error(`⚠️ OVERCONSUMPTION: ${body.message}`, {
                    position: "top-right",
                    autoClose: 10000,
                    hideProgressBar: false,
                    closeOnClick: true,
                    pauseOnHover: true,
                    draggable: true,
                });
            });

            return () => sub.unsubscribe();
        }
    }, [stompClient, isConnected, username]); // [FIX] Re-run when connection finishes

    return (
        <div style={{ backgroundColor: '#f8f9fa', minHeight: '100vh' }}>
            <Navbar color="dark" dark expand="md" className="mb-4 px-4">
                <NavbarBrand href="/">Energy Client Dashboard</NavbarBrand>
                <Nav className="ms-auto" navbar>
                    <NavItem>
                        <Button color="danger" outline onClick={() => { localStorage.clear(); navigate('/login'); }}>
                            Logout
                        </Button>
                    </NavItem>
                </Nav>
            </Navbar>

            <Container fluid>
                <Row>
                    {/* Device List Column */}
                    <Col md="4">
                        <h4 className="mb-3">My Devices</h4>
                        {devices.length === 0 && <p className="text-muted">No devices assigned.</p>}

                        {devices.map(d => (
                            <Card
                                key={d.id}
                                className="mb-3 shadow-sm"
                                onClick={() => setSelectedDeviceId(d.id)}
                                style={{
                                    cursor: 'pointer',
                                    border: selectedDeviceId === d.id ? '2px solid #007bff' : 'none',
                                    transition: '0.2s'
                                }}
                            >
                                <CardBody>
                                    <CardTitle tag="h5">{d.name}</CardTitle>
                                    <CardText>
                                        <strong>Max Limit:</strong> {d.maxConsumption} kWh
                                    </CardText>
                                </CardBody>
                            </Card>
                        ))}
                    </Col>

                    {/* Chart Column */}
                    <Col md="8">
                        {selectedDeviceId ? (
                            <Card className="shadow-lg p-3">
                                <h4 className="text-center mb-4">Consumption History</h4>
                                <EnergyChart deviceId={selectedDeviceId} />
                            </Card>
                        ) : (
                            <div className="d-flex justify-content-center align-items-center" style={{ height: '200px', border: '2px dashed #ccc', borderRadius: '10px' }}>
                                <span className="text-muted h5">Select a device to view chart</span>
                            </div>
                        )}
                    </Col>
                </Row>
            </Container>

            {/* Chat Widget */}
            <Chat stompClient={stompClient} username={username} isAdmin={false} />
        </div>
    );
}