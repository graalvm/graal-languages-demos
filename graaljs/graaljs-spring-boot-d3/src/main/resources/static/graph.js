import * as d3 from 'd3';
import { parseHTML } from 'linkedom';

// Creating a simulated 'document' object using linkedom
globalThis.document = parseHTML('<html><body></body></html>').document;

// Original chord diagram from D3 ObservableHQ:
// https://observablehq.com/@d3/chord-diagram/2
const dataMatrix = [
    [0, 20, 10, 5, 8, 3, 12, 1],
    [20, 0, 15, 7, 6, 9, 4, 2],
    [10, 15, 0, 12, 3, 5, 6, 8],
    [5, 7, 12, 0, 10, 1, 7, 3],
    [8, 6, 3, 10, 0, 14, 9, 11],
    [3, 9, 5, 1, 14, 0, 8, 6],
    [12, 4, 6, 7, 9, 8, 0, 10],
    [1, 2, 8, 3, 11, 6, 10, 0]
];

const colors = [
  "#89dceb",
  "#5fa8d3",
  "#468faf",
  "#2c7da0",
  "#1e6091",
  "#144552",
  "#0b2e38",
  "#000000"
];
const sum = dataMatrix.flat().reduce((a, b) => a + b, 0);
const tickStep = d3.tickStep(0, sum, 100);
const tickStepMajor = d3.tickStep(0, sum, 20);
const formatValue = d3.formatPrefix(",.0", tickStep);


function renderChord(width, height) {
    const outerRadius = Math.min(width, height) * 0.5 - 30;
    const innerRadius = outerRadius - 20;

    const chord = d3.chord()
        .padAngle(20 / innerRadius)
        .sortSubgroups(d3.descending);

    const arc = d3.arc()
        .innerRadius(innerRadius)
        .outerRadius(outerRadius);

    const ribbon = d3.ribbon()
        .radius(innerRadius);

    const svg = d3.create("svg")
        .attr("width", width)
        .attr("height", height)
        .attr("viewBox", [-width / 2, -height / 2, width, height])
        .attr("style", "max-width: 100%; height: auto; font: 10px sans-serif;");

    const chords = chord(dataMatrix);

    const group = svg.append("g")
        .selectAll()
        .data(chords.groups)
        .join("g");

    group.append("path")
        .attr("fill", d => colors[d.index])
        .attr("d", arc)
        .append("title")

    const groupTick = group.append("g")
        .selectAll()
        .data(d => groupTicks(d, tickStep))
        .join("g")
        .attr("transform", d => `rotate(${d.angle * 180 / Math.PI - 90}) translate(${outerRadius},0)`);

    groupTick.append("line")
        .attr("stroke", "currentColor")
        .attr("x2", 6);

    groupTick
        .filter(d => d.value % tickStepMajor === 0)
        .append("text")
        .attr("x", 8)
        .attr("dy", ".35em")
        .attr("transform", d => d.angle > Math.PI ? "rotate(180) translate(-16)" : null)
        .attr("text-anchor", d => d.angle > Math.PI ? "end" : null)
        .text(d => formatValue(d.value));

    svg.append("g")
        .attr("fill-opacity", 0.7)
        .selectAll()
        .data(chords)
        .join("path")
        .attr("d", ribbon)
        .attr("fill", d => colors[d.target.index])
        .attr("stroke", "white")
        .append("title")
    return svg.node().outerHTML
}

function groupTicks(d, step) {
    const k = (d.endAngle - d.startAngle) / d.value;
    return d3.range(0, d.value, step).map(value => ({value: value, angle: d.startAngle + value * k}));
}

globalThis.renderChord = renderChord;