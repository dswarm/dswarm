'use strict';

angular.module('dmpApp')
  .controller('TreeCtrl', ['$scope', function ($scope) {
    $scope.isSource = $scope.$parent && $scope.$parent.isSource;
    $scope.isTarget = $scope.$parent && $scope.$parent.isTarget;

    $scope.update = $scope.$parent && $scope.$parent.update;

    $scope.jspSourceOptions = {
      scope: 'schema',
      container: 'schema',
      anchor: 'Continuous',
      endpoint: ['Dot', {
        radius: 5,
        cssClass: 'source-endpoint'
      }],
      connectorOverlays: [
        ['Arrow', {
          location: 1,
          width: 10,
          length: 12,
          foldback: 0.75
        }]
      ],
      connector: 'StateMachine',
      connectorStyle: {
        strokeStyle: 'black',
        lineWidth: 3
      },
      paintStyle: {
        fillStyle: 'black',
        lineWidth: 3
      }
    };

    $scope.jspTargetOptions = {
      scope: 'schema',
      container: 'schema',
      anchor: 'Continuous',
      endpoint: ['Dot', {
        radius: 5,
        cssClass: 'transparent'
      }],
      connector: 'StateMachine',
      connectorStyle: {
        strokeStyle: 'black',
        lineWidth: 3
      },
      paintStyle: {
        fillStyle: 'transparent',
        lineWidth: 3
      },
      dropOptions: {
        hoverClass: 'mapping-droppable'
      }
    };

    $scope.chevron = function (data) {
      if (data.children && data.children.length) {
        return 'glyphicon-chevron-' + (data.show ? 'down' : 'right');
      }
    };

    $scope.handleClick = function (evt, data) {
      if ($scope.isLeaf(data)) {
        $scope.$emit('leafClicked', {
          event: evt,
          data: data
        });
      }
    };

    $scope.expandCollapse = function (data) {
      data.show = !$scope.isLeaf(data) && !data.show;
    };

    $scope.isLeaf = function (data) {
      return data.leaf || !data.children || !data.children.length;
    };

  }])
  .directive('tree', ['$compile', function ($compile) {
    return {
      restrict: 'E',
      scope: {
        data: '=',
        onLeafClick: '&'
      },
      replace: true,
      templateUrl: 'views/directives/tree.html',
      controller: 'TreeCtrl',
      compile: function (tElement, tAttrs) {
        var contents = tElement.contents().remove()
          , compiledContents
          , isInternal = angular.isDefined(tAttrs.internal);

        return function (scope, iElement) {
          if (!compiledContents) {
            compiledContents = $compile(contents);
          }

          if (!isInternal) {
            scope.$on('leafClicked', function (evt, data) {
              evt.stopPropagation();
              evt.preventDefault();

              scope.onLeafClick(data);
            });

            scope.isSource = angular.isDefined(tAttrs.asSource);
            scope.isTarget = angular.isDefined(tAttrs.asTarget);
          }

          compiledContents(scope, function (clone) {
            iElement.append(clone);
          });
        };
      }
    };
  }]);
