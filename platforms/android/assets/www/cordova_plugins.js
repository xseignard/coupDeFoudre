cordova.define('cordova/plugin_list', function(require, exports, module) {
module.exports = [
    {
        "file": "plugins/org.stereolux.cordova.serial/www/serial.js",
        "id": "org.stereolux.cordova.serial.Serial",
        "clobbers": [
            "window.serial"
        ]
    }
]
});