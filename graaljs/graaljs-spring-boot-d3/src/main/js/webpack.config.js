const path = require('path');

module.exports = {
  entry: './d3-chord.js',
  output: {
    path: path.resolve(process.env.BUILD_DIR) || path.resolve(__dirname, '../../../target/classes/js'),
    filename: 'd3-chord.bundle.js',
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
