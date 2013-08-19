'use strict';

angular.module('dmpApp').
  factory('schemaParser', function() {
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

    function makeItem(name, children, title, extra) {
      var item = {'name': name, 'show': true}
      if (children && children.length) {
        item['children'] = children
      }
      if (title) {
        item['title'] = title
      }
      return angular.extend(extra || {}, item)
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
        return makeItem(name, null, container.trim(), {leaf: true})
      }

      if (container['#text'] && container['#text'].trim()) {
        return makeItem(name, null, container['#text'].trim(), {leaf: true})
      }

      if (angular.isArray(container)) {
        var ary = []
        angular.forEach(container, function (item) {
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

    return {
      mapData: mapData
    , makeItem: makeItem
    , parseObject: parseObject
    , parseArray: parseArray
    , parseString: parseString
    , parseEnum: parseEnum
    , parseAny: parseAny
    }
  })
