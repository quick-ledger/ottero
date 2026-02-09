const webpack = require('webpack');

module.exports = function override(config) {
  config.resolve.fallback = {
    stream: require.resolve('stream-browserify'),
    crypto: require.resolve('crypto-browserify'),
    util: require.resolve('util/'),
    // Add other polyfills as needed
  };

  config.plugins.push(
    new webpack.ProvidePlugin({
      process: 'process/browser.js', // Ensure the .js extension is included
      Buffer: ['buffer', 'Buffer'],
    })
  );

  return config;
};