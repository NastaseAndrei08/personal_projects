import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { jwtDecode } from "jwt-decode";
import {
    Container, Row, Col, Card, CardBody, Form, FormGroup, Label, Input, Button, Alert
} from 'reactstrap';

export default function Login({ onLogin }) {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [isRegister, setIsRegister] = useState(false); // Toggle state
    const [role, setRole] = useState('CLIENT');
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    const handleAuth = async () => {
        setError(null);
        try {
            const endpoint = isRegister
                ? 'http://localhost:8080/api/auth/register'
                : 'http://localhost:8080/api/auth/login';

            const payload = isRegister
                ? { username, password, role }
                : { username, password };

            const res = await axios.post(endpoint, payload);

            if (isRegister) {
                alert("Registration successful! Please login.");
                setIsRegister(false); // Switch back to login mode
            } else {
                // --- LOGIN SUCCESS LOGIC ---
                const token = res.data.token;
                localStorage.setItem('token', token);

                // Decode Token to get Details
                const decoded = jwtDecode(token);
                localStorage.setItem('role', decoded.role);
                localStorage.setItem('username', decoded.sub);

                // CRITICAL FOR ASSIGNMENT 3: Save User ID
                // Ensure your JWT (M1 Service) actually includes the "id" claim.
                // If it doesn't, you might need to fetch it via /api/users/me
                if (decoded.id) {
                    localStorage.setItem('userId', decoded.id);
                } else if (res.data.id) {
                    // Fallback if ID is in the response body directly
                    localStorage.setItem('userId', res.data.id);
                }

                // Update Parent State
                if (onLogin) onLogin(decoded.role);

                // Redirect
                navigate(decoded.role === 'ADMIN' ? '/admin' : '/client');
            }
        } catch (err) {
            console.error(err);
            setError(isRegister ? "Registration failed. Username may exist." : "Invalid credentials.");
        }
    };

    return (
        <div style={{ backgroundColor: '#f0f2f5', minHeight: '100vh', display: 'flex', alignItems: 'center' }}>
            <Container>
                <Row className="justify-content-center">
                    <Col md="6" lg="4">
                        <Card className="shadow border-0">
                            <CardBody className="p-4">
                                <h3 className="text-center mb-4 text-primary">
                                    {isRegister ? 'Create Account' : 'Welcome Back'}
                                </h3>

                                {error && <Alert color="danger">{error}</Alert>}

                                <Form>
                                    <FormGroup className="mb-3">
                                        <Label>Username</Label>
                                        <Input
                                            type="text"
                                            value={username}
                                            onChange={e => setUsername(e.target.value)}
                                        />
                                    </FormGroup>

                                    <FormGroup className="mb-3">
                                        <Label>Password</Label>
                                        <Input
                                            type="password"
                                            value={password}
                                            onChange={e => setPassword(e.target.value)}
                                        />
                                    </FormGroup>

                                    {/* Show Role Selection ONLY during Registration */}
                                    {isRegister && (
                                        <FormGroup className="mb-3">
                                            <Label>Role</Label>
                                            <Input type="select" value={role} onChange={e => setRole(e.target.value)}>
                                                <option value="CLIENT">Client</option>
                                                <option value="ADMIN">Admin</option>
                                            </Input>
                                        </FormGroup>
                                    )}

                                    <Button color="primary" block size="lg" className="w-100 mt-3" onClick={handleAuth}>
                                        {isRegister ? 'Register' : 'Login'}
                                    </Button>
                                </Form>

                                <div className="text-center mt-4">
                                    <span
                                        style={{ cursor: 'pointer', color: '#0d6efd', fontWeight: '500' }}
                                        onClick={() => { setIsRegister(!isRegister); setError(null); }}
                                    >
                                        {isRegister ? 'Already have an account? Login' : 'Need an account? Register'}
                                    </span>
                                </div>
                            </CardBody>
                        </Card>
                    </Col>
                </Row>
            </Container>
        </div>
    );
}