(function() {

  /**
   * Handle multitouch and deal with it thanks to requestAnimationFrame (or its polyfill!)
   * @param {Object} opts - object containing:  
   *    - domElem : the DOM element to handle (body else)
   *    - events : an array of events to track (['touchstart', 'touchend'] else)
   *    - handleTouches : a user defined function where to deal with all these touch events
   */
  var Multitouch = function(opts) {
    // element on which you have to listen for touch events
    this.domElem = opts.domElem || document.body;
    var self = this;
    // touch events you want to track
    if (opts.handleTouchstart && typeof opts.handleTouchstart === 'function') {
      this.handleTouchstart = opts.handleTouchstart;
      this.domElem.addEventListener('touchstart', function(e) {
        self.handleTouchstart(e);
      }, false);
    }
    //
    if (opts.handleTouchend && typeof opts.handleTouchend === 'function') {
      this.handleTouchend = opts.handleTouchend;
      this.domElem.addEventListener('touchend', function(e) {
        self.handleTouchend(e);
      }, false);
    }
  };

  window.Multitouch = Multitouch;
}());