'use strict';

angular.module('dmpApp')
  .directive('dmpEndpoint', ['$compile', '$window', '$rootScope', 'jsP', 'PubSub', function ($compile, $window, $rootScope, jsP, PubSub) {
    var components = {
      active: null,
      pool: []
    };

    function setColor(connection, color) {
      connection.endpoints[0].setPaintStyle({fillStyle: color});
      connection.setPaintStyle({strokeStyle: color});
    }

    function deSelect(connection) {
      setColor(connection, 'black');
      connection.getLabelOverlay().removeClass('mapping-active');
      connection.getConnector().removeClass('mapping-active');
    }
    function doSelect(connection) {
      setColor(connection, 'red');
      connection.getLabelOverlay().addClass('mapping-active');
      connection.getConnector().addClass('mapping-active');
    }

    function deSelectAll() {
      var active = components.active;
      angular.forEach(components.pool, function(comp) {
        if (comp !== active) {
          deSelect(comp);
        }
      });
    }

    function realPath(segments, scp) {
      if (angular.isUndefined(scp.data) || scp.data.name === 'record') {
        return segments.join('.');
      }

      var lastSegment = segments[0]
        , currentSegment = scp.data && scp.data.name;

      if (!currentSegment || currentSegment === lastSegment) {
        return realPath(segments, scp.$parent);
      }

      return realPath([currentSegment].concat(segments), scp.$parent);
    }

    function activate(connection, dontFire) {
      if (components.pool.indexOf(connection) === -1) {
        components.pool.push(connection);
      }
      components.active = connection;
      deSelectAll();
      doSelect(connection);

      function getData(c) {
        var scp = angular.element(c).scope()
          , data = scp.data;

        data.path = realPath([], scp);
        return data;
      }

      var label = connection.getLabel()
        , id = connection.id;

      if (!dontFire) {
        PubSub.broadcast('connectionSelected', {
          id: id,
          label: label,
          sourceData: getData(connection.source),
          targetData: getData(connection.target)
        });
      }
    }

    function reLabel(connection, callback, promptText) {
      var text = promptText || 'Name this connection'
        , label = $window.prompt(text)
        , valid = label && label.length && label.length >= 5;

      if (valid) {
        connection.setLabel(label);
        var labelOverlay = connection.getLabelOverlay();
        labelOverlay.addClass('mapping-label');

        if (callback) {
          callback(connection, label);
        }
      }

      return valid;
    }

    jsP.on('beforeDrop', function(component) {
      if (component.scope === 'schema') {
        return reLabel(component.connection);
      } else {
        return true;
      }
    });
    jsP.on('connection', function(component) {
      if (component.scope === 'schema' || component.connection.scope === 'schema') {
        activate(component.connection);
      }
    });
    jsP.on('click', function(component, event) {
      if (component.scope === 'schema') {
        switch (event.target.tagName) {
        case 'DIV':
  //        var text = ['Rename this', ' component from "', component.getLabel(), '"'];
  //        if (components.active === component) {
  //          text.splice(1, 0, ', currently active,');
  //        }
  //        reLabel(component, function(connection, label) {
  //          PubSub.broadcast('connectionRenamed', {
  //            id: connection.id,
  //            label: label
  //          });
  //        }, text.join(''));
          activate(component);
          break;

        case 'path':
          activate(component);
          break;
        }
      }
    });


    PubSub.subscribe($rootScope, 'connectionSwitched', function (data) {
      var pool = components.pool
        , i = 0
        , id = data.id
        , connection
        , found;

      for (; !found && (connection = pool[i++]);) {
        if (connection.id === id) {
          found = connection;
        }
      }

      if (found) {
        activate(found, true);
      }
    });

    return {
      restrict: 'A',
      replace: true,
      compile: function (tElement, tAttrs) {
        var asSource = tAttrs['source']
          , asSourceWatch = function(scope) {
              return scope.$eval(asSource);
            }
          , asTarget = tAttrs['target']
          , asTargetWatch = function(scope) {
              return scope.$eval(asTarget);
            }
          , jspSourceOpts = tAttrs['jspSourceOptions'] || tAttrs['jsPlumbSourceOptions']
          , jspSourceOptsWatch = function(scope) {
              return scope.$eval(jspSourceOpts);
            }
          , jspTargetOpts = tAttrs['jspTargetOptions'] || tAttrs['jsPlumbTargetOptions']
          , jspTargetOptsWatch = function(scope) {
              return scope.$eval(jspTargetOpts);
            };

        return function(scope, iElement, iAttrs) {
          var sourceOpts = jspSourceOptsWatch(scope) || {}
            , targetOpts = jspTargetOptsWatch(scope) || {};

          scope.$watch(asSourceWatch, function (isSource) {
            if (isSource) {
              jsP.makeSource(iElement, iAttrs, sourceOpts);
            } else {
              jsP.unmakeSource(iElement);
            }
          });
          scope.$watch(asTargetWatch, function (isTarget) {
            if (isTarget) {
              jsP.makeTarget(iElement, iAttrs, targetOpts);
            } else {
              jsP.unmakeTarget(iElement);
            }
          });
        };
      }
    };
  }]);
