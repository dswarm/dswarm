'use strict';

angular.module('dmpApp')
  .directive('dmpConnectible', ['$timeout', 'jsP', function ($timeout, jsP) {
    return {
      restrict: 'A',
      scope: false,
      controller: ['$scope', '$element', function($scope, $element) {
        $scope.$element = $element;
      }],
      link: function (scope, iElement) {
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
