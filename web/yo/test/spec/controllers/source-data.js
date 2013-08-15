'use strict';

beforeEach(module('dmpApp', 'mockedSchema', 'mockedRecord'));

describe('Controller: SourceDataCtrl', function () {

    var SourceDataCtrl,
        scope,
        mockedSchema;

    // Initialize the controller and a mock scope
    beforeEach(inject(function ($controller, $httpBackend, $rootScope, mockSchemaJSON, mockRecordJSON) {
        scope = $rootScope.$new();

        $httpBackend.whenGET('/data/schema.json').respond(mockSchemaJSON);
        $httpBackend.whenGET('/data/record.json').respond(mockRecordJSON);

        SourceDataCtrl = $controller('SourceDataCtrl', {
            $scope: scope
        });

        $httpBackend.flush();

    }));

    it('should have loaded schema data', function () {

        expect(scope.data.name).toBe('OAI-PMH');

        expect(scope.data.children.length).toBe(3);

    });

    it('should have loaded record data', function () {

        expect(scope.data.children[0].children[0].children[0].children[0].title).toBe('urn:nbn:de:bsz:14-ds-1229427875176-76287');

    });

});
