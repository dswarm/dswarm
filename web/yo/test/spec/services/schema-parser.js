'use strict';

describe('schemaParser tests', function (){
    var schemaParser;

    //excuted before each "it" is run.
    beforeEach(function (){

        //load the module.
        module('dmpApp');

        //inject your service for testing.
        inject(function(_schemaParser_) {
            schemaParser = _schemaParser_;
        });
    });

    it('should have an mapData function', function () {
        expect(angular.isFunction(schemaParser.mapData)).toBe(true);
    });

    it('should return mapped data with mapData', function () {
        var result = schemaParser.mapData('bar', { 'properties' : { 'foo' : {}}});
        expect(result['name']).toBe('bar');
        expect(result['children'].length).toBe(1);
    });

    it('should have an makeItem function', function () {
        expect(angular.isFunction(schemaParser.makeItem)).toBe(true);
    });

    it('should return correct item structure with makeItem', function () {
        var result = schemaParser.makeItem('bar', ['baz', 'bazz' ], 'foo');
        expect(result['name']).toBe('bar');

        expect(result['children'].length).toBe(2);
        expect(result['children'][1]).toBe('bazz');

        expect(result['title']).toBe('foo');
    });

    it('should have an parseObject function', function () {
        expect(angular.isFunction(schemaParser.parseObject)).toBe(true);
    });

    it('should return item from parsed object with parseObject', function () {
        var result = schemaParser.parseObject(
            { 'identifier': 'urn:nbn:de:bsz:14-ds-1229427875176-76287' },
            'bar', 
            { 'identifier': {'type': 'string'} }
        );

        expect(result['name']).toBe('bar');

        expect(result['children'].length).toBe(1);
        expect(result['children'][0]['title']).toBe('urn:nbn:de:bsz:14-ds-1229427875176-76287');

    });

    it('should have an parseArray function', function () {
        expect(angular.isFunction(schemaParser.parseArray)).toBe(true);
    });

    it('should return item from parsed array with parseArray', function () {
        var result = schemaParser.parseArray(
            [ 'urn:nbn:de:bsz:14-ds-1229427875176-76287' ],
            'bar',
            { 'type': 'string' }
        );

        expect(result['name']).toBe('bar');

        expect(result['children'].length).toBe(1);
        expect(result['children'][0]['title']).toBe('urn:nbn:de:bsz:14-ds-1229427875176-76287');

    });

    it('should have an parseString function', function () {
        expect(angular.isFunction(schemaParser.parseString)).toBe(true);
    });

    it('should return item from parsed string with parseString when container string', function () {
        var result = schemaParser.parseString('foo', 'bar');

        expect(result['name']).toBe('bar');
        expect(result['title']).toBe('foo');

    });

    it('should return item from parsed string with parseString when container string in associative array', function () {

        var container = [];
        container['#text'] = 'foo';

        var result = schemaParser.parseString(container, 'bar');

        expect(result['name']).toBe('bar');
        expect(result['title']).toBe('foo');

    });

    it('should return item from parsed string with parseString when container strings in array', function () {

        var container = ['foo','fooo','foooo'];

        var result = schemaParser.parseString(container, 'bar');

        expect(result['name']).toBe('bar');
        expect(result['children'].length).toBe(3);

    });

    it('should have an parseEnum function', function () {
        expect(angular.isFunction(schemaParser.parseEnum)).toBe(true);
    });

    it('should return item from parsed enum with parseEnum', function () {
        var result = schemaParser.parseEnum(
            'IDENTIFIER',
            'bar',
            'IDENTIFIER, TEXT'
        );

        expect(result['name']).toBe('bar');

        expect(result['title']).toBe('IDENTIFIER');

    });

    it('should have an parseAny function', function () {
        expect(angular.isFunction(schemaParser.parseAny)).toBe(true);
    });

    it('should return item from any type, trying object', function () {

        // Test object
        var result = schemaParser.parseAny(
            { 'identifier': 'urn:nbn:de:bsz:14-ds-1229427875176-76287' },
            'bar',
            {
                'type' : 'object',
                'properties' : { 'identifier': {'type': 'string'} }
            }
        );

        expect(result['name']).toBe('bar');
        expect(result['children'].length).toBe(1);

    });

    it('should return item from any type, trying array', function () {

        // Test array
        var result = schemaParser.parseAny(
            [ 'urn:nbn:de:bsz:14-ds-1229427875176-76287' ],
            'bar',
            { 'type' : 'array', 'items' : { 'type': 'string' } }
        );

        expect(result['name']).toBe('bar');
        expect(result['children'].length).toBe(1);

    });

    it('should return item from any type, trying string', function () {

        // Test string
        var result = schemaParser.parseAny('foo', 'bar', { 'type' : 'string' } );

        expect(result['name']).toBe('bar');
        expect(result['title']).toBe('foo');

    });

    it('should return item from any type, trying enum', function () {

        // Test enum
        var result = schemaParser.parseAny('IDENTIFIER', 'bar', { 'enum' : 'IDENTIFIER' } );

        expect(result['name']).toBe('bar');
        expect(result['title']).toBe('IDENTIFIER');

    });


});