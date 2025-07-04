const path = require('path');
const isProduction = process.env.NODE_ENV == 'production';
module.exports = {
  entry: './pdf-to-html.js',
  output: {
    filename: 'pdf-to-html.bundle.js',
    path: path.resolve(__dirname, 'dist'),
    library: {
      type: 'umd',
      name: 'pdfToHtmlLib',
    },
  },
  mode: isProduction ? 'production' : 'development',
  experiments: {
    topLevelAwait: true,
  },
  resolve: {
    fallback: {
      fs: false,
      path: false,
      stream: false,
      crypto: false,
    },
  },
};
