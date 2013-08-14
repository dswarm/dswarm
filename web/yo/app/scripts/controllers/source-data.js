'use strict';

angular.module('dmpApp')
  .controller('SourceDataCtrl', ['$scope', '$http', '$q', function ($scope, $http, $q) {
    $scope.internalName = "Source Data Widget"

    $scope.data = {}

    function parseObject(container, name, properties) {
      var ary = []
      angular.forEach(properties, function (val, key) {
        if (container[key]) {
          var it = parseAny(container[key], key, val)
          it && ary.push(it)
        }
      })
      return {'name': name, 'children': ary}
    }

    function parseArray(container, name, properties) {
      var ary = []
      angular.forEach(container, function (item) {
        var it = parseAny(item, name, properties)
        it && ary.push(it)
      })
      return {'name': name, 'children': ary}
    }

    function parseString(container, name) {
      if (angular.isString(container)) {
        return {'name': name, 'title': container.trim()}
      }

      if (container['#text'] && container['#text'].trim()) {
        return {'name': name, 'title': container['#text'].trim()}
      }

      if (angular.isArray(container)) {
        var ary = []
        angular.forEach(container, function(item) {
          var it = parseString(item, name)
          it && ary.push(it)
        })
        return {'name': name, 'children': ary}
      }
    }

    function parseEnum(container, name, enumeration) {
      if (enumeration.indexOf(container) !== -1) {
        return {'name': name, 'title': container}
      }
    }

    function parseAny(container, name, obj) {
      if (obj.type === "object") {
        return parseObject(container, name, obj.properties)
      }
      else if (obj.type === "array") {
        return parseArray(container, name, obj.items);
      }
      else if (obj.type === "string") {
        return parseString(container, name)
      }
      else if (obj.enum) {
        return parseEnum(container, name, obj.enum)
      }
    }

    var schemaPromise = $http.get('/data/schema.json')
      , dataPromise = $http.get("/data/urn:nbn:de:bsz:14-ds-1229427875176-76287.json")
      , allPromise = $q.all([schemaPromise, dataPromise])

    allPromise.then(function (result) {
      var schemaResult = result[0]['data']
        , dataResult = result[1]['data']

      $scope.data = parseAny(dataResult[schemaResult['title']], schemaResult['title'], schemaResult)

    })
  }])
