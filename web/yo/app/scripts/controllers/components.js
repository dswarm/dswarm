'use strict';

angular.module('dmpApp')
  .controller('ComponentsCtrl', ['$scope', '$http', function ($scope, $http) {
    $scope.internalName = 'Function List Widget';

    $scope.functions = {
      'name': 'Functions',
      'show': true,
      'children': []
    };

    $scope.draggableOptions = {
      appendTo: 'body',
      connectToSortable: '.functionSortable',
      containment: '#transformation',
      cursor: 'move',
      cursorAt: {top: -5, left: -5},
      helper: 'clone',
      opacity: 0.7,
      revert: 'invalid',
      revertDuration: 400
    };

    $http.get('/data/functions.json')
      .success(function (result) {
        $scope.functions.children = result['functions'];
      });
  }]);
