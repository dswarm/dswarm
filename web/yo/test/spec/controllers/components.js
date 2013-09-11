'use strict';

beforeEach(module('dmpApp', 'mockedFunctions'));

describe('Controller: ComponentsCtrl', function () {

  var componentsCtrl,
    scope,
    $httpBackend;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, _$httpBackend_, $rootScope, mockFunctionsJSON) {
      scope = $rootScope.$new();
      $httpBackend = _$httpBackend_;
      $httpBackend.whenGET('/data/functions.json').respond(mockFunctionsJSON);

      componentsCtrl = function() {
        return $controller('ComponentsCtrl', {
          '$scope': scope
        });
      };
    }
  ));

  afterEach(inject(function () {
    $httpBackend.verifyNoOutstandingExpectation();
    $httpBackend.verifyNoOutstandingRequest();
  }));

  it('should have loaded function data', inject(function () {
      $httpBackend.expectGET('/data/functions.json');
      componentsCtrl();
      $httpBackend.flush();

      expect(scope.functions.children.length).toBe(23);

    }
  ));
});
