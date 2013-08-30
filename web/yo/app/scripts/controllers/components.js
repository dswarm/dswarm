'use strict';

angular.module('dmpApp')
  .controller('ComponentsCtrl', ['$scope', '$http', function ($scope, $http) {
    $scope.internalName = 'Function List Widget';

    /* jshint unused:false */
    //noinspection JSUnusedLocalSymbols
    /**
     * @typedef {{name: string, show: boolean, children: Array.<TreeModel>}}
     */
    var TreeModel;

    /**
     * Model for a list of functions, that are available.
     *
     * @type {TreeModel}
     */
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
