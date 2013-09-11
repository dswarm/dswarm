'use strict';

angular.module('dmpApp')
  .controller('SchemaCtrl', ['$scope', '$http', 'schemaParser', function ($scope, $http, schemaParser) {
    $scope.internalName = 'Source Target Schema Mapper'

    $scope.data = {}

    $http.get('/data/schema.json')
      .success(function (result) {
        $scope.data = schemaParser.mapData(result['title'], result)
      })
  }])
