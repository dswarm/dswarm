'use strict';

angular.module('dmpApp')
  .directive('functionSource', ['$timeout', 'jsP', function($timeout, jsP) {
    return {
      restrict: 'C',
      link: function(scope, iElement, iAttrs) {
        var drawn = false;
        scope.$watch(iAttrs.connectWhen, function(source) {
          if (!drawn && source) {
            var connectWith = scope.$eval(iAttrs.connectWith);
            angular.forEach(connectWith, function (defined, selector) {
              if (defined && (defined.length || angular.isObject(defined))) {
                var target = iElement.siblings(selector);
                if (target.length) {
                  $timeout(function () {
                    var opts = {
                      anchors: [
                        [0, 1, 0, 1, -4, -9],
                        [0, 0, 0, -1,-4, -9]
                      ],
                      connector: 'Straight',
                      endpoint: 'Blank',
                      overlays: [
                        ['Arrow', {
                          location: 1,
                          width: 10,
                          length: 12,
                          foldback: 0.75
                        }]
                      ],
                      paintStyle: {
                        lineWidth: 3,
                        strokeStyle: 'black'
                      }
                    };
                    jsP.connect(iElement, target, opts);
                    opts.anchors[0][0] = 1;
                    opts.anchors[0][4] = -8;
                    opts.anchors[1][0] = 1;
                    opts.anchors[1][4] = -8;
                    jsP.connect(iElement, target, opts);

                    drawn = true;
                  }, 0);
                }
              }
            });
          }
        });
      }
    };
  }])
  .directive('dmpConnectible', ['$timeout', 'jsP', function ($timeout, jsP) {
    return {
      restrict: 'A',
      scope: false,
      controller: ['$scope', '$element', function($scope, $element) {
        $scope.$element = $element;
      }],
      link: function (scope, iElement) {
        scope.$on('tabSwitch', function() {
          jsP.detachAll(scope.$element);
        });

        $timeout(function() {
          var src = scope.$element.prev('.function')
            , next = scope.$element.nextAll('.function')
            , outbound = src.data('_outbound');

          jsP.detach(outbound, src, next);

          if (next.length) {
            var prev = iElement
              , l = next.length;

            for (var i = 0; i < l; i++) {
              var current = next.slice(i, i + 1);

              if (current.length) {
                outbound = prev.data('_outbound');
                jsP.detach(outbound, prev, current);
                jsP.connect(prev, current);
              }

              prev = current;
            }
          }
          if (src.length) {
            jsP.connect(src, iElement);
          }
        }, 0, false);
      }
    };
  }]);
