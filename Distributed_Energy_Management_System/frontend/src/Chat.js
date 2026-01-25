import React, { useState, useEffect, useRef } from 'react';
import './Chat.css';

const Chat = ({ stompClient, username, isAdmin }) => {
    const [isOpen, setIsOpen] = useState(false);
    const [messages, setMessages] = useState([]);
    const [input, setInput] = useState("");
    const [typingUser, setTypingUser] = useState("");
    const messagesEndRef = useRef(null);

    const toggleChat = () => setIsOpen(!isOpen);

    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    }, [messages, isOpen]);

    useEffect(() => {
        if (!stompClient || !stompClient.connected) return;

        // Subscribe to Messages
        const msgSub = stompClient.subscribe('/topic/messages', (message) => {
            const body = JSON.parse(message.body);
            setMessages((prev) => [...prev, body]);
            setTypingUser("");
        });

        // Subscribe to Typing Indicator
        const typeSub = stompClient.subscribe('/topic/typing', (message) => {
            if (message.body !== username) {
                setTypingUser(message.body);
                setTimeout(() => setTypingUser(""), 3000);
            }
        });

        return () => {
            msgSub.unsubscribe();
            typeSub.unsubscribe();
        };
    }, [stompClient, username]);

    const handleSend = () => {
        if (input.trim() && stompClient) {
            const chatMessage = {
                senderId: username,
                content: input,
                isAdmin: isAdmin
            };
            stompClient.publish({
                destination: "/app/send",
                body: JSON.stringify(chatMessage)
            });
            setInput("");
        }
    };

    const handleTyping = (e) => {
        setInput(e.target.value);
        stompClient.publish({ destination: "/app/typing", body: username });
    };

    return (
        <div style={{ position: 'fixed', bottom: '20px', right: '20px', zIndex: 9999 }}>
            {!isOpen && (
                <button onClick={toggleChat} style={{
                    backgroundColor: isAdmin ? '#dc3545' : '#007bff',
                    color: 'white', padding: '15px 20px', borderRadius: '50%', border: 'none', boxShadow: '0 4px 8px rgba(0,0,0,0.3)', fontSize: '24px'
                }}>
                    💬
                </button>
            )}

            {isOpen && (
                <div style={{ width: '320px', height: '450px', backgroundColor: 'white', borderRadius: '10px', boxShadow: '0 5px 15px rgba(0,0,0,0.3)', display: 'flex', flexDirection: 'column' }}>

                    {/* Header */}
                    <div style={{ padding: '15px', backgroundColor: isAdmin ? '#dc3545' : '#007bff', color: 'white', borderTopLeftRadius: '10px', borderTopRightRadius: '10px', display: 'flex', justifyContent: 'space-between' }}>
                        <strong>{isAdmin ? 'Admin Chat' : 'Support Chat'}</strong>
                        <button onClick={toggleChat} style={{ background: 'transparent', border: 'none', color: 'white', fontSize: '16px', cursor: 'pointer' }}>X</button>
                    </div>

                    {/* Messages Body */}
                    <div style={{ flex: 1, padding: '10px', overflowY: 'auto', backgroundColor: '#f8f9fa' }}>
                        {messages.map((msg, index) => (
                            <div key={index} style={{ textAlign: msg.senderId === username ? 'right' : 'left', marginBottom: '10px' }}>
                                <div style={{
                                    display: 'inline-block',
                                    padding: '8px 12px',
                                    borderRadius: '15px',
                                    backgroundColor: msg.senderId === username ? (isAdmin ? '#dc3545' : '#007bff') : '#e9ecef',
                                    color: msg.senderId === username ? 'white' : 'black',
                                    maxWidth: '80%',
                                    wordWrap: 'break-word'
                                }}>
                                    {/* Render newlines for AI responses */}
                                    {msg.content.split('\n').map((line, i) => (
                                        <div key={i}>{line}</div>
                                    ))}
                                </div>
                                <div style={{ fontSize: '10px', color: '#888', marginTop: '2px' }}>{msg.senderId}</div>
                            </div>
                        ))}
                        <div ref={messagesEndRef} />
                    </div>

                    {/* Typing Status */}
                    {typingUser && (
                        <div style={{ padding: '0 10px', fontStyle: 'italic', fontSize: '12px', color: '#888' }}>
                            {typingUser} is typing...
                        </div>
                    )}

                    {/* Footer Input */}
                    <div style={{ padding: '10px', borderTop: '1px solid #ddd', display: 'flex' }}>
                        <input
                            type="text"
                            value={input}
                            onChange={handleTyping}
                            onKeyPress={(e) => e.key === 'Enter' && handleSend()}
                            placeholder="Type a message..."
                            style={{ flex: 1, marginRight: '10px', padding: '8px', borderRadius: '5px', border: '1px solid #ccc' }}
                        />
                        <button onClick={handleSend} style={{ backgroundColor: isAdmin ? '#dc3545' : '#007bff', color: 'white', border: 'none', borderRadius: '5px', padding: '0 15px' }}>Send</button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Chat;