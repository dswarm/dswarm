'use strict';

angular.module('dmpApp')
  .controller('ComponentsCtrl', ['$scope', '$http', function ($scope, $http) {
    $scope.internalName = 'Function List Widget'

    $scope.functions = {
      'name': 'Functions',
      'show': true,
      'children': []
    }

    $scope.onLeafClick = function (event, data) {
      var $el = angular.element(event.target)

      if (!$el.data('uiDraggable')) {
        $el.draggable({
          cursor: 'move',
          cursorAt: {top: -5, left: -5},
          opacity: 0.7,
          containment: '#transformation',
          revert: 'invalid',
          appendTo: 'body',
//          helper: 'clone',
          helper: function() {
            var $el = angular.element('<div class="component">' + data.name + '</div>')
            $el.data('component-type', 'function')
              .data('payload', data);
            return $el
          }
        })
      }
      console.log('on leaf click');
      console.log(data);
    }

    $http.get('/data/functions.json')
      .success(function (result) {
        $scope.functions.children = result['functions']
      })
  }])
