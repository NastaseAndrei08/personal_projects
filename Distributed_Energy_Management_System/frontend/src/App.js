import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './Login';
import Client from './Client';
import Admin from './Admin';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

// WebSocket Imports
import SockJS from 'sockjs-client';
import { Client as StompClient } from '@stomp/stompjs';

function App() {
    const [userRole, setUserRole] = useState(localStorage.getItem('role'));
    const [stompClient, setStompClient] = useState(null);
    // [FIX 1] Track connection status to force re-renders on connect
    const [isConnected, setIsConnected] = useState(false);

    useEffect(() => {
        // Connect to Backend WebSocket
        const socket = new SockJS('http://localhost:8080/ws');
        const client = new StompClient({
            webSocketFactory: () => socket,
            reconnectDelay: 5000,
            onConnect: () => {
                console.log(">>> WebSocket Connected!");
                // [FIX 2] Update state so React knows we are ready
                setIsConnected(true);
            },
            onStompError: (frame) => {
                console.error('Broker error: ' + frame.headers['message']);
            },
            onDisconnect: () => {
                setIsConnected(false);
            }
        });

        client.activate();
        setStompClient(client);

        return () => {
            client.deactivate();
        };
    }, []);

    const handleLogin = (role) => {
        setUserRole(role);
    };

    return (
        <div className="App">
            <ToastContainer position="top-right" autoClose={5000} />
            <Router>
                <Routes>
                    <Route path="/login" element={<Login onLogin={handleLogin} />} />

                    {/* [FIX 3] Pass 'isConnected' to Client so it knows when to subscribe */}
                    <Route
                        path="/client"
                        element={userRole === 'CLIENT' ?
                            <Client stompClient={stompClient} isConnected={isConnected} />
                            : <Navigate to="/login" />}
                    />

                    <Route
                        path="/admin"
                        element={userRole === 'ADMIN' ?
                            <Admin stompClient={stompClient} />
                            : <Navigate to="/login" />}
                    />

                    <Route path="*" element={<Navigate to="/login" />} />
                </Routes>
            </Router>
        </div>
    );
}

export default App;