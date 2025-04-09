var d3 = require('./node_modules/d3/dist/d3.js');
var linkedom = require('linkedom');

globalThis.document = linkedom.parseHTML('<html><body></body></html>').document;

const width = 600;
const height = 600;
const margin = 50;
const innerRadius = Math.min(width, height) * 0.5 - 60;
const outerRadius = innerRadius + 20;

const data = [
    [0, 20, 10, 5, 8, 3, 12, 1],
    [20, 0, 15, 7, 6, 9, 4, 2],
    [10, 15, 0, 12, 3, 5, 6, 8],
    [5, 7, 12, 0, 10, 1, 7, 3],
    [8, 6, 3, 10, 0, 14, 9, 11],
    [3, 9, 5, 1, 14, 0, 8, 6],
    [12, 4, 6, 7, 9, 8, 0, 10],
    [1, 2, 8, 3, 11, 6, 10, 0]
];

const names = ["Java", "JavaScript", "Python", "Ruby", "R", "C/C++", "Native Image", "Polyglot"];
const colors = ["#1f77b4", "#ff7f0e", "#2ca02c", "#d62728", "#9467bd", "#8c564b", "#e377c2", "#7f7f7f"];

const chord = d3.chord()
    .padAngle(0.05)
    .sortSubgroups(d3.descending)
    .sortChords(d3.descending);

const chords = chord(data);

const arc = d3.arc()
    .innerRadius(innerRadius)
    .outerRadius(outerRadius);

const ribbon = d3.ribbon()
    .radius(innerRadius);

const color = d3.scaleOrdinal()
    .domain(names)
    .range(colors);

const svg = d3.create('svg')
    .attr('width', width)
    .attr('height', height)
    .append('g')
    .attr('transform', `translate(${width / 2},${height / 2})`);

svg.append('g')
    .selectAll('g')
    .data(chords.groups)
    .enter()
    .append('g')
    .append('path')
    .style('fill', d => color(names[d.index]))
    .style('stroke', 'black')
    .attr('d', arc);

svg.append('g')
    .selectAll('path')
    .data(chords)
    .enter()
    .append('path')
    .attr('d', ribbon)
    .style('fill', d => color(names[d.source.index]))
    .style('opacity', 0.8)
    .style('stroke', 'black');

svg.append('g')
    .selectAll('text')
    .data(chords.groups)
    .enter()
    .append('text')
    .each(d => { d.angle = (d.startAngle + d.endAngle) / 2; })
    .attr('dy', '.35em')
    .attr('transform', d => `
        rotate(${(d.angle * 180 / Math.PI - 90)})
        translate(${outerRadius + 10})
        ${d.angle > Math.PI ? 'rotate(180)' : ''}
    `)
    .style('text-anchor', d => d.angle > Math.PI ? 'end' : null)
    .text(d => names[d.index]);

module.exports = svg.node().outerHTML;