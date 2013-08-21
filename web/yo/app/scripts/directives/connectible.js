'use strict';

angular.module('dmpApp')
  .value('transformationConnectionOptions', {
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
  })
  .directive('dmpConnectible', ['transformationConnectionOptions', '$timeout', function (transformationConnectionOptions, $timeout) {
    return {
      restrict: 'A',
      scope: false,
      controller: ['$scope', '$element', function($scope, $element) {
        $scope.$element = $element;
      }],
      compile: function() {
        /* global jsPlumb */
        var jsP = jsPlumb.getInstance();

        function connect(source, target) {
          var connection = jsP.connect(angular.extend({
            source: source,
            target: target
          }, transformationConnectionOptions));

          source.data('_outbound', connection);

          return connection;
        }

        function detach(connection, source, target) {
          if (connection && connection.source === source[0] && connection.target === target[0]) {
            jsP.detach(connection);
            source.data('_outbound', null);
          }
        }

        return function (scope, iElement) {
          $timeout(function() {
            var src = scope.$element.prev('.function')
              , next = scope.$element.nextAll('.function')
              , outbound = src.data('_outbound');

            detach(outbound, src[0], next[0]);

            if (next.length) {
              var prev = iElement
                , l = next.length;

              for (var i = 0; i < l; i++) {
                var current = next.slice(i, i + 1);

                if (current.length) {
                  outbound = prev.data('_outbound');
                  detach(outbound, prev[0], current[0]);
                  connect(prev, current);
                }

                prev = current;
              }
            } else if (src.length) {
              connect(src, iElement);
            }
          }, 0, false);
        };
      }
    };
  }]);
