(function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){
"use strict";

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { "default": obj }; }

var _chromeExtension = require("./chrome-extension");

var _chromeExtension2 = _interopRequireDefault(_chromeExtension);

var _editor = require("./editor");

var _editor2 = _interopRequireDefault(_editor);

var _editSwirl = require("./edit-swirl");

var _editSwirl2 = _interopRequireDefault(_editSwirl);

$(document).ready(function () {
    (0, _editor2["default"])();
    (0, _editSwirl2["default"])();
    (0, _chromeExtension2["default"])();
});

},{"./chrome-extension":2,"./edit-swirl":3,"./editor":4}],2:[function(require,module,exports){
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

},{}],3:[function(require,module,exports){
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

},{}],4:[function(require,module,exports){
"use strict";

var setup = function setup() {
    $(".rte").each(function (i, holder) {
        var textarea = $(holder).find("textarea").first();
        var editor = $(holder).find(".editor").first();

        var html = textarea.val();
        editor.html(html);

        $(holder).closest("form").on("submit", function () {
            textarea.val(editor.html());
            return true;
        });
    });
};
module.exports = setup;

},{}]},{},[1]);
