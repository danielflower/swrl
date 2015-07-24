(function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){
(function (process,global){
"use strict";

(function () {
  "use strict";function lib$es6$promise$utils$$objectOrFunction(x) {
    return typeof x === "function" || typeof x === "object" && x !== null;
  }function lib$es6$promise$utils$$isFunction(x) {
    return typeof x === "function";
  }function lib$es6$promise$utils$$isMaybeThenable(x) {
    return typeof x === "object" && x !== null;
  }var lib$es6$promise$utils$$_isArray;if (!Array.isArray) {
    lib$es6$promise$utils$$_isArray = function (x) {
      return Object.prototype.toString.call(x) === "[object Array]";
    };
  } else {
    lib$es6$promise$utils$$_isArray = Array.isArray;
  }var lib$es6$promise$utils$$isArray = lib$es6$promise$utils$$_isArray;var lib$es6$promise$asap$$len = 0;var lib$es6$promise$asap$$toString = ({}).toString;var lib$es6$promise$asap$$vertxNext;var lib$es6$promise$asap$$customSchedulerFn;function lib$es6$promise$asap$$asap(callback, arg) {
    lib$es6$promise$asap$$queue[lib$es6$promise$asap$$len] = callback;lib$es6$promise$asap$$queue[lib$es6$promise$asap$$len + 1] = arg;lib$es6$promise$asap$$len += 2;if (lib$es6$promise$asap$$len === 2) {
      if (lib$es6$promise$asap$$customSchedulerFn) {
        lib$es6$promise$asap$$customSchedulerFn(lib$es6$promise$asap$$flush);
      } else {
        lib$es6$promise$asap$$scheduleFlush();
      }
    }
  }var lib$es6$promise$asap$$default = lib$es6$promise$asap$$asap;function lib$es6$promise$asap$$setScheduler(scheduleFn) {
    lib$es6$promise$asap$$customSchedulerFn = scheduleFn;
  }var lib$es6$promise$asap$$browserWindow = typeof window !== "undefined" ? window : undefined;var lib$es6$promise$asap$$browserGlobal = lib$es6$promise$asap$$browserWindow || {};var lib$es6$promise$asap$$BrowserMutationObserver = lib$es6$promise$asap$$browserGlobal.MutationObserver || lib$es6$promise$asap$$browserGlobal.WebKitMutationObserver;var lib$es6$promise$asap$$isNode = typeof process !== "undefined" && ({}).toString.call(process) === "[object process]";var lib$es6$promise$asap$$isWorker = typeof Uint8ClampedArray !== "undefined" && typeof importScripts !== "undefined" && typeof MessageChannel !== "undefined";function lib$es6$promise$asap$$useNextTick() {
    var nextTick = process.nextTick;var version = process.versions.node.match(/^(?:(\d+)\.)?(?:(\d+)\.)?(\*|\d+)$/);if (Array.isArray(version) && version[1] === "0" && version[2] === "10") {
      nextTick = setImmediate;
    }return function () {
      nextTick(lib$es6$promise$asap$$flush);
    };
  }function lib$es6$promise$asap$$useVertxTimer() {
    return function () {
      lib$es6$promise$asap$$vertxNext(lib$es6$promise$asap$$flush);
    };
  }function lib$es6$promise$asap$$useMutationObserver() {
    var iterations = 0;var observer = new lib$es6$promise$asap$$BrowserMutationObserver(lib$es6$promise$asap$$flush);var node = document.createTextNode("");observer.observe(node, { characterData: true });return function () {
      node.data = iterations = ++iterations % 2;
    };
  }function lib$es6$promise$asap$$useMessageChannel() {
    var channel = new MessageChannel();channel.port1.onmessage = lib$es6$promise$asap$$flush;return function () {
      channel.port2.postMessage(0);
    };
  }function lib$es6$promise$asap$$useSetTimeout() {
    return function () {
      setTimeout(lib$es6$promise$asap$$flush, 1);
    };
  }var lib$es6$promise$asap$$queue = new Array(1000);function lib$es6$promise$asap$$flush() {
    for (var i = 0; i < lib$es6$promise$asap$$len; i += 2) {
      var callback = lib$es6$promise$asap$$queue[i];var arg = lib$es6$promise$asap$$queue[i + 1];callback(arg);lib$es6$promise$asap$$queue[i] = undefined;lib$es6$promise$asap$$queue[i + 1] = undefined;
    }lib$es6$promise$asap$$len = 0;
  }function lib$es6$promise$asap$$attemptVertex() {
    try {
      var r = require;var vertx = r("vertx");lib$es6$promise$asap$$vertxNext = vertx.runOnLoop || vertx.runOnContext;return lib$es6$promise$asap$$useVertxTimer();
    } catch (e) {
      return lib$es6$promise$asap$$useSetTimeout();
    }
  }var lib$es6$promise$asap$$scheduleFlush;if (lib$es6$promise$asap$$isNode) {
    lib$es6$promise$asap$$scheduleFlush = lib$es6$promise$asap$$useNextTick();
  } else if (lib$es6$promise$asap$$BrowserMutationObserver) {
    lib$es6$promise$asap$$scheduleFlush = lib$es6$promise$asap$$useMutationObserver();
  } else if (lib$es6$promise$asap$$isWorker) {
    lib$es6$promise$asap$$scheduleFlush = lib$es6$promise$asap$$useMessageChannel();
  } else if (lib$es6$promise$asap$$browserWindow === undefined && typeof require === "function") {
    lib$es6$promise$asap$$scheduleFlush = lib$es6$promise$asap$$attemptVertex();
  } else {
    lib$es6$promise$asap$$scheduleFlush = lib$es6$promise$asap$$useSetTimeout();
  }function lib$es6$promise$$internal$$noop() {}var lib$es6$promise$$internal$$PENDING = void 0;var lib$es6$promise$$internal$$FULFILLED = 1;var lib$es6$promise$$internal$$REJECTED = 2;var lib$es6$promise$$internal$$GET_THEN_ERROR = new lib$es6$promise$$internal$$ErrorObject();function lib$es6$promise$$internal$$selfFullfillment() {
    return new TypeError("You cannot resolve a promise with itself");
  }function lib$es6$promise$$internal$$cannotReturnOwn() {
    return new TypeError("A promises callback cannot return that same promise.");
  }function lib$es6$promise$$internal$$getThen(promise) {
    try {
      return promise.then;
    } catch (error) {
      lib$es6$promise$$internal$$GET_THEN_ERROR.error = error;return lib$es6$promise$$internal$$GET_THEN_ERROR;
    }
  }function lib$es6$promise$$internal$$tryThen(then, value, fulfillmentHandler, rejectionHandler) {
    try {
      then.call(value, fulfillmentHandler, rejectionHandler);
    } catch (e) {
      return e;
    }
  }function lib$es6$promise$$internal$$handleForeignThenable(promise, thenable, then) {
    lib$es6$promise$asap$$default(function (promise) {
      var sealed = false;var error = lib$es6$promise$$internal$$tryThen(then, thenable, function (value) {
        if (sealed) {
          return;
        }sealed = true;if (thenable !== value) {
          lib$es6$promise$$internal$$resolve(promise, value);
        } else {
          lib$es6$promise$$internal$$fulfill(promise, value);
        }
      }, function (reason) {
        if (sealed) {
          return;
        }sealed = true;lib$es6$promise$$internal$$reject(promise, reason);
      }, "Settle: " + (promise._label || " unknown promise"));if (!sealed && error) {
        sealed = true;lib$es6$promise$$internal$$reject(promise, error);
      }
    }, promise);
  }function lib$es6$promise$$internal$$handleOwnThenable(promise, thenable) {
    if (thenable._state === lib$es6$promise$$internal$$FULFILLED) {
      lib$es6$promise$$internal$$fulfill(promise, thenable._result);
    } else if (thenable._state === lib$es6$promise$$internal$$REJECTED) {
      lib$es6$promise$$internal$$reject(promise, thenable._result);
    } else {
      lib$es6$promise$$internal$$subscribe(thenable, undefined, function (value) {
        lib$es6$promise$$internal$$resolve(promise, value);
      }, function (reason) {
        lib$es6$promise$$internal$$reject(promise, reason);
      });
    }
  }function lib$es6$promise$$internal$$handleMaybeThenable(promise, maybeThenable) {
    if (maybeThenable.constructor === promise.constructor) {
      lib$es6$promise$$internal$$handleOwnThenable(promise, maybeThenable);
    } else {
      var then = lib$es6$promise$$internal$$getThen(maybeThenable);if (then === lib$es6$promise$$internal$$GET_THEN_ERROR) {
        lib$es6$promise$$internal$$reject(promise, lib$es6$promise$$internal$$GET_THEN_ERROR.error);
      } else if (then === undefined) {
        lib$es6$promise$$internal$$fulfill(promise, maybeThenable);
      } else if (lib$es6$promise$utils$$isFunction(then)) {
        lib$es6$promise$$internal$$handleForeignThenable(promise, maybeThenable, then);
      } else {
        lib$es6$promise$$internal$$fulfill(promise, maybeThenable);
      }
    }
  }function lib$es6$promise$$internal$$resolve(promise, value) {
    if (promise === value) {
      lib$es6$promise$$internal$$reject(promise, lib$es6$promise$$internal$$selfFullfillment());
    } else if (lib$es6$promise$utils$$objectOrFunction(value)) {
      lib$es6$promise$$internal$$handleMaybeThenable(promise, value);
    } else {
      lib$es6$promise$$internal$$fulfill(promise, value);
    }
  }function lib$es6$promise$$internal$$publishRejection(promise) {
    if (promise._onerror) {
      promise._onerror(promise._result);
    }lib$es6$promise$$internal$$publish(promise);
  }function lib$es6$promise$$internal$$fulfill(promise, value) {
    if (promise._state !== lib$es6$promise$$internal$$PENDING) {
      return;
    }promise._result = value;promise._state = lib$es6$promise$$internal$$FULFILLED;if (promise._subscribers.length !== 0) {
      lib$es6$promise$asap$$default(lib$es6$promise$$internal$$publish, promise);
    }
  }function lib$es6$promise$$internal$$reject(promise, reason) {
    if (promise._state !== lib$es6$promise$$internal$$PENDING) {
      return;
    }promise._state = lib$es6$promise$$internal$$REJECTED;promise._result = reason;lib$es6$promise$asap$$default(lib$es6$promise$$internal$$publishRejection, promise);
  }function lib$es6$promise$$internal$$subscribe(parent, child, onFulfillment, onRejection) {
    var subscribers = parent._subscribers;var length = subscribers.length;parent._onerror = null;subscribers[length] = child;subscribers[length + lib$es6$promise$$internal$$FULFILLED] = onFulfillment;subscribers[length + lib$es6$promise$$internal$$REJECTED] = onRejection;if (length === 0 && parent._state) {
      lib$es6$promise$asap$$default(lib$es6$promise$$internal$$publish, parent);
    }
  }function lib$es6$promise$$internal$$publish(promise) {
    var subscribers = promise._subscribers;var settled = promise._state;if (subscribers.length === 0) {
      return;
    }var child,
        callback,
        detail = promise._result;for (var i = 0; i < subscribers.length; i += 3) {
      child = subscribers[i];callback = subscribers[i + settled];if (child) {
        lib$es6$promise$$internal$$invokeCallback(settled, child, callback, detail);
      } else {
        callback(detail);
      }
    }promise._subscribers.length = 0;
  }function lib$es6$promise$$internal$$ErrorObject() {
    this.error = null;
  }var lib$es6$promise$$internal$$TRY_CATCH_ERROR = new lib$es6$promise$$internal$$ErrorObject();function lib$es6$promise$$internal$$tryCatch(callback, detail) {
    try {
      return callback(detail);
    } catch (e) {
      lib$es6$promise$$internal$$TRY_CATCH_ERROR.error = e;return lib$es6$promise$$internal$$TRY_CATCH_ERROR;
    }
  }function lib$es6$promise$$internal$$invokeCallback(settled, promise, callback, detail) {
    var hasCallback = lib$es6$promise$utils$$isFunction(callback),
        value,
        error,
        succeeded,
        failed;if (hasCallback) {
      value = lib$es6$promise$$internal$$tryCatch(callback, detail);if (value === lib$es6$promise$$internal$$TRY_CATCH_ERROR) {
        failed = true;error = value.error;value = null;
      } else {
        succeeded = true;
      }if (promise === value) {
        lib$es6$promise$$internal$$reject(promise, lib$es6$promise$$internal$$cannotReturnOwn());return;
      }
    } else {
      value = detail;succeeded = true;
    }if (promise._state !== lib$es6$promise$$internal$$PENDING) {} else if (hasCallback && succeeded) {
      lib$es6$promise$$internal$$resolve(promise, value);
    } else if (failed) {
      lib$es6$promise$$internal$$reject(promise, error);
    } else if (settled === lib$es6$promise$$internal$$FULFILLED) {
      lib$es6$promise$$internal$$fulfill(promise, value);
    } else if (settled === lib$es6$promise$$internal$$REJECTED) {
      lib$es6$promise$$internal$$reject(promise, value);
    }
  }function lib$es6$promise$$internal$$initializePromise(promise, resolver) {
    try {
      resolver(function resolvePromise(value) {
        lib$es6$promise$$internal$$resolve(promise, value);
      }, function rejectPromise(reason) {
        lib$es6$promise$$internal$$reject(promise, reason);
      });
    } catch (e) {
      lib$es6$promise$$internal$$reject(promise, e);
    }
  }function lib$es6$promise$enumerator$$Enumerator(Constructor, input) {
    var enumerator = this;enumerator._instanceConstructor = Constructor;enumerator.promise = new Constructor(lib$es6$promise$$internal$$noop);if (enumerator._validateInput(input)) {
      enumerator._input = input;enumerator.length = input.length;enumerator._remaining = input.length;enumerator._init();if (enumerator.length === 0) {
        lib$es6$promise$$internal$$fulfill(enumerator.promise, enumerator._result);
      } else {
        enumerator.length = enumerator.length || 0;enumerator._enumerate();if (enumerator._remaining === 0) {
          lib$es6$promise$$internal$$fulfill(enumerator.promise, enumerator._result);
        }
      }
    } else {
      lib$es6$promise$$internal$$reject(enumerator.promise, enumerator._validationError());
    }
  }lib$es6$promise$enumerator$$Enumerator.prototype._validateInput = function (input) {
    return lib$es6$promise$utils$$isArray(input);
  };lib$es6$promise$enumerator$$Enumerator.prototype._validationError = function () {
    return new Error("Array Methods must be provided an Array");
  };lib$es6$promise$enumerator$$Enumerator.prototype._init = function () {
    this._result = new Array(this.length);
  };var lib$es6$promise$enumerator$$default = lib$es6$promise$enumerator$$Enumerator;lib$es6$promise$enumerator$$Enumerator.prototype._enumerate = function () {
    var enumerator = this;var length = enumerator.length;var promise = enumerator.promise;var input = enumerator._input;for (var i = 0; promise._state === lib$es6$promise$$internal$$PENDING && i < length; i++) {
      enumerator._eachEntry(input[i], i);
    }
  };lib$es6$promise$enumerator$$Enumerator.prototype._eachEntry = function (entry, i) {
    var enumerator = this;var c = enumerator._instanceConstructor;if (lib$es6$promise$utils$$isMaybeThenable(entry)) {
      if (entry.constructor === c && entry._state !== lib$es6$promise$$internal$$PENDING) {
        entry._onerror = null;enumerator._settledAt(entry._state, i, entry._result);
      } else {
        enumerator._willSettleAt(c.resolve(entry), i);
      }
    } else {
      enumerator._remaining--;enumerator._result[i] = entry;
    }
  };lib$es6$promise$enumerator$$Enumerator.prototype._settledAt = function (state, i, value) {
    var enumerator = this;var promise = enumerator.promise;if (promise._state === lib$es6$promise$$internal$$PENDING) {
      enumerator._remaining--;if (state === lib$es6$promise$$internal$$REJECTED) {
        lib$es6$promise$$internal$$reject(promise, value);
      } else {
        enumerator._result[i] = value;
      }
    }if (enumerator._remaining === 0) {
      lib$es6$promise$$internal$$fulfill(promise, enumerator._result);
    }
  };lib$es6$promise$enumerator$$Enumerator.prototype._willSettleAt = function (promise, i) {
    var enumerator = this;lib$es6$promise$$internal$$subscribe(promise, undefined, function (value) {
      enumerator._settledAt(lib$es6$promise$$internal$$FULFILLED, i, value);
    }, function (reason) {
      enumerator._settledAt(lib$es6$promise$$internal$$REJECTED, i, reason);
    });
  };function lib$es6$promise$promise$all$$all(entries) {
    return new lib$es6$promise$enumerator$$default(this, entries).promise;
  }var lib$es6$promise$promise$all$$default = lib$es6$promise$promise$all$$all;function lib$es6$promise$promise$race$$race(entries) {
    var Constructor = this;var promise = new Constructor(lib$es6$promise$$internal$$noop);if (!lib$es6$promise$utils$$isArray(entries)) {
      lib$es6$promise$$internal$$reject(promise, new TypeError("You must pass an array to race."));return promise;
    }var length = entries.length;function onFulfillment(value) {
      lib$es6$promise$$internal$$resolve(promise, value);
    }function onRejection(reason) {
      lib$es6$promise$$internal$$reject(promise, reason);
    }for (var i = 0; promise._state === lib$es6$promise$$internal$$PENDING && i < length; i++) {
      lib$es6$promise$$internal$$subscribe(Constructor.resolve(entries[i]), undefined, onFulfillment, onRejection);
    }return promise;
  }var lib$es6$promise$promise$race$$default = lib$es6$promise$promise$race$$race;function lib$es6$promise$promise$resolve$$resolve(object) {
    var Constructor = this;if (object && typeof object === "object" && object.constructor === Constructor) {
      return object;
    }var promise = new Constructor(lib$es6$promise$$internal$$noop);lib$es6$promise$$internal$$resolve(promise, object);return promise;
  }var lib$es6$promise$promise$resolve$$default = lib$es6$promise$promise$resolve$$resolve;function lib$es6$promise$promise$reject$$reject(reason) {
    var Constructor = this;var promise = new Constructor(lib$es6$promise$$internal$$noop);lib$es6$promise$$internal$$reject(promise, reason);return promise;
  }var lib$es6$promise$promise$reject$$default = lib$es6$promise$promise$reject$$reject;var lib$es6$promise$promise$$counter = 0;function lib$es6$promise$promise$$needsResolver() {
    throw new TypeError("You must pass a resolver function as the first argument to the promise constructor");
  }function lib$es6$promise$promise$$needsNew() {
    throw new TypeError("Failed to construct 'Promise': Please use the 'new' operator, this object constructor cannot be called as a function.");
  }var lib$es6$promise$promise$$default = lib$es6$promise$promise$$Promise;function lib$es6$promise$promise$$Promise(resolver) {
    this._id = lib$es6$promise$promise$$counter++;this._state = undefined;this._result = undefined;this._subscribers = [];if (lib$es6$promise$$internal$$noop !== resolver) {
      if (!lib$es6$promise$utils$$isFunction(resolver)) {
        lib$es6$promise$promise$$needsResolver();
      }if (!(this instanceof lib$es6$promise$promise$$Promise)) {
        lib$es6$promise$promise$$needsNew();
      }lib$es6$promise$$internal$$initializePromise(this, resolver);
    }
  }lib$es6$promise$promise$$Promise.all = lib$es6$promise$promise$all$$default;lib$es6$promise$promise$$Promise.race = lib$es6$promise$promise$race$$default;lib$es6$promise$promise$$Promise.resolve = lib$es6$promise$promise$resolve$$default;lib$es6$promise$promise$$Promise.reject = lib$es6$promise$promise$reject$$default;lib$es6$promise$promise$$Promise._setScheduler = lib$es6$promise$asap$$setScheduler;lib$es6$promise$promise$$Promise._asap = lib$es6$promise$asap$$default;lib$es6$promise$promise$$Promise.prototype = { constructor: lib$es6$promise$promise$$Promise, then: function then(onFulfillment, onRejection) {
      var parent = this;var state = parent._state;if (state === lib$es6$promise$$internal$$FULFILLED && !onFulfillment || state === lib$es6$promise$$internal$$REJECTED && !onRejection) {
        return this;
      }var child = new this.constructor(lib$es6$promise$$internal$$noop);var result = parent._result;if (state) {
        var callback = arguments[state - 1];lib$es6$promise$asap$$default(function () {
          lib$es6$promise$$internal$$invokeCallback(state, child, callback, result);
        });
      } else {
        lib$es6$promise$$internal$$subscribe(parent, child, onFulfillment, onRejection);
      }return child;
    }, "catch": function _catch(onRejection) {
      return this.then(null, onRejection);
    } };function lib$es6$promise$polyfill$$polyfill() {
    var local;if (typeof global !== "undefined") {
      local = global;
    } else if (typeof self !== "undefined") {
      local = self;
    } else {
      try {
        local = Function("return this")();
      } catch (e) {
        throw new Error("polyfill failed because global object is unavailable in this environment");
      }
    }var P = local.Promise;if (P && Object.prototype.toString.call(P.resolve()) === "[object Promise]" && !P.cast) {
      return;
    }local.Promise = lib$es6$promise$promise$$default;
  }var lib$es6$promise$polyfill$$default = lib$es6$promise$polyfill$$polyfill;var lib$es6$promise$umd$$ES6Promise = { Promise: lib$es6$promise$promise$$default, polyfill: lib$es6$promise$polyfill$$default };if (typeof define === "function" && define["amd"]) {
    define(function () {
      return lib$es6$promise$umd$$ES6Promise;
    });
  } else if (typeof module !== "undefined" && module["exports"]) {
    module["exports"] = lib$es6$promise$umd$$ES6Promise;
  } else if (typeof this !== "undefined") {
    this["ES6Promise"] = lib$es6$promise$umd$$ES6Promise;
  }lib$es6$promise$polyfill$$default();
}).call(undefined);

}).call(this,require('_process'),typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {})
},{"_process":3}],2:[function(require,module,exports){
'use strict';

(function () {
  'use strict';

  if (self.fetch) {
    return;
  }

  function normalizeName(name) {
    if (typeof name !== 'string') {
      name = name.toString();
    }
    if (/[^a-z0-9\-#$%&'*+.\^_`|~]/i.test(name)) {
      throw new TypeError('Invalid character in header field name');
    }
    return name.toLowerCase();
  }

  function normalizeValue(value) {
    if (typeof value !== 'string') {
      value = value.toString();
    }
    return value;
  }

  function Headers(headers) {
    this.map = {};

    if (headers instanceof Headers) {
      headers.forEach(function (value, name) {
        this.append(name, value);
      }, this);
    } else if (headers) {
      Object.getOwnPropertyNames(headers).forEach(function (name) {
        this.append(name, headers[name]);
      }, this);
    }
  }

  Headers.prototype.append = function (name, value) {
    name = normalizeName(name);
    value = normalizeValue(value);
    var list = this.map[name];
    if (!list) {
      list = [];
      this.map[name] = list;
    }
    list.push(value);
  };

  Headers.prototype['delete'] = function (name) {
    delete this.map[normalizeName(name)];
  };

  Headers.prototype.get = function (name) {
    var values = this.map[normalizeName(name)];
    return values ? values[0] : null;
  };

  Headers.prototype.getAll = function (name) {
    return this.map[normalizeName(name)] || [];
  };

  Headers.prototype.has = function (name) {
    return this.map.hasOwnProperty(normalizeName(name));
  };

  Headers.prototype.set = function (name, value) {
    this.map[normalizeName(name)] = [normalizeValue(value)];
  };

  Headers.prototype.forEach = function (callback, thisArg) {
    Object.getOwnPropertyNames(this.map).forEach(function (name) {
      this.map[name].forEach(function (value) {
        callback.call(thisArg, value, name, this);
      }, this);
    }, this);
  };

  function consumed(body) {
    if (body.bodyUsed) {
      return Promise.reject(new TypeError('Already read'));
    }
    body.bodyUsed = true;
  }

  function fileReaderReady(reader) {
    return new Promise(function (resolve, reject) {
      reader.onload = function () {
        resolve(reader.result);
      };
      reader.onerror = function () {
        reject(reader.error);
      };
    });
  }

  function readBlobAsArrayBuffer(blob) {
    var reader = new FileReader();
    reader.readAsArrayBuffer(blob);
    return fileReaderReady(reader);
  }

  function readBlobAsText(blob) {
    var reader = new FileReader();
    reader.readAsText(blob);
    return fileReaderReady(reader);
  }

  var support = {
    blob: 'FileReader' in self && 'Blob' in self && (function () {
      try {
        new Blob();
        return true;
      } catch (e) {
        return false;
      }
    })(),
    formData: 'FormData' in self
  };

  function Body() {
    this.bodyUsed = false;

    this._initBody = function (body) {
      this._bodyInit = body;
      if (typeof body === 'string') {
        this._bodyText = body;
      } else if (support.blob && Blob.prototype.isPrototypeOf(body)) {
        this._bodyBlob = body;
      } else if (support.formData && FormData.prototype.isPrototypeOf(body)) {
        this._bodyFormData = body;
      } else if (!body) {
        this._bodyText = '';
      } else {
        throw new Error('unsupported BodyInit type');
      }
    };

    if (support.blob) {
      this.blob = function () {
        var rejected = consumed(this);
        if (rejected) {
          return rejected;
        }

        if (this._bodyBlob) {
          return Promise.resolve(this._bodyBlob);
        } else if (this._bodyFormData) {
          throw new Error('could not read FormData body as blob');
        } else {
          return Promise.resolve(new Blob([this._bodyText]));
        }
      };

      this.arrayBuffer = function () {
        return this.blob().then(readBlobAsArrayBuffer);
      };

      this.text = function () {
        var rejected = consumed(this);
        if (rejected) {
          return rejected;
        }

        if (this._bodyBlob) {
          return readBlobAsText(this._bodyBlob);
        } else if (this._bodyFormData) {
          throw new Error('could not read FormData body as text');
        } else {
          return Promise.resolve(this._bodyText);
        }
      };
    } else {
      this.text = function () {
        var rejected = consumed(this);
        return rejected ? rejected : Promise.resolve(this._bodyText);
      };
    }

    if (support.formData) {
      this.formData = function () {
        return this.text().then(decode);
      };
    }

    this.json = function () {
      return this.text().then(JSON.parse);
    };

    return this;
  }

  // HTTP methods whose capitalization should be normalized
  var methods = ['DELETE', 'GET', 'HEAD', 'OPTIONS', 'POST', 'PUT'];

  function normalizeMethod(method) {
    var upcased = method.toUpperCase();
    return methods.indexOf(upcased) > -1 ? upcased : method;
  }

  function Request(url, options) {
    options = options || {};
    this.url = url;

    this.credentials = options.credentials || 'omit';
    this.headers = new Headers(options.headers);
    this.method = normalizeMethod(options.method || 'GET');
    this.mode = options.mode || null;
    this.referrer = null;

    if ((this.method === 'GET' || this.method === 'HEAD') && options.body) {
      throw new TypeError('Body not allowed for GET or HEAD requests');
    }
    this._initBody(options.body);
  }

  function decode(body) {
    var form = new FormData();
    body.trim().split('&').forEach(function (bytes) {
      if (bytes) {
        var split = bytes.split('=');
        var name = split.shift().replace(/\+/g, ' ');
        var value = split.join('=').replace(/\+/g, ' ');
        form.append(decodeURIComponent(name), decodeURIComponent(value));
      }
    });
    return form;
  }

  function headers(xhr) {
    var head = new Headers();
    var pairs = xhr.getAllResponseHeaders().trim().split('\n');
    pairs.forEach(function (header) {
      var split = header.trim().split(':');
      var key = split.shift().trim();
      var value = split.join(':').trim();
      head.append(key, value);
    });
    return head;
  }

  Body.call(Request.prototype);

  function Response(bodyInit, options) {
    if (!options) {
      options = {};
    }

    this._initBody(bodyInit);
    this.type = 'default';
    this.url = null;
    this.status = options.status;
    this.ok = this.status >= 200 && this.status < 300;
    this.statusText = options.statusText;
    this.headers = options.headers instanceof Headers ? options.headers : new Headers(options.headers);
    this.url = options.url || '';
  }

  Body.call(Response.prototype);

  self.Headers = Headers;
  self.Request = Request;
  self.Response = Response;

  self.fetch = function (input, init) {
    // TODO: Request constructor should accept input, init
    var request;
    if (Request.prototype.isPrototypeOf(input) && !init) {
      request = input;
    } else {
      request = new Request(input, init);
    }

    return new Promise(function (resolve, reject) {
      var xhr = new XMLHttpRequest();

      function responseURL() {
        if ('responseURL' in xhr) {
          return xhr.responseURL;
        }

        // Avoid security warnings on getResponseHeader when not allowed by CORS
        if (/^X-Request-URL:/m.test(xhr.getAllResponseHeaders())) {
          return xhr.getResponseHeader('X-Request-URL');
        }

        return;
      }

      xhr.onload = function () {
        var status = xhr.status === 1223 ? 204 : xhr.status;
        if (status < 100 || status > 599) {
          reject(new TypeError('Network request failed'));
          return;
        }
        var options = {
          status: status,
          statusText: xhr.statusText,
          headers: headers(xhr),
          url: responseURL()
        };
        var body = 'response' in xhr ? xhr.response : xhr.responseText;
        resolve(new Response(body, options));
      };

      xhr.onerror = function () {
        reject(new TypeError('Network request failed'));
      };

      xhr.open(request.method, request.url, true);

      if (request.credentials === 'include') {
        xhr.withCredentials = true;
      }

      if ('responseType' in xhr && support.blob) {
        xhr.responseType = 'blob';
      }

      request.headers.forEach(function (value, name) {
        xhr.setRequestHeader(name, value);
      });

      xhr.send(typeof request._bodyInit === 'undefined' ? null : request._bodyInit);
    });
  };
  self.fetch.polyfill = true;
})();

},{}],3:[function(require,module,exports){
// shim for using process in browser

var process = module.exports = {};
var queue = [];
var draining = false;
var currentQueue;
var queueIndex = -1;

function cleanUpNextTick() {
    draining = false;
    if (currentQueue.length) {
        queue = currentQueue.concat(queue);
    } else {
        queueIndex = -1;
    }
    if (queue.length) {
        drainQueue();
    }
}

function drainQueue() {
    if (draining) {
        return;
    }
    var timeout = setTimeout(cleanUpNextTick);
    draining = true;

    var len = queue.length;
    while(len) {
        currentQueue = queue;
        queue = [];
        while (++queueIndex < len) {
            currentQueue[queueIndex].run();
        }
        queueIndex = -1;
        len = queue.length;
    }
    currentQueue = null;
    draining = false;
    clearTimeout(timeout);
}

process.nextTick = function (fun) {
    var args = new Array(arguments.length - 1);
    if (arguments.length > 1) {
        for (var i = 1; i < arguments.length; i++) {
            args[i - 1] = arguments[i];
        }
    }
    queue.push(new Item(fun, args));
    if (queue.length === 1 && !draining) {
        setTimeout(drainQueue, 0);
    }
};

// v8 likes predictible objects
function Item(fun, array) {
    this.fun = fun;
    this.array = array;
}
Item.prototype.run = function () {
    this.fun.apply(null, this.array);
};
process.title = 'browser';
process.browser = true;
process.env = {};
process.argv = [];
process.version = ''; // empty string to avoid regexp issues
process.versions = {};

function noop() {}

process.on = noop;
process.addListener = noop;
process.once = noop;
process.off = noop;
process.removeListener = noop;
process.removeAllListeners = noop;
process.emit = noop;

process.binding = function (name) {
    throw new Error('process.binding is not supported');
};

// TODO(shtylman)
process.cwd = function () { return '/' };
process.chdir = function (dir) {
    throw new Error('process.chdir is not supported');
};
process.umask = function() { return 0; };

},{}],4:[function(require,module,exports){
'use strict';

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { 'default': obj }; }

require('../../bower_components/es6-promise/promise.min.js');

require('../../bower_components/fetch/fetch.js');

var _chromeExtension = require('./chrome-extension');

var _chromeExtension2 = _interopRequireDefault(_chromeExtension);

var _editor = require('./editor');

var _editor2 = _interopRequireDefault(_editor);

var _editSwirl = require('./edit-swirl');

var _editSwirl2 = _interopRequireDefault(_editSwirl);

var _responseForm = require('./response-form');

var _responseForm2 = _interopRequireDefault(_responseForm);

var _commentForm = require('./comment-form');

var _commentForm2 = _interopRequireDefault(_commentForm);

var _menu = require('./menu');

var _menu2 = _interopRequireDefault(_menu);

var _swirlList = require('./swirl-list');

var _swirlList2 = _interopRequireDefault(_swirlList);

var _ga = require('./ga');

var _ga2 = _interopRequireDefault(_ga);

$(document).ready(function () {
    _editor2['default'].init($);
    _editor2['default'].initWidgets($);
    (0, _editSwirl2['default'])();
    (0, _chromeExtension2['default'])();
    _responseForm2['default'].init($);
    _commentForm2['default'].init($);
    _menu2['default'].init($);
    _swirlList2['default'].init($);
    _ga2['default'].addAnalyticsIfProd();

    if (document.getElementById('share-buttons')) {
        !(function (d, s, id) {
            var js,
                fjs = d.getElementsByTagName(s)[0],
                p = /^http:/.test(d.location) ? 'http' : 'https';
            if (!d.getElementById(id)) {
                js = d.createElement(s);
                js.id = id;
                js.src = p + '://platform.twitter.com/widgets.js';
                fjs.parentNode.insertBefore(js, fjs);
            }
        })(document, 'script', 'twitter-wjs');
        !(function (d, s, id) {
            var js,
                fjs = d.getElementsByTagName(s)[0];
            if (d.getElementById(id)) return;
            js = d.createElement(s);
            js.id = id;
            js.src = '//connect.facebook.net/en_GB/sdk.js#xfbml=1&version=v2.4&appId=893395944039576';
            fjs.parentNode.insertBefore(js, fjs);
        })(document, 'script', 'facebook-jssdk');
    }
});

},{"../../bower_components/es6-promise/promise.min.js":1,"../../bower_components/fetch/fetch.js":2,"./chrome-extension":5,"./comment-form":6,"./edit-swirl":7,"./editor":8,"./ga":9,"./menu":11,"./response-form":12,"./swirl-list":13}],5:[function(require,module,exports){
'use strict';

var setupChromeExtension = function setupChromeExtension() {
    var chrome = window.chrome;
    if (chrome && chrome.app) {
        $('.chrome-only').css('display', 'block');

        if (chrome.app.isInstalled) {
            // Note: this always returns false even when installed
            $('.chrome-extension-installed').css('display', 'block');
        } else {
            $('.install-chrome-extension-box').css('display', 'block');
            $('.add-to-chrome-button').click(function () {
                console.log('running');
                chrome.webstore.install(undefined, function (suc) {
                    $('.chrome-extension-installed').css('display', 'block');
                    $('.install-chrome-extension-box').css('display', 'none');
                    console.log('Installation succeeded', suc);
                }, function (err) {
                    console.log('Installation failed', err);
                });
            });
        }
    }
};
module.exports = setupChromeExtension;

},{}],6:[function(require,module,exports){
'use strict';

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ('value' in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { 'default': obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError('Cannot call a class as a function'); } }

var _httpJs = require('./http.js');

var _httpJs2 = _interopRequireDefault(_httpJs);

var _editorJs = require('./editor.js');

var _editorJs2 = _interopRequireDefault(_editorJs);

var CommentForm = (function () {
    function CommentForm($, form) {
        var _this = this;

        _classCallCheck(this, CommentForm);

        this.editor = new _editorJs2['default'].RichTextEditor($(form).find('div.rte'));
        this.$form = $(form);
        this.$addButton = this.$form.find('input[type=submit]');
        this.$maxCommentIdField = this.$form.find('.max-comment-id-field');
        this.swirlId = parseInt($(form).find('.swirl-id-field').val(), 10);
        this.refreshToken = null;

        $(form).submit(function () {
            _this.setLoading();

            var commentHtml = _this.editor.getHtmlContent();
            console.log('Going to post comment', commentHtml);
            if (commentHtml) {
                _httpJs2['default'].post('/swirls/' + _this.swirlId + '/comment', { comment: commentHtml }).then(_this.addMissingComments.bind(_this)).then(function () {
                    _this.resetForm();
                });
            }
            return false;
        });

        this.addMissingComments();
    }

    _createClass(CommentForm, [{
        key: 'addMissingComments',
        value: function addMissingComments() {
            if (this.refreshToken) {
                clearTimeout(this.refreshToken);
            }
            var me = this;
            return _httpJs2['default'].getJson('/swirls/' + me.swirlId + '/comments?comment-id-start=' + me.$maxCommentIdField.val()).then(function (comments) {
                if (comments.count > 0) {
                    $('.comments').append(comments.html);
                    me.$maxCommentIdField.val(comments.maxId);
                }
                me.refreshToken = setTimeout(me.addMissingComments.bind(me), 30000);
            });
        }
    }, {
        key: 'setLoading',
        value: function setLoading() {
            this.$addButton.addClass('button-loading');
            this.$addButton.prop('disabled', true);
        }
    }, {
        key: 'resetForm',
        value: function resetForm() {
            this.$addButton.prop('disabled', false);
            this.$addButton.removeClass('button-loading');
            this.editor.clear();
        }
    }]);

    return CommentForm;
})();

var init = function init($) {
    $('form.comment').each(function (i, f) {
        return new CommentForm($, f);
    });
};

module.exports = { init: init };

},{"./editor.js":8,"./http.js":10}],7:[function(require,module,exports){
'use strict';

var setup = function setup() {

    var addUser = function addUser(textbox) {
        var nameOrEmail = textbox.value;
        var label = $(document.createElement('label'));
        $(textbox).before(label);
        var cb = $(document.createElement('input')).attr('type', 'checkbox').attr('name', 'who').attr('value', nameOrEmail).attr('checked', 'checked');
        label.append(cb);
        label.append(document.createTextNode(nameOrEmail));
        textbox.value = '';
    };

    $('.user-select-box input').keypress(function (event) {
        if (event.which == 13) {
            addUser(this);
            return false;
        }
    });
};

module.exports = setup;

},{}],8:[function(require,module,exports){
'use strict';

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ('value' in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { 'default': obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError('Cannot call a class as a function'); } }

var _httpJs = require('./http.js');

var _httpJs2 = _interopRequireDefault(_httpJs);

var RichTextEditor = (function () {
    function RichTextEditor($rteDiv) {
        var _this = this;

        _classCallCheck(this, RichTextEditor);

        this.$textarea = $rteDiv.find('textarea').first();
        this.$editorDiv = $rteDiv.find('.editor').first();
        this.editorDiv = this.$editorDiv[0];

        $rteDiv.find('.spoiler-alert-button').click(function () {
            _this.addHtmlAtCursor('<div class="spoiler-alert">' + '<div class="spoiler-alert--bar" title="Click to expand" contenteditable="false">' + '<button class="spoiler-alert--close-button" title="Delete spoiler alert">x</button>' + '<a href="#">Spoiler alert</a>' + '</div>' + '<div class="spoiler-alert--content" data-ph="Write your spoilers here - they will not be shown unless clicked on"></div>' + '</div>' + '<p data-ph="..."></p>');
            // HACK: this is repeated below and is just adding more and more click handlers each time
            $('.spoiler-alert--close-button').click(function (b) {
                $(b.target).closest('.spoiler-alert').remove();
                return false;
            });

            return false;
        });

        var me = this;
        this.$editorDiv.bind('paste', function () {
            setTimeout(me.convertPlainTextLinksToAnchorTags.bind(me), 1);
        });

        var html = this.$textarea.val();
        this.$editorDiv.html(html);

        $rteDiv.closest('form').on('submit', function () {
            _this.$textarea.val(_this.getHtmlContent());
            return true;
        });
    }

    _createClass(RichTextEditor, [{
        key: 'visitDescendents',
        value: function visitDescendents(startNode, visitor) {
            var children = startNode.childNodes;
            for (var i = 0; i < children.length; i++) {
                var child = children[i];
                var continueDown = visitor(child);
                if (continueDown) {
                    this.visitDescendents(child, visitor);
                }
            }
        }
    }, {
        key: 'enrichLinks',
        value: function enrichLinks() {
            this.$editorDiv.find('a').each(function (i, e) {
                if (e.href === e.innerText && e.className !== 'transforming-link') {
                    e.className = 'transforming-link';
                    $(e).append('<i class="fa fa-spin fa-spinner"></i>');
                    _httpJs2['default'].getJson('/website-service/get-metadata?url=' + encodeURI(e.href)).then(function (metadata) {
                        console.log('Got metadata', metadata);
                        e.innerText = metadata.title || e.href;
                        e.className = '';
                        var html = metadata['embed-html'];
                        if (html) {
                            $(e).after('<div class="user-entered-embed-box">' + html + '</div>');
                        }
                    });
                }
            });
        }
    }, {
        key: 'convertPlainTextLinksToAnchorTags',
        value: function convertPlainTextLinksToAnchorTags() {
            var node = this.editorDiv;
            var links = this.getHtmlContent().match(/(http(s|):\/\/[^<>\s]+(\.[^<>\s]+)*(|:[0-9]+)[^<>\s]*)/g);
            if (!links) {
                return;
            }

            this.visitDescendents(node, function (e) {
                if (e.nodeType === 3) {
                    // text nodes
                    for (var i = 0; i < links.length; i++) {
                        var htmlEncodedLink = $('<div/>').html(links[i]).text();
                        var index = e.data.indexOf(htmlEncodedLink);
                        if (index > -1) {
                            // split the text node into 3 bits - the 'nextBit' is the part containing the URL
                            var nextBit = e.splitText(index);
                            nextBit.splitText(htmlEncodedLink.length);
                            var target = htmlEncodedLink.indexOf('http://www.swrl.co') === 0 ? '' : ' target="_blank"';
                            $(nextBit).before('<a href="' + htmlEncodedLink + '"' + target + '>' + htmlEncodedLink + '</a>');
                            nextBit.data = ' ';
                            return false; // stop processing this bit - we've changed it so processing will be weird
                        }
                    }
                    if (!window.todo) window.todo = e;
                } else if (e.nodeType === 1 && e.tagName === 'A') {
                    // This is an anchor element already - don't convert the HTML twice dawg
                    return false;
                }
                return true;
            });

            this.enrichLinks();
        }
    }, {
        key: 'addHtmlAtCursor',
        value: function addHtmlAtCursor(html) {
            this.$editorDiv.append(html);
        }
    }, {
        key: 'getHtmlContent',
        value: function getHtmlContent() {
            return this.$editorDiv.html().trim();
        }
    }, {
        key: 'clear',
        value: function clear() {
            this.$textarea.val('');
            this.$editorDiv.html('');
        }
    }]);

    return RichTextEditor;
})();

var setup = function setup($) {
    $('.rte').each(function (i, holder) {
        new RichTextEditor($(holder));
    });
};

var initWidgets = function initWidgets($) {
    $('.spoiler-alert--bar a').click(function (b) {
        $(b.target).closest('.spoiler-alert').find('.spoiler-alert--content').toggle();
        return false;
    });

    $('.spoiler-alert--close-button').click(function (b) {
        $(b.target).closest('.spoiler-alert').remove();
        return false;
    });
};

module.exports = { init: setup, RichTextEditor: RichTextEditor, initWidgets: initWidgets };

},{"./http.js":10}],9:[function(require,module,exports){
'use strict';

var init = function init() {
    if (document.location.hostname === 'www.swrl.co') {

        // the contents of this if block is copied directly from google analytics

        (function (i, s, o, g, r, a, m) {
            i['GoogleAnalyticsObject'] = r;
            i[r] = i[r] || function () {
                (i[r].q = i[r].q || []).push(arguments);
            }, i[r].l = 1 * new Date();
            a = s.createElement(o), m = s.getElementsByTagName(o)[0];
            a.async = 1;
            a.src = g;
            m.parentNode.insertBefore(a, m);
        })(window, document, 'script', '//www.google-analytics.com/analytics.js', 'ga');

        ga('create', 'UA-63844233-1', 'auto');
        ga('send', 'pageview');
    }
};

module.exports = { addAnalyticsIfProd: init };

},{}],10:[function(require,module,exports){
'use strict';

var getJson = function getJson(url) {
    return fetch('/api/v1' + url, {
        credentials: 'same-origin',
        headers: {
            'Accept': 'application/json'
        }
    }).then(function (r) {
        return r.json();
    });
};

var post = function post(url, json) {
    return fetch('/api/v1' + url, {
        method: 'post',
        credentials: 'same-origin',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(json)
    });
};

module.exports = { getJson: getJson, post: post };

},{}],11:[function(require,module,exports){
'use strict';

var setup = function setup($) {
    var body = $('body');

    $('.menu-button').click(function () {
        body.toggleClass('menu-open');
        window.scrollTo(0, 0);
        return false;
    });
};

module.exports = { init: setup };

},{}],12:[function(require,module,exports){
'use strict';

var _createClass = (function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ('value' in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; })();

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { 'default': obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError('Cannot call a class as a function'); } }

var _httpJs = require('./http.js');

var _httpJs2 = _interopRequireDefault(_httpJs);

var RespondForm = (function () {
    function RespondForm($, form) {
        var _this = this;

        _classCallCheck(this, RespondForm);

        this.$form = $(form);
        this.response = null;
        $(form).find('button').click(this.buttonClick.bind(this));

        var customInputBox = $(form).find('.custom-response');

        $(form).submit(function () {
            var swirlId = parseInt($(form).find('.swirl-id-field').val(), 10);
            var response = _this.getResponse();
            $(customInputBox).val('');
            if (response) {
                _this.setSelectedButton(response, 'button-loading');
                _httpJs2['default'].post('/swirls/' + swirlId + '/respond', { responseButton: response }).then(function () {
                    _this.setSelectedButton(response, 'swirl-button');
                });
            }
            return false;
        });

        customInputBox.keypress(function (e) {
            if (e.keyCode === 13) {
                $(form).find('.custom-response-button').click();
                return false;
            }
        });
    }

    _createClass(RespondForm, [{
        key: 'setSelectedButton',
        value: function setSelectedButton(val, selectedClass) {
            var buttonIsOnScreen = false;
            var arbitraryButton = null;
            this.$form.find('button').each(function (i, el) {
                $(el).removeClass('swirl-button');
                $(el).removeClass('button-loading');
                if (el.value.toLowerCase() === val.toLowerCase()) {
                    buttonIsOnScreen = true;
                    $(el).addClass(selectedClass);
                } else {
                    if (!arbitraryButton) {
                        arbitraryButton = el;
                    }
                }
            });
            if (!buttonIsOnScreen) {
                var newOne = $(arbitraryButton).clone(true);
                newOne.val(val).addClass(selectedClass);
                newOne.text(val);
                this.$form.find('.response-buttons').append(newOne);
            }
        }
    }, {
        key: 'buttonClick',
        value: function buttonClick(e) {
            if (e.target.getAttribute('data-button-type') === 'custom') {
                this.response = $(e.target.form).find('.custom-response').val();
            } else {
                this.response = e.target.value;
            }
        }
    }, {
        key: 'getResponse',
        value: function getResponse() {
            return (this.response || '').trim();
        }
    }]);

    return RespondForm;
})();

var init = function init($) {

    $('.respond-form').each(function (i, f) {
        return new RespondForm($, f);
    });
    //new RespondForm();
};

module.exports = { init: init };

},{"./http.js":10}],13:[function(require,module,exports){
'use strict';

var currentFilter = null;

var showSwirls = function showSwirls(button) {
    var swirlType = button.getAttribute('data-swirl-type');
    $(button).toggleClass('hidden', false);
    $('.mini-swirl.' + swirlType).show();
};

var hideSwirls = function hideSwirls(button) {
    var swirlType = button.getAttribute('data-swirl-type');
    $(button).toggleClass('hidden', true);
    $('.mini-swirl.' + swirlType).hide();
};

function init($) {
    $('.type-filter button').click(function (b) {
        if (currentFilter == null) {
            $('.type-filter button').each(function (i, typeButton) {
                if (b.target !== typeButton) {
                    hideSwirls(typeButton);
                }
            });
            currentFilter = b.target;
        } else if (currentFilter === b.target) {
            $('.type-filter button').each(function (i, typeButton) {
                if (b.target !== typeButton) {
                    showSwirls(typeButton);
                }
            });
            currentFilter = null;
        } else {
            hideSwirls(currentFilter);
            showSwirls(b.target);
            currentFilter = b.target;
        }
    });
}

module.exports = {
    init: init
};

},{}]},{},[4]);
