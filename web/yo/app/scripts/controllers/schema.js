'use strict';

angular.module('dmpApp')
  .controller('SchemaCtrl', ['$scope', '$http', function ($scope, $http) {
    $scope.internalName = "Source Target Schema Mapper"

    $scope.sourceSchema = {}

    $scope.chevron = function (data) {
      if (data.show) {
        if (data.children && data.children.length) {
          return "icon-chevron-down"
        }
      } else {
        return "icon-chevron-right"
      }
    }

    $http.get('/data/schema.json')
      .success(function (result) {
        $scope.sourceSchema = result
      })
  }])
