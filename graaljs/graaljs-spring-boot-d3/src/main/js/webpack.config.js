const path = require('path');

module.exports = {
  entry: './graph.js',
  output: {
    path: path.resolve(__dirname, '../../../target/classes/js/bundle'),
    filename: 'graph.bundle.js',
    libraryTarget: 'umd',
    globalObject: 'this',
  },
  mode: 'production',
  target: 'web',
  module: {
    rules: [
      {
        test: /\.js$/,
        exclude: /node_modules/,
        use: {
          loader: 'babel-loader',
        },
      },
    ],
  },
};
