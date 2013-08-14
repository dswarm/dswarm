'use strict';

angular.module('dmpApp')
  .controller('SchemaCtrl', ['$scope', '$http', function ($scope, $http) {
    $scope.internalName = "Source Target Schema Mapper"

    $scope.sourceSchema = {}

    $http.get('/data/schema.json')
      .success(function (result) {
        $scope.sourceSchema = result
      })
  }])
