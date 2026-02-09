
//proxy for outgoing requests. should be in src folder

const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function(app) {
	//Proxy for /in-proxy
	app.use(
		'/in-ottero',
		createProxyMiddleware({
			target: 'https://auth.quickledger.net',
			changeOrigin: true,
			pathRewrite: {
				'^/in-ottero': '', // Remove /in-proxy from the request path
			},
			logLevel: 'debug',
			onProxyReq: (proxyReq, req, res) => {
				console.log('Proxying request:', req.url);
			  },
			  onProxyRes: (proxyRes, req, res) => {
				console.log('Received response from target:', proxyRes.statusCode);
			  },
		})
	);

	// Proxy for all other routes

	/**
	 * this was giving me a hard time. i just finally added a /api to the target and it worked!! doesn't make sense.
	 */
	app.use(

		'/api',
		createProxyMiddleware({
			target: 'http://localhost:8080/api',
			changeOrigin: true,
			// pathRewrite: {
			//   '^/api': '', // Remove /api from the request path
			// },			// Optionally, you can add more configurations here
			logLevel: 'debug',
			onProxyReq: (proxyReq, req, res) => {
				console.log('Proxying request:', req.url);
			  },
			  onProxyRes: (proxyRes, req, res) => {
				console.log('Received response from target:', proxyRes.statusCode);
			  },
			  onError: (err, req, res) => {
				console.error('Error proxying request:', err);
			  }

		})
	);
};

