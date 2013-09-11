'use strict';

angular.module('dmpApp')
  .value('functionSortableOptions', {
    fnName: 'sortable',
    options: {
      tolerance: 'pointer',
      items: '.function',
      cancel: '.component',
      revert: 350,
      snap: true,
      forcePlaceholderSize: true,
      containment: '#transformation',
  //    cursor: 'move',
      cursorAt: {top: -5, left: -5},
      opacity: 0.7
    }
  })
  .value('componentDraggableOptions', {
    fnName: 'draggable',
    options: {
      appendTo: 'body',
      connectToSortable: '.function-sortable',
      containment: '#transformation',
      cursor: 'move',
      cursorAt: {top: -5, left: -5},
      helper: 'clone',
      opacity: 0.7,
      revert: 'invalid',
      revertDuration: 400
    }
  })
  .directive('functionSortable', ['functionSortableOptions', '$timeout', function (functionSortableOptions, $timeout) {
    //noinspection JSUnresolvedVariable
    var functionSortableFnName = functionSortableOptions.fnName;

    return {
      restrict: 'C',
      scope: false,
      compile: function(tElement) {
        if (!angular.isFunction(tElement[functionSortableFnName])) {
          throw new Error('The "' + functionSortableFnName + '" function does not exist, you need to load jQuery-UI');
        }

        return function (scope, iElement) {
          var options = angular.extend({},
            functionSortableOptions.options,
            scope.sortableCallbacks || {});

          $timeout(function () {
            iElement[functionSortableFnName].call(iElement, options);
            iElement['disableSelection']();

            iElement.bind('$destroy', function () {
              iElement[functionSortableFnName].call(iElement, 'destroy');
            });
          }, 0, false);
        };
      }
    };
  }])
  .directive('componentMember', ['componentDraggableOptions', '$timeout', function (componentDraggableOptions, $timeout) {
    //noinspection JSUnresolvedVariable
    var componentDraggableFnName = componentDraggableOptions.fnName;

    return {
      restrict: 'C',
      scope: false,
      compile: function (tElement) {
        if (!angular.isFunction(tElement[componentDraggableFnName])) {
          throw new Error('The "' + componentDraggableFnName + '" function does not exist, you need to load jQuery-UI');
        }

        return function (scope, iElement) {
          var options;

          options = angular.extend({}, componentDraggableOptions.options, scope.draggableCallbacks || {});

          $timeout(function () {
            iElement[componentDraggableFnName].call(iElement, options);
            iElement['disableSelection']();

            iElement.bind('$destroy', function () {
              iElement[componentDraggableFnName].call(iElement, 'destroy');
            });
          }, 0, false);
        };
      }
    };
  }]);
