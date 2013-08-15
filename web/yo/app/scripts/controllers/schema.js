'use strict';

angular.module('dmpApp')
  .controller('SchemaCtrl', ['$scope', '$http', function ($scope, $http) {
    $scope.internalName = 'Source Target Schema Mapper'

    $scope.data = {}

    function mapData(name, container) {
      var data = {'name': name, 'show': true}

      if (container['properties']) {
        var children = []
        angular.forEach(container['properties'], function (val, key) {
          children.push(mapData(key, val))
        })
        data['children'] = children
      }

      return data
    }

    $http.get('/data/schema.json')
      .success(function (result) {
        $scope.data = mapData(result['title'], result)
      })
  }])
