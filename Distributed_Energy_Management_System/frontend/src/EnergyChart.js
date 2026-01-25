import React, { useState, useEffect } from 'react';
import axios from 'axios';
import {
    LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, BarChart, Bar
} from 'recharts';
import { Card, CardBody, CardTitle, Input, Button, ButtonGroup } from 'reactstrap';

export default function EnergyChart({ deviceId }) {
    const [data, setData] = useState([]);
    const [date, setDate] = useState(new Date().toISOString().split('T')[0]); // Today YYYY-MM-DD
    const [chartType, setChartType] = useState('line');

    useEffect(() => {
        if (!deviceId) return;
        fetchHistory();
    }, [deviceId, date]);

    const fetchHistory = async () => {
        const token = localStorage.getItem('token');
        try {
            // M4 Endpoint
            const res = await axios.get(`http://localhost:8080/api/monitoring/history/${deviceId}?date=${date}`, {
                headers: { Authorization: `Bearer ${token}` }
            });

            // Format data for Recharts (extract hour from timestamp)
            const formatted = res.data.map(item => ({
                hour: new Date(item.timestamp).getHours() + ":00",
                kwh: item.totalConsumption
            }));

            // Sort by hour
            formatted.sort((a,b) => parseInt(a.hour) - parseInt(b.hour));
            setData(formatted);
        } catch (err) {
            console.error("Failed to fetch history", err);
        }
    };

    return (
        <Card className="mt-4 border-0 shadow-sm">
            <CardBody>
                <CardTitle tag="h5" className="mb-3 d-flex justify-content-between align-items-center">
                    <span>Energy Consumption</span>
                    <div className="d-flex gap-2">
                        <Input
                            type="date"
                            value={date}
                            onChange={e => setDate(e.target.value)}
                            style={{ width: 'auto' }}
                        />
                        <ButtonGroup>
                            <Button size="sm" outline={chartType !== 'line'} color="primary" onClick={() => setChartType('line')}>Line</Button>
                            <Button size="sm" outline={chartType !== 'bar'} color="primary" onClick={() => setChartType('bar')}>Bar</Button>
                        </ButtonGroup>
                    </div>
                </CardTitle>

                <div style={{ height: 300 }}>
                    <ResponsiveContainer width="100%" height="100%">
                        {chartType === 'line' ? (
                            <LineChart data={data}>
                                <CartesianGrid strokeDasharray="3 3" />
                                <XAxis dataKey="hour" />
                                <YAxis unit=" kWh" />
                                <Tooltip />
                                <Legend />
                                <Line type="monotone" dataKey="kwh" stroke="#8884d8" strokeWidth={2} />
                            </LineChart>
                        ) : (
                            <BarChart data={data}>
                                <CartesianGrid strokeDasharray="3 3" />
                                <XAxis dataKey="hour" />
                                <YAxis unit=" kWh" />
                                <Tooltip />
                                <Legend />
                                <Bar dataKey="kwh" fill="#82ca9d" />
                            </BarChart>
                        )}
                    </ResponsiveContainer>
                    {data.length === 0 && <p className="text-center text-muted mt-3">No data for selected date</p>}
                </div>
            </CardBody>
        </Card>
    );
}