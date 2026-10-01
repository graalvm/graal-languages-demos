const path = require('path');
const webpack = require('webpack');

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
  plugins: [
    new webpack.DefinePlugin({
      // The GraalJS context installs atob before evaluating the bundle.
      // This lets webpack remove the unreachable Node.js Buffer fallback.
      'typeof atob': JSON.stringify('function'),
    }),
  ],
};
