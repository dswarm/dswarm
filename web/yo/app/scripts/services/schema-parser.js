'use strict';

angular.module('dmpApp').
  /**
   * A service that parses json-schema into an internal tree model [1]. It also
   *   provides functionality to transform an XML2JSON model [2] into the
   *   aforementioned tree model, using the same json-schema definition.
   *
   *   [1] The internal tree model is defined in directives/tree.js
   *   [2] Based on the unmodified results of https://github.com/hay/xml2json
   *
   *   Due to the nature of representing a tree structure, most of these
   *   methods are utilizing either plain recursion or trampoline recursion.
   */
  factory('schemaParser', function () {
    /**
     * Maps from json-schema to the internal tree model.  Since json-schema
     *   already is very tree-ish, there is nothing much to do but renaming
     *   some properties and apply recursion all the way down.
     *
     * @param name {String}  The name of the current property, that is
     *   enumerated on.
     * @param container {Object}  The definition of the current property.
     *   A json-schema is usually like "Property": { ... (definition) }
     *   and `name' and `container' are just that.
     * @returns {{name: String, show: boolean}}
     */
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

    /**
     * TODO
     * @param name {String}
     * @param children {Object}
     * @param title {String=}
     * @param extra {Object=}
     * @returns {Object|{name: String, show: boolean, children: {?Object}, title: {?String}}
     */
    function makeItem(name, children, title, extra) {
      var item = {'name': name, 'show': true};
      if (children && children.length) {
        item['children'] = children;
      }
      if (title) {
        item['title'] = title;
      }
      return angular.extend(extra || {}, item);
    }

    /**
     * TODO
     * @param container
     * @param name
     * @param properties
     * @returns {*}
     */
    function parseObject(container, name, properties) {
      var ary = [];
      angular.forEach(properties, function (val, key) {
        if (container[key]) {
          var it = parseAny(container[key], key, val);
          if (it) {
            ary.push(it);
          }
        }
      });
      return makeItem(name, ary);
    }

    /**
     * TODO
     * @param container
     * @param name
     * @param properties
     * @returns {*}
     */
    function parseArray(container, name, properties) {
      var ary = [];
      angular.forEach(container, function (item) {
        var it = parseAny(item, name, properties);
        if (it) {
          ary.push(it);
        }
      });
      return makeItem(name, ary);
    }

    /**
     * TODO
     * @param container
     * @param name
     * @returns {*}
     */
    function parseString(container, name) {
      if (angular.isString(container)) {
        return makeItem(name, null, container.trim(), {leaf: true});
      }

      if (container['#text'] && container['#text'].trim()) {
        return makeItem(name, null, container['#text'].trim(), {leaf: true});
      }

      if (angular.isArray(container)) {
        var ary = [];
        angular.forEach(container, function (item) {
          var it = parseString(item, name);
          if (it) {
            ary.push(it);
          }
        });
        return makeItem(name, ary);
      }
    }

    /**
     * TODO
     * @param container
     * @param name
     * @param enumeration
     * @returns {*}
     */
    function parseEnum(container, name, enumeration) {
      if (enumeration.indexOf(container) !== -1) {
        return makeItem(name, null, container);
      }
    }

    /**
     * TODO
     * @param container
     * @param name
     * @param obj
     * @returns {*}
     */
    function parseAny(container, name, obj) {
      if (obj['type'] === 'object') {
        return parseObject(container, name, obj['properties']);
      }
      if (obj['type'] === 'array') {
        return parseArray(container, name, obj['items']);
      }
      if (obj['type'] === 'string') {
        return parseString(container, name);
      }
      if (obj['enum']) {
        return parseEnum(container, name, obj['enum']);
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
    };
  });
