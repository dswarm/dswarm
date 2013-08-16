'use strict';

angular.module('dmpApp')
  .controller('SchemaCtrl', ['$scope', '$http', 'schemaParser', function ($scope, $http, schemaParser) {
    $scope.internalName = 'Source Target Schema Mapper'

    $scope.sourceSchema = {};
    $scope.targetSchema = {};

    var sourceSchema = $http.get('/data/schema.json')
        , targetSchema = $http.get('/data/targetschema.json')
        , allPromise = $q.all([sourceSchema, targetSchema]);

    allPromise.then(function (result) {
        var sourceSchema = result[0]['data']
            , targetSchema = result[1]['data'];

        $scope.sourceSchema = schemaParser.mapData(sourceSchema['title'], sourceSchema);
        $scope.targetSchema = schemaParser.mapData(targetSchema['title'], targetSchema);

    });

  }]);
