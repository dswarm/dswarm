'use strict';

angular.module('dmpApp')
  .service('treeService', ['$compile', function ($compile) {
    return {
      restrict: 'E',
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
