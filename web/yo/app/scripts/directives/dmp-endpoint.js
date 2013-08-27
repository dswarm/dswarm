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
          , asTarget = tAttrs['source']
          , asTargetWatch = function(scope) {
              return scope.$eval(asTarget);
            }
          , jspOpts = tAttrs['jspOptions'] || tAttrs['jsPlumbOptions']
          , jspOptsWatch = function(scope) {
              return scope.$eval(jspOpts);
            };

        var opts;

        return function(scope, iElement, iAttrs) {
          opts = jspOptsWatch(scope) || {};

          scope.$watch(asSourceWatch, function (isSource) {
            if (isSource) {
              jsP.makeSource(iElement, iAttrs, opts);
            } else {
              jsP.detachAll(iElement);
            }
          });
          scope.$watch(asTargetWatch, function (isTarget) {
            if (isTarget) {
              jsP.makeTarget(iElement, iAttrs, opts);
            } else {
              jsP.detachAll(iElement);
            }
          });
        };
      }
    };
  }]);
