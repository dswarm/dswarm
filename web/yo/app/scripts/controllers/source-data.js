'use strict';

angular.module('dmpApp')
  .controller('SourceDataCtrl', ['$scope', '$http', '$q', 'schemaParser', function ($scope, $http, $q, schemaParser) {
    $scope.internalName = 'Source Data Widget'

    $scope.data = {}

    var schemaPromise = $http.get('/data/schema.json')
      , dataPromise = $http.get('/data/record.json')
      , allPromise = $q.all([schemaPromise, dataPromise])

    allPromise.then(function (result) {
      var schemaResult = result[0]['data']
        , dataResult = result[1]['data']

      $scope.data = schemaParser.parseAny(
        dataResult[schemaResult['title']], schemaResult['title'], schemaResult)

    })
  }])
