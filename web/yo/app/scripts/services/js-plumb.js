'use strict';

angular.module('dmpApp')
  /**
   * Provides configurable options for jsPlumb.
   * @see http://jsplumbtoolkit.com/doc/parameters
   * These will be used whenever a new connection is created.
   * to configure:
   * `myApp.config(function(jsPlumbOptionsProvider) {
   *    jsPlumbOptionsProvider.set({
   *      paintStyle: {
   *       strokeStyle: 'red'
   *      }
   *    });
   *  });`
   *
   */
  .provider('jsPlumbOptions', function() {
    var defaultOptions = {
      anchors: [                  // [anchor for source, anchor for target], everything is a [x, y, dx, dy, ox, oy]
        [1, 0.5, 1, 0, -9, -6],   // [right-most border, center), pointing right, pointing horizontally, move 9 pixels to the left, move 6 pixels to the top]
        [0, 0.5, -1, 0, -7, -6]   // [left-most border, center, pointing left, pointing horizontally, move 7 pixels to the left, move 6 pixels to the top]
      ],
      endpoint: 'Blank',          // do not display eny endpoints
      connector: ['Flowchart', {  // this thing makes a nice corner when elements overflow into the next row
        cornerRadius: 5
      }],
      detachable: false,          // do not allow interaction with mouse
      paintStyle: {
        lineWidth: 3,
        strokeStyle: 'black'
      },
      overlays: [
        ['Arrow', {
          location: 1,            // at the source-end
          width: 10,              // widest point of arrowhead
          length: 12,             // longest span of arrowhead
          foldback: 0.75          // tailpoint is at 9 pixels (9 / 12)
        }]
      ]
    } , options;

    function setDefault() {
      options = angular.extend({}, defaultOptions);
    }
    setDefault();

    this.set = function(opts) {
      options = angular.extend(options, opts);
    };

    this.setDefault = setDefault;

    this.$get = function() {
      if (!options) {
        setDefault();
      }
      return options;
    };
  })
  /**
   * Provides an injectable instance of jsPlumb. Defaults to jsPlumb.getInstance
   * but can be mocked out
   */
  .provider('jsPlumb', function() {
    var instance = null;

    function setDefaultInstance() {
      /* global jsPlumb */
      instance = jsPlumb.getInstance();
    }

    this.$get = function() {
      if (!instance) {
        setDefaultInstance();
      }
      return instance;
    };

    this.setInstance = function(inst) {
      instance = inst;
    };
  })
  /**
   * Provides the js-plumb service that is meant to be used by the application
   * code that deals with the jsPlumb specifics should go in here.
   */
  .factory('jsP', ['jsPlumbOptions', 'jsPlumb', function(jsPlumbOptions, jsPlumb) {
    /**
     * Creates a new connection between two nodes, that is, it draws an arrow
     * unless configured otherwise. connection is directed from source to target
     * @param source {JQLite|jQuery} source of the new connection
     * @param target {JQLite|jQuery} target of the new connection
     * @returns {jsPlumb.Connection}
     */
    function connect(source, target) {
      var connection = jsPlumb.connect(angular.extend({
        source: source,
        target: target
      }, jsPlumbOptions));

      source.data('_outbound', connection);

      return connection;
    }

    /**
     * Detaches an existing connection between the two given elements.
     * @param connection {jsPlumb.Connection}
     * @param source {JQLite|jQuery}
     * @param target {JQLite|jQuery}
     */
    function detach(connection, source, target) {
      if (connection && connection.source === source[0] && connection.target === target[0]) {
        jsPlumb.detach(connection);
        source.data('_outbound', null);
      }
    }

    return {
      connect:connect,
      detach: detach
    };
  }]);
