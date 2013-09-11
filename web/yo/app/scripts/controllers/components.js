'use strict';

angular.module('dmpApp')
  .controller('ComponentsCtrl', ['$scope', '$http', function ($scope, $http) {
    $scope.internalName = 'Function List Widget';

    $scope.functions = {
      'name': 'Functions',
      'show': true,
      'children': []
    };

    $http.get('/data/functions.json')
      .success(function (result) {
        $scope.functions.children = result['functions'];
      });
  }]);
