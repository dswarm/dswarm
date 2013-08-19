'use strict';

angular.module('dmpApp')
  .controller('TreeCtrl', ['$scope', function ($scope) {
    $scope.chevron = function (data) {
      if (data.children && data.children.length) {
        return 'icon-chevron-' + (data.show ? 'down' : 'right')
      }
    }

    $scope.handleClick = function (evt, data) {
      if (data.leaf || !data.children || !data.children.length) {
        $scope.$emit('leafClicked', {
          event: evt,
          data: data
        });
      }
    }

    $scope.expandCollapse = function (data) {
      data.show = data.children && data.children.length && !data.show
    }
  }])
  .directive('tree', ['$compile', function ($compile) {
    return {
      restrict: 'E',
      scope: {
        data: '=',
        onLeafClick: '&'
      },
      templateUrl: 'views/directives/tree.html',
      controller: 'TreeCtrl',
      compile: function (tElement, tAttrs) {
        var contents = tElement.contents().remove()
          , compiledContents
          , isInternal = angular.isDefined(tAttrs.internal);

        return function (scope, iElement) {
          if (!compiledContents) {
            compiledContents = $compile(contents)
          }

          if (!isInternal) {
            scope.$on('leafClicked', function(evt, data) {
              evt.stopPropagation();
              evt.preventDefault();

              scope.onLeafClick(data);
            });
          }

          compiledContents(scope, function (clone) {
            iElement.append(clone)
          })
        }
      }
    }
  }])
