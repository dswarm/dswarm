'use strict';

angular.module('dmpApp')
  .controller('TreeCtrl', ['$scope', function ($scope) {
    $scope.chevron = function (data) {
      if (data.children && data.children.length) {
        return "icon-chevron-" + (data.show ? "down" : "right")
      }
    }

    $scope.expandCollapse = function (data) {
      data.show = data.children && data.children.length && !data.show
    }
  }])
  .directive("tree", ['$compile', function ($compile) {
    return {
      restrict: "E",
      scope: {data: '='},
      templateUrl: 'views/directives/tree.html',
      controller: 'TreeCtrl',
      compile: function (tElement) {
        var contents = tElement.contents().remove()
          , compiledContents

        return function (scope, iElement) {
          if (!compiledContents) {
            compiledContents = $compile(contents)
          }

          compiledContents(scope, function (clone) {
            iElement.append(clone)
          })
        }
      }
    }
  }])
