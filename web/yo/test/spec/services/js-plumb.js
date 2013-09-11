'use strict';

describe('jsPlump service tests', function (){
  beforeEach(module('dmpApp', function (jsPlumbProvider) {
    jsPlumbProvider.setInstance({
      connect: function (options) {
        var connection = {
          source: options.source[0],
          target: options.target[0]
        };

        options.source.addClass('_jsPlumb_endpoint_anchor_');
        options.target.addClass('_jsPlumb_endpoint_anchor_');

        return connection;
      },
      detach: function (connection) {
        angular.element(connection.source).removeClass('_jsPlumb_endpoint_anchor_');
        angular.element(connection.target).removeClass('_jsPlumb_endpoint_anchor_');


        connection.source = null;
        connection.target = null;
      }
    });
  }));

  it('should have connect and detach methods', inject(function (jsP) {
    expect(angular.isFunction(jsP.connect)).toBe(true);
    expect(angular.isFunction(jsP.detach)).toBe(true);
  }));

  it('should connect two nodes', inject(function (jsP) {
    var src = angular.element('<div>')
      , tgt = angular.element('<div>');

    var connection = jsP.connect(src, tgt);

    expect(src.hasClass('_jsPlumb_endpoint_anchor_')).toBe(true);
    expect(tgt.hasClass('_jsPlumb_endpoint_anchor_')).toBe(true);

    expect(connection.source).toBe(src[0]);
    expect(connection.target).toBe(tgt[0]);

    expect(src.data('_outbound')).not.toBe(null);
    expect(src.data('_outbound')).toBe(connection);
  }));


  it('should detach a connection between two nodes', inject(function (jsP) {
    var src = angular.element('<div>')
      , tgt = angular.element('<div>');

    var connection = jsP.connect(src, tgt);

    jsP.detach(connection, src, tgt);

    expect(src.hasClass('_jsPlumb_endpoint_anchor_')).toBe(false);
    expect(tgt.hasClass('_jsPlumb_endpoint_anchor_')).toBe(false);

    expect(connection.source).toBe(null);
    expect(connection.target).toBe(null);

    expect(src.data('_outbound')).toBe(null);
  }));

});
