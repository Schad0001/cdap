/*
 * Services Controller
 */

define([], function () {

  var ERROR_TXT = 'Requested Instance count out of bounds.';

  var Controller = Em.Controller.extend({

    load: function () {
      var self = this;
      self.set('systemServices', []);

      self.resetServices();

      this.interval = setInterval(function () {
        self.resetServices();
      }, C.POLLING_INTERVAL)
    },
    config: function(service) {
      this.transitionToRoute('Userservice.Config', service);
    },

    resetServices: function () {
      var self = this;
      var systemServices = [];

      self.HTTP.rest('system/services', function (services) {
        services.map(function(service) {
          var imgSrc = service.status === 'OK' ? 'complete' : 'loading';
          var logSrc = service.status === 'OK' ? 'complete' : 'loading';
          systemServices.push(C.Service.create({
            modelId: service.name,
            description: service.description,
            id: service.name,
            name: service.name,
            description: service.description,
            status: service.status,
            min: service.min,
            max: service.max,
            isIncreaseEnabled: service.requested < service.max,
            isDecreaseEnabled: service.requested > service.min,
            logs: service.logs,
            requested: service.requested,
            provisioned: service.provisioned,
            logsStatusOk: !!(service.logs === 'OK'),
            logsStatusNotOk: !!(service.logs === 'NOTOK'),
            metricEndpoint: C.Util.getMetricEndpoint(service.name),
            metricName: C.Util.getMetricName(service.name),
            imgClass: imgSrc,
            logClass: logSrc
          }));
        });
        self.set('systemServices', systemServices);

        // Bind all the tooltips after UI has rendered after call has returned.
        setTimeout(function () {
          $("[data-toggle='tooltip']").tooltip();
        }, 1000);
      });
    },

    start: function (service) {
      var self = this;
      if (service.status == "RUNNING") {
        C.Util.showWarning("Program is already running.");
        return;
      }
      C.Modal.show(
        "Start Service",
        "Start Service: " + service.app + ":" + service.name + "?",
        function () {
          var startURL = 'rest/apps/' + service.app + '/services/' + service.name + '/start';
          self.HTTP.post(startURL, function() {
            service.update(self.HTTP);
          });
        }
      );
    },

    stop: function (service) {
      var self = this;
      if (service.status == "STOPPED") {
        C.Util.showWarning("Program is already stopped.");
        return;
      }
      C.Modal.show(
        "Stop Service",
        "Stop Service: " + service.app + ":" + service.name + "?",
        function () {
          var stopURL = 'rest/apps/' + service.app + '/services/' + service.name + '/stop';
          self.HTTP.post(stopURL, function() {
            service.update(self.HTTP);
          });
        }
      );
    },


    increaseInstance: function (service, instanceCount) {
      this.verifyInstanceBounds(service, ++instanceCount, "Increase");
    },

    decreaseInstance: function (service, instanceCount) {
      this.verifyInstanceBounds(service, --instanceCount, "Decrease");
    },

    verifyInstanceBounds: function(service, numRequested, direction) {
      var self = this;
      if (numRequested > service.max || numRequested < service.min) {
        C.Modal.show("Instances Error", ERROR_TXT);
        return;
      }
      C.Modal.show(
        direction + " instances",
        direction + " instances for " + service.name + "?",
        function () {
          self.executeInstanceCall('rest/system/services/' + service.name + '/instances', numRequested);
        }
      );
    },

    executeInstanceCall: function (url, numRequested) {
      var self = this;
      var payload = {data: {instances: numRequested}};
      this.HTTP.put(url, payload,
        function(resp, status) {
        if (status === 'error') {
          C.Util.showWarning(resp);
        } else {
          self.resetServices();
        }
      });
    },

    unload: function () {
      clearInterval(this.interval);
    }

  });

  Controller.reopenClass({
    type: 'Services',
    kind: 'Controller'
  });

  return Controller;

});
