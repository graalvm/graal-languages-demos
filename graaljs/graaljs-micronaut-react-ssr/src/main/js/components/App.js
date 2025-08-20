import React from 'react';
import SimpleLineChart from './SimpleLineChart';

function App({ title }) {
    return (
        <html>
            <head>
                <title>{title}</title>
                <meta charSet="UTF-8" />
            </head>
            <body>
                 <div>
                    <h2>Line Chart</h2>
                    <SimpleLineChart />
                 </div>
            </body>
        </html>
    );
}

export default App;
