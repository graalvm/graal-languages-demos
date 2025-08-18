import React from 'react';
import { LineChart, Line, CartesianGrid, XAxis, YAxis, Tooltip, Legend, ResponsiveContainer } from 'recharts';

// Original line chart example from Recharts:
// https://recharts.org/en-US/examples/SimpleLineChart
const data = [
  { name: 'A', uv: 400, pv: 240 },
  { name: 'B', uv: 300, pv: 456 },
  { name: 'C', uv: 300, pv: 139 },
  { name: 'D', uv: 200, pv: 980 },
  { name: 'E', uv: 278, pv: 390 },
  { name: 'F', uv: 189, pv: 480 },
];

function SimpleLineChart() {
  return (
    <LineChart width={500} height={300} data={data}>
      <XAxis dataKey="name" />
      <YAxis />
      <CartesianGrid stroke="#eee" strokeDasharray="5 5" />
      <Line type="monotone" dataKey="uv" stroke="#ff7300" />
      <Line type="monotone" dataKey="pv" stroke="#387908" />
    </LineChart>
  );
}

export default SimpleLineChart;
