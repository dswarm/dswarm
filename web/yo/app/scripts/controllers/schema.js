'use strict';

angular.module('dmpApp')
  .controller('SchemaCtrl', ['$scope', '$http', '$q', function ($scope, $http, $q) {
    $scope.internalName = 'Source Target Schema Mapper';

    $scope.sourceSchema = {};
    $scope.targetSchema = {};

    function mapData(name, container) {
      var data = {'name': name, 'show': true};

      if (container['properties']) {
        var children = [];
        angular.forEach(container['properties'], function (val, key) {
          children.push(mapData(key, val));
        });
        data['children'] = children;
      }

      return data;
    }

    var sourceSchema = $http.get('/data/schema.json')
        , targetSchema = $http.get('/data/targetschema.json')
        , allPromise = $q.all([sourceSchema, targetSchema]);

    allPromise.then(function (result) {
        var sourceSchema = result[0]['data']
            , targetSchema = result[1]['data'];

        $scope.sourceSchema = mapData(sourceSchema['title'], sourceSchema);
        $scope.targetSchema = mapData(targetSchema['title'], targetSchema);

    });

  }]);
