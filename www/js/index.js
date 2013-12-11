var app = {
    // Application Constructor
    initialize: function() {
        this.bindEvents();
    },
    // Bind Event Listeners
    //
    // Bind any events that are required on startup. Common events are:
    // 'load', 'deviceready', 'offline', and 'online'.
    bindEvents: function() {
        document.addEventListener('deviceready', this.onDeviceReady, false);
    },
    // deviceready Event Handler
    //
    // The scope of 'this' is the event. In order to call the 'receivedEvent'
    // function, we must explicity call 'app.receivedEvent(...);'
    onDeviceReady: function() {
        app.handleSerial();
    },
    handleSerial: function() {
        var buttons = document.getElementById('buttons');
        buttons.addEventListener('touchstart', function(event) {
            for (var i = 0; i < event.touches.length; i++) {
                var touch = event.touches[i];
                var elem = document.elementFromPoint(touch.pageX, touch.pageY);
                switch (elem.id) {
                    case 'manuel':
                        serial.write('1');
                        break;
                    case 'rythme':
                        serial.write('2');
                        break;
                    case 'melodie':
                        serial.write('3');
                        break;
                    case 'sequenceur':
                        serial.write('4');
                        break;
                }
            }
        });
        buttons.addEventListener('touchend', function(event) {
            var contains = false;
            for (var i = 0; i < event.touches.length; i++) {
                var touch = event.touches[i];
                var elem = document.elementFromPoint(touch.pageX, touch.pageY);
                if (elem.id === 'manuel') contains = true;
            }
            if (!contains) serial.write('0');
        });

        var errorCallback = function(message) {
            alert('Error: ' + message);
        };

        serial.requestPermission(
            function(successMessage) {
                serial.open(
                    {baudRate: 9600},
                    function(successMessage) {
                        alert('Connecté à la bobine!');
                    },
                    errorCallback
                );
            },
            errorCallback
        );
    }
};
