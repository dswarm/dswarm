'use strict';

beforeEach(module('dmpApp', 'mockedSchema', 'mockedRecord'));

describe('Controller: SourceDataCtrl', function () {

    var sourceDataCtrl,
        scope,
        $httpBackend;

    // Initialize the controller and a mock scope
    beforeEach(inject(function ($controller, _$httpBackend_, $rootScope, mockSchemaJSON, mockRecordJSON) {
        scope = $rootScope.$new();
        $httpBackend = _$httpBackend_;

        $httpBackend.whenGET('/data/schema.json').respond(mockSchemaJSON);
        $httpBackend.whenGET('/data/record.json').respond(mockRecordJSON);

        sourceDataCtrl = function () {
          return $controller('SourceDataCtrl', {
            $scope: scope
          });
        };

    }));

    afterEach(inject(function () {
      $httpBackend.verifyNoOutstandingExpectation();
      $httpBackend.verifyNoOutstandingRequest();
    }));

    it('should have a SchemaCtrl controller', function() {
        var SourceDataCtrl = sourceDataCtrl();
        $httpBackend.flush();
        expect(SourceDataCtrl).not.toBe(null);
    });

    it('should have loaded schema data', function () {
        $httpBackend.expectGET('/data/schema.json');
        sourceDataCtrl();
        $httpBackend.flush();

        expect(scope.data.name).toBe('OAI-PMH');

        expect(scope.data.children.length).toBe(3);

    });

    it('should have loaded record data', function () {
        $httpBackend.expectGET('/data/record.json');
        sourceDataCtrl();
        $httpBackend.flush();

        expect(scope.data.children[0].children[0].children[0].children[0].title).toBe('urn:nbn:de:bsz:14-ds-1229427875176-76287');

    });

});
