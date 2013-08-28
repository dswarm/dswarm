'use strict';

angular.module('dmpApp')
  .directive('dmpEndpoint', ['$compile', 'jsP', function ($compile, jsP) {
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
