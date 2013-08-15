'use strict';

angular.module('dmpApp')
  .controller('SourceDataCtrl', ['$scope', '$http', '$q', function ($scope, $http, $q) {
    $scope.internalName = 'Source Data Widget'

    $scope.data = {}

    function makeItem(name, children, title) {
      var item = {'name': name, 'show': true}
      if (children && children.length) {
        item['children'] = children
      }
      if (title) {
        item['title'] = title
      }
      return item
    }

    function parseObject(container, name, properties) {
      var ary = []
      angular.forEach(properties, function (val, key) {
        if (container[key]) {
          var it = parseAny(container[key], key, val)
          if (it) {
            ary.push(it)
          }
        }
      })
      return makeItem(name, ary)
    }

    function parseArray(container, name, properties) {
      var ary = []
      angular.forEach(container, function (item) {
        var it = parseAny(item, name, properties)
        if (it) {
          ary.push(it)
        }
      })
      return makeItem(name, ary)
    }

    function parseString(container, name) {
      if (angular.isString(container)) {
        return makeItem(name, null, container.trim())
      }

      if (container['#text'] && container['#text'].trim()) {
        return makeItem(name, null, container['#text'].trim())
      }

      if (angular.isArray(container)) {
        var ary = []
        angular.forEach(container, function(item) {
          var it = parseString(item, name)
          if (it) {
            ary.push(it)
          }
        })
        return makeItem(name, ary)
      }
    }

    function parseEnum(container, name, enumeration) {
      if (enumeration.indexOf(container) !== -1) {
        return makeItem(name, null, container)
      }
    }

    function parseAny(container, name, obj) {
      if (obj['type'] === 'object') {
        return parseObject(container, name, obj['properties'])
      }
      if (obj['type'] === 'array') {
        return parseArray(container, name, obj['items'])
      }
      if (obj['type'] === 'string') {
        return parseString(container, name)
      }
      if (obj['enum']) {
        return parseEnum(container, name, obj['enum'])
      }
    }

    var schemaPromise = $http.get('/data/schema.json')
      , dataPromise = $http.get('/data/record.json')
      , allPromise = $q.all([schemaPromise, dataPromise])

    allPromise.then(function (result) {
      var schemaResult = result[0]['data']
        , dataResult = result[1]['data']

      $scope.data = parseAny(dataResult[schemaResult['title']], schemaResult['title'], schemaResult)

    })
  }])
