import React from 'react';
import SimpleLineChart from './SimpleLineChart';
import SimpleAreaChart from "./SimpleAreaChart";
import SimpleBarChart from "./SimpleBarChart";
import VerticalComposedChart from "./VerticalComposedChart";
import TwoLevelPieChart from "./TwoLevelPieChart";
import SimpleTreemap from "./SimpleTreemap";

function App({ title, width, height }) {
    return (
        <html>
            <head>
                <title>{title}</title>
                <meta charSet="UTF-8" />
            </head>
            <body>
                 <div>
                     <h2>SimpleLineChart</h2>
                     <SimpleLineChart width={width} height={height}/>
                     <h2>SimpleAreaChart</h2>
                     <SimpleAreaChart width={width} height={height}/>
                     <h2>SimpleBarChart</h2>
                     <SimpleBarChart width={width} height={height}/>
                     <h2>VerticalComposedChart</h2>
                     <VerticalComposedChart width={width} height={height}/>
                     <h2>TwoLevelPieChart</h2>
                     <TwoLevelPieChart width={width} height={height}/>
                     <h2>SimpleTreemap</h2>
                     <SimpleTreemap width={width} height={height}/>
                 </div>
            </body>
        </html>
    );
}

export default App;
