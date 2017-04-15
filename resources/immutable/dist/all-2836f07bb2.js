(function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){
'use strict';

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

var _search = require('./search');

var _search2 = _interopRequireDefault(_search);

var _swirlList = require('./swirl-list');

var _swirlList2 = _interopRequireDefault(_swirlList);

var _ga = require('./ga');

var _ga2 = _interopRequireDefault(_ga);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

(function () {
    var fill = function fill(skip, filename) {
        if (!skip) {
            document.write('<scr' + 'ipt type="text/javascript" src="/immutable/js/' + filename + '"></scr' + 'ipt>');
        }
    };
    fill(window.Promise, 'promise-6.0.2.min.js');
    fill(window.fetch, 'fetch-2.0.3.js');
})();

$(document).ready(function () {
    _editor2.default.init($);
    _editor2.default.initWidgets($);
    (0, _editSwirl2.default)();
    (0, _chromeExtension2.default)();
    _responseForm2.default.init($);
    _commentForm2.default.init($);
    _menu2.default.init($);
    _swirlList2.default.init($);
    _search2.default.init($);
    _ga2.default.addAnalyticsIfProd();
    !function (d, s, id) {
        var js,
            fjs = d.getElementsByTagName(s)[0],
            p = /^http:/.test(d.location) ? 'http' : 'https';
        if (!d.getElementById(id)) {
            js = d.createElement(s);
            js.id = id;
            js.src = p + '://platform.twitter.com/widgets.js';
            fjs.parentNode.insertBefore(js, fjs);
        }
    }(document, 'script', 'twitter-wjs');
    !function (d, s, id) {
        var js,
            fjs = d.getElementsByTagName(s)[0];
        if (d.getElementById(id)) return;
        js = d.createElement(s);
        js.id = id;
        js.src = "//connect.facebook.net/en_GB/sdk.js#xfbml=1&version=v2.4&appId=893395944039576";
        fjs.parentNode.insertBefore(js, fjs);
    }(document, 'script', 'facebook-jssdk');

    $('.show-notifications-button').click(function () {
        $('.notification li').css('display', 'list-item');
        $('.show-notifications-button').hide();
    });

    $('.expansion-content').hide();

    $('.expand-toggle-button').click(function (e, i) {
        var content = $(e.currentTarget).closest('.expansion-area').find('.expansion-content');
        content.toggle(250);
        $(e.currentTarget).toggleClass('expanded');
    });

    document.documentElement.className += "ontouchstart" in document.documentElement ? ' touch' : ' no-touch';
});

},{"./chrome-extension":2,"./comment-form":3,"./edit-swirl":4,"./editor":5,"./ga":6,"./menu":8,"./response-form":9,"./search":10,"./swirl-list":11}],2:[function(require,module,exports){
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

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _http = require('./http.js');

var _http2 = _interopRequireDefault(_http);

var _editor = require('./editor.js');

var _editor2 = _interopRequireDefault(_editor);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var CommentForm = function () {
    function CommentForm($, form) {
        var _this = this;

        _classCallCheck(this, CommentForm);

        this.editor = new _editor2.default.RichTextEditor($(form).find('div.rte'));
        this.$form = $(form);
        this.$addButton = this.$form.find('input[type=submit]');
        this.$maxCommentIdField = this.$form.find('.max-comment-id-field');
        this.swirlId = parseInt($(form).find('.swirl-id-field').val(), 10);
        this.refreshToken = null;

        $(form).submit(function () {
            _this.setLoading();

            var commentHtml = _this.editor.getHtmlContent();
            console.log('Going to post comment', commentHtml);
            if (commentHtml && commentHtml.trim().length > 0) {
                _http2.default.post('/swirls/' + _this.swirlId + '/comment', { comment: commentHtml }).then(_this.addMissingComments.bind(_this)).then(function () {
                    _this.resetForm();
                });
            } else {
                console.warn('No HTML found in editor', _this.editor);
                window.alert('Oops, your comment could not be posted. Please try again.');
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
            return _http2.default.getJson('/swirls/' + me.swirlId + '/comments?comment-id-start=' + me.$maxCommentIdField.val()).then(function (comments) {
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
            this.$addButton.prop("disabled", true);
        }
    }, {
        key: 'resetForm',
        value: function resetForm() {
            this.$addButton.prop("disabled", false);
            this.$addButton.removeClass('button-loading');
            this.editor.clear();
        }
    }]);

    return CommentForm;
}();

var init = function init($) {
    $('form.comment').each(function (i, f) {
        return new CommentForm($, f);
    });
};

module.exports = { init: init };

},{"./editor.js":5,"./http.js":7}],4:[function(require,module,exports){
'use strict';

var setup = function setup() {

    var addUser = function addUser(textbox) {
        var nameOrEmail = textbox.value;
        var allUsersList = textbox.getAttribute('list');
        var allUsersOptions = document.querySelectorAll('#' + allUsersList + ' option');
        var userHTML = false;

        for (var i = 0; i < allUsersOptions.length; i++) {
            var option = allUsersOptions[i];

            if (option.innerText === nameOrEmail) {
                userHTML = option.getAttribute('data-value');
                break;
            }
        }

        if (userHTML) {
            $(textbox).before(userHTML);
        } else {
            var input = $(document.createElement('input')).attr('type', 'checkbox').attr('name', 'who').attr('value', nameOrEmail).attr('checked', 'checked').attr('id', nameOrEmail);
            var label = $(document.createElement('label')).attr('for', nameOrEmail).attr('class', 'no-avatar');
            $(textbox).before(input);
            $(textbox).before(label);
            label.append(document.createTextNode(nameOrEmail));
        }
        var br = $(document.createElement('div')).attr('class', 'small-padding');
        $(textbox).before(br);
        textbox.value = '';
    };

    $('.user-select-box input').keydown(function (event) {
        if (event.which == 13 || event.which == 9) {
            event.preventDefault();
            addUser(this);
            return false;
        }
    });

    $('.change-image-link').click(function (b) {
        "use strict";

        $('.change-image-area').removeClass('start-hidden');
        $(b.target).addClass('start-hidden');
        return false;
    });
};

module.exports = setup;

},{}],5:[function(require,module,exports){
'use strict';

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _http = require('./http.js');

var _http2 = _interopRequireDefault(_http);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var RichTextEditor = function () {
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

        $rteDiv.closest("form").on("submit", function () {
            _this.copyInputFromEditableDivToPostableTextArea();
            return true;
        });
        this.$editorDiv.focusout(function () {
            _this.copyInputFromEditableDivToPostableTextArea();
        });
    }

    _createClass(RichTextEditor, [{
        key: 'copyInputFromEditableDivToPostableTextArea',
        value: function copyInputFromEditableDivToPostableTextArea() {
            this.$textarea.val(this.getHtmlContent());
        }
    }, {
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
                    _http2.default.getJson('/website-service/get-metadata?url=' + encodeURI(e.href)).then(function (metadata) {
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
                            var target = htmlEncodedLink.indexOf('https://www.swrl.co') === 0 ? '' : ' target="_blank"';
                            $(nextBit).before('<a href="' + htmlEncodedLink + '"' + target + '>' + htmlEncodedLink + '</a>');
                            nextBit.data = '\xA0';
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
}();

var setup = function setup($) {
    $(".rte").each(function (i, holder) {
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

},{"./http.js":7}],6:[function(require,module,exports){
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

},{}],7:[function(require,module,exports){
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

},{}],8:[function(require,module,exports){
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

},{}],9:[function(require,module,exports){
'use strict';

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _http = require('./http.js');

var _http2 = _interopRequireDefault(_http);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var RespondForm = function () {
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
                _http2.default.post('/swirls/' + swirlId + '/respond', { responseButton: response }).then(function () {
                    _this.setSelectedButton(response, 'swirl-button');
                });
            }
            return false;
        });

        customInputBox.keydown(function (e) {
            if (e.which == 13 || e.which == 9) {
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
}();

var init = function init($) {

    $('.respond-form').each(function (i, f) {
        return new RespondForm($, f);
    });
    //new RespondForm();

};

module.exports = { init: init };

},{"./http.js":7}],10:[function(require,module,exports){
'use strict';

var currentSearch = null;
var currentTimeout = null;

window.onpopstate = function (event) {
    var q = (event.state ? event.state.query : '') || '';
    $('.search-form .query').val(q);
    $('.search-form').submit();
};

function init($) {

    $('.search-form .query').keydown(function (e, k) {
        if (currentTimeout) {
            window.clearTimeout(currentTimeout);
        }
        currentTimeout = window.setTimeout(function () {
            $('.search-form').submit();
        }, 300);
    });

    $('.search-form').submit(function (e) {
        var $f = $(e.currentTarget);
        var query = $f.find('.query').val();
        currentSearch = query;
        var $summary = $f.find('.search-result-summary');
        $summary.toggleClass('no-query', !query);

        if (!query) {
            query = ''; // nothing entered... let the browser deal with it
        }
        var $b = $f.find('.submit');

        var $summaryQuery = $f.find('.query-val');
        $summaryQuery.text(query);
        var $resultCount = $f.find('.result-count');
        $resultCount.html('<i class="fa fa-spin fa-spinner"></i>');

        var fetchUrl = '/api/v1/swirls/search?query=' + encodeURIComponent(query);
        fetch(fetchUrl, { credentials: 'same-origin' }).then(function (resp) {
            if (resp.status !== 200) {
                throw 'from ' + fetchUrl + ': ' + resp.status + ' ' + resp.statusText;
            }
            return resp.text();
        }).then(function (html) {
            if (currentSearch === query) {
                $('.search-results').html(html);
                $resultCount.html($('.search-results .mini-swirl').length);
                $b.html('Go');
                requestAnimationFrame(function () {
                    $('.pending-to-appear').css('opacity', '1.0');
                });
                history.replaceState({ query: query }, 'Search results for ' + query, '/search?query=' + encodeURIComponent(query));
            }
        }).catch(function (e) {
            console.log('Error while getting search results', e);
            $f.off();
            $f.submit();
        });

        return false;
    });
}

module.exports = {
    init: init
};

},{}],11:[function(require,module,exports){
'use strict';

var _http = require('./http.js');

var _http2 = _interopRequireDefault(_http);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var currentFilter = null;

var respondAndRemove = function respondAndRemove(element, response) {
    var swirlElement = $(element)[0].parentNode.parentNode;
    var swirlID = swirlElement.getAttribute('id');
    $(swirlElement).remove();
    _http2.default.post('/swirls/' + swirlID + '/respond', { responseButton: response });
};

var filterVisibleSwirls = function filterVisibleSwirls($) {
    $('.type-filter button').each(function (i, typeButton) {
        var curType = typeButton.getAttribute('data-swirl-type');
        var show = currentFilter == null || currentFilter === curType;
        $(typeButton).toggleClass('hidden', !show);
        $('.swirl.' + curType).toggle(show);
    });
};
function init($) {

    $(window).scroll(function () {
        if ($(window).scrollTop() + $(window).height() + 320 >= $(document).height()) {
            // 320 is the height of a swrl
            $('.more-swirls-button').click();
        }
    });

    $('.more-swirls-button').click(function (e) {
        var b = $(e.target);
        if (b.attr('data-disabled')) {
            return false; // button is disabled as it is still loading data, so do nothing
        }

        // data-ids contains a fixed number of IDs that come AFTER those returned from the server.
        var ids = b.attr('data-ids').split(',');
        var numLoads = parseInt(b.attr('data-num-loads'), 10);
        var pageSize = parseInt(b.attr('data-per-page'), 10);
        var from = numLoads * pageSize;
        var to = from + pageSize;
        var idsToGet = ids.slice(from, to);
        var startIndex = parseInt(b.attr('data-start-index'), 10);
        var nextPageUrl = b.attr('data-url-prefix') + (startIndex + to + pageSize);
        var nextPageUrlIfQueryFails = b.attr('data-url-prefix') + (startIndex + to);
        var originalValue = b.text();

        if (idsToGet.length === 0) {
            return true; // run out of pre-fetched IDs so let the standard link work
        }

        b.attr('data-disabled', 'true'); // disable the button
        b.html('<i class="fa fa-spin fa-spinner"></i> Loading');

        var fetchUrl = '/api/v1/swirls?swirl-list=' + idsToGet.join(',');
        fetch(fetchUrl, { credentials: 'same-origin' }).then(function (resp) {
            if (resp.status !== 200) {
                throw 'from ' + fetchUrl + ': ' + resp.status + ' ' + resp.statusText;
            }
            return resp.text();
        }).then(function (html) {
            $('.swirl-insertion-point').before(html);
            filterVisibleSwirls($);
            setTimeout(function () {
                $('.pending-to-appear').css('opacity', '1.0');
            }, 20);
            b.removeAttr('data-disabled');
            b.text(originalValue);
            b.attr('href', nextPageUrl);
        }).catch(function (e) {
            console.log('Error while getting more', e);
            location.href = nextPageUrlIfQueryFails;
        });

        b.attr('data-num-loads', numLoads + 1);
        return false;
    });

    $('.type-filter button').click(function (b) {
        var clickedType = b.target.getAttribute('data-swirl-type');
        currentFilter = clickedType === currentFilter ? null : clickedType;
        filterVisibleSwirls($);
    });

    var swirlAndRemove = function swirlAndRemove(element) {
        var swirlElement = $(element)[0].parentNode.parentNode;
        var swirlTitle = swirlElement.getAttribute('data-title');
        var swirlImageUrl = swirlElement.getAttribute('data-image-url');
        var swirlReview = swirlElement.getAttribute('data-review');
        var swirlType = swirlElement.getAttribute('data-swirl-type');
        $(swirlElement).remove();
        _http2.default.post('/swirls/create-swirl', { title: swirlTitle,
            review: swirlReview,
            type: swirlType,
            imageUrl: swirlImageUrl });
    };
    $('.swirl-list').on('click', 'i.add-to-wishlist-button', function () {
        swirlAndRemove(this);
    });

    $('.swirl-list').on('click', 'i.dismiss-button', function () {
        respondAndRemove(this, 'Dismissed');
    });

    $('.swirl-list').on('click', 'i.later-button', function () {
        respondAndRemove(this, 'Later');
    });
}

module.exports = {
    init: init
};

},{"./http.js":7}]},{},[1]);
