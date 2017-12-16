var webpack = require('webpack');
var path = require('path');

var BUILD_DIR = path.resolve(__dirname, 'src/main/resources/public/build');
var APP_DIR = path.resolve(__dirname, 'src/main/js');

var config = {
    entry: APP_DIR + '/app.jsx',
    output: {
        path: BUILD_DIR,
        filename: 'bundle.js'
    },
    module : {
        loaders : [
            {
                test : /\.jsx?/,
                include : APP_DIR,
                use: {
                    loader: 'babel-loader',
                    options: {
                        presets: ["es2015", "react"]
                    }
                }
            }
        ]
    }
};

module.exports = config;
